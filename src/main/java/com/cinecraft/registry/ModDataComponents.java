package com.cinecraft.registry;

import com.cinecraft.CineCraft;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.Supplier;

/** Items only store the film's ID and length; the video itself lives in the server's film store. */
public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, CineCraft.MOD_ID);

    public static final Supplier<DataComponentType<UUID>> FILM_ID = COMPONENTS.registerComponentType("film_id",
            b -> b.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));

    public static final Supplier<DataComponentType<Integer>> FILM_SECONDS = COMPONENTS.registerComponentType("film_seconds",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
}
