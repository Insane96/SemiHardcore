package com.insane96mcp.semihardcore.event;

import com.insane96mcp.semihardcore.SemiHardcore;
import com.insane96mcp.semihardcore.capability.Lives;
import com.insane96mcp.semihardcore.capability.LivesImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SemiHardcore.MOD_ID)
public class Capabilities {
    @SubscribeEvent
    public static void eventPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getPlayer();

        oldPlayer.reviveCaps();
        oldPlayer.getCapability(Lives.INSTANCE).ifPresent(newLives -> newPlayer.getCapability(Lives.INSTANCE).ifPresent(oldLives -> {
            oldLives.setLives(newLives.getLives());
        }));
        oldPlayer.invalidateCaps();
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(LivesImpl.class);
    }
}
