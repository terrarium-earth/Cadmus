package earth.terrarium.cadmus.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
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
    }
}
