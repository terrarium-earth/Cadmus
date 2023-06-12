package earth.terrarium.cadmus.common.compat.journeymap;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.ClientClaims;
import journeymap.client.api.display.IOverlayListener;
import journeymap.client.api.display.ModPopupMenu;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import journeymap.client.api.util.PolygonHelper;
import journeymap.client.api.util.UIState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.awt.geom.Point2D;

public class ClaimedChunkDisplay {

    public static PolygonOverlay create(ChunkPos pos, ClientClaims.Entry entry, ResourceKey<Level> dimension) {
        String displayId = "claim_" + pos.toString();

        int darkColor = FastColor.ARGB32.color(
            255,
            (int) ((entry.color() >> 16 & 255) * 0.8f),
            (int) ((entry.color() >> 8 & 255) * 0.8f),
            (int) ((entry.color() & 255) * 0.8f)
        );
        int lightColor = entry.color();

        ShapeProperties shapeProps = new ShapeProperties()
            .setStrokeColor(darkColor).setStrokeWidth(2).setStrokeOpacity(.7f)
            .setFillColor(lightColor).setFillOpacity(.4f);

        MapPolygon polygon = PolygonHelper.createChunkPolygon(pos.x, 70, pos.z);

        PolygonOverlay overlay = new PolygonOverlay(Cadmus.MOD_ID, displayId, dimension, shapeProps, polygon);
        overlay.setTextProperties(new TextProperties().setColor(entry.color()));
        overlay.setOverlayListener(new Listener(overlay, entry));
        return overlay;
    }

    private record Listener(PolygonOverlay overlay, ClientClaims.Entry entry) implements IOverlayListener {


        @Override
        public void onMouseMove(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {
            overlay.setTitle("Claimed by " + entry.name().getString());
        }

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