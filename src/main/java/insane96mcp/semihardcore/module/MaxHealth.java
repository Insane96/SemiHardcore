package insane96mcp.semihardcore.module;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.ModNBTData;
import insane96mcp.semihardcore.SemiHardcore;
import insane96mcp.semihardcore.capability.PlayerLifeImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = SemiHardcore.RESOURCE_PREFIX + "base")
public class MaxHealth extends Feature {
	@Config(min = 0, description = "How much max health players spawn with")
	public static Integer startingHealth = 10;
	@Config(description = "Max health lost when the player dies. Negative numbers can be used to give health to players.")
	public static Integer healthPenalty = -2;
	@Config(description = "Min and max max health players can have. E.g. Setting min to -10 and max to 0 means that the player can't have less than 5 hearts and more than 10")
	public static MinMax cap = new MinMax(-18, 0);

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

		player.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(livesCap -> {
			if (livesCap.isOptOut())
				return;
			livesCap.addHealthModifier(-healthPenalty);
		});
	}

	public static final UUID MAX_HEALTH_MODIFIER_UUID = UUID.fromString("5ee1626e-c727-4ecd-96b5-f1487896fd44");

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
                || event.getEntity().level().isClientSide
                || !(event.getEntity() instanceof ServerPlayer serverPlayer)
                || ModNBTData.get(serverPlayer, SemiHardcore.location("starting_health_applied"), Boolean.class))
            return;

        updateMaxHealth(serverPlayer, false);
        ModNBTData.put(serverPlayer, SemiHardcore.location("starting_health_applied"), true);
    }

	@SuppressWarnings("ConstantConditions")
	public static void updateMaxHealth(Player player, boolean message) {
		player.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(playerLife -> {
			if (playerLife.isOptOut())
				return;
			if (!player.getAttributes().hasAttribute(Attributes.MAX_HEALTH))
				return;
			if (player.getAttribute(Attributes.MAX_HEALTH).getModifier(MAX_HEALTH_MODIFIER_UUID) != null) {
				player.getAttribute(Attributes.MAX_HEALTH).removeModifier(MAX_HEALTH_MODIFIER_UUID);
			}
			player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(
					new AttributeModifier(MAX_HEALTH_MODIFIER_UUID, SemiHardcore.lang("health_modifier"), playerLife.getHealthModifier(), AttributeModifier.Operation.ADDITION));

			player.setHealth(player.getMaxHealth());
            if (message) {
                if (healthPenalty > 0)
                    player.sendSystemMessage(Component.translatable(SemiHardcore.lang("max_health_lost"), healthPenalty));
                else
                    player.sendSystemMessage(Component.translatable(SemiHardcore.lang("max_health_gained"), healthPenalty * -1));
            }
		});
	}
}
