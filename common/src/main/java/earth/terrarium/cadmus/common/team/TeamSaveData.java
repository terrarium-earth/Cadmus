package earth.terrarium.cadmus.common.team;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.team.TeamProviderApi;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

    public TeamSaveData(CompoundTag tag, MinecraftServer server) {
        tag.getAllKeys().forEach(key -> {
            var teamTag = tag.getCompound(key);
            var creator = UUID.fromString(teamTag.getString("creator"));
            Set<UUID> members = new HashSet<>();
            teamTag.getList("members", Tag.TAG_STRING).forEach((member) -> members.add(UUID.fromString(member.getAsString())));
            var name = teamTag.getString("name");
            teams.put(UUID.fromString(key), new Team(UUID.fromString(key), creator, members, name));
        });
        updateInternal(server);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        teams.forEach((uuid, team) -> {
            var teamIdTag = new CompoundTag();
            teamIdTag.putString("creator", team.creator().toString());
            ListTag members = new ListTag();
            team.members().forEach((member) -> members.add(StringTag.valueOf(member.toString())));
            teamIdTag.put("members", members);
            teamIdTag.putString("name", team.name() == null ? "TODO" : team.name());
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
        return server.overworld().getDataStorage().computeIfAbsent(tag -> new TeamSaveData(tag, server), TeamSaveData::new, "cadmus_teams");
    }

    public static Team set(ServerLevel level, ServerPlayer player) {
        var data = read(level);
        UUID teamId = ModUtils.generate(Predicate.not(data.teams::containsKey), UUID::randomUUID);
        Set<GameProfile> members = TeamProviderApi.API.getSelected().getTeamMembers(level.getServer(), player.getGameProfile());
        String name = TeamProviderApi.API.getSelected().getTeamName(level.getServer(), player.getGameProfile());
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

    public static Team getOrCreate(ServerPlayer player) {
        var data = read(player.level);
        UUID uuid = data.players.get(player.getUUID());
        Team team = data.teams.get(uuid);
        if (team == null) return set(player.getLevel(), player);
        return team;
    }

    public static Collection<Team> getTeams(Level level) {
        return read(level).teams.values();
    }

    public static void update(MinecraftServer server) {
        read(server).updateInternal(server);
    }

    private void updateInternal(MinecraftServer server) {
        teams.forEach((uuid, team) -> {
            team.members().clear();
            server.getProfileCache().get(team.creator()).ifPresent(profile -> {
                List<UUID> uuids = TeamProviderApi.API.getSelected()
                        .getTeamMembers(server, profile)
                        .stream()
                        .map(GameProfile::getId)
                        .toList();
                team.members().addAll(uuids);
            });
        });

        players.clear();

        for (Team team : teams.values()) {
            for (UUID member : team.members()) {
                players.put(member, team.teamId());
            }
        }
    }
}
