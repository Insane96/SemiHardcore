package com.insane96mcp.semihardcore.capability;

import com.insane96mcp.semihardcore.setup.Strings;
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

    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Strings.Tags.PLAYER_LIFE);

    private final IPlayerLife backend = new PlayerLifeImpl();
    private final LazyOptional<IPlayerLife> optionalData = LazyOptional.of(() -> backend);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PlayerLife.INSTANCE.orEmpty(cap, this.optionalData);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(Strings.Tags.LIVES, backend.getLives());
        nbt.putInt(Strings.Tags.HEALTH_MODIFIER, backend.getHealthModifier());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.setLives(nbt.getInt(Strings.Tags.LIVES));
        backend.setHealthModifier(nbt.getInt(Strings.Tags.HEALTH_MODIFIER));
    }
}
