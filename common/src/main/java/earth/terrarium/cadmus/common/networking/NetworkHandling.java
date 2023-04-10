package earth.terrarium.cadmus.common.networking;

import com.teamresourceful.resourcefullib.common.networking.NetworkChannel;
import earth.terrarium.cadmus.Cadmus;

public class NetworkHandling {
    public static final NetworkChannel CHANNEL = new NetworkChannel(Cadmus.MOD_ID, 0, "main");

    public static void init() {
    }
}
