package com.cinecraft.registry;

import com.cinecraft.CineCraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CineCraft.MOD_ID);

    public static final DeferredBlock<Block> TRIPOD = BLOCKS.registerBlock("tripod",
            props -> new com.cinecraft.block.ShapedBlock(props, com.cinecraft.block.ShapedBlock.box(4, 0, 4, 12, 16, 12)),
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());

    public static final DeferredBlock<Block> SCREEN = BLOCKS.registerBlock("screen",
            props -> new com.cinecraft.block.ShapedBlock(props, com.cinecraft.block.ShapedBlock.box(0, 0, 13, 16, 16, 16)),
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.0f));

    public static final DeferredBlock<Block> PROJECTOR = BLOCKS.registerBlock("projector",
            com.cinecraft.block.ProjectorBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());

    public static final DeferredBlock<Block> DARKROOM_ENLARGER = BLOCKS.registerBlock("darkroom_enlarger",
            props -> new com.cinecraft.block.ShapedBlock(props, com.cinecraft.block.ShapedBlock.box(1, 0, 0, 15, 15, 15)),
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());

    public static final DeferredBlock<Block> DARKROOM_SAFELIGHT = BLOCKS.registerBlock("darkroom_safelight",
            props -> new com.cinecraft.block.ShapedBlock(props, com.cinecraft.block.ShapedBlock.box(3, 0, 4, 13, 14, 12)),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.5f).noOcclusion());

    public static final DeferredBlock<Block> DEVELOPER_TRAY = BLOCKS.registerBlock("developer_tray",
            props -> new com.cinecraft.block.ShapedBlock(props, com.cinecraft.block.ShapedBlock.box(0.5, 0, 0.5, 15.5, 4, 15.5)),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.0f).noOcclusion());

    public static final DeferredBlock<Block> DEVELOPER_STATION = BLOCKS.registerSimpleBlock("developer_station",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0f).noOcclusion());

    public static final DeferredBlock<Block> FILM_SHELF = BLOCKS.registerSimpleBlock("film_shelf",
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());

    /** Registers a BlockItem; called from ModItems. */
    static void registerBlockItems(DeferredRegister.Items items) {
        items.registerSimpleBlockItem("tripod", TRIPOD, new Item.Properties());
        items.registerSimpleBlockItem("screen", SCREEN, new Item.Properties());
        items.registerSimpleBlockItem("projector", PROJECTOR, new Item.Properties());
        items.registerSimpleBlockItem("darkroom_enlarger", DARKROOM_ENLARGER, new Item.Properties());
        items.registerSimpleBlockItem("darkroom_safelight", DARKROOM_SAFELIGHT, new Item.Properties());
        items.registerSimpleBlockItem("developer_tray", DEVELOPER_TRAY, new Item.Properties());
        items.registerSimpleBlockItem("developer_station", DEVELOPER_STATION, new Item.Properties());
        items.registerSimpleBlockItem("film_shelf", FILM_SHELF, new Item.Properties());
    }
}
