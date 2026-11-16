package com.cinecraft.registry;

import com.cinecraft.CineCraft;
import com.cinecraft.block.DeveloperStationBlockEntity;
import com.cinecraft.block.ProjectorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CineCraft.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProjectorBlockEntity>> PROJECTOR =
            TYPES.register("projector", () -> BlockEntityType.Builder
                    .of(ProjectorBlockEntity::new, ModBlocks.PROJECTOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeveloperStationBlockEntity>> DEVELOPER_STATION =
            TYPES.register("developer_station", () -> BlockEntityType.Builder
                    .of(DeveloperStationBlockEntity::new, ModBlocks.DEVELOPER_STATION.get()).build(null));
}
