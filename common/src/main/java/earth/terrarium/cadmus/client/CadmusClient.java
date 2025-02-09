package earth.terrarium.cadmus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.client.compat.prometheus.PrometheusClientCompat;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.commands.claims.ClaimCommandType;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.serverbound.ChatClaimPacket;
import earth.terrarium.cadmus.common.protections.SettingsData;
import earth.terrarium.cadmus.common.teams.TeamInfo;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CadmusClient {

    public static final Map<TeamId, TeamInfo> TEAM_INFO = new HashMap<>();

    public static final KeyMapping KEY_OPEN_CLAIM_MAP = new KeyMapping(
        ConstantComponents.OPEN_CLAIM_MAP_KEY.getString(),
        InputConstants.KEY_M,
        ConstantComponents.PROJECT_ODYSSEY_CATEGORY.getString());

    public static void init() {
        if (Cadmus.IS_PROMETHEUS_LOADED) {
            PrometheusClientCompat.init();
        }
    }

    public static void onClientTick() {
        if (KEY_OPEN_CLAIM_MAP.consumeClick()) {
            openClaimMap();
        }
    }

    public static void onPlayerLoggedOut() {
        ClaimSaveData.clearClientClaims();
        TEAM_INFO.clear();
    }

    public static void openClaimMap() {
        Minecraft.getInstance().setScreen(new ClaimMapScreen());
    }

    public static void updateClaimMapSettings(Map<TeamId, SettingsData> settings) {
        if(Minecraft.getInstance().screen instanceof ClaimMapScreen screen) {
            screen.updateSettings(settings, canModifyColor);
        }
    }

    public static void onEnterSection() {
        if (Minecraft.getInstance().screen instanceof ClaimMapScreen screen) {
            screen.refresh();
            screen.refreshMap();
        }
    }

    @NotNull
    public static Level level() {
        return Objects.requireNonNull(Minecraft.getInstance().level);
    }

    public static void sendClaimCommand(ClaimCommandType type, String command) {
        NetworkHandler.CHANNEL.sendToServer(new ChatClaimPacket(type, command));
    }
}
