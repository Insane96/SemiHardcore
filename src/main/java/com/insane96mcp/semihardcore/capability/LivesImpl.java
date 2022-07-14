package com.insane96mcp.semihardcore.capability;

public class LivesImpl implements ILives {

    private int lives;

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
