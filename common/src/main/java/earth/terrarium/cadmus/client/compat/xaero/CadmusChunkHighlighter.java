package earth.terrarium.cadmus.client.compat.xaero;

import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.color.ConstantColors;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.client.CadmusClient;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xaero.map.WorldMap;
import xaero.map.highlight.ChunkHighlighter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CadmusChunkHighlighter extends ChunkHighlighter {

    public CadmusChunkHighlighter() {
        super(true);
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return true;
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (!WorldMap.settings.displayClaims) return null;

        UUID id = getClaim(dimension, chunkX, chunkZ).map(Pair::left).orElse(null);
        if (id == null) return null;

        var topClaim = getClaim(dimension, chunkX, chunkZ - 1);
        var rightClaim = getClaim(dimension, chunkX + 1, chunkZ);
        var bottomClaim = getClaim(dimension, chunkX, chunkZ + 1);
        var leftClaim = getClaim(dimension, chunkX - 1, chunkZ);

        int color = getColor(id);
        int claimColorFormatted = (color & 255) << 24 | (color >> 8 & 255) << 16 | (color >> 16 & 255) << 8;
        int fillOpacity = WorldMap.settings.claimsFillOpacity;
        int borderOpacity = WorldMap.settings.claimsBorderOpacity;
        int centerColor = claimColorFormatted | 255 * fillOpacity / 100;
        int sideColor = claimColorFormatted | 255 * borderOpacity / 100;

        this.resultStore[0] = centerColor;
        this.resultStore[1] = topClaim.isEmpty() ? sideColor : centerColor;
        this.resultStore[2] = rightClaim.isEmpty() ? sideColor : centerColor;
        this.resultStore[3] = bottomClaim.isEmpty() ? sideColor : centerColor;
        this.resultStore[4] = leftClaim.isEmpty() ? sideColor : centerColor;
        return this.resultStore;
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!WorldMap.settings.displayClaims) return 0;
        if (!regionHasHighlights(dimension, regionX, regionZ)) return 0;

        UUID id = getClaim(dimension, regionX << 5, regionZ << 5).map(Pair::left).orElse(null);
        if (id == null) return 0;

        long accumulator = WorldMap.settings.claimsBorderOpacity;
        accumulator += id.getLeastSignificantBits();
        accumulator *= 37L;
        accumulator += id.getMostSignificantBits();
        accumulator *= 37L;
        accumulator = accumulator * 37L + (long) WorldMap.settings.claimsFillOpacity;
        accumulator = accumulator * 37L + regionX;
        accumulator = accumulator * 37L + regionZ;

        return (int) (accumulator >> 32) * 37 + (int) (accumulator);
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return this.isClaimed(dimension, chunkX, chunkZ);
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int x, int z) {
        if (!WorldMap.settings.displayClaims) return null;
        var claim = getClaim(dimension, x, z).orElse(null);
        if (claim == null) return null;
        Component name = TeamApi.API.getName(CadmusClient.level(), claim.left());
        return Component.translatable("text.cadmus.claimed_by", name)
            .append(CommonComponents.SPACE)
            .append(claim.rightBoolean() ?
                Component.translatable("text.cadmus.chunk_loaded").withStyle(ChatFormatting.GOLD) :
                CommonComponents.EMPTY
            );
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int x, int z) {
        return null;
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension, int blockX, int blockZ, int width) {}

    private Optional<ObjectBooleanPair<UUID>> getClaim(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return ClaimApi.API.getClientClaim(dimension, new ChunkPos(chunkX, chunkZ));
    }

    private boolean isClaimed(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return getClaim(dimension, chunkX, chunkZ).isPresent();
    }

    private static int getColor(UUID id) {
        return Optionull.mapOrDefault(
            TeamApi.API.getColor(CadmusClient.level(), id),
            Color::getValue,
            ConstantColors.aqua.getValue());
    }
}
