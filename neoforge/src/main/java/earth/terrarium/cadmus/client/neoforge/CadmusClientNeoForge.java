package earth.terrarium.cadmus.client.neoforge;

import earth.terrarium.cadmus.client.CadmusClient;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CadmusClientNeoForge {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CadmusClient::init);
        NeoForge.EVENT_BUS.addListener(CadmusClientNeoForge::onClientTick);
        NeoForge.EVENT_BUS.addListener(CadmusClientNeoForge::onRegisterClientCommands);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(CadmusClientNeoForge::onRegisterKeyBindings);
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            CadmusClient.clientTick();
        }
    }

    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register((Commands.literal("claimmap").executes(context -> {
            Minecraft.getInstance().tell(CadmusClient::openClaimMap);
            return 0;
        })));
    }

    @SubscribeEvent
    public static void onRegisterKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(CadmusClient.KEY_OPEN_CLAIM_MAP);
    }
}
