package earth.terrarium.cadmus.forge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.forge.CadmusClientForge;
import earth.terrarium.cadmus.common.registry.ModCommands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Cadmus.MOD_ID)
public class CadmusForge {
    public CadmusForge() {
        Cadmus.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CadmusClientForge::init);
        MinecraftForge.EVENT_BUS.addListener(CadmusForge::onRegisterCommands);
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.registerCommands(command -> command.accept(event.getDispatcher()));
    }
}