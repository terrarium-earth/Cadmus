package earth.terrarium.cadmus.api.claims;

import com.teamresourceful.resourcefullib.common.network.Packet;
import earth.terrarium.cadmus.api.ApiHelper;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public interface ClaimApi {

    ClaimApi API = ApiHelper.load(ClaimApi.class);

    /**
     * Claims a chunk.
     *
     * @param level     The level to claim.
     * @param id        The team ID.
     * @param pos       The chunk position to claim.
     * @param chunkLoad If the chunk should be chunk loaded.
     */
    void claim(Level level, TeamId id, ChunkPos pos, boolean chunkLoad);

    /**
     * Claims a set of chunks.
     *
     * @param level     The level to claim.
     * @param id        The team ID.
     * @param positions The positions mapped to chunk load status.
     */
    void claim(Level level, TeamId id, Object2BooleanMap<ChunkPos> positions);

    /**
     * Unclaims a chunk as long as its owned by a team the player can modify.
     *
     * @param level     The level.
     * @param player    The player.
     * @param pos       The chunk position to unclaim.
     */
    void unclaim(Level level, Player player, ChunkPos pos);

    /**
     * Unclaims a chunk for a specific team. If the team doesn't own the chunk it will not have an effect
     *
     * @param level The level.
     * @param id    The team ID.
     * @param pos   The chunk position to unclaim.
     */
    void unclaim(Level level, TeamId id, ChunkPos pos);

    /**
     * Unclaims a set of chunks.
     *
     * @param level     The level.
     * @param id        The team ID.
     * @param positions The chunk positions to unclaim.
     */
    void unclaim(Level level, TeamId id, Set<ChunkPos> positions);

    /**
     * Unclaims a set of chunks for all teams the player has permission to act on the behalf of.
     *
     * @param level     The level.
     * @param player    The player
     * @param positions The chunk positions to unclaim.
     */
    default void unclaim(Level level, Player player, Set<ChunkPos> positions) {
        Set<TeamId> teams = new HashSet<>();
        positions.forEach(position -> getClaim(level, position).map(ClaimData::team).ifPresent(teams::add));
        teams.forEach(id -> {
            if (TeamApi.API.isMember(level, id, player)) unclaim(level, id, positions);
        });
    }

    /**
     * Clears all claims in the level for the given team.
     *
     * @param level The level.
     * @param id    The team ID.
     */
    void clear(Level level, TeamId id);

    /**
     * Clears all claims in the level for the given player.
     *
     * @param player The player.
     */
    void clear(Player player);

    /**
     * Completely clears all claims in every level.
     *
     * @param server The server.
     */
    void clearAll(MinecraftServer server);

    /**
     * Gets a claim.
     *
     * @param level The level to get the claim from.
     * @param pos   The chunk position to get the claim from.
     * @return The claim ID and chunk load status if the chunk is claimed, empty otherwise.
     */
    Optional<ClaimData> getClaim(Level level, ChunkPos pos);

    /**
     * Checks if a chunk is claimed.
     *
     * @param level The level to check the claim from.
     * @param pos   The chunk position to check the claim from.
     * @return True if the chunk is claimed, false otherwise.
     */
    default boolean isClaimed(Level level, ChunkPos pos) {
        return this.getClaim(level, pos).isPresent();
    }

    /**
     * Checks if a block is claimed.
     *
     * @param level The level to check the claim from.
     * @param pos   The block position to check the claim from.
     * @return True if the chunk is claimed, false otherwise.
     */
    default boolean isClaimed(Level level, BlockPos pos) {
        return isClaimed(level, new ChunkPos(pos));
    }

    /**
     * Checks if the chunk the player is in is claimed.
     *
     * @param player The player to check the claim from.
     * @return True if the chunk is claimed, false otherwise.
     */
    default boolean isClaimed(Player player) {
        return this.isClaimed(player.level(), player.chunkPosition());
    }

    /**
     * Gets all claims within the given chunk positions.
     *
     * @param level     The level to get the claims from.
     * @param positions The chunk positions to get the claim from.
     * @return The claim ID and chunk load status for all claimed chunks.
     */
    List<ObjectBooleanPair<TeamId>> getClaims(Level level, Collection<ChunkPos> positions);

    /**
     * Gets all claims for the given team iD.
     *
     * @param level The level to get the claims from.
     * @param id    The team ID.
     * @return A map of chunk positions to chunk load status.
     */
    Optional<Object2BooleanMap<ChunkPos>> getOwnedClaims(Level level, TeamId id);

    /**
     * Gets all claims for the given player. If the player is on a team, retrieves the team's claims, otherwise retrieves the player's claims.
     *
     * @param player The player to get the claims from.
     * @return A map of chunk positions to chunk load status.
     */
    default Optional<Object2BooleanMap<ChunkPos>> getOwnedClaims(Player player) {
        var map = new Object2BooleanArrayMap<ChunkPos>();
        for (TeamId team : TeamApi.API.getTeamsList(player)) {
            this.getOwnedClaims(player.level(), team).ifPresent(map::putAll);
        }
        return Optional.of(map);
    }

    /**
     * Gets all claims for the given level.
     *
     * @param level The level to get the claims from.
     * @return A map of chunk positions to a pair of claim ID and chunk load status.
     */
    Object2ObjectMap<ChunkPos, ObjectBooleanPair<TeamId>> getAllClaims(ServerLevel level);

    /**
     * Gets all claims for the given level.
     *
     * @param level The level to get the claims from.
     * @return A map of chunk positions to a pair of claim ID and chunk load status.
     */
    Object2ObjectMap<TeamId, Object2BooleanMap<ChunkPos>> getAllClaimsByOwner(ServerLevel level);

    /**
     * Gets a claim that has been synced to the client for the given level.
     *
     * @param level The level to get the claim from.
     * @param pos   The chunk position to get the claim from.
     * @return The claim ID and chunk load status if the chunk is claimed, empty otherwise.
     */
    Optional<ObjectBooleanPair<TeamId>> getClientClaim(ResourceKey<Level> level, ChunkPos pos);

    /**
     * Gets all claims that have been synced to the client for the given level.
     *
     * @param level The level to get the claims from.
     * @return A map of chunk positions to a pair of claim ID and chunk load status.
     */
    Object2ObjectMap<ChunkPos, ObjectBooleanPair<TeamId>> getAllClientClaims(ResourceKey<Level> level);
}
