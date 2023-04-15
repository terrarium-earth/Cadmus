package earth.terrarium.cadmus.client;

import earth.terrarium.cadmus.client.map.ClaimMapScreen;
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

        if (Minecraft.getInstance().screen instanceof ClaimMapScreen claimsScreen) {
            claimsScreen.update();
        }
    }
}
