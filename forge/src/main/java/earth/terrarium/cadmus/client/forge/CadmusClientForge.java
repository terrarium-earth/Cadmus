package earth.terrarium.cadmus.client.forge;

import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.client.screen.ClaimsScreen;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

public class CadmusClientForge {
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(CadmusClientForge::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(CadmusClientForge::onRegisterClientCommands);
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            CadmusClient.onStartClientTick();
        }
    }

    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register((Commands.literal("claim").executes(context -> {
            CadmusClient.screen = new ClaimsScreen();
            return 0;
        })));
    }
}
