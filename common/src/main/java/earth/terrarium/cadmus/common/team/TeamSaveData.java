package earth.terrarium.cadmus.common.team;

import earth.terrarium.cadmus.common.claiming.ClaimType;
import earth.terrarium.cadmus.common.claiming.ClaimedChunk;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public class TeamSaveData extends SavedData {
    private final Map<String, Set<ClaimedChunk>> teams = new HashMap<>();

    public TeamSaveData() {
    }

    public TeamSaveData(CompoundTag tag) {
        tag.getAllKeys().forEach(key -> {
            var teamTag = tag.getCompound(key);
            Set<ClaimedChunk> chunks = teams.getOrDefault(key, new HashSet<>());
            int size = teamTag.getInt("Size");
            for (int i = 0; i < size; i++) {
                CompoundTag chunkTag = teamTag.getCompound("Chunk_" + i);
                int x = chunkTag.getInt("X");
                int z = chunkTag.getInt("Z");
                ClaimType type = ClaimType.values()[chunkTag.getByte("Type")];
                chunks.add(new ClaimedChunk(new ChunkPos(x, z), type));
            }
            teams.put(key, chunks);
        });
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        teams.forEach((team, chunks) -> {
            var teamTag = new CompoundTag();
            int i = 0;
            teamTag.putInt("Size", chunks.size());
            for (ClaimedChunk chunk : chunks) {
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putInt("X", chunk.pos().x);
                chunkTag.putInt("Z", chunk.pos().z);
                chunkTag.putByte("Type", (byte) chunk.type().ordinal());
                teamTag.put("Chunk_" + i, chunkTag);
                i++;
            }
            tag.put(team, teamTag);
        });
        return tag;
    }

    public static TeamSaveData read(ServerLevel level) {
        return read(level.getServer().overworld().getDataStorage());
    }

    public static TeamSaveData read(DimensionDataStorage storage) {
        return storage.computeIfAbsent(TeamSaveData::new, TeamSaveData::new, "cadmus_claimed_chunks");
    }

    public static void set(ServerPlayer player, String team, Set<ClaimedChunk> chunks) {
        var data = read((ServerLevel) player.level);
        data.teams.put(team, chunks);
        data.setDirty();
    }

    public static Set<ClaimedChunk> get(ServerPlayer player) {
        var data = read((ServerLevel) player.level);
        // TODO use team provider
        return data.teams.getOrDefault(player.getTeam() == null ? "" : player.getTeam().getName(), new HashSet<>());
    }

    public static Set<ClaimedChunk> getAll(ServerPlayer player) {
        var data = read((ServerLevel) player.level);
        return data.teams.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }
}