package earth.terrarium.cadmus.api.teams;

import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.api.ApiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface TeamApi {

    TeamApi API = ApiHelper.load(TeamApi.class);

    /**
     * Registers a team.
     *
     * @param team   The team.
     * @param weight The weight of the team. The team with the highest weight will be used. By default, only the vanilla team is registered with a weight of 0 so register your team with a higher weight to use yours instead.
     */
    void register(Team team, int weight);

    /**
     * Gets the selected team.
     *
     * @return the selected team.
     */
    Team getSelected();

    /**
     * Gets all the teams.
     *
     * @param server The server.
     * @return All the teams.
     */
    Set<UUID> getAllTeams(MinecraftServer server);

    /**
     * Checks if the team exists.
     *
     * @param server The server.
     * @param id     The ID of the team.
     * @return true if the team exists, false otherwise.
     */
    default boolean teamExists(MinecraftServer server, UUID id) {
        return this.getAllTeams(server).contains(id);
    }

    /**
     * If the ID is a team, gets the team name. If the ID is a player, gets the player name. If the ID is an admin team, gets the name flag. If it can't find any of these, return "Unknown"
     *
     * @param level The level.
     * @param id    The ID of the team.
     * @return The name of the team or the player, or "Unknown" if the team or player is not found. The component also has the color of the team.
     */
    Component getName(Level level, UUID id);

    /**
     * If the ID is a team, gets the team name. If the ID is a player, gets the player name. If the ID is an admin team, gets the name flag. If it can't find any of these, return "Unknown"
     *
     * @param server The server.
     * @param id     The ID of the team.
     * @return The name of the team or the player, or "Unknown" if the team or player is not found. The component also has the color of the team.
     */
    Component getName(MinecraftServer server, UUID id);

    /**
     * If the ID is a team, gets the team color. If the ID is an admin team, gets the color flag. If it can't find any of these, return a random color using the ID as a seed.
     *
     * @param level The level.
     * @param id    The ID of the team.
     * @return The color of the team or a seed-based random color if the team is not found.
     */
    Color getColor(Level level, UUID id);

    /**
     * If the ID is a team, gets the team color. If the ID is an admin team, gets the color flag. If it can't find any of these, return a random color using the ID as a seed.
     *
     * @param server The server.
     * @param id     The ID of the team.
     * @return The color of the team or a seed-based random color if the team is not found.
     */
    Color getColor(MinecraftServer server, UUID id);

    /**
     * Checks if the player is a member of the team, or if the player owns the personal team.
     *
     * @param level  The level.
     * @param id     The ID of the team.
     * @param player The player.
     * @return true if the player is a member of the team, false otherwise.
     */
    boolean isMember(Level level, UUID id, Player player);

    /**
     * Gets the id of the team if the player is in one, or the player's UUID if not.
     *
     * @param player The player.
     * @return The team's ID or the player's UUID.
     */
    UUID getId(@NotNull Player player);

    /**
     * Checks if the player is on a team.
     *
     * @param player The player.
     * @return true if the player is on a team, false otherwise.
     */
    boolean isOnTeam(@NotNull Player player);

    /**
     * Checks if the player can modify the team's settings.
     *
     * @param player The player.
     * @return true if the player is not on a team or if the player can modify the team's settings, false otherwise.
     */
    boolean canModifySettings(@NotNull Player player);

    /**
     * Removes the team. Clears all the team's chunks and settings.
     *
     * @param server The server.
     * @param id     The ID of the team.
     */
    void removeTeam(MinecraftServer server, UUID id);

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
    void syncTeamInfo(MinecraftServer server, UUID id, boolean updateMaps);

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
