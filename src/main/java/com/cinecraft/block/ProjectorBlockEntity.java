package com.cinecraft.block;

import com.cinecraft.registry.ModBlockEntities;
import com.cinecraft.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/** Projector: spins its reels while switched on and holds the video tape whose film it projects. */
public class ProjectorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.projector.run");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private ItemStack tape = ItemStack.EMPTY;

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROJECTOR.get(), pos, state);
    }

    public ItemStack tape() {
        return tape;
    }

    /** Film on the inserted tape, or {@code null} if there is none. */
    public UUID filmId() {
        return tape.isEmpty() ? null : tape.get(ModDataComponents.FILM_ID.get());
    }

    public void setTape(ItemStack newTape) {
        this.tape = newTape;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!tape.isEmpty()) {
            tag.put("Tape", tape.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tape = tag.contains("Tape") ? ItemStack.parseOptional(registries, tag.getCompound("Tape")) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            var be = state.getAnimatable();
            if (be.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                return state.setAndContinue(RUN);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
