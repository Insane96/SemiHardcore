package com.insane96mcp.semihardcore.capability;

public interface IPlayerLife {
    int getLives();
    void setLives(int lives);
    void setLives(int lives, int upperCap);
    void addLives(int lives);
    void addLives(int lives, int upperCap);

    int getHealthModifier();
    void setHealthModifier(int healthModifier);
    void addHealthModifier(int healthModifier);
}
