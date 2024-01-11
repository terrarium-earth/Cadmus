package earth.terrarium.cadmus.common.compat.prometheus;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProvider;
import earth.terrarium.cadmus.common.claims.CadmusDataHandler;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import earth.terrarium.cadmus.common.util.ModGameRules;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.UUID;

public class PrometheusMaxClaimProvider implements MaxClaimProvider {

    @Override
    public void calculate(String id, MinecraftServer server) {
        Set<GameProfile> members = TeamHelper.getTeamMembers(id, server);
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

        CadmusDataHandler.getMaxTeamClaims(server).put(id, IntIntPair.of(maxClaims, maxChunkLoaded));
    }

    @Override
    public void removeTeam(String id, MinecraftServer server) {
        CadmusDataHandler.getMaxTeamClaims(server).remove(id);
    }

    @Override
    public int getMaxClaims(String id, MinecraftServer server, Player player) {
        return getMaxClaims(id, (ServerLevel) player.level(), player.getUUID());
    }

    @Override
    public int getMaxClaims(String id, ServerLevel level, UUID player) {
        var maxClaims = CadmusDataHandler.getMaxTeamClaims(level.getServer()).get(id);
        return maxClaims == null ? PrometheusIntegration.getMaxClaims(level, player) : maxClaims.firstInt();
    }

    @Override
    public int getMaxChunkLoaded(String id, MinecraftServer server, Player player) {
        return getMaxChunkLoaded(id, (ServerLevel) player.level(), player.getUUID());
    }

    @Override
    public int getMaxChunkLoaded(String id, ServerLevel level, UUID player) {
        var maxChunkLoaded = CadmusDataHandler.getMaxTeamClaims(level.getServer()).get(id);
        return maxChunkLoaded == null ? PrometheusIntegration.getMaxChunkLoaded(level, player) : maxChunkLoaded.secondInt();
    }
}
