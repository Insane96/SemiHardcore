package com.insane96mcp.semihardcore.capability;

public interface IPlayerLife {
    int getLives();
    void setLives(int lives);
    void addLives(int lives);

    int getHealthModifier();
    void setHealthModifier(int healthModifier);
    void addHealthModifier(int healthModifier);
}
