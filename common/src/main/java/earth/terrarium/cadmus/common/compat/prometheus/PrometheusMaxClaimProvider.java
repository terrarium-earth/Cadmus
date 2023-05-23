package earth.terrarium.cadmus.common.compat.prometheus;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProvider;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.CadmusDataHandler;
import earth.terrarium.cadmus.common.util.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public class PrometheusMaxClaimProvider implements MaxClaimProvider {

    @Override
    public void calculate(String id, MinecraftServer server) {
        Set<GameProfile> members = TeamProviderApi.API.getSelected().getTeamMembers(id, server);
        if (members.isEmpty()) return;

        boolean combinedClaimLimit = ModGameRules.getOrCreateBooleanGameRule(server.overworld(), ModGameRules.DO_COMBINED_CLAIM_LIMIT);
        int maxClaims = 0;
        int maxChunkLoaded = 0;

        for (GameProfile profile : members) {
            int maxClaimsSetting = PrometheusIntegration.getMaxClaims(server.overworld(), profile.getId());
            int maxChunkLoadedSetting = PrometheusIntegration.getMaxChunkLoaded(server.overworld(), profile.getId());
            if (combinedClaimLimit) {
                // Sum the max claims of all members on the team
                maxClaims += maxClaimsSetting;
                maxChunkLoaded += maxChunkLoadedSetting;
            } else {
                // Use the max claims of the member with the highest max claims
                maxClaims = Math.max(maxClaims, maxClaimsSetting);
                maxChunkLoaded = Math.max(maxChunkLoaded, maxChunkLoadedSetting);
            }
        }

        CadmusDataHandler.getMaxTeamClaims(server).put(id, Pair.of(maxClaims, maxChunkLoaded));
    }

    @Override
    public void removeTeam(String id, MinecraftServer server) {
        CadmusDataHandler.getMaxTeamClaims(server).remove(id);
    }

    @Override
    public int getMaxClaims(String id, MinecraftServer server, Player player) {
        var maxClaims = CadmusDataHandler.getMaxTeamClaims(server).get(id);
        return maxClaims == null ? PrometheusIntegration.getMaxClaims(player) : maxClaims.getFirst();
    }

    @Override
    public int getMaxChunkLoaded(String id, MinecraftServer server, Player player) {
        var maxChunkLoaded = CadmusDataHandler.getMaxTeamClaims(server).get(id);
        return maxChunkLoaded == null ? PrometheusIntegration.getMaxChunkLoaded(player) : maxChunkLoaded.getSecond();
    }
}
