package earth.terrarium.cadmus.common.teams;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TeamSaveData extends SavedData {
    private final Map<UUID, Team> teams = new HashMap<>();
    private final Map<UUID, UUID> players = new HashMap<>();

    public TeamSaveData() {
    }

    public TeamSaveData(CompoundTag tag) {
        tag.getAllKeys().forEach(key -> {
            CompoundTag teamTag = tag.getCompound(key);
            Set<UUID> members = new HashSet<>();
            teamTag.getList("members", Tag.TAG_STRING).forEach((member) -> members.add(UUID.fromString(member.getAsString())));
            String name = teamTag.getString("name");
            teams.put(UUID.fromString(key), new Team(UUID.fromString(key), members, name));
        });
        updateInternal();
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag) {
        teams.forEach((uuid, team) -> {
            CompoundTag teamIdTag = new CompoundTag();
            ListTag members = new ListTag();
            team.members().forEach((member) -> members.add(StringTag.valueOf(member.toString())));
            teamIdTag.put("members", members);
            teamIdTag.putString("name", team.name());
            tag.put(uuid.toString(), teamIdTag);
        });
        return tag;
    }

    public static TeamSaveData read(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TeamSaveData::new, TeamSaveData::new, "cadmus_teams");
    }

    @Nullable
    public static Team get(MinecraftServer server, UUID teamId) {
        return read(server).teams.get(teamId);
    }

    public static Team getOrCreateTeam(ServerPlayer player) {
        String name = TeamProviderApi.API.getSelected().getTeamId(player);
        if (name == null) {
            return getOrCreateTeam(player, player.getUUID().toString());
        } else {
            return getOrCreateTeam(player, name);
        }
    }

    public static Team getOrCreateTeam(ServerPlayer player, String name) {
        Team team = getPlayerTeam(player);
        if (team == null) return set(player, name);
        return team;
    }

    public static Team set(ServerPlayer player, String name) {
        var data = read(player.server);
        UUID teamId = ModUtils.generate(Predicate.not(data.teams::containsKey), UUID::randomUUID);
        Set<GameProfile> members = TeamProviderApi.API.getSelected().getTeamMembers(name, player.getServer());
        members.add(player.getGameProfile());
        Team team = new Team(teamId, members
            .stream()
            .map(GameProfile::getId)
            .collect(Collectors.toSet()), name);

        data.teams.put(teamId, team);
        data.setDirty();
        for (GameProfile member : members) {
            data.players.put(member.getId(), teamId);
        }
        return team;
    }

    @Nullable
    public static Team getPlayerTeam(ServerPlayer player) {
        return getPlayerTeam(player.server, player.getUUID());
    }

    @Nullable
    public static Team getPlayerTeam(MinecraftServer server, UUID player) {
        var data = read(server);
        UUID uuid = data.players.get(player);
        return data.teams.get(uuid);
    }

    public static Collection<Team> getTeams(MinecraftServer server) {
        return read(server).teams.values();
    }

    public static void addTeamMember(ServerPlayer player, Team team) {
        var data = read(player.server);
        data.teams.get(team.teamId()).members().add(player.getUUID());
        data.setDirty();
        data.players.put(player.getUUID(), team.teamId());
    }

    public static Set<ChunkPos> removeTeamMember(ServerPlayer player, Team team) {
        var data = read(player.server);
        data.teams.get(team.teamId()).members().remove(player.getUUID());
        // Remove team if it has no members
        Set<ChunkPos> removedChunks = new HashSet<>();
        if (team.members().isEmpty()) {
            removedChunks = disband(team, player.server);
        }
        data.setDirty();
        data.players.remove(player.getUUID());
        return removedChunks;
    }

    public static Set<ChunkPos> disband(Team team, MinecraftServer server) {
        var data = read(server);
        data.teams.remove(team.teamId());

        Set<ChunkPos> removedChunks = new HashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            Set<ChunkPos> toRemove = ClaimSaveData.getAll(level).entrySet().stream()
                .filter(entry -> entry.getValue().teamId().equals(team.teamId()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
            ClaimSaveData.getAll(level).keySet().removeAll(toRemove);
            data.teams.remove(team.teamId());
            removedChunks.addAll(toRemove);
            toRemove.forEach(chunkPos -> ClaimSaveData.remove(level, chunkPos));
        }

        data.setDirty();
        data.updateInternal();
        return removedChunks;
    }

    private void updateInternal() {
        players.clear();
        teams.values().forEach(team -> team.members().forEach(member -> players.put(member, team.teamId())));
    }
}
