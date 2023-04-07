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

            byte[] colors = new byte[(maxBlockX - minBlockX) * (maxBlockZ - minBlockZ)];
            if (level.dimensionType().hasCeiling()) {
                setColorsWithCeiling(colors, player.blockPosition(), minBlockX, minBlockZ, maxBlockX, maxBlockZ, level);
            } else {
                setColors(colors, minBlockX, minBlockZ, maxBlockX, maxBlockZ, level);
            }

            screen.update(new ClaimMapData(colors), getPixelScale(scale));
        }
    }

    public static int getPixelScale(int mapScale) {
        return mapScale * 2 + 16;
    }

    private static void setColors(byte[] colors, int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ, ClientLevel level) {
        for (int z = minBlockZ; z < maxBlockZ; z++) {
            for (int x = minBlockX; x < maxBlockX; x++) {
                var pos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
                var blockState = level.getBlockState(pos.below());

                MaterialColor.Brightness brightness = MaterialColor.Brightness.HIGH;

                colors[(z - minBlockZ) * (maxBlockX - minBlockX) + (x - minBlockX)] = blockState.getMapColor(level, pos).getPackedId(brightness);
            }
        }
    }

    private static void setColorsWithCeiling(byte[] colors, BlockPos playerPos, int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ, ClientLevel level) {
        for (int z = minBlockZ; z < maxBlockZ; z++) {
            for (int x = minBlockX; x < maxBlockX; x++) {
                var pos = findBlockWithAirAbove(level, new BlockPos(x, playerPos.getY(), z));

                var blockState = level.getBlockState(pos);

                MaterialColor.Brightness brightness = MaterialColor.Brightness.HIGH;

                colors[(z - minBlockZ) * (maxBlockX - minBlockX) + (x - minBlockX)] = blockState.getMapColor(level, pos).getPackedId(brightness);
            }
        }
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
