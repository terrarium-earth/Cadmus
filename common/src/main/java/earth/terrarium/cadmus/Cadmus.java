package earth.terrarium.cadmus;

import earth.terrarium.cadmus.common.network.NetworkHandler;

public class Cadmus {
    public static final String MOD_ID = "cadmus";

    public static void init() {
        NetworkHandler.init();
    }
}