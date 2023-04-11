package earth.terrarium.cadmus.common.team;

import com.teamresourceful.resourcefullib.common.lib.Constants;
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
        tag.getAllKeys().forEach(teamKey -> {
            var teamTag = tag.getCompound(teamKey);
            Set<ClaimedChunk> chunks = teams.getOrDefault(teamKey, new HashSet<>());
            for (String key : teamTag.getAllKeys()) {
                try {
                    long longPos = Long.parseLong(key);
                    var pos = new ChunkPos(longPos);
                    ClaimType type = ClaimType.values()[teamTag.getByte(key)];
                    chunks.add(new ClaimedChunk(pos, type));
                } catch (Exception e) {
                    Constants.LOGGER.error("Failed to load claimed chunk for {}", key);
                    e.printStackTrace();
                }
            }
            teams.put(teamKey, chunks);
        });
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        teams.forEach((team, chunks) -> {
            var teamTag = new CompoundTag();
            chunks.forEach(chunk -> teamTag.putByte(String.valueOf(chunk.pos().toLong()), (byte) chunk.type().ordinal()));
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