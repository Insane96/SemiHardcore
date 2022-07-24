package com.insane96mcp.semihardcore.module.base.feature;

import com.insane96mcp.semihardcore.capability.PlayerLife;
import com.insane96mcp.semihardcore.setup.Config;
import com.insane96mcp.semihardcore.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Lives")
public class Lives extends Feature {

    private final ForgeConfigSpec.IntValue startingLivesConfig;
    private final ForgeConfigSpec.IntValue maxLivesConfig;
    private final ForgeConfigSpec.BooleanValue announceLifeLostToChatConfig;

    public int startingLives = 5;
    public int maxLives = 0;
    public boolean announceLifeLostToChat = true;

    public Lives(Module module) {
        super(Config.builder, module, true);
        super.pushConfig(Config.builder);
        startingLivesConfig = Config.builder
                .comment("How many lives players spawns with")
                .defineInRange("Starting Lives", this.startingLives, 0, Integer.MAX_VALUE);
        maxLivesConfig = Config.builder
                .comment("Max lives you can gain. 0 for infinite.")
                .defineInRange("Max Lives", this.maxLives, 0, Integer.MAX_VALUE);
        announceLifeLostToChatConfig = Config.builder
                .comment("Announce players' life lost to chat.")
                .define("Announce Life Lost to Chat", this.announceLifeLostToChat);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.startingLives = this.startingLivesConfig.get();
        this.maxLives = this.maxLivesConfig.get();
        this.announceLifeLostToChat = this.announceLifeLostToChatConfig.get();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event)
    {
        if (!this.isEnabled()
                || event.getEntityLiving().getLevel().isClientSide
                || !(event.getEntityLiving() instanceof ServerPlayer player)
                || event.getEntityLiving() instanceof FakePlayer
                || event.getEntityLiving().getLevel().getLevelData().isHardcore()
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
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (!this.isEnabled()
                || event.getPlayer().level.isClientSide
                || !(event.getEntityLiving() instanceof ServerPlayer player)
                || event.isEndConquered() //This event is called even when the player enters the End Portal
                || player.getLevel().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
            return;

        event.getPlayer().getCapability(PlayerLife.INSTANCE).ifPresent(playerLife -> {
            if (playerLife.getLives() > 0 && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                player.connection.send(new ClientboundSetTitleTextPacket(new TranslatableComponent(Strings.Translatable.LIFE_LOST)));
                if (playerLife.getLives() > 1)
                    player.connection.send(new ClientboundSetSubtitleTextPacket(new TranslatableComponent(Strings.Translatable.LIVES_REMAINING, playerLife.getLives())));
                else
                    player.connection.send(new ClientboundSetSubtitleTextPacket(new TranslatableComponent(Strings.Translatable.LIFE_REMAINING, playerLife.getLives())));

                if (this.announceLifeLostToChat) {
                    player.server.getPlayerList().broadcastMessage(new TranslatableComponent(Strings.Translatable.PLAYER_LIFE_LOST, player.getDisplayName().getString(), playerLife.getLives()), ChatType.CHAT, player.getUUID());
                }
            }
            else {
                player.connection.send(new ClientboundSetTitleTextPacket(new TranslatableComponent(Strings.Translatable.GG_WP)));
                player.connection.send(new ClientboundSetSubtitleTextPacket(new TranslatableComponent(Strings.Translatable.NO_LIVES_REMAINING, playerLife.getLives())));

                if (this.announceLifeLostToChat) {
                    player.server.getPlayerList().broadcastMessage(new TranslatableComponent(Strings.Translatable.PLAYER_NO_LIFE_REMAINING, player.getDisplayName().getString(), playerLife.getLives()), ChatType.CHAT, player.getUUID());
                }
            }
        });
    }
}