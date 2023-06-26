package earth.terrarium.cadmus.common.compat.xaero;

import earth.terrarium.cadmus.client.ClientClaims;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.map.WorldMap;
import xaero.map.highlight.ChunkHighlighter;

import java.util.List;

public class CadmusChunkHighlighter extends ChunkHighlighter {
    private final XaeroClaimsManager manager;
    protected CadmusChunkHighlighter(XaeroClaimsManager manager) {
        super(true);
        this.manager = manager;
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (!WorldMap.settings.displayClaims) return null;
        ClientClaims.Entry currentClaim = this.manager.get(dimension, chunkX, chunkZ);
        if (currentClaim == null) return null;

        ClientClaims.Entry topClaim = this.manager.get(dimension, chunkX, chunkZ - 1);
        ClientClaims.Entry rightClaim = this.manager.get(dimension, chunkX + 1, chunkZ);
        ClientClaims.Entry bottomClaim = this.manager.get(dimension, chunkX, chunkZ + 1);
        ClientClaims.Entry leftClaim = this.manager.get(dimension, chunkX - 1, chunkZ);

        int claimColor = currentClaim.color();
        int claimColorFormatted = (claimColor & 255) << 24 | (claimColor >> 8 & 255) << 16 | (claimColor >> 16 & 255) << 8;
        int fillOpacity = WorldMap.settings.claimsFillOpacity;
        int borderOpacity = WorldMap.settings.claimsBorderOpacity;
        int centerColor = claimColorFormatted | 255 * fillOpacity / 100;
        int sideColor = claimColorFormatted | 255 * borderOpacity / 100;

        this.resultStore[0] = centerColor;
        this.resultStore[1] = topClaim != currentClaim ? sideColor : centerColor;
        this.resultStore[2] = rightClaim != currentClaim ? sideColor : centerColor;
        this.resultStore[3] = bottomClaim != currentClaim ? sideColor : centerColor;
        this.resultStore[4] = leftClaim != currentClaim ? sideColor : centerColor;
        return this.resultStore;
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int x, int z) {
        return null;
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int x, int z) {
        if (!WorldMap.settings.displayClaims) return null;
        if (!ClientClaims.CLAIMS.containsKey(dimension)) return null;
        ClientClaims.Entry claim = this.manager.get(dimension, x, z);
        if (claim == null) return null;
        return claim.name();
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!WorldMap.settings.displayClaims) return 0;
        if (!ClientClaims.CLAIMS.containsKey(dimension)) return 0;
        if (!regionHasHighlights(dimension, regionX, regionZ)) return 0;

        long accumulator = 0L;
        accumulator = accumulator * 37L + (long)WorldMap.settings.claimsBorderOpacity;
        accumulator = accumulator * 37L + (long)WorldMap.settings.claimsFillOpacity;

        accumulator = accumulator * 37L + regionX;
        accumulator = accumulator * 37L + regionZ;

        return (int)(accumulator >> 32) * 37 + (int)(accumulator & -1L);
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!WorldMap.settings.displayClaims) return false;
        if (!ClientClaims.CLAIMS.containsKey(dimension)) return false;

        for (int chunkXOffset = 0; chunkXOffset < 32; chunkXOffset++) {
            for (int chunkZOffset = 0; chunkZOffset < 32; chunkZOffset++) {
                if (this.manager.get(dimension, regionX * 32 + chunkXOffset, regionZ * 32 + chunkZOffset) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return this.manager.get(dimension, chunkX, chunkZ) != null;
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension, int blockX, int blockZ, int width) {

    }
}
