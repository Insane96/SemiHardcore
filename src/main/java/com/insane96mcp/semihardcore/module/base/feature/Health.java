package com.insane96mcp.semihardcore.module.base.feature;

import com.insane96mcp.semihardcore.SemiHardcore;
import com.insane96mcp.semihardcore.capability.PlayerLife;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@Label(name = "Health")
@LoadFeature(module = SemiHardcore.RESOURCE_PREFIX + "base")
public class Health extends Feature {
    @Config(min = 1)
    @Label(name = "Starting Health", description = "Players starting Health")
    public static Integer startingHealth = 20;
    @Config
    @Label(name = "Half hearts penalty on death", description = "Half hearts lost when the player dies. Negative numbers can be used to give health to players.")
    public static Integer healthPenalty = 2;

    public Health(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event)
    {
        if (!this.isEnabled()
                || healthPenalty == 0
                || event.getEntity().getLevel().isClientSide
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getEntity() instanceof FakePlayer
                || event.getEntity().getLevel().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE
                || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
            return;

        player.getCapability(PlayerLife.INSTANCE).ifPresent(livesCap -> {
            livesCap.addHealthModifier(-healthPenalty);
        });
    }

    public static final UUID MAX_HEALTH_MODIFIER_UUID = UUID.fromString("5ee1626e-c727-4ecd-96b5-f1487896fd44");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (!this.isEnabled()
                || event.getEntity().level.isClientSide
                || event.isEndConquered())
            return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
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