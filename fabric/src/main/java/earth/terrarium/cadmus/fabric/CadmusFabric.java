package earth.terrarium.cadmus.fabric;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.registry.ModCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CadmusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Cadmus.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, selection) -> ModCommands.registerCommands(command -> command.accept(dispatcher)));
    }
}