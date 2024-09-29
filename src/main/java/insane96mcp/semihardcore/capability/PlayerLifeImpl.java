package insane96mcp.semihardcore.capability;

import insane96mcp.semihardcore.module.Lives;
import insane96mcp.semihardcore.module.MaxHealth;
import net.minecraft.util.Mth;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class PlayerLifeImpl implements IPlayerLife {
	public static final Capability<IPlayerLife> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

	private boolean optOut;
	private int lives;
	private int healthModifier;

	public PlayerLifeImpl() {
		this.lives = Lives.startingLives;
		this.healthModifier = MaxHealth.startingHealth - 20;
	}

	@Override
	public boolean isOptOut() {
		return this.optOut;
	}

	@Override
	public void setOptOut(boolean optOut) {
		this.optOut = optOut;
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
		if (this.healthModifier < MaxHealth.cap.min)
			this.healthModifier = (int) MaxHealth.cap.min;
		else if (this.healthModifier > MaxHealth.cap.max)
			this.healthModifier = (int) MaxHealth.cap.max;
	}
}
