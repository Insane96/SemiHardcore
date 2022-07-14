package com.insane96mcp.semihardcore.capability;

import com.insane96mcp.semihardcore.module.Modules;

public class LivesImpl implements ILives {

    private int lives;

    public LivesImpl() {
        this.lives = Modules.base.base.startingLives;
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
}
