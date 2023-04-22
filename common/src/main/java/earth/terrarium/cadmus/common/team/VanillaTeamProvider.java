package earth.terrarium.cadmus.common.team;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.team.TeamProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class VanillaTeamProvider implements TeamProvider {
    @NotNull
    @Override
    public Set<GameProfile> getTeamMembers(MinecraftServer server, GameProfile creator) {
        PlayerTeam team = server.getScoreboard().getPlayersTeam(creator.getName());
        Set<GameProfile> profiles = new HashSet<>();
        if (team == null) {
            profiles.add(creator);
            return profiles;
        }
        for (String player : team.getPlayers()) {
            server.getProfileCache().get(player).ifPresent(profiles::add);
        }
        return profiles;
    }

    @NotNull
    @Override
    public String getTeamName(MinecraftServer server, GameProfile creator) {
        PlayerTeam team = server.getScoreboard().getPlayersTeam(creator.getName());
        if (team == null) return creator.getName();
        return team.getName();
    }

    // TODO: transfer chunks when player joins a new team
    public void addPlayerToTeam(MinecraftServer server, String playerName, PlayerTeam scoreboardTeam) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) return;

        for (Team team : TeamSaveData.getTeams(server)) {
            if (team.name().equals(scoreboardTeam.getName())) {
                TeamSaveData.addTeamMember(player, team);
                return;
            }
            TeamSaveData.removeTeamMember(player, team);
        }
        TeamSaveData.getOrCreate(player, scoreboardTeam.getName());
    }

    public void removePlayerFromTeam(MinecraftServer server, String playerName, PlayerTeam scoreboardTeam) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) return;

        for (Team team : TeamSaveData.getTeams(server)) {
            if (team.name().equals(scoreboardTeam.getName())) {
                TeamSaveData.removeTeamMember(player, team);
                return;
            }
        }
    }
}
