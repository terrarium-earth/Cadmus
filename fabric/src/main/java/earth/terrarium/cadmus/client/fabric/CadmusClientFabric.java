package earth.terrarium.cadmus.client.fabric;

import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.client.map.ClaimMapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class CadmusClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CadmusClient.init();
        ClientTickEvents.START_CLIENT_TICK.register(client -> CadmusClient.onStartClientTick());
        registerClientCommands();
    }

    private static void registerClientCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("claim").executes(context -> {
                    CadmusClient.screen = new ClaimMapScreen();
                    return 0;
                })));
    }
}
