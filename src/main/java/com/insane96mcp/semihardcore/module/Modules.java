package com.insane96mcp.semihardcore.module;


import com.insane96mcp.semihardcore.SemiHardcore;
import com.insane96mcp.semihardcore.setup.Config;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.fml.config.ModConfig;

public class Modules {
    public static Module Base;
    public static void init() {
        Base = Module.Builder.create(SemiHardcore.MOD_ID, "base", "Base", ModConfig.Type.COMMON, Config.builder)
                .canBeDisabled(false)
                .build();
    }
}
