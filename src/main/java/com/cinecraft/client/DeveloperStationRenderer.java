package com.cinecraft.client;

import com.cinecraft.CineCraft;
import com.cinecraft.block.DeveloperStationBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DeveloperStationRenderer extends GeoBlockRenderer<DeveloperStationBlockEntity> {
    public DeveloperStationRenderer() {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, "developer_station")));
    }
}
