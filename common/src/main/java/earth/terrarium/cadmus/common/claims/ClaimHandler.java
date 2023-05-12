package earth.terrarium.cadmus.common.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClaimHandler extends SavedData {
    private final Map<ChunkPos, Pair<String, ClaimType>> claims = new HashMap<>();
    private final Map<String, Map<ChunkPos, ClaimType>> claimsById = new HashMap<>();

    public ClaimHandler() {
    }

    public ClaimHandler(CompoundTag tag) {
        CompoundTag teamsTag = tag.getCompound("teams");
        teamsTag.getAllKeys().forEach(id -> {
            CompoundTag teamTag = teamsTag.getCompound(id);
            Map<ChunkPos, ClaimType> claimData = new HashMap<>();
            teamTag.getAllKeys().forEach(chunkPos -> {
                ChunkPos pos = new ChunkPos(Long.parseLong(chunkPos));
                ClaimType type = ClaimType.values()[teamTag.getByte(chunkPos)];
                claimData.put(pos, type);
            });
            claimsById.put(id, claimData);
        });
        TeamProviderApi.API.setSelected(new ResourceLocation(tag.getString("team_provider")));
        updateInternal();
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag) {
        CompoundTag teamsTag = new CompoundTag();
        claimsById.forEach((id, claimData) -> {
            CompoundTag teamTag = new CompoundTag();
            claimData.forEach((pos, type) -> teamTag.putByte(String.valueOf(pos.toLong()), (byte) type.ordinal()));
            teamsTag.put(id, teamTag);
        });
        tag.put("teams", teamsTag);

        ResourceLocation selectedId = TeamProviderApi.API.getSelectedId();
        if (selectedId != null) {
            tag.putString("team_provider", selectedId.toString());
        }
        return tag;
    }

    public static ClaimHandler read(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ClaimHandler::new, ClaimHandler::new, "cadmus_claims");
    }

    public static void addClaims(ServerLevel level, String id, Map<ChunkPos, ClaimType> claimData) {
        var data = read(level);
        claimData.forEach((pos, type) -> data.claims.put(pos, Pair.of(id, type)));
        var currentClaims = data.claimsById.getOrDefault(id, new HashMap<>());
        currentClaims.putAll(claimData);
        data.claimsById.put(id, currentClaims);
    }

    public static void removeClaims(ServerLevel level, String id, Set<ChunkPos> claimData) {
        var data = read(level);
        claimData.forEach(pos -> {
            data.claims.remove(pos);
            data.claimsById.get(id).remove(pos);
        });
    }

    public static void clear(ServerLevel level, String id) {
        var data = read(level);
        data.claimsById.remove(id);
        data.updateInternal();
    }

    @Nullable
    public static Pair<String, ClaimType> getClaim(ServerLevel level, ChunkPos pos) {
        return read(level).claims.get(pos);
    }

    @Nullable
    public static Map<ChunkPos, ClaimType> getTeamClaims(ServerLevel level, String id) {
        return read(level).claimsById.get(id);
    }

    public static Map<String, Map<ChunkPos, ClaimType>> getAllTeamClaims(ServerLevel level) {
        return read(level).claimsById;
    }

    public static void updateChunkLoaded(ServerLevel level, String id, boolean setLoaded) {
        ChunkSource chunkSource = level.getChunkSource();
        var claimData = getTeamClaims(level, id);
        if (claimData == null) return;
        claimData.forEach((pos, type) -> {
            if (type == ClaimType.CHUNK_LOADED) {
                chunkSource.updateChunkForced(pos, setLoaded);
            }
        });
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    private void updateInternal() {
        claims.clear();
        claimsById.forEach((id, claimData) -> claimData.forEach((pos, type) -> claims.put(pos, Pair.of(id, type))));
    }
}