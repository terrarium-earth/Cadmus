package earth.terrarium.cadmus.api.teams;

import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.api.events.CadmusEvents;
import earth.terrarium.cadmus.common.claims.limit.ClaimLimitApiImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TeamProvider {

    ResourceLocation id();

    /**
     * Gets the team's name.
     *
     * @param level The level.
     * @param id    The ID of the team.
     * @return The team's name, or empty if no team is found.
     */
    Optional<Component> getName(Level level, UUID id);

    /**
     * Gets the team's color.
     *
     * @param level The level.
     * @return The color of the team, or empty if no team is found.
     */
    Optional<Color> getColor(Level level, UUID id);

    /**
     * Gets the members of the team.
     *
     * @param level The level.
     * @param id    The ID of the team.
     * @return The members of the team.
     */
    Set<UUID> getMembers(Level level, UUID id);

    /**
     * Checks if the player is a member of the team.
     *
     * @param level  The level.
     * @param id     The ID of the team.
     * @param player The player.
     * @return true if the player is a member of the team, false otherwise.
     */
    boolean isMember(Level level, UUID id, Player player);

    /**
     * Gets the id of the player's team.
     *
     * @param player The player.
     * @return The team's ID or empty if the player is not in a team.
     */
    Set<UUID> getTeams(Player player);

    /**
     * Checks if the player can modify the team's settings.
     *
     * @param player The player.
     * @return true if the player can modify the team's settings, false otherwise.
     */
    boolean canModifySettings(Player player, UUID teamId);

    Set<UUID> getAllTeams(MinecraftServer server);

    default void onCreate(MinecraftServer server, UUID id) {
        TeamId teamId = new TeamId(id(), id);
        CadmusEvents.CreateTeamEvent.fire(server, teamId);
        TeamApi.API.syncTeamInfo(server, teamId, true);
        ClaimLimitApiImpl.API.calculate(server, teamId, true);
    }

    default void onRemove(MinecraftServer server, UUID id) {
        TeamId teamId = new TeamId(id(), id);
        CadmusEvents.RemoveTeamEvent.fire(server, teamId);
        TeamApi.API.removeTeam(server, teamId);
    }

    default void onChange(MinecraftServer server, UUID id) {
        TeamId teamId = new TeamId(id(), id);
        CadmusEvents.TeamChangedEvent.fire(server, teamId);
        TeamApi.API.syncTeamInfo(server, teamId, true);
    }

    default void onPlayerAdded(MinecraftServer server, UUID id, @Nullable ServerPlayer player) {
        TeamId teamId = new TeamId(id(), id);
        CadmusEvents.AddPlayerToTeamEvent.fire(server, teamId, player);
        ClaimLimitApiImpl.API.calculate(server, teamId, true);
    }

    default void onPlayerRemoved(MinecraftServer server, UUID id, @Nullable ServerPlayer player) {
        TeamId teamId = new TeamId(id(), id);
        CadmusEvents.RemovePlayerFromTeamEvent.fire(server, teamId, player);
        ClaimLimitApiImpl.API.calculate(server, id, true);
    }
}
