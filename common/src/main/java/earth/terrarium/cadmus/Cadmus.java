package earth.terrarium.cadmus;

import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.claims.CadmusDataHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.claims.maxclaims.CadmusMaxClaimProvider;
import earth.terrarium.cadmus.common.compat.prometheus.PrometheusIntegration;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.teams.VanillaTeamProvider;
import earth.terrarium.cadmus.common.util.ModGameRules;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Cadmus {
    public static final String MOD_ID = "cadmus";
    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(MOD_ID, "default");

    public static void init() {
        NetworkHandler.init();
        TeamProviderApi.API.register(DEFAULT_ID, new VanillaTeamProvider());
        MaxClaimProviderApi.API.register(DEFAULT_ID, new CadmusMaxClaimProvider());
        ModGameRules.init();
        ModFlags.init();
        if (!ModUtils.isModLoaded("argonauts")) {
            TeamProviderApi.API.setSelected(DEFAULT_ID);
        }

        if (ModUtils.isModLoaded("prometheus")) {
            PrometheusIntegration.register();
        } else {
            MaxClaimProviderApi.API.setSelected(DEFAULT_ID);
        }
    }

    public static void enterChunkSection(Player player) {
        if (player.level.isClientSide()) {
            CadmusClient.enterChunkSection();
        } else {
            ModUtils.displayTeamName((ServerPlayer) player);
        }
    }

    public static void serverStarted(MinecraftServer server) {
        // Set chunk loaded chunks
        server.getAllLevels().forEach(level ->
            ClaimHandler.getAllTeamClaims(level).forEach((id, data) ->
                data.forEach((pos, type) -> {
                    if (type == ClaimType.CHUNK_LOADED) {
                        level.getLevel().getChunkSource().updateChunkForced(pos, true);
                    }
                })));
        // Initialize the data handler
        CadmusDataHandler.read(server);
    }
}