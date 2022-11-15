package com.insane96mcp.semihardcore.module.base.feature;

import com.insane96mcp.semihardcore.SemiHardcore;
import com.insane96mcp.semihardcore.capability.PlayerLife;
import com.insane96mcp.semihardcore.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Lives")
@LoadFeature(module = SemiHardcore.RESOURCE_PREFIX + "base")
public class Lives extends Feature {
    @Config(min = 0)
    @Label(name = "Starting Lives", description = "How many lives players spawns with")
    public static Integer startingLives = 5;
    @Config(min = 0)
    @Label(name = "Max Lives", description = "Max lives you can gain. 0 for infinite.")
    public static Integer maxLives = 0;
    @Config
    @Label(name = "Announce Life Lost to Chat", description = "Announce players' life lost to chat.")
    public static Boolean announceLifeLostToChat = true;

    public Lives(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || event.getEntity().getLevel().isClientSide
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getEntity() instanceof FakePlayer
                || event.getEntity().getLevel().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE
                || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
            return;

        player.getCapability(PlayerLife.INSTANCE).ifPresent(playerLife -> {
            playerLife.addLives(-1);
            if (playerLife.getLives() <= 0) {
                player.setRespawnPosition(player.level.dimension(), player.blockPosition(), player.getXRot(), true, false);
                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, player.level);
                lightningBolt.setVisualOnly(true);
                lightningBolt.setPos(player.position());
                player.level.addFreshEntity(lightningBolt);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level.isClientSide
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.isEndConquered() //This event is called even when the player enters the End Portal
                || player.getLevel().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
            return;

        event.getEntity().getCapability(PlayerLife.INSTANCE).ifPresent(playerLife -> {
            if (playerLife.getLives() > 0 && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(Strings.Translatable.LIFE_LOST)));
                if (playerLife.getLives() > 1)
                    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(Strings.Translatable.LIVES_REMAINING, playerLife.getLives())));
                else
                    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(Strings.Translatable.LIFE_REMAINING, playerLife.getLives())));

                if (announceLifeLostToChat) {
                    player.server.getPlayerList().broadcastSystemMessage(Component.translatable(Strings.Translatable.PLAYER_LIFE_LOST, player.getDisplayName().getString(), playerLife.getLives()), false);
                }
            }
            else {
                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(Strings.Translatable.GG_WP)));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(Strings.Translatable.NO_LIVES_REMAINING, playerLife.getLives())));
                player.setGameMode(GameType.SPECTATOR);

                if (announceLifeLostToChat) {
                    player.server.getPlayerList().broadcastSystemMessage(Component.translatable(Strings.Translatable.PLAYER_NO_LIFE_REMAINING, player.getDisplayName().getString(), playerLife.getLives()), false);
                }
            }
        });
    }
}