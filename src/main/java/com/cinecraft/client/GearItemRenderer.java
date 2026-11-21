package com.cinecraft.client;

import com.cinecraft.CineCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Renders a GeckoLib gear item from geo/item/&lt;id&gt;.geo.json with textures/item/&lt;id&gt;_model.png. */
public class GearItemRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    public GearItemRenderer(String id) {
        super(new DefaultedItemGeoModel<T>(ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, id)) {
            @Override
            public ResourceLocation getTextureResource(T animatable) {
                return ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, "textures/item/" + id + "_model.png");
            }
        });
    }
}
