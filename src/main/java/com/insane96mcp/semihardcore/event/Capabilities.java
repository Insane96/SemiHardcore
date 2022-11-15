package com.insane96mcp.semihardcore.event;

import com.insane96mcp.semihardcore.SemiHardcore;
import com.insane96mcp.semihardcore.capability.PlayerLife;
import com.insane96mcp.semihardcore.capability.PlayerLifeImpl;
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
        Player newPlayer = event.getEntity();

        oldPlayer.reviveCaps();
        oldPlayer.getCapability(PlayerLife.INSTANCE).ifPresent(newLives -> newPlayer.getCapability(PlayerLife.INSTANCE).ifPresent(oldLives -> {
            oldLives.setLives(newLives.getLives());
            oldLives.setHealthModifier(newLives.getHealthModifier());
        }));
        oldPlayer.invalidateCaps();
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(PlayerLifeImpl.class);
    }
}
