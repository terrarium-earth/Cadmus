package earth.terrarium.cadmus.client.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

public class ClaimMapUpdater {
    private static int prevScale;

    public static void update(boolean forceUpdate, @NotNull ClaimScreen screen, @NotNull LocalPlayer player, @NotNull ClientLevel level) {
        if (level instanceof ClaimChunkHolder holder) {
            // scale is based on render distance and is capped to 24 for mods like Optifine that allow for higher render distances
            int scale = Mth.clamp(Minecraft.getInstance().options.renderDistance().get(), 4, 24) * 4;
            scale -= scale % 16;
            // Don't update if the player hasn't moved chunks
            if (!forceUpdate && player.chunkPosition().equals(holder.cadmus$getChunkPos()) && prevScale == scale)
                return;
            holder.cadmus$setChunkPos(player.chunkPosition());
            prevScale = scale;

            ChunkPos chunkPos = holder.cadmus$getChunkPos();
            int minBlockX = chunkPos.getMinBlockX() - scale;
            int minBlockZ = chunkPos.getMinBlockZ() - scale;
            int maxBlockX = chunkPos.getMaxBlockX() + scale + 1;
            int maxBlockZ = chunkPos.getMaxBlockZ() + scale + 1;

            int[][] colors = new int[maxBlockX - minBlockX][maxBlockZ - minBlockZ];
            if (level.dimensionType().hasCeiling()) {
                colors = setColorsWithCeiling(colors, player.blockPosition(), minBlockX, minBlockZ, maxBlockX, maxBlockZ, level);
            } else {
                colors = setColors(colors, minBlockX, minBlockZ, maxBlockX, maxBlockZ, level);
            }

            screen.update(new ClaimMapData(colors), getPixelScale(scale));
        }
    }

    public static int getPixelScale(int mapScale) {
        return mapScale * 2 + 16;
    }

    private static int[][] setColors(int[][] colors, int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ, ClientLevel level) {
        int[][] yPositions = new int[colors.length][colors[0].length];
        for (int z = minBlockZ; z < maxBlockZ; z++) {
            for (int x = minBlockX; x < maxBlockX; x++) {
                var pos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
                var blockState = level.getBlockState(pos.below());

                yPositions[(x - minBlockX)][(z - minBlockZ)] = pos.getY();
                colors[(x - minBlockX)][(z - minBlockZ)] = (byte) blockState.getMapColor(level, pos).id;
            }
        }

        return applyTopology(colors, yPositions);
    }

    private static int[][] setColorsWithCeiling(int[][] colors, BlockPos playerPos, int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ, ClientLevel level) {
        int[][] yPositions = new int[colors.length][colors[0].length];
        for (int z = minBlockZ; z < maxBlockZ; z++) {
            for (int x = minBlockX; x < maxBlockX; x++) {
                var pos = findBlockWithAirAbove(level, new BlockPos(x, playerPos.getY(), z));

                var blockState = level.getBlockState(pos);

                yPositions[(x - minBlockX)][(z - minBlockZ)] = pos.getY();
                colors[(x - minBlockX)][(z - minBlockZ)] = (byte) (blockState.getMapColor(level, pos).id);
            }
        }

        return applyTopology(colors, yPositions);
    }

    public static int[][] applyTopology(int[][] colors, int[][] yPositions) {
        boolean[][] topology = new boolean[colors.length][colors[0].length];
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[i].length; j++) {
                int y = yPositions[i][j];
                topology[i][j] = (j != 0 && yPositions[i][j - 1] < y) ||
                        (i != colors.length - 1 && yPositions[i + 1][j] < y) ||
                        (j != colors[i].length - 1 && yPositions[i][j + 1] < y) ||
                        (i != 0 && yPositions[i - 1][j] < y);
            }
        }

        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[i].length; j++) {
                int color = colors[i][j];

                boolean isLower = (j != 0 && topology[i][j - 1]) ||
                        (i != colors.length - 1 && topology[i + 1][j]) ||
                        (j != colors[i].length - 1 && topology[i][j + 1]) ||
                        (i != 0 && topology[i - 1][j]);

                if (topology[i][j]) {
                    if (isLower) {
                        colors[i][j] = MapColors.getColor(color, MaterialColor.Brightness.NORMAL);
                    } else {
                        colors[i][j] = MapColors.getColor(color, MaterialColor.Brightness.HIGH);
                    }
                } else {
                    if (isLower) {
                        colors[i][j] = MapColors.getColor(color, MaterialColor.Brightness.LOWEST);
                    } else {
                        colors[i][j] = MapColors.getColor(color, MaterialColor.Brightness.LOW);
                    }
                }
            }
        }

        return colors;
    }

    private static BlockPos findBlockWithAirAbove(ClientLevel level, BlockPos pos) {
        int offset = 0;
        int y = pos.getY();
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        while (!(!level.getBlockState(mutablePos).isAir() && (level.getBlockState(mutablePos.above()).isAir() || level.getBlockState(mutablePos.below()).isAir()))) {
            if (offset <= 0) {
                offset = -offset + 1;
            } else {
                offset = -offset;
            }
            mutablePos.setY(offset + y);
        }
        return mutablePos.immutable();
    }
}
