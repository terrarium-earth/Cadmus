package earth.terrarium.cadmus.client.forge;

import earth.terrarium.cadmus.client.CadmusClient;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CadmusClientForge {
    public static void init() {
        CadmusClient.init();
        MinecraftForge.EVENT_BUS.addListener(CadmusClientForge::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(CadmusClientForge::onRegisterClientCommands);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(CadmusClientForge::onRegisterKeyBindings);
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            CadmusClient.clientTick();
        }
    }

    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register((Commands.literal("claim").executes(context -> {
            Minecraft.getInstance().tell(CadmusClient::openClaimMap);
            return 0;
        })));
    }

    public static void onRegisterKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(CadmusClient.KEY_OPEN_CLAIM_MAP);
    }
}
