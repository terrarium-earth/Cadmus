package earth.terrarium.cadmus.common.compat.prometheus;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProvider;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.util.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public class PrometheusMaxClaimProvider implements MaxClaimProvider {

    @Override
    public void calculate(String id, MinecraftServer server) {
        Set<GameProfile> members = TeamProviderApi.API.getSelected().getTeamMembers(id, server);
        boolean combinedClaimLimit = ModGameRules.getOrCreateBooleanGameRule(server.overworld(), ModGameRules.DO_COMBINED_CLAIM_LIMIT);
        int maxClaims = 0;
        int maxChunkLoaded = 0;
        boolean changed = false;

        for (GameProfile profile : members) {
            ServerPlayer member = server.getPlayerList().getPlayer(profile.getId());
            if (member != null) {
                int maxClaimsSetting = PrometheusIntegration.getMaxClaims(member);
                int maxChunkLoadedSetting = PrometheusIntegration.getMaxChunkLoaded(member);
                if (combinedClaimLimit) {
                    // Sum the max claims of all members on the team
                    maxClaims += maxClaimsSetting;
                    maxChunkLoaded += maxChunkLoadedSetting;
                } else {
                    // Use the max claims of the member with the highest max claims
                    maxClaims = Math.max(maxClaims, maxClaimsSetting);
                    maxChunkLoaded = Math.max(maxChunkLoaded, maxChunkLoadedSetting);
                }
                changed = true;
            }
        }

        if (changed) {
            ClaimHandler.getMaxTeamClaims(server.overworld()).put(id, Pair.of(maxClaims, maxChunkLoaded));
        }
    }

    @Override
    public void removeTeam(String id, MinecraftServer server) {
        ClaimHandler.getMaxTeamClaims(server.overworld()).remove(id);
    }

    @Override
    public int getMaxClaims(String id, MinecraftServer server, Player player) {
        var maxClaims = ClaimHandler.getMaxTeamClaims(server.overworld()).get(id);
        return maxClaims == null ? PrometheusIntegration.getMaxClaims(player) : maxClaims.getFirst();
    }

    @Override
    public int getMaxChunkLoaded(String id, MinecraftServer server, Player player) {
        var maxChunkLoaded = ClaimHandler.getMaxTeamClaims(server.overworld()).get(id);
        return maxChunkLoaded == null ? PrometheusIntegration.getMaxChunkLoaded(player) : maxChunkLoaded.getSecond();
    }
}
