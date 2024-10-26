package earth.terrarium.cadmus.api.client.events;

import com.teamresourceful.resourcefullib.common.color.Color;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CadmusClientEvents {

    private static final List<UpdateTeamInfo> UPDATE_TEAM_INFO = new ArrayList<>();

    @FunctionalInterface
    public interface UpdateTeamInfo {

        void updateTeamInfo(UUID id, String name, Color color, boolean updateMaps);

        /**
         * Called when the team info, consisting of the team's name and color, is synced to the client.
         */
        static void register(UpdateTeamInfo listener) {
            UPDATE_TEAM_INFO.add(listener);
        }

        @ApiStatus.Internal
        static void fire(UUID id, String name, Color color, boolean updateMaps) {
            for (var listener : UPDATE_TEAM_INFO) {
                listener.updateTeamInfo(id, name, color, updateMaps);
            }
        }
    }
}
