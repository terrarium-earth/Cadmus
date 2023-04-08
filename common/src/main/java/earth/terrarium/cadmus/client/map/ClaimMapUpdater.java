package earth.terrarium.cadmus.client.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ClaimMapUpdater {
    private static int prevScale;

    public static void update(boolean forceUpdate, @NotNull ClaimScreen screen, @NotNull LocalPlayer player, @NotNull ClientLevel level) {
        if (level instanceof ClaimChunkHolder holder) {
            // scale is based on render distance and is capped to 24
            int scale = getScaledRenderDistance();
            // Don't update if the player hasn't moved chunks
            if (!forceUpdate && player.chunkPosition().equals(holder.cadmus$getChunkPos()) && prevScale == scale)
                return;
            holder.cadmus$setChunkPos(player.chunkPosition());
            prevScale = scale;

            var chunkPos = holder.cadmus$getChunkPos();
            int minBlockX = chunkPos.getMinBlockX() - scale;
            int minBlockZ = chunkPos.getMinBlockZ() - scale;
            int maxBlockX = chunkPos.getMaxBlockX() + scale + 1;
            int maxBlockZ = chunkPos.getMaxBlockZ() + scale + 1;

            int[][] colors = ClaimMapColorers.setColors(new int[maxBlockX - minBlockX][maxBlockZ - minBlockZ], minBlockX, minBlockZ, maxBlockX, maxBlockZ, level, player);
            screen.update(new ClaimMapData(colors), getPixelScale(scale));
        }
    }

    public static int getPixelScale(int mapScale) {
        return mapScale * 2 + 16;
    }

    public static int getScaledRenderDistance() {
        int scale = Mth.clamp(Minecraft.getInstance().options.renderDistance().get(), 4, 24) * 4;
        scale -= scale % 16;
        return scale;
    }
}
