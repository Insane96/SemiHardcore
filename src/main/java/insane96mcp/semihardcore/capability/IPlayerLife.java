package insane96mcp.semihardcore.capability;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPlayerLife {
	boolean isOptOut();
	void setOptOut(boolean optOut);

	int getLives();
	void setLives(int lives);
	void setLives(int lives, int upperCap);
	void addLives(int lives);
	void addLives(int lives, int upperCap);

	int getHealthModifier();
	void setHealthModifier(int healthModifier);
	void addHealthModifier(int healthModifier);
}
