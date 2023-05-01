package earth.terrarium.cadmus.client;

import com.mojang.blaze3d.platform.InputConstants;
import earth.terrarium.cadmus.client.claims.ClaimMapScreen;
import earth.terrarium.cadmus.client.claims.ClaimMapUpdater;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class CadmusClient {
    public static boolean shouldOpenClaimMap;

    public static final KeyMapping KEY_OPEN_CLAIM_MAP = new KeyMapping(
        ConstantComponents.OPEN_CLAIM_MAP_KEY.getString(),
        InputConstants.UNKNOWN.getValue(),
        ConstantComponents.CADMUS_CATEGORY.getString());

    public static void init() {
    }

    public static void onStartClientTick() {
        if (shouldOpenClaimMap) {
            Minecraft.getInstance().setScreen(new ClaimMapScreen());
            shouldOpenClaimMap = false;
        }

        ClaimMapUpdater.update(ClaimMapScreen.MAP_RENDERER::update);

        if (KEY_OPEN_CLAIM_MAP.consumeClick()) {
            shouldOpenClaimMap = true;
        }
    }
}
