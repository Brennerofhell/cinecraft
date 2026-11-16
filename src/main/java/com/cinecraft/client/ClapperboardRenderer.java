package com.cinecraft.client;

import com.cinecraft.CineCraft;
import com.cinecraft.item.ClapperboardItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ClapperboardRenderer extends GeoItemRenderer<ClapperboardItem> {
    public ClapperboardRenderer() {
        super(new DefaultedItemGeoModel<ClapperboardItem>(
                ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, "clapperboard")) {
            @Override
            public ResourceLocation getTextureResource(ClapperboardItem animatable) {
                return ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, "textures/item/clapperboard_model.png");
            }
        });
    }
}
