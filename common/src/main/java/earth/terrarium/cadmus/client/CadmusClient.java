package earth.terrarium.cadmus.client;

import earth.terrarium.cadmus.client.claims.ClaimMapScreen;
import earth.terrarium.cadmus.client.claims.ClaimMapUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class CadmusClient {
    @Nullable
    public static Screen screen;

    public static void init() {
    }

    public static void onStartClientTick() {
        if (screen != null) {
            Minecraft.getInstance().setScreen(screen);
            screen = null;
        }

        ClaimMapUpdater.update(ClaimMapScreen.MAP_RENDERER::update);
    }
}
