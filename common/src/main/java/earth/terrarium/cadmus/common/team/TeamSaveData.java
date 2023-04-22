package earth.terrarium.cadmus.common.team;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.team.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimChunkSaveData;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TeamSaveData extends SavedData {
    private static final TeamSaveData CLIENT_SIDE = new TeamSaveData();
    private final Map<UUID, Team> teams = new HashMap<>();
    private final Map<UUID, UUID> players = new HashMap<>();

    public TeamSaveData() {
    }

    public TeamSaveData(CompoundTag tag) {
        tag.getAllKeys().forEach(key -> {
            var teamTag = tag.getCompound(key);
            var creator = UUID.fromString(teamTag.getString("creator"));
            Set<UUID> members = new HashSet<>();
            teamTag.getList("members", Tag.TAG_STRING).forEach((member) -> members.add(UUID.fromString(member.getAsString())));
            var name = teamTag.getString("name");
            teams.put(UUID.fromString(key), new Team(UUID.fromString(key), creator, members, name));
        });
        updateInternal();
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        teams.forEach((uuid, team) -> {
            var teamIdTag = new CompoundTag();
            teamIdTag.putString("creator", team.creator().toString());
            ListTag members = new ListTag();
            team.members().forEach((member) -> members.add(StringTag.valueOf(member.toString())));
            teamIdTag.put("members", members);
            teamIdTag.putString("name", team.name());
            tag.put(uuid.toString(), teamIdTag);
        });
        return tag;
    }

    public static TeamSaveData read(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return CLIENT_SIDE;
        }
        return read(serverLevel.getServer());
    }

    public static TeamSaveData read(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(tag -> new TeamSaveData(tag), TeamSaveData::new, "cadmus_teams");
    }

    public static Team getOrCreate(ServerPlayer player) {
        Team team = getPlayerTeam(player);
        if (team == null) return set(player);
        return team;
    }

    public static Team getOrCreate(ServerPlayer player, String name) {
        Team team = getPlayerTeam(player);
        if (team == null) return set(player, name);
        return team;
    }

    public static Team set(ServerPlayer player) {
        return set(player, TeamProviderApi.API.getSelected().getTeamName(player.server, player.getGameProfile()));
    }

    public static Team set(ServerPlayer player, String name) {
        var data = read(player.level);
        UUID teamId = ModUtils.generate(Predicate.not(data.teams::containsKey), UUID::randomUUID);
        Set<GameProfile> members = TeamProviderApi.API.getSelected().getTeamMembers(player.getServer(), player.getGameProfile());
        Team team = new Team(teamId, player.getUUID(), members.stream().map(GameProfile::getId).collect(Collectors.toSet()), name);
        data.teams.put(teamId, team);
        data.setDirty();
        for (GameProfile member : members) {
            data.players.put(member.getId(), teamId);
        }
        return team;
    }

    @Nullable
    public static Team get(Level level, UUID teamId) {
        var data = read(level);
        return data.teams.get(teamId);
    }

    public static Team getPlayerTeam(Player player) {
        var data = read(player.level);
        UUID uuid = data.players.get(player.getUUID());
        return data.teams.get(uuid);
    }

    public static void addTeamMember(ServerPlayer player, Team team) {
        var data = read(player.server);
        data.teams.get(team.teamId()).members().add(player.getUUID());
        data.setDirty();
        data.updateInternal();
    }

    public static void removeTeamMember(ServerPlayer player, Team team) {
        var data = read(player.server);
        data.teams.get(team.teamId()).members().remove(player.getUUID());
        removeTeamsWithNoMembers(player.server);
        data.setDirty();
        data.updateInternal();
    }

    public static void removeTeamsWithNoMembers(MinecraftServer server) {
        var data = read(server);
        for (Team team : data.teams.values()) {
            if (team.members().isEmpty()) {
                data.teams.remove(team.teamId());
                for (ServerLevel level : server.getAllLevels()) {
                    for (var entry : ClaimChunkSaveData.getTeamChunks(level, team).entrySet()) {
                        if (entry.getValue().team().teamId().equals(team.teamId())) {
                            ClaimChunkSaveData.remove(level, entry.getKey());
                        }
                    }
                }
            }
        }
    }

    public static Collection<Team> getTeams(MinecraftServer server) {
        return read(server).teams.values();
    }

    public static void update(MinecraftServer server) {
        read(server).updateInternal();
    }

    private void updateInternal() {
        players.clear();

        for (Team team : teams.values()) {
            for (UUID member : team.members()) {
                players.put(member, team.teamId());
            }
        }
    }
}
