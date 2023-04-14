package earth.terrarium.cadmus.client;

import earth.terrarium.cadmus.client.map.ClaimMapScreen;
import earth.terrarium.cadmus.common.claiming.ClaimedChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CadmusClient {
    @Nullable
    public static Screen screen;
    public static final Map<String, Set<ClaimedChunk>> TEAMS = new HashMap<>();

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
