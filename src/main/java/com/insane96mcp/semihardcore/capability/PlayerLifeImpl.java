package com.insane96mcp.semihardcore.capability;

import com.insane96mcp.semihardcore.module.Modules;

public class PlayerLifeImpl implements IPlayerLife {

    private int lives;
    private int healthModifier;

    public PlayerLifeImpl() {
        this.lives = Modules.base.lives.startingLives;
        this.healthModifier = Modules.base.health.startingHealth - 20;
    }

    @Override
    public int getLives() {
        return this.lives;
    }

    @Override
    public void setLives(int lives) {
        this.lives = lives;
    }

    @Override
    public void addLives(int lives) {
        this.lives = Math.max(this.lives + lives, 0);
    }

    @Override
    public int getHealthModifier() {
        return this.healthModifier;
    }

    @Override
    public void setHealthModifier(int healthModifier) {
        this.healthModifier = healthModifier;
    }

    @Override
    public void addHealthModifier(int healthModifier) {
        this.healthModifier += healthModifier;
    }
}
