package com.insane96mcp.semihardcore;

import com.insane96mcp.semihardcore.capability.LivesProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SemiHardcore.MOD_ID)
public class SemiHardcore
{
    public static final String MOD_ID = "semihardcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SemiHardcore()
    {
        
    }

    @SubscribeEvent
    public void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
            event.addCapability(LivesProvider.IDENTIFIER, new LivesProvider());
    }
}
