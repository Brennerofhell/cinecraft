package com.cinecraft.client;

import com.cinecraft.CineCraft;
import com.cinecraft.item.GearGeoItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Renders a {@link GearGeoItem} from geo/item/&lt;id&gt;.geo.json with textures/item/&lt;id&gt;_model.png. */
public class GearItemRenderer extends GeoItemRenderer<GearGeoItem> {
    public GearItemRenderer(String id) {
        super(new DefaultedItemGeoModel<GearGeoItem>(ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, id)) {
            @Override
            public ResourceLocation getTextureResource(GearGeoItem animatable) {
                return ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, "textures/item/" + id + "_model.png");
            }
        });
    }
}
