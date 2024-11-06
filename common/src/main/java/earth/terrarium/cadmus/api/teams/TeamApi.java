package earth.terrarium.cadmus.api.teams;

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

import java.util.Set;

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
    Component getName(Level level, ResourceLocation provider, TeamId id);

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
     * @param player The player.
     * @return true if the player is a member of the team, false otherwise.
     */
    boolean isMember(Level level, TeamId id, Player player);

    /**
     * Gets the id of the team if the player is in one, or the player's UUID if not.
     *
     * @param player The player.
     * @return The team's ID or the player's UUID.
     */
    Set<TeamId> getTeams(@NotNull Player player);

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
