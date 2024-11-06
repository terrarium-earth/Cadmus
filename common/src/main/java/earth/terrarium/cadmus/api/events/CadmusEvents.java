package earth.terrarium.cadmus.api.events;

import earth.terrarium.cadmus.api.teams.TeamId;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CadmusEvents {

    private static final List<AddClaimsEvent> ADD_CLAIMS_EVENT = new ArrayList<>();
    private static final List<RemoveClaimsEvent> REMOVE_CLAIMS_EVENT = new ArrayList<>();
    private static final List<ClearClaimsEvent> CLEAR_CLAIMS_EVENT = new ArrayList<>();

    private static final List<CreateTeamEvent> CREATE_TEAM_EVENT = new ArrayList<>();
    private static final List<RemoveTeamEvent> REMOVE_TEAM_EVENT = new ArrayList<>();
    private static final List<TeamChangedEvent> TEAM_CHANGED_EVENT = new ArrayList<>();
    private static final List<AddPlayerToTeamEvent> ADD_PLAYER_TO_TEAM_EVENT = new ArrayList<>();
    private static final List<RemovePlayerFromTeamEvent> REMOVE_PLAYER_FROM_TEAM_EVENT = new ArrayList<>();

    @FunctionalInterface
    public interface AddClaimsEvent {

        void addClaims(Level level, TeamId id, Object2BooleanMap<ChunkPos> positions);

        static void register(AddClaimsEvent listener) {
            ADD_CLAIMS_EVENT.add(listener);
        }

        @ApiStatus.Internal
        static void fire(Level level, TeamId id, Object2BooleanMap<ChunkPos> positions) {
            for (var listener : ADD_CLAIMS_EVENT) {
                listener.addClaims(level, id, positions);
            }
        }
    }

    @FunctionalInterface
    public interface RemoveClaimsEvent {

        void removeClaims(Level level, TeamId id, Set<ChunkPos> positions);

        static void register(RemoveClaimsEvent listener) {
            REMOVE_CLAIMS_EVENT.add(listener);
        }

        @ApiStatus.Internal
        static void fire(Level level, TeamId id, Set<ChunkPos> positions) {
            for (var listener : REMOVE_CLAIMS_EVENT) {
                listener.removeClaims(level, id, positions);
            }
        }
    }

    @FunctionalInterface
    public interface ClearClaimsEvent {

        void clearClaims(Level level, TeamId id);

        static void register(ClearClaimsEvent listener) {
            CLEAR_CLAIMS_EVENT.add(listener);
        }

        @ApiStatus.Internal
        static void fire(Level level, TeamId id) {
            for (var listener : CLEAR_CLAIMS_EVENT) {
                listener.clearClaims(level, id);
            }
        }
    }

    @FunctionalInterface
    public interface CreateTeamEvent {

        void createTeam(MinecraftServer server, TeamId id);

        static void register(CreateTeamEvent listener) {
            CREATE_TEAM_EVENT.add(listener);
        }

        @ApiStatus.Internal
        static void fire(MinecraftServer server, TeamId id) {
            for (var listener : CREATE_TEAM_EVENT) {
                listener.createTeam(server, id);
            }
        }
    }

    @FunctionalInterface
    public interface RemoveTeamEvent {

        void removeTeam(MinecraftServer server, TeamId id);

        static void register(RemoveTeamEvent listener) {
            REMOVE_TEAM_EVENT.add(listener);
        }

        @ApiStatus.Internal
        static void fire(MinecraftServer server, TeamId id) {
            for (var listener : REMOVE_TEAM_EVENT) {
                listener.removeTeam(server, id);
            }
        }
    }

    @FunctionalInterface
    public interface TeamChangedEvent {

        void teamChanged(MinecraftServer server, TeamId id);

        /**
         * Called when team info, like the name or color, has changed.
         */
        static void register(TeamChangedEvent listener) {
            TEAM_CHANGED_EVENT.add(listener);
        }

        @ApiStatus.Internal
        static void fire(MinecraftServer server, TeamId id) {
            for (var listener : TEAM_CHANGED_EVENT) {
                listener.teamChanged(server, id);
            }
        }
    }

    @FunctionalInterface
    public interface AddPlayerToTeamEvent {

        void addPlayerToTeam(MinecraftServer server, TeamId id, @Nullable ServerPlayer player);

        static void register(AddPlayerToTeamEvent listener) {
            ADD_PLAYER_TO_TEAM_EVENT.add(listener);
        }

        @ApiStatus.Internal
        static void fire(MinecraftServer server, TeamId id, @Nullable ServerPlayer player) {
            for (var listener : ADD_PLAYER_TO_TEAM_EVENT) {
                listener.addPlayerToTeam(server, id, player);
            }
        }
    }

    @FunctionalInterface
    public interface RemovePlayerFromTeamEvent {

        void removePlayerFromTeam(MinecraftServer server, TeamId id, @Nullable ServerPlayer player);

        static void register(RemovePlayerFromTeamEvent listener) {
            REMOVE_PLAYER_FROM_TEAM_EVENT.add(listener);
        }

        @ApiStatus.Internal
        static void fire(MinecraftServer server, TeamId id, @Nullable ServerPlayer player) {
            for (var listener : REMOVE_PLAYER_FROM_TEAM_EVENT) {
                listener.removePlayerFromTeam(server, id, player);
            }
        }
    }
}
