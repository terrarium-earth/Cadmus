package earth.terrarium.cadmus.client.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ClaimMapUpdater {
    private static int lastScale;

    public static void update(boolean forceUpdate, @NotNull LocalPlayer player, @NotNull ClientLevel level, Consumer<ClaimMapData> callback) {
        if (level instanceof ChunkHolder holder) {
            int scale = getScaledRenderDistance();
            var playerChunkPos = player.chunkPosition();
            // Don't update if the player hasn't moved chunks
            if (!forceUpdate && playerChunkPos.equals(holder.cadmus$getChunkPos()) && lastScale == scale) {
                return;
            }

            holder.cadmus$setChunkPos(playerChunkPos);
            lastScale = scale;

            var chunkPos = holder.cadmus$getChunkPos();
            int minBlockX = chunkPos.getMinBlockX() - scale;
            int minBlockZ = chunkPos.getMinBlockZ() - scale;
            int maxBlockX = chunkPos.getMaxBlockX() + scale + 1;
            int maxBlockZ = chunkPos.getMaxBlockZ() + scale + 1;

            ClaimMapScreen.calculatingMap = true;
            CompletableFuture.supplyAsync(() -> {
                int[][] colors = ClaimMapColorers.setColors(minBlockX, minBlockZ, maxBlockX, maxBlockZ, level, player);
                return new ClaimMapData(colors, getChunkScale(scale));
            }).thenAcceptAsync(callback, Minecraft.getInstance());
        }
    }

    public static int getChunkScale(int mapScale) {
        return mapScale * 2 + 16;
    }

    public static int getScaledRenderDistance() {
        int scale = Math.min(Minecraft.getInstance().options.renderDistance().get(), ClaimMapScreen.MAX_MAP_SIZE * 2) * 8;
        scale -= scale % 16;
        return scale;
    }
}
