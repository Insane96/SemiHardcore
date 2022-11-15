package com.insane96mcp.semihardcore.capability;

import com.insane96mcp.semihardcore.module.base.feature.Health;
import com.insane96mcp.semihardcore.module.base.feature.Lives;
import net.minecraft.util.Mth;

public class PlayerLifeImpl implements IPlayerLife {

    private int lives;
    private int healthModifier;

    public PlayerLifeImpl() {
        this.lives = Lives.startingLives;
        this.healthModifier = Health.startingHealth - 20;
    }

    @Override
    public int getLives() {
        return this.lives;
    }

    @Override
    public void setLives(int lives) {
        this.setLives(lives, Lives.maxLives == 0 ? Integer.MAX_VALUE : Lives.maxLives);
    }

    @Override
    public void setLives(int lives, int upperCap) {
        this.lives = Mth.clamp(lives, 0, upperCap);
    }

    @Override
    public void addLives(int lives) {
        this.addLives(lives, Lives.maxLives == 0 ? Integer.MAX_VALUE : Lives.maxLives);
    }

    @Override
    public void addLives(int lives, int upperCap) {
        this.lives = Mth.clamp(this.lives + lives, 0, upperCap);
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
