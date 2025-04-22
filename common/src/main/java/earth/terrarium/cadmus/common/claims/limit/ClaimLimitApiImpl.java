package earth.terrarium.cadmus.common.claims.limit;

import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimiter;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.clientbound.SyncAllMaxClaimsPacket;
import earth.terrarium.cadmus.common.network.packets.clientbound.SyncMaxClaimsPacket;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.server.MinecraftServer;

import java.util.*;

public class ClaimLimitApiImpl implements ClaimLimitApi {

    private final Set<ClaimLimiter> limiters = new HashSet<>();
    private final Map<TeamId, IntIntPair> maxClaimsByTeam = new HashMap<>();

    @Override
    public void register(ClaimLimiter limiter) {
        this.limiters.add(limiter);
    }

    private IntIntPair get(TeamId id) {
        return this.maxClaimsByTeam.computeIfAbsent(id, uuid -> IntIntPair.of(0, 0));
    }

    @Override
    public int getMaxClaims(TeamId id) {
        return this.get(id).leftInt();
    }

    @Override
    public int getMaxChunkLoadedClaims(TeamId id) {
        return this.get(id).rightInt();
    }

    @Override
    public void set(TeamId id, int maxClaims, int maxChunkLoaded) {
        this.maxClaimsByTeam.put(id, IntIntPair.of(maxClaims, maxChunkLoaded));
    }

    @Override
    public void set(Map<TeamId, IntIntPair> maxClaimsByTeam) {
        this.maxClaimsByTeam.clear();
        this.maxClaimsByTeam.putAll(maxClaimsByTeam);
    }

    @Override
    public void calculate(MinecraftServer server) {
        this.maxClaimsByTeam.clear();
        TeamApi.API.getAllTeams(server).forEach(id -> calculate(server, id, false));
        if (!maxClaimsByTeam.isEmpty()) {
            NetworkHandler.sendToAllClientPlayers(new SyncAllMaxClaimsPacket(this.maxClaimsByTeam), server);
        }
    }

    @Override
    public void calculate(MinecraftServer server, TeamId id, boolean sync) {
        int maxClaims = Integer.MAX_VALUE;
        int maxChunkLoaded = Integer.MAX_VALUE;
        for (var limiter : this.limiters) {
            maxClaims = Math.min(maxClaims, limiter.getMaxClaims(server, id));
            maxChunkLoaded = Math.min(maxChunkLoaded, limiter.getMaxChunkLoadedClaims(server, id));
        }
        this.maxClaimsByTeam.put(id, IntIntPair.of(maxClaims, maxChunkLoaded));
        if (sync) {
            NetworkHandler.sendToAllClientPlayers(new SyncMaxClaimsPacket(id, maxClaims, maxChunkLoaded), server);
        }
    }
}
