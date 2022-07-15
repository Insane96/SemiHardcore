package com.insane96mcp.semihardcore.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class PlayerLife {

    public static final Capability<IPlayerLife> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    public PlayerLife() { }
}
