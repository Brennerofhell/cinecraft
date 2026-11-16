package com.cinecraft.client;

import com.cinecraft.CineCraft;
import com.cinecraft.block.ProjectorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ProjectorRenderer extends GeoBlockRenderer<ProjectorBlockEntity> {
    public ProjectorRenderer() {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, "projector")));
    }
}
