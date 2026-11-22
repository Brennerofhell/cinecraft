package com.cinecraft.compat;

import com.cinecraft.CineCraft;
import com.cinecraft.registry.ModBlocks;
import com.cinecraft.registry.ModItems;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * In-game tutorials for CineCraft blocks through Ponder (the system Create uses). Optional: nothing here is
 * touched unless the "ponder" mod is loaded (see ClientSetup); keep every Ponder reference inside this class.
 */
public final class PonderCompat implements PonderPlugin {
    private PonderCompat() {}

    /** Call only when Ponder is loaded (check {@code ModList} first, otherwise this class cannot load). */
    public static void register() {
        PonderIndex.addPlugin(new PonderCompat());
    }

    @Override
    public String getModId() {
        return CineCraft.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.addStoryBoard(ModBlocks.PROJECTOR.getId(), "projector", PonderCompat::projector);
        helper.addStoryBoard(ModBlocks.DEVELOPER_STATION.getId(), "developer_station", PonderCompat::developerStation);
        helper.addStoryBoard(ModBlocks.FILM_SHELF.getId(), "film_shelf", PonderCompat::filmShelf);
        helper.addStoryBoard(ModItems.ANALOG_CAMERA.getId(), "camera", PonderCompat::camera);
        helper.addStoryBoard(ModItems.DIGITAL_CAMERA.getId(), "digital_camera", PonderCompat::digitalCamera);
        helper.addStoryBoard(ModItems.CLAPPERBOARD.getId(), "clapperboard", PonderCompat::clapperboard);

        for (PonderScenes.Entry entry : PonderScenes.ENTRIES) {
            var components = entry.components().stream()
                    .map(id -> ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, id)).toList();
            helper.forComponents(components).addStoryBoard(entry.id(), (scene, util) -> simpleScene(scene, util, entry));
        }
    }

    /** Structure: 5x5 floor, screen at (2,1,0), projector at (2,1,3) facing the screen. */
    private static void projector(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("projector", "Using the Film Projector");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);

        BlockPos projector = util.grid().at(2, 1, 3);
        scene.overlay().showText(70)
                .text("The Film Projector shows films on a Projection Screen")
                .pointAt(util.vector().topOf(projector))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlock(projector, state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.overlay().showText(70)
                .text("Right-click it to switch it on. The reels start turning and it gives off light")
                .pointAt(util.vector().topOf(projector))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("A redstone signal switches it on and off as well")
                .pointAt(util.vector().centerOf(projector))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(100)
                .text("Right-click with a recorded video tape to insert it. The film plays on the Projection Screen it faces. Sneak and right-click with an empty hand takes the tape out")
                .pointAt(util.vector().topOf(projector))
                .placeNearTarget();
        scene.idle(110);
    }

    /** Structure: 5x5 floor, developer station at (2,1,2). */
    private static void developerStation(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("developer_station", "Developing Film");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);

        BlockPos station = util.grid().at(2, 1, 2);
        scene.overlay().showText(70)
                .text("The Developer Station turns exposed film into developed film")
                .pointAt(util.vector().topOf(station))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(80)
                .text("Right-click it, insert the exposed film, the developer and the fixer, and take out the result")
                .pointAt(util.vector().centerOf(station))
                .placeNearTarget();
        scene.idle(90);
    }

    /** Shows a tripod and the given texts one by one; camera items have no block of their own. */
    private static void tripodScene(SceneBuilder scene, SceneBuildingUtil util, String id, String title, String... texts) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);
        BlockPos tripod = util.grid().at(2, 1, 2);
        for (String text : texts) {
            scene.overlay().showText(90)
                    .text(text)
                    .pointAt(util.vector().topOf(tripod))
                    .placeNearTarget();
            scene.idle(100);
        }
    }

    private static void camera(SceneBuilder scene, SceneBuildingUtil util) {
        tripodScene(scene, util, "camera", "Taking Photos with the Analog Camera",
                "The Analog Camera works like an Exposure camera: right-click to open the viewfinder",
                "It takes film rolls. Filters, lenses and a flash can be added through the attachments",
                "Put a Lens Cap on it by using the cap with the camera in your other hand. A capped camera cannot shoot");
    }

    private static void digitalCamera(SceneBuilder scene, SceneBuildingUtil util) {
        tripodScene(scene, util, "digital_camera", "The Digital Camera",
                "The Digital Camera stores photos on Memory Cards instead of film",
                "It needs a charged Battery. Use the battery with the camera in your other hand",
                "A battery lasts 100 photos. To view the photos, craft the Memory Card with Redstone");
    }

    private static void clapperboard(SceneBuilder scene, SceneBuildingUtil util) {
        tripodScene(scene, util, "clapperboard", "Using the Clapperboard",
                "Right-click the Clapperboard to clap and count up the take",
                "Sneak and right-click to start a new scene. The take counter starts over",
                "While recording, clapping flashes the picture and shows the scene and take number in the film");
    }

    private static void filmShelf(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("film_shelf", "Storing Film");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);
        BlockPos shelf = util.grid().at(2, 1, 2);
        scene.overlay().showText(80)
                .text("The Film Shelf stores film rolls and other film material")
                .pointAt(util.vector().topOf(shelf))
                .placeNearTarget();
        scene.idle(90);
        scene.overlay().showText(80)
                .text("Right-click it to open its 18 slots. Other items do not fit")
                .pointAt(util.vector().centerOf(shelf))
                .placeNearTarget();
        scene.idle(90);
    }

    /** One block in the middle of the floor and the entry's texts one after another. */
    private static void simpleScene(SceneBuilder scene, SceneBuildingUtil util, PonderScenes.Entry entry) {
        scene.title(entry.id(), entry.title());
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);
        BlockPos block = util.grid().at(2, 1, 2);
        for (String text : entry.texts()) {
            scene.overlay().showText(100)
                    .text(text)
                    .pointAt(util.vector().topOf(block))
                    .placeNearTarget();
            scene.idle(110);
        }
    }
}
