package earth.terrarium.cadmus.common.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.common.team.TeamSaveData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@MethodsReturnNonnullByDefault
public class ClaimChunkSaveData extends SavedData {
    private static final ClaimChunkSaveData CLIENT_SIDE = new ClaimChunkSaveData();
    private final Map<ChunkPos, ClaimInfo> claims = new HashMap<>();

    public ClaimChunkSaveData() {
    }

    public ClaimChunkSaveData(CompoundTag tag, ServerLevel server) {
        tag.getAllKeys().forEach(key -> {
            var team = TeamSaveData.get(server, UUID.fromString(key));
            var teamTag = tag.getCompound(key);
            for (String key1 : teamTag.getAllKeys()) {
                var pos = new ChunkPos(Long.parseLong(key1));
                var type = ClaimType.values()[teamTag.getByte(key1)];
                var claimInfo = new ClaimInfo(team, type);
                claims.put(pos, claimInfo);
            }
        });
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        Map<UUID, List<Pair<ChunkPos, ClaimType>>> parsedClaims = new HashMap<>();
        claims.forEach((pos, info) -> parsedClaims.compute(info.team().teamId(), (key, pairs) -> {
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

    public static ClaimChunkSaveData read(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return CLIENT_SIDE;
        }
        return read(serverLevel);
    }

    public static ClaimChunkSaveData read(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(tag -> new ClaimChunkSaveData(tag, level), ClaimChunkSaveData::new, "cadmus_claimed_chunks");
    }

    public static void set(Level level, ChunkPos pos, ClaimInfo info) {
        var data = read(level);
        data.claims.put(pos, info);
        data.setDirty();
    }

    @Nullable
    public static ClaimInfo get(Player player) {
        return get(player.level, player.chunkPosition());
    }

    @Nullable
    public static ClaimInfo get(Level level, ChunkPos pos) {
        return read(level).claims.get(pos);
    }

    public static Map<ChunkPos, ClaimInfo> getAll(Level level) {
        return read(level).claims;
    }
}