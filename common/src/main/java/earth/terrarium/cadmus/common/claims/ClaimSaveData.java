package earth.terrarium.cadmus.common.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.common.util.ModGameRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimSaveData extends SavedData {
    private final Map<ChunkPos, ClaimInfo> claims = new HashMap<>();

    public ClaimSaveData() {
    }

    public ClaimSaveData(CompoundTag tag) {
        tag.getAllKeys().forEach(key -> {
            var teamTag = tag.getCompound(key);
            for (String key1 : teamTag.getAllKeys()) {
                ChunkPos pos = new ChunkPos(Long.parseLong(key1));
                ClaimType type = ClaimType.values()[teamTag.getByte(key1)];
                ClaimInfo claimInfo = new ClaimInfo(UUID.fromString(key), type);
                claims.put(pos, claimInfo);
            }
        });
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag) {
        Map<UUID, List<Pair<ChunkPos, ClaimType>>> parsedClaims = new HashMap<>();
        claims.forEach((pos, info) -> parsedClaims.compute(info.teamId(), (key, pairs) -> {
            if (pairs == null) {
                pairs = new ArrayList<>();
            }
            pairs.add(Pair.of(pos, info.type()));
            return pairs;
        }));
        parsedClaims.forEach((id, claims) -> {
            CompoundTag claimsTag = new CompoundTag();
            for (Pair<ChunkPos, ClaimType> claim : claims) {
                claimsTag.putByte(String.valueOf(claim.getFirst().toLong()), ((byte) claim.getSecond().ordinal()));
            }
            tag.put(id.toString(), claimsTag);
        });
        return tag;
    }

    public static ClaimSaveData read(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ClaimSaveData::new, ClaimSaveData::new, "cadmus_claimed_chunks");
    }

    public static void set(ServerLevel level, ChunkPos pos, ClaimInfo info) {
        var data = read(level);
        int maxClaims = ModGameRules.getOrCreateIntGameRule(level, ModGameRules.RULE_MAX_CLAIMED_CHUNKS);
        if (data.claims.size() + 1 >= maxClaims) return;
        data.claims.put(pos, info);
        data.setDirty();
    }

    public static void remove(ServerLevel level, ChunkPos pos) {
        var data = read(level);
        data.claims.remove(pos);
        data.setDirty();
    }

    public static void clear(ServerLevel level, UUID teamId) {
        var data = read(level);
        data.claims.entrySet().removeIf(entry -> entry.getValue().teamId().equals(teamId));
        data.setDirty();
    }

    @Nullable
    public static ClaimInfo get(ServerPlayer player) {
        return get(player.getLevel(), player.chunkPosition());
    }

    @Nullable
    public static ClaimInfo get(ServerLevel level, ChunkPos pos) {
        return read(level).claims.get(pos);
    }

    public static Map<ChunkPos, ClaimInfo> getAll(ServerLevel level) {
        return read(level).claims;
    }

    public static void updateChunkLoaded(ServerLevel level, UUID teamId, boolean setLoaded) {
        ChunkSource chunkSource = level.getChunkSource();

        getAll(level).forEach((pos, info) -> {
            if (info.teamId().equals(teamId) && info.type() == ClaimType.CHUNK_LOADED) {
                chunkSource.updateChunkForced(pos, setLoaded);
            }
        });
    }
}
