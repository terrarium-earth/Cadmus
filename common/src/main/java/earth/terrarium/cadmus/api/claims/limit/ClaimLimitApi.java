package earth.terrarium.cadmus.api.claims.limit;

import earth.terrarium.cadmus.api.ApiHelper;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface ClaimLimitApi {

    ClaimLimitApi API = ApiHelper.load(ClaimLimitApi.class);

    /**
     * Registers a claim limiter. A claim limiter controls the maximum amount of claims a player can have. When multiple claim limiters are ges registered, each limiter calculates the max amount of claims and the lowest value is used.
     *
     * @param limiter The limiter.
     */
    void register(ClaimLimiter limiter);

    /**
     * Gets the maximum number of claims the team can have.
     *
     * @param id The team ID.
     * @return The maximum number of claims the team can have.
     */
    int getMaxClaims(UUID id);

    /**
     * Gets the maximum number of chunk loaded claims the team can have.
     *
     * @param id The team ID.
     * @return The maximum number of chunk loaded claims the team can have.
     */
    int getMaxChunkLoadedClaims(UUID id);

    /**
     * Gets the maximum number of claims the player can have.
     *
     * @param player The player to get the claims from.
     * @return The maximum number of claims the player can have.
     */
    default int getMaxClaims(@NotNull Player player) {
        TeamApi.API.getTeams()
        return this.getMaxClaims(TeamApi.API.getTeams(player));
    }

    /**
     * Gets the maximum number of chunk loaded claims the player can have.
     *
     * @param player The player to get the claims from.
     * @return The maximum number of chunk loaded claims the player can have.
     */
    default int getMaxChunkLoadedClaims(@NotNull Player player) {
        return this.getMaxChunkLoadedClaims(TeamApi.API.getTeams(player));
    }

    /**
     * Sets the maximum claims for the team.
     *
     * @param id             The team ID.
     * @param maxClaims      The maximum claims for the team.
     * @param maxChunkLoaded The maximum chunk loaded claims for the team.
     */
    void set(UUID id, int maxClaims, int maxChunkLoaded);

    /**
     * Sets the maximum claims for each team.
     *
     * @param maxClaimsByTeam The maximum claims for each team.
     */
    void set(Map<UUID, IntIntPair> maxClaimsByTeam);

    /**
     * Calculates the maximum claims for each team.
     *
     * @param server The server.
     */
    void calculate(MinecraftServer server);

    /**
     * Calculates the maximum claims for the team
     *
     * @param server The server.
     * @param id     The team ID.
     * @param sync   If the changes should be synced to the client.
     */
    void calculate(MinecraftServer server, TeamId id, boolean sync);
}
