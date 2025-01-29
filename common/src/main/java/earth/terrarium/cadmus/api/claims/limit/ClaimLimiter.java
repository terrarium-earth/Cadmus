package earth.terrarium.cadmus.api.claims.limit;

import earth.terrarium.cadmus.api.teams.TeamId;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public interface ClaimLimiter {

    int getMaxClaims(MinecraftServer server, TeamId id);

    int getMaxChunkLoadedClaims(MinecraftServer server, TeamId id);
}
