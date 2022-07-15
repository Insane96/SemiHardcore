package com.insane96mcp.semihardcore.module.base;

import com.insane96mcp.semihardcore.module.base.feature.Health;
import com.insane96mcp.semihardcore.module.base.feature.Lives;
import com.insane96mcp.semihardcore.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Semi-Hardcore")
public class BaseModule extends Module {

    public Lives lives;
    public Health health;

    public BaseModule() {
        super(Config.builder, true, false);
        this.pushConfig(Config.builder);
        lives = new Lives(this);
        health = new Health(this);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        lives.loadConfig();
        health.loadConfig();
    }
}