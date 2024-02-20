package earth.terrarium.cadmus;

import com.teamresourceful.resourcefullib.common.utils.modinfo.ModInfoUtils;
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
import earth.terrarium.cadmus.common.util.AdminUtils;
import earth.terrarium.cadmus.common.util.ModGameRules;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;

public class Cadmus {
    public static final String MOD_ID = "cadmus";
    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(MOD_ID, "default");

    public static int FORCE_LOADED_CHUNK_COUNT;
    public static final TagKey<Block> ALLOWS_CLAIM_INTERACTIONS = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "allows_claim_interactions"));
    public static final TagKey<Item> ALLOWS_CLAIM_PICKUP = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "allows_claim_pickup"));

    //platform tags
    public static final TagKey<Block> INTERACTABLE_STORAGE = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "interactable_storage"));
    public static final TagKey<Block> REDSTONE = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "redstone"));
    public static final TagKey<Block> DOOR_LIKE = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "door_like"));

    public static void init() {
        NetworkHandler.init();
        TeamProviderApi.API.register(DEFAULT_ID, new VanillaTeamProvider());
        MaxClaimProviderApi.API.register(DEFAULT_ID, new CadmusMaxClaimProvider());
        ModGameRules.init();
        ModFlags.init();
        if (!ModInfoUtils.isModLoaded("argonauts")) {
            TeamProviderApi.API.setSelected(DEFAULT_ID);
        }

        if (PrometheusIntegration.prometheusLoaded()) {
            PrometheusIntegration.register();
        } else {
            MaxClaimProviderApi.API.setSelected(DEFAULT_ID);
        }
    }

    public static void enterChunkSection(Player player, ChunkPos pos) {
        if (player.level().isClientSide()) {
            CadmusClient.enterChunkSection();
        } else {
            ModUtils.displayTeamName((ServerPlayer) player, pos);
            AdminUtils.checkAccess((ServerPlayer) player, pos);
        }
    }

    public static void serverStarted(MinecraftServer server) {
        // Set chunk loaded chunks
        Cadmus.FORCE_LOADED_CHUNK_COUNT = 0;
        server.getAllLevels().forEach(level ->
            ClaimHandler.getAllTeamClaims(level).forEach((id, data) ->
                data.forEach((pos, type) -> {
                    if (type == ClaimType.CHUNK_LOADED) {
                        Cadmus.FORCE_LOADED_CHUNK_COUNT++;
                        level.getChunkSource().updateChunkForced(pos, true);
                    }
                })));
        // Initialize the data handler
        CadmusDataHandler.read(server);
    }
}