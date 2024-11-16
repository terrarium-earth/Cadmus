package earth.terrarium.cadmus.api.protections;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.flags.types.BooleanFlag;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.compat.prometheus.PrometheusCompat;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.UUID;

public interface Protection {

    /**
     * The setting name. Used in the `/claim settings` command.
     *
     * @return the setting name
     */
    String setting();

    /**
     * The permission name, used to enable or disable the protection. Used when Prometheus is installed in the `/roles` menu.
     *
     * @return the permission name
     */
    String permission();

    /**
     * The personal permission name, used to allow players to manage their personal claim settings. Used when Prometheus is installed in the `/roles` menu.
     *
     * @return the personal permission name
     */
    String personalPermission();

    /**
     * The name of this protection's admin claim flag.
     *
     * @return the flag name
     */
    BooleanFlag flag();

    /**
     * The game rule key. Used to enable or disable the protection.
     *
     * @return the game rule key
     */
    GameRules.Key<GameRules.BooleanValue> gameRule();

    private boolean hasPermission(Player player) {
        return Cadmus.IS_PROMETHEUS_LOADED && PrometheusCompat.hasPermission(player, permission());
    }

    private boolean gameRuleEnabled(Level level) {
        return level.getGameRules().getBoolean(gameRule());
    }

    private boolean settingEnabled(MinecraftServer server, TeamId id) {
        return CadmusSaveData.getClaimSettingOrDefault(server, id, setting());
    }

    private boolean flagEnabled(MinecraftServer server, TeamId id) {
        return FlagApi.API.isAdminTeam(server, id) &&
            flag().get(server, id);
    }

    default Optional<TeamId> getId(Level level, BlockPos pos) {
        return getId(level, new ChunkPos(pos));
    }

    default Optional<TeamId> getId(Level level, ChunkPos pos) {
        return ClaimApi.API.getClaim(level, pos).map(Pair::left);
    }

    default boolean isPlayerAllowed(Player player, TeamId id) {
        if (CadmusSaveData.canBypass(player.getServer(), player.getUUID())) return true;

        if (id.providerId().equals()) {
            return flagEnabled(player.getServer(), id.teamId());
        }

        if (hasPermission(player)) return true;
        if (gameRuleEnabled(player.level())) return true;

        if (settingEnabled(player.getServer(), id)) return true;
        return TeamApi.API.isMember(player.level(), id, player);
    }

    default boolean isEntityAllowed(Entity entity, TeamId id) {
        if (flagEnabled(entity.getServer(), id)) return false;
        if (gameRuleEnabled(entity.level())) return true;
        return settingEnabled(entity.getServer(), id);
    }

    default boolean isBlockAllowed(Level level, TeamId id, BlockPos pos) {
        return isBlockAllowed(level, id, level.getBlockState(pos));
    }

    default boolean isBlockAllowed(Level level, TeamId id, BlockState state) {
        return CadmusSaveData.isBlockAllowed(level.getServer(), id, state.getBlock());
    }
}
