package earth.terrarium.cadmus.common.claims.limit;

import earth.terrarium.cadmus.api.claims.limit.ClaimLimiter;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.utils.CadmusGameRules;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class VanillaClaimLimiter implements ClaimLimiter {

    @Override
    public int getMaxClaims(MinecraftServer server, TeamId id) {
        return server.getGameRules().getInt(CadmusGameRules.MAX_CLAIMS);
    }

    @Override
    public int getMaxChunkLoadedClaims(MinecraftServer server, TeamId id) {
        return server.getGameRules().getInt(CadmusGameRules.MAX_CHUNK_LOADED_CLAIMS);
    }
}
