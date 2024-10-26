package earth.terrarium.cadmus.api.flags;

import earth.terrarium.cadmus.api.ApiHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface FlagApi {

    FlagApi API = ApiHelper.load(FlagApi.class);

    /**
     * Registers a new flag with a default value.
     *
     * @param defaultValue The value of the flag.
     */
    <T extends Flag<?>> T register(T defaultValue);

    /**
     * Gets the default value of a flag.
     *
     * @param name The name of the flag.
     * @return The default value of the flag.
     */
    Flag<?> getDefaultValue(String name);


    /**
     * Gets all registered flag names and their default values.
     *
     * @return All registered flag names and default values.
     */
    Map<String, Flag<?>> getAllDefaults();

    /**
     * Checks if a claim is an admin team.
     *
     * @param server The server.
     * @param id     The id of the admin team.
     * @return Whether the claim is an admin team.
     */
    boolean isAdminTeam(MinecraftServer server, UUID id);

    /**
     * Checks if a claim is an admin team.
     *
     * @param server The server.
     * @param name   The name of the admin team.
     * @return Whether the claim is an admin team.
     */
    boolean isAdminTeam(MinecraftServer server, String name);

    /**
     * Gets all admin teams.
     *
     * @param server The server.
     * @return All admin teams.
     */
    Map<UUID, Map<String, Flag<?>>> getAllAdminTeams(MinecraftServer server);

    /**
     * Gets all admin team names.
     *
     * @param server The server.
     * @return All admin team names.
     */
    Collection<String> getAllAdminTeamNames(MinecraftServer server);

    /**
     * Creates an admin team.
     *
     * @param server The server.
     * @param name   The name of the admin team.
     * @return The id of the team.
     */
    UUID createAdminTeam(MinecraftServer server, String name);

    /**
     * Removes an admin team.
     *
     * @param server The server.
     * @param id     The id of the admin team.
     */
    void removeAdminTeam(MinecraftServer server, UUID id);

    /**
     * Gets the id of an admin team from its name.
     *
     * @param server The server.
     * @param name   The name of the team.
     * @return The id of the team, or empty if the team does not exist.
     */
    Optional<UUID> getIdFromName(MinecraftServer server, String name);

    /**
     * Gets the value of a flag for a claim.
     *
     * @param level    The level.
     * @param pos      The position of the chunk.
     * @param flagName The name of the flag.
     * @return The value of the flag, or the flag's default value if the chunk is not claimed.
     * @throws IllegalArgumentException If the flag is not registered.
     */
    <T> Optional<Flag<T>> getFlag(ServerLevel level, ChunkPos pos, String flagName);

    /**
     * Gets the value of a flag for an admin team.
     *
     * @param server   The server.
     * @param id       The id of the admin team.
     * @param flagName The name of the flag.
     * @return The value of the flag, or the flag's default value if the team hasn't overridden it.
     * @throws IllegalArgumentException If the flag is not registered.
     */
    @NotNull
    <T> Flag<T> getFlag(MinecraftServer server, UUID id, String flagName);

    /**
     * Sets the value of a flag for an admin team.
     *
     * @param server   The server.
     * @param id       The id of the admin team.
     * @param flagName The name of the flag.
     * @param flag     The new value of the flag.
     */
    void setFlag(MinecraftServer server, UUID id, String flagName, Flag<?> flag);

    /**
     * Removes a flag from an admin team.
     *
     * @param server   The server.
     * @param id       The id of the admin team.
     * @param flagName The name of the flag.
     */
    void removeFlag(MinecraftServer server, UUID id, String flagName);

    /**
     * Clears all flags.
     *
     * @param server The server.
     */
    void clearAll(MinecraftServer server);
}
