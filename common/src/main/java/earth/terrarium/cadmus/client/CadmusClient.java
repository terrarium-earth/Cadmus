package earth.terrarium.cadmus.client;

import com.mojang.blaze3d.platform.InputConstants;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.claims.ClaimScreen;
import earth.terrarium.cadmus.client.compat.prometheus.PrometheusClientCompat;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.ServerboundRequestClaimedChunksPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class CadmusClient {

    public static final KeyMapping KEY_OPEN_CLAIM_MAP = new KeyMapping(
        ConstantComponents.OPEN_CLAIM_MAP_KEY.getString(),
        InputConstants.KEY_M,
        ConstantComponents.ODYSSEY_CATEGORY.getString());

    public static void init() {
        if (Cadmus.IS_PROMETHEUS_LOADED) {
            PrometheusClientCompat.init();
        }
    }

    public static void clientTick() {
        if (KEY_OPEN_CLAIM_MAP.consumeClick()) {
            openClaimMap();
        }
    }

    // Send a request to the server to send the claim data and then open the claim map screen.
    public static void openClaimMap() {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundRequestClaimedChunksPacket(Minecraft.getInstance().options.renderDistance().get()));
    }

    public static void enterChunkSection() {
        if (Minecraft.getInstance().screen instanceof ClaimScreen screen) {
            screen.refreshMap();
        }
    }
}
