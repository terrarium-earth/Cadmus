package earth.terrarium.cadmus.api.team;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.claims.InteractionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface TeamProvider {
    /**
     * Gets the team members of the creator
     *
     * @param server  the server
     * @param creator the creator
     * @return the team members, returns a list with just the creator if no team is found
     */
    @NotNull
    Set<GameProfile> getTeamMembers(MinecraftServer server, GameProfile creator);

    /**
     * Gets the name of the team of the creator
     *
     * @param server  the server
     * @param creator the creator
     * @return the name of the team, or the creator's name if no team is found
     */
    @NotNull
    String getTeamName(MinecraftServer server, GameProfile creator);

    /**
     * Checks if the player can break a block
     *
     * @param level the level
     * @param pos   the position of the block
     * @param id    the id of the player
     * @return true if the player can break the block, false otherwise
     */
    boolean canBreakBlock(Level level, BlockPos pos, UUID id);


    /**
     * Checks if the player can place a block
     *
     * @param level the level
     * @param pos   the position of the block
     * @param id    the id of the player
     * @return true if the player can place the block, false otherwise
     */
    boolean canPlaceBlock(Level level, BlockPos pos, UUID id);


    /**
     * Checks if the player can use an item
     *
     * @param level the level
     * @param pos   the position of the block
     * @param id    the id of the player
     * @return true if the player can use the item, false otherwise
     */
    boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID id);


    /**
     * Checks if the player can interact with a block
     *
     * @param level the level
     * @param pos   the position of the block
     * @param type  the type of interaction
     * @param id    the id of the player
     * @return true if the player can interact with the block, false otherwise
     */
    boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, UUID id);


    /**
     * Checks if the player can interact with an entity
     *
     * @param level  the level
     * @param entity the entity
     * @param id     the id of the player
     * @return true if the player can interact with the entity, false otherwise
     */
    boolean canInteractWithEntity(Level level, Entity entity, UUID id);


    /**
     * Checks if the player can damage an entity
     *
     * @param level  the level
     * @param entity the entity
     * @param id     the id of the player
     * @return true if the player can damage the entity, false otherwise
     */
    boolean canDamageEntity(Level level, Entity entity, UUID id);
}
