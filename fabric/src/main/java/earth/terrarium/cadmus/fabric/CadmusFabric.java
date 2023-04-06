package earth.terrarium.cadmus.fabric;

import earth.terrarium.cadmus.Cadmus;
import net.fabricmc.api.ModInitializer;

public class CadmusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Cadmus.init();
    }
}