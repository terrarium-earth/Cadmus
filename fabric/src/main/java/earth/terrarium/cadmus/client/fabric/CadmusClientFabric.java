package earth.terrarium.cadmus.client.fabric;

import earth.terrarium.cadmus.client.CadmusClient;
import net.fabricmc.api.ClientModInitializer;

public class CadmusClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CadmusClient.init();
    }
}
