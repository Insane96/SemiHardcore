package com.insane96mcp.semihardcore.module.base.feature;

import com.insane96mcp.semihardcore.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.common.ForgeConfigSpec;

@Label(name = "Semi Hardcore")
public class Base extends Feature {

    private final ForgeConfigSpec.ConfigValue<Integer> startingLivesConfig;

    public int startingLives = 3;

    public Base(Module module) {
        super(Config.builder, module, true, false);
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
}