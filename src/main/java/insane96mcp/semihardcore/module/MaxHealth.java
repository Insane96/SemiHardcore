package insane96mcp.semihardcore.module;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.semihardcore.SemiHardcore;
import insane96mcp.semihardcore.util.PlayerLifeData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@LoadFeature(canBeDisabled = false)
public class MaxHealth extends Feature {
	private static final ResourceLocation HEALTH_INITIALIZED = SemiHardcore.location("health_initialized");
	public static final ResourceLocation MAX_HEALTH_MODIFIER_ID = SemiHardcore.location("max_health_modifier");

	@Config(min = 0, description = "How much max health players spawn with")
	public static Integer startingHealth = 10;
	@Config(description = "Max health lost when the player dies. Negative numbers can be used to give health to players.")
	public static Integer healthPenalty = -2;
	@Config(description = "Min and max max health players can have. E.g. Setting min to -10 and max to 0 means that the player can't have less than 5 hearts and more than 10")
	public static MinMaxConfig cap = new MinMaxConfig(-18, 0);

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| healthPenalty == 0
				|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| event.getEntity() instanceof FakePlayer
				|| event.getEntity().level().getLevelData().isHardcore()
                || !player.gameMode.isSurvival())
			return;

		if (PlayerLifeData.isOptOut(player))
			return;
		PlayerLifeData.addHealthModifier(player, -healthPenalty);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| event.isEndConquered())
			return;

		ServerPlayer player = (ServerPlayer) event.getEntity();
		updateMaxHealth(player, true);
	}

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof ServerPlayer serverPlayer)
                || ModNBTData.getPersisted(serverPlayer, HEALTH_INITIALIZED, Boolean.class))
            return;

        PlayerLifeData.setHealthModifier(serverPlayer, startingHealth - 20f);
        updateMaxHealth(serverPlayer, false);
        ModNBTData.putPersisted(serverPlayer, HEALTH_INITIALIZED, true);
    }

	@SuppressWarnings("ConstantConditions")
	public static void updateMaxHealth(Player player, boolean message) {
		if (PlayerLifeData.isOptOut(player))
			return;
		if (!player.getAttributes().hasAttribute(Attributes.MAX_HEALTH))
			return;
		AttributeInstance maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealthAttribute.getModifier(MAX_HEALTH_MODIFIER_ID) != null)
			maxHealthAttribute.removeModifier(MAX_HEALTH_MODIFIER_ID);

		maxHealthAttribute.addPermanentModifier(
				new AttributeModifier(MAX_HEALTH_MODIFIER_ID, PlayerLifeData.getHealthModifier(player), AttributeModifier.Operation.ADD_VALUE));

		player.setHealth(player.getMaxHealth());
        if (message) {
            if (healthPenalty > 0)
                player.sendSystemMessage(Component.translatable(SemiHardcore.lang("max_health_lost"), healthPenalty));
            else
                player.sendSystemMessage(Component.translatable(SemiHardcore.lang("max_health_gained"), healthPenalty * -1));
        }
	}
}
