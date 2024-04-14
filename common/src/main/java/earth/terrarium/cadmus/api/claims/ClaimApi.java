package earth.terrarium.cadmus.api.claims;

import earth.terrarium.cadmus.api.ApiHelper;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This API is being heavily reworked in 1.21.
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.21")
public interface ClaimApi {

    ClaimApi API = ApiHelper.load(ClaimApi.class);

    /**
     * Checks if the player can claim the chunk.
     *
     * @param level     The level to check.
     * @param pos       The chunk position to check.
     * @param id        The id of the claimer.
     * @param chunkLoad If the chunk should be chunk loaded.
     * @param player    The player to check.
     * @return True if the player has enough claims to claim the chunk, false otherwise.
     */
    boolean canClaim(ServerLevel level, ChunkPos pos, String id, boolean chunkLoad, UUID player);

    /**
     * Checks if the player can claim the chunk.
     *
     * @param level     The level to check.
     * @param pos       The chunk position to check.
     * @param chunkLoad If the chunk should be chunk loaded.
     * @param player    The player to check.
     * @return True if the player has enough claims to claim the chunk, false otherwise.
     */
    default boolean canClaim(ServerLevel level, ChunkPos pos, boolean chunkLoad, @NotNull ServerPlayer player) {
        return this.canClaim(level, pos, TeamHelper.getTeamId(player.server, player.getUUID()), chunkLoad, player.getUUID());
    }

    /**
     * Claims the chunk.
     *
     * @param level     The level to claim.
     * @param pos       The chunk position to claim.
     * @param id        The id of the claimer.
     * @param chunkLoad If the chunk should be chunk loaded.
     */
    void claim(ServerLevel level, ChunkPos pos, String id, boolean chunkLoad);

    /**
     * Claims the chunk.
     *
     * @param level     The level to claim.
     * @param pos       The chunk position to claim.
     * @param chunkLoad If the chunk should be chunk loaded.
     * @param player    The player to claim the chunk.
     */
    default void claim(ServerLevel level, ChunkPos pos, boolean chunkLoad, @NotNull ServerPlayer player) {
        this.claim(level, pos, TeamHelper.getTeamId(player.server, player.getUUID()), chunkLoad);
    }

    /**
     * Unclaims the chunk.
     *
     * @param level The level to unclaim.
     * @param pos   The chunk position to unclaim.
     * @param id    The id of the claimer.
     */
    void unclaim(ServerLevel level, ChunkPos pos, String id);

    /**
     * Unclaims the chunk.
     *
     * @param level  The level to unclaim.
     * @param pos    The chunk position to unclaim.
     * @param player The player to unclaim the chunk.
     */
    default void unclaim(ServerLevel level, ChunkPos pos, @NotNull ServerPlayer player) {
        this.unclaim(level, pos, TeamHelper.getTeamId(player.server, player.getUUID()));
    }


    /**
     * Checks if the chunk is claimed.
     *
     * @param level The level to check.
     * @param pos   The chunk position to check.
     * @return True if the chunk is claimed, false otherwise.
     */
    boolean isClaimed(Level level, ChunkPos pos);

    /**
     * Checks if the chunk is chunk loaded.
     *
     * @param level The level to check.
     * @param pos   The chunk position to check.
     * @return True if the chunk is chunk loaded, false otherwise.
     */
    boolean isChunkLoaded(Level level, ChunkPos pos);

    /**
     * Checks if the block is in a claimed chunk.
     *
     * @param level The level to check.
     * @param pos   The block position to check.
     * @return True if the block is in a claimed chunk, false otherwise.
     */
    default boolean isClaimed(Level level, BlockPos pos) {
        return this.isClaimed(level, new ChunkPos(pos));
    }

    /**
     * Checks if a player can break the block.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param player The player to check.
     * @return True if the player can break the block, false otherwise.
     */
    boolean canBreakBlock(Level level, BlockPos pos, UUID player);

    /**
     * Checks if a player can break the block.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param player The player to check.
     * @return True if the player can break the block, false otherwise.
     */
    boolean canBreakBlock(Level level, BlockPos pos, @NotNull Player player);

    /**
     * Checks if a player can place the block.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param player The player to check.
     * @return True if the player can place the block, false otherwise.
     */
    boolean canPlaceBlock(Level level, BlockPos pos, UUID player);

    /**
     * Checks if a player can place the block.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param player The player to check.
     * @return True if the player can place the block, false otherwise.
     */
    boolean canPlaceBlock(Level level, BlockPos pos, @NotNull Player player);

    /**
     * Checks if the block can be exploded.
     *
     * @param level The level to check.
     * @param pos   The block position to check.
     * @return True if the block can be exploded, false otherwise.
     */
    boolean canExplodeBlock(Level level, ChunkPos pos);

    /**
     * Checks if the block can be exploded by the player.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param player The player to check.
     * @return True if the block can be exploded by the player, false otherwise.
     */
    boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID player);

    /**
     * Checks if the block can be exploded by the player.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param player The player to check.
     * @return True if the block can be exploded by the player, false otherwise.
     */
    boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, @NotNull Player player);

    /**
     * Checks if a player can interact with the block.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param type   The interaction type to check.
     * @param player The player to check.
     * @return True if the player can interact with the block, false otherwise.
     */
    boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, UUID player);

    /**
     * Checks if a player can interact with the block.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param type   The interaction type to check.
     * @param player The player to check.
     * @return True if the player can interact with the block, false otherwise.
     */
    boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, @NotNull Player player);

    /**
     * Checks if a player can interact with the entity.
     *
     * @param level  The level to check.
     * @param entity The entity to check.
     * @param player The player to check.
     * @return True if the player can interact with the entity, false otherwise.
     */
    boolean canInteractWithEntity(Level level, Entity entity, UUID player);

    /**
     * Checks if a player can interact with the entity.
     *
     * @param level  The level to check.
     * @param entity The entity to check.
     * @param player The player to check.
     * @return True if the player can interact with the entity, false otherwise.
     */
    boolean canInteractWithEntity(Level level, Entity entity, @NotNull Player player);

    /**
     * Checks if a player can damage the entity.
     *
     * @param level  The level to check.
     * @param entity The entity to check.
     * @param player The player to check.
     * @return True if the player can damage the entity, false otherwise.
     */
    boolean canDamageEntity(Level level, Entity entity, UUID player);

    /**
     * Checks if a player can damage the entity.
     *
     * @param level  The level to check.
     * @param entity The entity to check.
     * @param player The player to check.
     * @return True if the player can damage the entity, false otherwise.
     */
    boolean canDamageEntity(Level level, Entity entity, @NotNull Player player);

    /**
     * Checks if the entity can grief.
     *
     * @param level  The level to check.
     * @param entity The entity to check.
     * @return True if the entity can grief, false otherwise.
     */
    boolean canEntityGrief(Level level, @NotNull Entity entity);

    /**
     * Checks if the entity can grief.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param entity The entity to check.
     * @return True if the entity can grief, false otherwise.
     */
    boolean canEntityGrief(Level level, BlockPos pos, @NotNull Entity entity);

    /**
     * Checks if the entity can pickup the item.
     *
     * @param level  The level to check.
     * @param pos    The block position to check.
     * @param item   The item to check.
     * @param picker The entity to check.
     * @return True if the entity can pickup the item, false otherwise.
     */
    boolean canPickupItem(Level level, BlockPos pos, ItemEntity item, @NotNull Entity picker);
}
