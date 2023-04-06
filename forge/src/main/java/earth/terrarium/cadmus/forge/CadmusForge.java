package earth.terrarium.cadmus.forge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.forge.CadmusClientForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Cadmus.MOD_ID)
public class CadmusForge {
    public CadmusForge() {
        Cadmus.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CadmusClientForge::init);
    }
}