package earth.terrarium.cadmus;

import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.teams.VanillaTeamProvider;
import earth.terrarium.cadmus.common.util.ModGameRules;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Cadmus {
    public static final String MOD_ID = "cadmus";
    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(MOD_ID, "default");

    public static void init() {
        NetworkHandler.init();
        TeamProviderApi.API.register(DEFAULT_ID, new VanillaTeamProvider());
        TeamProviderApi.API.setSelected(DEFAULT_ID);
        ModGameRules.init();
    }

    public static void enterChunkSection(Player player) {
        if (player.level.isClientSide()) {
            CadmusClient.enterChunkSection();
        } else {
            ModUtils.displayTeamName((ServerPlayer) player);
        }
    }
}