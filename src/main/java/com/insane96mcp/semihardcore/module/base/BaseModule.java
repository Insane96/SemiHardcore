package com.insane96mcp.semihardcore.module.base;

import com.insane96mcp.semihardcore.module.base.feature.Base;
import com.insane96mcp.semihardcore.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Base")
public class BaseModule extends Module {

    public Base base;

    public BaseModule() {
        super(Config.builder);
        this.pushConfig(Config.builder);
        base = new Base(this);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        base.loadConfig();
    }
}