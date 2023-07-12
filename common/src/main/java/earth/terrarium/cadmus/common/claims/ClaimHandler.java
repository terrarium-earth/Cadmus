package earth.terrarium.cadmus.common.claims;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.utils.SaveHandler;
import earth.terrarium.cadmus.Cadmus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimHandler extends SaveHandler {

    public static final String PLAYER_PREFIX = "p:";
    public static final String TEAM_PREFIX = "t:";
    public static final String ADMIN_PREFIX = "a:";

    private final Map<ChunkPos, Pair<String, ClaimType>> claims = new HashMap<>();
    private final Map<String, Map<ChunkPos, ClaimType>> claimsById = new HashMap<>();

    private final ClaimListenHandler listenHandler;

    private ClaimHandler(ResourceKey<Level> dimension) {
        this.listenHandler = new ClaimListenHandler(dimension);
    }

    @Override
    public void loadData(CompoundTag tag) {
        tag.getAllKeys().forEach(id -> {
            CompoundTag teamTag = tag.getCompound(id);
            Map<ChunkPos, ClaimType> claimData = new HashMap<>();
            teamTag.getAllKeys().forEach(chunkPos -> {
                ChunkPos pos = new ChunkPos(Long.parseLong(chunkPos));
                ClaimType type = ClaimType.values()[teamTag.getByte(chunkPos)];
                claimData.put(pos, type);
            });
            claimsById.put(id, claimData);
        });

        updateInternal();
    }

    @Override
    public void saveData(CompoundTag tag) {
        claimsById.forEach((id, claimData) -> {
            CompoundTag teamTag = new CompoundTag();
            claimData.forEach((pos, type) -> teamTag.putByte(String.valueOf(pos.toLong()), (byte) type.ordinal()));
            tag.put(id, teamTag);
        });
    }

    public static ClaimHandler read(ServerLevel level) {
        return read(level.getDataStorage(), () -> new ClaimHandler(level.dimension()), "cadmus_claims");
    }

    public static void addClaims(ServerLevel level, String id, Map<ChunkPos, ClaimType> claimData) {
        var data = read(level);
        // Remove any claims that are already claimed by another team
        claimData.keySet().removeAll(data.claims.keySet());

        data.listenHandler.addClaims(level, id, claimData.keySet());

        claimData.forEach((pos, type) -> data.claims.put(pos, Pair.of(id, type)));
        var currentClaims = data.claimsById.getOrDefault(id, new HashMap<>());
        currentClaims.putAll(claimData);
        data.claimsById.put(id, currentClaims);
    }

    public static void removeClaims(ServerLevel level, String id, Set<ChunkPos> claimData) {
        var data = read(level);
        data.listenHandler.removeClaims(level, id, claimData);
        claimData.forEach(pos -> {
            data.claims.remove(pos);
            data.claimsById.get(id).remove(pos);
        });
    }

    public static void clear(ServerLevel level, String id) {
        var data = read(level);
        if (data.claimsById.containsKey(id)) {
            data.listenHandler.removeClaims(level, id, data.claimsById.get(id).keySet());
        }
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
        var claimData = getTeamClaims(level, id);
        if (claimData == null) return;
        claimData.forEach((pos, type) -> {
            if (type == ClaimType.CHUNK_LOADED) {
                Cadmus.updateChunkForced(level, pos, setLoaded);
            }
        });
    }

    public static ClaimListenHandler getListener(ServerLevel level) {
        return read(level).listenHandler;
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
