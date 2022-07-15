package com.insane96mcp.semihardcore.module.base.feature;

import com.insane96mcp.semihardcore.SemiHardcore;
import com.insane96mcp.semihardcore.capability.PlayerLife;
import com.insane96mcp.semihardcore.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@Label(name = "Health")
public class Health extends Feature {

    private final ForgeConfigSpec.ConfigValue<Integer> startingHealthConfig;
    private final ForgeConfigSpec.ConfigValue<Integer> healthPenaltyConfig;

    public int startingHealth = 20;
    public int healthPenalty = 2;

    public Health(Module module) {
        super(Config.builder, module, true);
        super.pushConfig(Config.builder);
        startingHealthConfig = Config.builder
                .comment("Players starting Health")
                .defineInRange("Starting Health", this.startingHealth, 1, Integer.MAX_VALUE);
        healthPenaltyConfig = Config.builder
                .comment("Half hearts lost when the player dies. Negative numbers can be used to give health to players.")
                .define("Half hearts penalty on death", this.healthPenalty);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.startingHealth = this.startingHealthConfig.get();
        this.healthPenalty = this.healthPenaltyConfig.get();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event)
    {
        if (!this.isEnabled()
                || this.healthPenalty == 0
                || event.getEntityLiving().getLevel().isClientSide
                || !(event.getEntityLiving() instanceof ServerPlayer player)
                || event.getEntityLiving() instanceof FakePlayer
                || event.getEntityLiving().getLevel().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE
                || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
            return;

        player.getCapability(PlayerLife.INSTANCE).ifPresent(livesCap -> {
            livesCap.addHealthModifier(-this.healthPenalty);
        });
    }

    public static final UUID MAX_HEALTH_MODIFIER_UUID = UUID.fromString("5ee1626e-c727-4ecd-96b5-f1487896fd44");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (!this.isEnabled()
                || event.getPlayer().level.isClientSide
                || event.isEndConquered())
            return;

        ServerPlayer player = (ServerPlayer) event.getPlayer();
        updateMaxHealth(player);
    }

    @SuppressWarnings("ConstantConditions")
    public static void updateMaxHealth(Player player) {
        player.getCapability(PlayerLife.INSTANCE).ifPresent(playerLife -> {
            if (!player.getAttributes().hasAttribute(Attributes.MAX_HEALTH))
                return;
            if (player.getAttribute(Attributes.MAX_HEALTH).getModifier(MAX_HEALTH_MODIFIER_UUID) != null) {
                player.getAttribute(Attributes.MAX_HEALTH).removeModifier(MAX_HEALTH_MODIFIER_UUID);
            }
            player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(
                    new AttributeModifier(MAX_HEALTH_MODIFIER_UUID, SemiHardcore.RESOURCE_PREFIX + "health_modifier", playerLife.getHealthModifier(), AttributeModifier.Operation.ADDITION));

            player.setHealth(player.getMaxHealth());
        });
    }
}