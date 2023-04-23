package earth.terrarium.cadmus;

import earth.terrarium.cadmus.api.team.TeamProviderApi;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.registry.ModGameRules;
import earth.terrarium.cadmus.common.team.VanillaTeamProvider;
import net.minecraft.resources.ResourceLocation;

public class Cadmus {
    public static final String MOD_ID = "cadmus";
    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(MOD_ID, "default");

    public static void init() {
        NetworkHandler.init();
        TeamProviderApi.API.register(DEFAULT_ID, new VanillaTeamProvider());
        TeamProviderApi.API.setSelected(DEFAULT_ID);
        ModGameRules.init();
    }
}