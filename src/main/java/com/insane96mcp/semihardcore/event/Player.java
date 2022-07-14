package com.insane96mcp.semihardcore.event;

import com.insane96mcp.semihardcore.SemiHardcore;
import com.insane96mcp.semihardcore.capability.Lives;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SemiHardcore.MOD_ID)
public class Player {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event)
    {
        if (event.getEntityLiving().getLevel().isClientSide
                || !(event.getEntityLiving() instanceof ServerPlayer player)
                || event.getEntityLiving() instanceof FakePlayer
                || event.getEntityLiving().getLevel().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE
                || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
            return;

        player.getCapability(Lives.INSTANCE).ifPresent(livesCap -> {
            livesCap.addLives(-1);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getPlayer().level.isClientSide)
            return;

        ServerPlayer player = (ServerPlayer) event.getPlayer();
        event.getPlayer().getCapability(Lives.INSTANCE).ifPresent(livesCap -> {
            if (livesCap.getLives() <= 0) {
                player.setGameMode(GameType.SPECTATOR);
                player.sendMessage(new TextComponent("You lost a life. You have no lives remaining."), Util.NIL_UUID);
            }
            else
                player.sendMessage(new TextComponent("You lost a life. %s lives remaining".formatted(livesCap.getLives())), Util.NIL_UUID);
        });
    }
}
