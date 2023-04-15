package earth.terrarium.cadmus.common.team;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.team.TeamProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class VanillaTeamProvider implements TeamProvider {
    @Override
    public @NotNull Set<GameProfile> getTeamMembers(MinecraftServer server, GameProfile creator) {
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

    @Override
    public @Nullable String getTeamName(MinecraftServer server, GameProfile creator) {
        PlayerTeam team = server.getScoreboard().getPlayersTeam(creator.getName());
        if (team == null) return null;
        return team.getName();
    }
}
