package earth.terrarium.cadmus;

import com.teamresourceful.resourcefullib.common.utils.modinfo.ModInfoUtils;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.claims.limit.ClaimLimitApiImpl;
import earth.terrarium.cadmus.common.claims.limit.VanillaClaimLimiter;
import earth.terrarium.cadmus.common.compat.prometheus.PrometheusCompat;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.protections.ClaimSettings;
import earth.terrarium.cadmus.common.protections.Protections;
import earth.terrarium.cadmus.common.teams.VanillaTeamProvider;
import earth.terrarium.cadmus.common.utils.AdminUtils;
import earth.terrarium.cadmus.common.utils.CadmusGameRules;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class Cadmus {

    public static final String MOD_ID = "cadmus";

    public static final boolean IS_PROMETHEUS_LOADED = ModInfoUtils.isModLoaded("prometheus");
    public static final int DEFAULT_MAX_CLAIMS = 1096;
    public static final int DEFAULT_MAX_CHUNK_LOADED_CLAIMS = 64;

    public static int FORCE_LOADED_CHUNK_COUNT;

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        NetworkHandler.init();
        CadmusGameRules.init();
        Protections.init();
        ClaimSettings.init();
        Flags.init();
        TeamApi.API.register(new VanillaTeamProvider(), 0);
        ClaimLimitApi.API.register(new VanillaClaimLimiter());
        if (IS_PROMETHEUS_LOADED) PrometheusCompat.init();
    }

    public static void onEnterSection(Player player, ChunkPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            TeamApi.API.displayTeamName(serverPlayer, pos);
            AdminUtils.checkAccess(serverPlayer, pos);
        } else CadmusClient.onEnterSection();
    }

    public static void onPlayerJoin(ServerPlayer player) {
        ModUtils.sendJoinPackets(player);
        TeamApi.API.syncAllTeamInfo(player);
        TeamApi.API.displayTeamName(player);
        ClaimLimitApiImpl.API.calculate(player.server, player.getUUID(), true);
    }

    public static void onServerStarted(MinecraftServer server) {
        FORCE_LOADED_CHUNK_COUNT = 0;
        server.getAllLevels().forEach(level ->
            ClaimApi.API.getAllClaims(level).forEach((pos, claim) -> {
                if (claim.rightBoolean()) {
                    FORCE_LOADED_CHUNK_COUNT++;
                    level.getChunkSource().updateChunkForced(pos, true);
                }
            })
        );
        ClaimLimitApi.API.calculate(server);
    }
}
