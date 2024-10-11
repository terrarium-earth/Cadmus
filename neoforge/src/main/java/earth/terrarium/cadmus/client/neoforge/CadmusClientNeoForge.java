package earth.terrarium.cadmus.client.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.CadmusClient;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(value = Cadmus.MOD_ID, dist = Dist.CLIENT)
public class CadmusClientNeoForge {

    public CadmusClientNeoForge() {
        NeoForge.EVENT_BUS.addListener(CadmusClientNeoForge::onClientTick);
        NeoForge.EVENT_BUS.addListener(CadmusClientNeoForge::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(CadmusClientNeoForge::onRegisterClientCommands);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CadmusClient::init);
    }

    public static void onClientTick(ClientTickEvent.Pre event) {
        CadmusClient.onClientTick();
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        CadmusClient.onPlayerLoggedOut();
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
