package earth.terrarium.cadmus.client.compat.journeymap;

import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.olympus.client.constants.MinecraftColors;
import journeymap.api.v2.client.display.IOverlayListener;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.fullscreen.ModPopupMenu;
import journeymap.api.v2.client.model.MapPolygon;
import journeymap.api.v2.client.model.ShapeProperties;
import journeymap.api.v2.client.util.PolygonHelper;
import journeymap.api.v2.client.util.UIState;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.awt.geom.Point2D;
import java.util.UUID;

public class ClaimedChunkDisplay {

    public static PolygonOverlay create(ChunkPos pos, UUID id, boolean chunkLoaded, ResourceKey<Level> dimension) {
        Component name = TeamApi.API.getName(CadmusClient.level(), id);
        int color = Optionull.mapOrDefault(
            TeamApi.API.getColor(CadmusClient.level(), id),
            Color::getValue,
            MinecraftColors.AQUA.getValue());

        int darkColor = FastColor.ARGB32.color(
            255,
            (int) ((color >> 16 & 255) * 0.8f),
            (int) ((color >> 8 & 255) * 0.8f),
            (int) ((color & 255) * 0.8f)
        );

        ShapeProperties shapeProps = new ShapeProperties()
            .setStrokeColor(darkColor).setStrokeWidth(2).setStrokeOpacity(.7f)
            .setFillColor(color).setFillOpacity(.4f);

        MapPolygon polygon = PolygonHelper.createChunkPolygon(pos.x, 70, pos.z);

        PolygonOverlay overlay = new PolygonOverlay(Cadmus.MOD_ID, dimension, shapeProps, polygon);
        overlay.setOverlayListener(new Listener(overlay, name, chunkLoaded));
        return overlay;
    }

    private record Listener(PolygonOverlay overlay, Component name, boolean chunkLoaded) implements IOverlayListener {

        @Override
        public void onMouseMove(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {
            overlay.setTitle(Component.translatable("text.cadmus.claimed_by", name)
                .append(CommonComponents.SPACE)
                .append(chunkLoaded ?
                    Component.translatable("text.cadmus.chunk_loaded") :
                    CommonComponents.EMPTY
                ).getString());
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public void onMouseOut(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {
            overlay.setTitle(null);
        }

        public void onActivate(UIState mapState) {}

        public void onDeactivate(UIState mapState) {}

        public boolean onMouseClick(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition, int button, boolean doubleClick) {return false;}

        public void onOverlayMenuPopup(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition, ModPopupMenu modPopupMenu) {}
    }
}