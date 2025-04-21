package earth.terrarium.cadmus.api.teams;

import com.mojang.authlib.GameProfile;
import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.api.ApiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface TeamApi {

    TeamApi API = ApiHelper.load(TeamApi.class);

    /**
     * Registers a team provider.
     *
     * @param id    The ID of the team provider.
     * @param team   The team provider.
     */
    void register(ResourceLocation id, TeamProvider team);

    /**
     * Gets all the teams.
     *
     * @param server The server.
     * @return All the teams.
     */
    Set<TeamId> getAllTeams(MinecraftServer server);

    /**
     * Gets all team providers.
     *
     * @return  A set of all team providers.
     */
    Set<ResourceLocation> getAllProviders();

    /**
     *
     * Gets a specific team provider by its ID. May return null if it doesn't exist.
     * @param id    Id of registered team provider
     * @return      a team provider, may be null
     */
    @Nullable
    TeamProvider getProvider(ResourceLocation id);

    /**
     * Checks if the team exists.
     *
     * @param server The server.
     * @param id     The ID of the team.
     * @return true if the team exists, false otherwise.
     */
    default boolean teamExists(MinecraftServer server, TeamId id) {
        return this.getAllTeams(server).contains(id);
    }

    /**
     * If the ID is a team, gets the team name. If the ID is a player, gets the player name. If the ID is an admin team, gets the name flag. If it can't find any of these, return "Unknown"
     *
     * @param level The level.
     * @param id    The ID of the team.
     * @return The name of the team or the player, or "Unknown" if the team or player is not found. The component also has the color of the team.
     */
    Component getName(Level level, TeamId id);

    /**
     * If the ID is a team, gets the team name. If the ID is a player, gets the player name. If the ID is an admin team, gets the name flag. If it can't find any of these, return "Unknown"
     *
     * @param server The server.
     * @param id     The ID of the team.
     * @return The name of the team or the player, or "Unknown" if the team or player is not found. The component also has the color of the team.
     */
    Component getName(MinecraftServer server, TeamId id);

    /**
     * If the ID is a team, gets the team color. If the ID is an admin team, gets the color flag. If it can't find any of these, return a random color using the ID as a seed.
     *
     * @param level The level.
     * @param id    The ID of the team.
     * @return The color of the team or a seed-based random color if the team is not found.
     */
    Color getColor(Level level, TeamId id);

    /**
     * If the ID is a team, gets the team color. If the ID is an admin team, gets the color flag. If it can't find any of these, return a random color using the ID as a seed.
     *
     * @param server The server.
     * @param id     The ID of the team.
     * @return The color of the team or a seed-based random color if the team is not found.
     */
    Color getColor(MinecraftServer server, TeamId id);

    /**
     * Checks if the player is a member of the team, or if the player owns the personal team.
     *
     * @param level  The level.
     * @param id     The ID of the team.
     * @param player The player's ID.
     * @return true if the player is a member of the team, false otherwise.
     */
    boolean isMember(Level level, GameProfile player, TeamId id);

    /**
     * Checks if the player is a member of the team, or if the player owns the personal team.
     *
     * @param id     The ID of the team.
     * @param player The player.
     * @return true if the player is a member of the team, false otherwise.
     */
    default boolean isMember(@NotNull Player player, TeamId id) {
        return isMember(player.level(), player.getGameProfile(), id);
    }

    /**
     * Checks if the player is a member of the team, or if the player owns the personal team.
     * @param level The level.
     * @param id    The ID of the team.
     * @return The members of the team.
     */
    Set<UUID> getMembers(Level level, TeamId id);

    /**
     * Gets the id of the team if the player is in one, or the player's UUID if not.
     *
     * @param player The player.
     * @return The team's ID or the player's UUID.
     */
    Set<TeamId> getTeamsList(Level level, GameProfile player);

    /**
     * Gets the id of the player's team.
     *
     * @param player The player.
     * @return The team's ID or empty if the player is not in a team.
     */
    default Set<TeamId> getTeamsList(@NotNull Player player) {
        return getTeamsList(player.level(), player.getGameProfile());
    }

    /**
     * Gets the id of the player's team.
     *
     * @param level  The level.
     * @param player The player profile.
     * @return The team's ID or empty if the player is not in a team.
     */
    Map<ResourceLocation, Set<UUID>> getTeams(Level level, GameProfile player);

    /**
     * Gets the id of the player's team.
     *
     * @param player The player.
     * @return The team's ID or empty if the player is not in a team.
     */
    default Map<ResourceLocation, Set<UUID>> getTeams(@NotNull Player player) {
        return getTeams(player.level(), player.getGameProfile());
    }

    /**
     * Checks if the player is on a team.
     *
     * @param player The player.
     * @return true if the player is on a team, false otherwise.
     */
    boolean isOnTeam(Level level, GameProfile player);

    /**
     * Checks if the player is on a team.
     *
     * @param player The player.
     * @return true if the player is on a team, false otherwise.
     */
    default boolean isOnTeam(@NotNull Player player) {
        return isOnTeam(player.level(), player.getGameProfile());
    }

    /**
     * Checks if the player can modify the team's settings.
     *
     * @param player The player.
     * @return true if the player is not on a team or if the player can modify the team's settings, false otherwise.
     */
    default boolean canModifySettings(Level level, GameProfile player, TeamId id) {
        return getProvider(id.provider()).canModifySettings(level, id.id(), player);
    }

    /**
     * Checks if the player can modify the team's settings.
     *
     * @param player The player.
     * @return true if the player is not on a team or if the player can modify the team's settings, false otherwise.
     */
    default boolean canModifySettings(@NotNull Player player, TeamId id) {
        return canModifySettings(player.level(), player.getGameProfile(), id);
    }

    /**
     * Removes the team. Clears all the team's chunks and settings.
     *
     * @param server The server.
     * @param id     The ID of the team.
     */
    void removeTeam(MinecraftServer server, TeamId id);

    /**
     * Syncs all IDs and their names and colors to all clients.
     *
     * @param server The server.
     */
    void syncAllTeamInfo(MinecraftServer server);

    /**
     * Syncs all IDs and their names and colors to the player.
     *
     * @param player The player.
     */
    void syncAllTeamInfo(ServerPlayer player);

    /**
     * Syncs the team ID and its corresponding name and color to all clients.
     *
     * @param server     The server.
     * @param id         The ID of the team.
     * @param updateMaps Whether to update display maps.
     */
    void syncTeamInfo(MinecraftServer server, TeamId id, boolean updateMaps);

    /**
     * Displays the name of the team that has claimed the chunk the player is in.
     *
     * @param player The player.
     * @param pos    The position of the chunk.
     */
    void displayTeamName(ServerPlayer player, ChunkPos pos);

    /**
     * Displays the name of the team that has claimed the chunk the player is in.
     *
     * @param player The player.
     */
    default void displayTeamName(ServerPlayer player) {
        displayTeamName(player, player.chunkPosition());
    }

    /**
     * For each player on the server, display the name of the team that has claimed the chunk they are in.
     *
     * @param server The server.
     */
    void displayTeamNameToAll(MinecraftServer server);
}
