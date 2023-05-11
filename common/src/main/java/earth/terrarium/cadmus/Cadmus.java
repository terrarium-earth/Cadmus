package earth.terrarium.cadmus;

import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.teams.TeamSaveData;
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
        ModGameRules.init();
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
            ClaimSaveData.getAll(level).forEach((pos, info) -> {
                if (info.type() == ClaimType.CHUNK_LOADED) {
                    level.getLevel().getChunkSource().updateChunkForced(pos, true);
                }
            }));

        // Set the Team Provider
        if (!ModUtils.isModLoaded("argonauts")) {
            TeamSaveData.read(server);
            // Should only ever get set once when the world is first created, and then it should grab the saved value.
            if (TeamProviderApi.API.getSelectedId() == null) {
                TeamProviderApi.API.setSelected(DEFAULT_ID);
            }
        }
    }

    @SuppressWarnings("unused")
    public static void serverStopped(MinecraftServer server) {
        TeamProviderApi.API.setSelected(null);
    }
}