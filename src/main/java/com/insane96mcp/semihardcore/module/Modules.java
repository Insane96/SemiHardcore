package com.insane96mcp.semihardcore.module;


import com.insane96mcp.semihardcore.module.base.BaseModule;

public class Modules {
    public static BaseModule base;
    public static void init() {
        base = new BaseModule();
    }

    public static void loadConfig() {
        base.loadConfig();
    }
}
