package earth.terrarium.cadmus.api.claims;

import earth.terrarium.cadmus.api.ApiHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ClaimApi {
    ClaimApi API = ApiHelper.load(ClaimApi.class);

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
     * @param level The level to check.
     * @param pos   The block position to check.
     * @param player The player to check.
     * @return True if the block can be exploded by the player, false otherwise.
     */
    boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID player);

    /**
     * Checks if the block can be exploded by the player.
     *
     * @param level The level to check.
     * @param pos   The block position to check.
     * @param player The player to check.
     * @return True if the block can be exploded by the player, false otherwise.
     */
    boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, @NotNull Player player);

    /**
     * Checks if a player can interact with the block.
     *
     * @param level The level to check.
     * @param pos   The block position to check.
     * @param type  The interaction type to check.
     * @param player The player to check.
     * @return True if the player can interact with the block, false otherwise.
     */
    boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, UUID player);

    /**
     * Checks if a player can interact with the block.
     *
     * @param level The level to check.
     * @param pos   The block position to check.
     * @param type  The interaction type to check.
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
     * @param level The level to check.
     * @param pos   The block position to check.
     * @param item  The item to check.
     * @param picker The entity to check.
     * @return True if the entity can pickup the item, false otherwise.
     */
    boolean canPickupItem(Level level, BlockPos pos, ItemEntity item, @NotNull Entity picker);
}
