package insane96mcp.semihardcore.capability;

import insane96mcp.semihardcore.SemiHardcore;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerLifeProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

	public static final ResourceLocation IDENTIFIER = new ResourceLocation(SemiHardcore.MOD_ID, "semihardcore");

	private final IPlayerLife backend = new PlayerLifeImpl();
	private final LazyOptional<IPlayerLife> optionalData = LazyOptional.of(() -> backend);

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return PlayerLifeImpl.INSTANCE.orEmpty(cap, this.optionalData);
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("lives", backend.getLives());
		nbt.putInt("health_modifier", backend.getHealthModifier());
		nbt.putBoolean("opt_out", backend.isOptOut());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		backend.setLives(nbt.getInt("lives"));
		backend.setHealthModifier(nbt.getInt("health_modifier"));
		backend.setOptOut(nbt.getBoolean("opt_out"));
	}
}