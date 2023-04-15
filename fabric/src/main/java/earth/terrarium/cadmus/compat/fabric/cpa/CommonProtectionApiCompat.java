package earth.terrarium.cadmus.compat.fabric.cpa;

import earth.terrarium.cadmus.Cadmus;
import eu.pb4.common.protection.api.CommonProtection;
import net.minecraft.resources.ResourceLocation;

public final class CommonProtectionApiCompat {

    private static final ResourceLocation CADMUS = new ResourceLocation(Cadmus.MOD_ID, Cadmus.MOD_ID);

    public static void init() {
        CommonProtection.register(CADMUS, new CadmusProtectionProvider());
    }
}
