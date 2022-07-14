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

public class LivesProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Strings.Tags.LIVES);

    private final ILives backend = new LivesImpl();
    private final LazyOptional<ILives> optionalData = LazyOptional.of(() -> backend);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return Lives.INSTANCE.orEmpty(cap, this.optionalData);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(Strings.Tags.LIVES, backend.getLives());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.setLives(nbt.getInt(Strings.Tags.LIVES));
    }
}
