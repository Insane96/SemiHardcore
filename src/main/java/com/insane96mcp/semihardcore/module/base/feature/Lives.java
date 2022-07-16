package com.insane96mcp.semihardcore.module.base.feature;

import com.insane96mcp.semihardcore.capability.PlayerLife;
import com.insane96mcp.semihardcore.setup.Config;
import com.insane96mcp.semihardcore.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.Util;
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

    private final ForgeConfigSpec.ConfigValue<Integer> startingLivesConfig;

    public int startingLives = 5;

    public Lives(Module module) {
        super(Config.builder, module, true);
        super.pushConfig(Config.builder);
        startingLivesConfig = Config.builder
                .comment("How many lives players spawns with")
                .defineInRange("Starting Lives", this.startingLives, 0, Integer.MAX_VALUE);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.startingLives = this.startingLivesConfig.get();
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
                //Reset health so you respawn where you died
                player.setHealth(player.getMaxHealth());
                player.setGameMode(GameType.SPECTATOR);
                player.sendMessage(new TranslatableComponent(Strings.Translatable.LIFE_LOST_LOSE), Util.NIL_UUID);
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
                || event.isEndConquered())
            return;

        ServerPlayer player = (ServerPlayer) event.getPlayer();
        event.getPlayer().getCapability(PlayerLife.INSTANCE).ifPresent(playerLife -> {
            if (playerLife.getLives() > 0) {
                player.connection.send(new ClientboundSetTitleTextPacket(new TranslatableComponent(Strings.Translatable.LIFE_LOST)));
                if (playerLife.getLives() > 1)
                    player.connection.send(new ClientboundSetSubtitleTextPacket(new TranslatableComponent(Strings.Translatable.LIVES_REMAINING, playerLife.getLives())));
                else
                    player.connection.send(new ClientboundSetSubtitleTextPacket(new TranslatableComponent(Strings.Translatable.LIFE_REMAINING, playerLife.getLives())));
            }
        });
    }
}