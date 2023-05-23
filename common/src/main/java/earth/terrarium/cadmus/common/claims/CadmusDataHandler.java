package earth.terrarium.cadmus.common.claims;

import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Data that should only have one save instance, the overworld save data.
 */
public class CadmusDataHandler extends SavedData {
    private final Map<String, IntIntPair> maxClaimsById = new HashMap<>();

    public CadmusDataHandler() {
    }

    public CadmusDataHandler(CompoundTag tag) {
        CompoundTag maxClaimsTag = tag.getCompound("max_claims");
        maxClaimsTag.getAllKeys().forEach(id -> {
            CompoundTag maxClaimTag = maxClaimsTag.getCompound(id);
            int maxClaims = maxClaimTag.getInt("maxClaims");
            int maxChunkLoaded = maxClaimTag.getInt("maxChunkLoaded");
            maxClaimsById.put(id, IntIntPair.of(maxClaims, maxChunkLoaded));
        });

        String teamProvider = tag.getString("team_provider");
        if (!teamProvider.isEmpty()) {
            TeamProviderApi.API.setSelected(new ResourceLocation(teamProvider));
        }

        String maxClaimProvider = tag.getString("max_claim_provider");
        if (!maxClaimProvider.isEmpty()) {
            MaxClaimProviderApi.API.setSelected(new ResourceLocation(maxClaimProvider));
        }
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag) {
        CompoundTag maxClaimsTag = new CompoundTag();
        maxClaimsById.forEach((id, maxClaims) -> {
            CompoundTag maxClaimTag = new CompoundTag();
            maxClaimTag.putInt("maxClaims", maxClaims.firstInt());
            maxClaimTag.putInt("maxChunkLoaded", maxClaims.secondInt());
            maxClaimsTag.put(id, maxClaimTag);
        });
        tag.put("max_claims", maxClaimsTag);

        ResourceLocation selectedId = TeamProviderApi.API.getSelectedId();
        if (selectedId != null) {
            tag.putString("team_provider", selectedId.toString());
        }

        ResourceLocation maxClaimSelectedId = MaxClaimProviderApi.API.getSelectedId();
        if (maxClaimSelectedId != null) {
            tag.putString("max_claim_provider", maxClaimSelectedId.toString());
        }
        return tag;
    }

    public static CadmusDataHandler read(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CadmusDataHandler::new, CadmusDataHandler::new, "cadmus_data");
    }

    public static Map<String, IntIntPair> getMaxTeamClaims(MinecraftServer server) {
        return read(server).maxClaimsById;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
