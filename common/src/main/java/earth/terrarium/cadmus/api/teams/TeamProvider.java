package earth.terrarium.cadmus.api.teams;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.claims.InteractionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface TeamProvider {

    /**
     * Gets the team members of the creator
     *
     * @param id      the id of the team
     * @param server  the server
     * @param creator the creator
     * @return the team members, returns a list with just the creator if no team is found
     */
    Set<GameProfile> getTeamMembers(String id, MinecraftServer server, GameProfile creator);

    /**
     * Gets the name of a team
     *
     * @param id     the id of the team
     * @param server the server
     * @return the name of the team, or null if no team is found
     */
    @Nullable
    String getTeamName(String id, MinecraftServer server);

    /**
     * Checks if the player is a member of the team
     *
     * @param id     the id of the team
     * @param server the server
     * @param player the uuid of the player
     * @return true if the player is a member of the team, false otherwise
     */
    boolean isMember(String id, MinecraftServer server, UUID player);

    /**
     * Checks if the player can break a block
     *
     * @param id     the id of the team
     * @param server the server
     * @param pos    the position of the block
     * @param player the uuid of the player
     * @return true if the player can break the block, false otherwise
     */
    boolean canBreakBlock(String id, MinecraftServer server, BlockPos pos, UUID player);

    /**
     * Checks if the player can place a block
     *
     * @param id     the id of the team
     * @param server the server
     * @param pos    the position of the block
     * @param player the uuid of the player
     * @return true if the player can place the block, false otherwise
     */
    boolean canPlaceBlock(String id, MinecraftServer server, BlockPos pos, UUID player);

    /**
     * Checks if the player can use an item
     *
     * @param id     the id of the team
     * @param server the server
     * @param pos    the position of the block
     * @param player the uuid of the player
     * @return true if the player can use the item, false otherwise
     */
    boolean canExplodeBlock(String id, MinecraftServer server, BlockPos pos, Explosion explosion, UUID player);

    /**
     * Checks if the player can interact with a block
     *
     * @param id     the id of the team
     * @param server the server
     * @param pos    the position of the block
     * @param type   the type of interaction
     * @param player the uuid of the player
     * @return true if the player can interact with the block, false otherwise
     */
    boolean canInteractWithBlock(String id, MinecraftServer server, BlockPos pos, InteractionType type, UUID player);

    /**
     * Checks if the player can interact with an entity
     *
     * @param id     the id of the team
     * @param server the server
     * @param entity the entity
     * @param player the uuid of the player
     * @return true if the player can interact with the entity, false otherwise
     */
    boolean canInteractWithEntity(String id, MinecraftServer server, Entity entity, UUID player);

    /**
     * Checks if the player can damage an entity
     *
     * @param id     the id of the team
     * @param server the server
     * @param entity the entity
     * @param player the uuid of the player
     * @return true if the player can damage the entity, false otherwise
     */
    boolean canDamageEntity(String id, MinecraftServer server, Entity entity, UUID player);
}
