package earth.terrarium.cadmus.api.teams;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "1.21")
public interface TeamProvider {

    /**
     * Gets the team members
     *
     * @param id     the id of the team
     * @param server the server
     * @return the team members
     */
    Set<GameProfile> getTeamMembers(String id, MinecraftServer server);

    /**
     * Gets the id of a team
     *
     * @param id     the id of the team
     * @param server the server
     * @return the id of the team, or null if no team is found
     */
    @Nullable
    Component getTeamName(String id, MinecraftServer server);

    /**
     * Gets the id of a team
     *
     * @param server the server
     * @param player the uuid of the player
     * @return the id of the team, or null if no team is found
     */
    @Nullable
    String getTeamId(MinecraftServer server, UUID player);

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
     * Gets the team color
     *
     * @param id     the id of the team
     * @param server the server
     * @return the team color
     */
    ChatFormatting getTeamColor(String id, MinecraftServer server);

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

    /**
     * Called when a player joins or leaves a team
     *
     * @param server the server
     */
    default void onTeamChanged(MinecraftServer server, String id) {
        MaxClaimProviderApi.API.getSelected().calculate(ClaimHandler.TEAM_PREFIX + id, server);
    }

    /**
     * Called when a team is removed
     *
     * @param server the server
     */
    default void onTeamRemoved(MinecraftServer server, String id) {
        MaxClaimProviderApi.API.getSelected().removeTeam(ClaimHandler.TEAM_PREFIX + id, server);
    }

    /**
     * Checks if the player can modify the settings of the team
     *
     * @param id     the id of the team
     * @param player the player
     * @return true if the player can modify the settings of the team, false otherwise
     * @apiNote this is only used if the member is a verifiable team member
     */
    default boolean canModifySettings(String id, Player player) {
        return false;
    }
}
