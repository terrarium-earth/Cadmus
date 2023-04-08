package earth.terrarium.cadmus.client.map;

import com.teamresourceful.resourcefullib.common.color.ConstantColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

public class ClaimMapUpdater {
    public static final int BRIGHTER_COLOR = 0xff030303;
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
            var playerPos = player.blockPosition();
            int minBlockX = chunkPos.getMinBlockX() - scale;
            int minBlockZ = chunkPos.getMinBlockZ() - scale;
            int maxBlockX = chunkPos.getMaxBlockX() + scale + 1;
            int maxBlockZ = chunkPos.getMaxBlockZ() + scale + 1;

            int[][] colors = new int[maxBlockX - minBlockX][maxBlockZ - minBlockZ];
//            if (level.dimensionType().hasCeiling()) {
//                colors = setColorsWithCeiling(colors, playerPos, minBlockX, minBlockZ, maxBlockX, maxBlockZ, level);
//            } else {
                colors = ClaimMapColorers.setColors(colors, minBlockX, minBlockZ, maxBlockX, maxBlockZ, level, player);
                //colors = setColors(colors, minBlockX, minBlockZ, maxBlockX, maxBlockZ, level);
//            }

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

    private static int[][] setColors(int[][] colors, int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ, ClientLevel level) {
        int[][] yPositions = new int[colors.length][colors[0].length];
        boolean[][] colorYPositions = new boolean[colors.length][colors[0].length];
        for (int z = minBlockZ; z < maxBlockZ; z++) {
            for (int x = minBlockX; x < maxBlockX; x++) {
                var pos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
                var blockState = level.getBlockState(pos.below());

                yPositions[(x - minBlockX)][(z - minBlockZ)] = pos.getY();
                if (blockState.getMaterial() == Material.WATER) {
                    int waterColor = BiomeColors.getAverageWaterColor(level, pos);
                    // bgr
                    colors[(x - minBlockX)][(z - minBlockZ)] = brighter(rgb2abgr(waterColor));
                    colorYPositions[(x - minBlockX)][(z - minBlockZ)] = true;
                    continue;
                }

                int color = Minecraft.getInstance().getBlockColors().getColor(blockState, level, pos, 0);
                if (color != -1) {
                    colorYPositions[(x - minBlockX)][(z - minBlockZ)] = true;
                    int multiply = FastColor.ARGB32.multiply(ConstantColors.grey.getValue() | 0xff000000, color | 0xff000000);
                    colors[(x - minBlockX)][(z - minBlockZ)] = brighter(rgb2abgr(multiply));
                } else {
                    colors[(x - minBlockX)][(z - minBlockZ)] = (byte) (blockState.getMapColor(level, pos).id);
                }
            }
        }

        return applyTopology(colors, yPositions, colorYPositions);
    }

    private static int rgb2abgr(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return (blue << 16) | (green << 8) | red | 0xff000000;
    }

    private static int darker(int abgr) {
        int red = (abgr >> 16) & 0xFF;
        int green = (abgr >> 8) & 0xFF;
        int blue = abgr & 0xFF;
        return (Math.max((int) (red * 0.7f), 0) << 16) | (Math.max((int) (green * 0.7f), 0) << 8) | Math.max((int) (blue * 0.7f), 0) | 0xff000000;
    }

    private static int brighter(int abgr) {
        int red = (abgr >> 16) & 0xFF;
        int green = (abgr >> 8) & 0xFF;
        int blue = abgr & 0xFF;
        if (red == 0 && green == 0 && blue == 0) {
            return BRIGHTER_COLOR;
        }
        if (red > 0 && red < 3) {
            red = 3;
        }
        if (green > 0 && green < 3) {
            green = 3;
        }
        if (blue > 0 && blue < 3) {
            blue = 3;
        }
        return (Math.min((int) (red / 0.7f), 255) << 16) | (Math.min((int) (green / 0.7f), 255) << 8) | Math.min((int) (blue / 0.7f), 255) | 0xff000000;
    }

    private static int[][] setColorsWithCeiling(int[][] colors, BlockPos playerPos, int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ, ClientLevel level) {
        int[][] yPositions = new int[colors.length][colors[0].length];
        boolean[][] colorYPositions = new boolean[colors.length][colors[0].length];
        for (int z = minBlockZ; z < maxBlockZ; z++) {
            for (int x = minBlockX; x < maxBlockX; x++) {
                var pos = findBlockWithAirAbove(level, new BlockPos(x, playerPos.getY(), z));

                var blockState = level.getBlockState(pos);

                yPositions[(x - minBlockX)][(z - minBlockZ)] = pos.getY();
                if (blockState.getMaterial() == Material.WATER) {
                    int waterColor = BiomeColors.getAverageWaterColor(level, pos);
                    colors[(x - minBlockX)][(z - minBlockZ)] = rgb2abgr(waterColor);
                    colorYPositions[(x - minBlockX)][(z - minBlockZ)] = true;
                    continue;
                }

                int color = Minecraft.getInstance().getBlockColors().getColor(blockState, level, pos, 0);
                if (color != -1) {
                    colorYPositions[(x - minBlockX)][(z - minBlockZ)] = true;
                    int multiply = FastColor.ARGB32.multiply(ConstantColors.grey.getValue() | 0xff000000, color | 0xff000000);
                    colors[(x - minBlockX)][(z - minBlockZ)] = rgb2abgr(multiply);
                } else {
                    colors[(x - minBlockX)][(z - minBlockZ)] = MapColors.getColor((byte) (blockState.getMapColor(level, pos).id), MaterialColor.Brightness.HIGH);
                }
            }
        }

        return applyTopology(colors, yPositions, colorYPositions);
    }

    public static int[][] applyTopology(int[][] colors, int[][] yPositions, boolean[][] colorYPositions) {
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
                        if (colorYPositions[i][j]) {
                            colors[i][j] = darker(colors[i][j]);
                        } else {
                            colors[i][j] = MapColors.getColor(color, MaterialColor.Brightness.LOW);
                        }
                    } else {
                        if (!colorYPositions[i][j]) {
                            colors[i][j] = MapColors.getColor(color, MaterialColor.Brightness.LOWEST);
                        }
                    }
                } else {
                    if (isLower) {
                        if (colorYPositions[i][j]) {
                            colors[i][j] = darker(darker(darker(colors[i][j])));
                        } else {
                            colors[i][j] = MapColors.getColor(color, MaterialColor.Brightness.HIGH);
                        }
                    } else {
                        if (colorYPositions[i][j]) {
                            colors[i][j] = darker(darker(colors[i][j]));
                        } else {
                            colors[i][j] = MapColors.getColor(color, MaterialColor.Brightness.NORMAL);
                        }
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
