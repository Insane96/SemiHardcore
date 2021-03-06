package com.insane96mcp.semihardcore;

import com.insane96mcp.semihardcore.capability.PlayerLifeProvider;
import com.insane96mcp.semihardcore.command.SHCommand;
import com.insane96mcp.semihardcore.setup.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SemiHardcore.MOD_ID)
public class SemiHardcore
{
    public static final String MOD_ID = "semihardcore";
    public static final String RESOURCE_PREFIX = MOD_ID + ":";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SemiHardcore()
    {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
            event.addCapability(PlayerLifeProvider.IDENTIFIER, new PlayerLifeProvider());
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        SHCommand.register(event.getDispatcher());
    }
}
