package earth.terrarium.cadmus.common.util;

import com.teamresourceful.resourcefullib.common.utils.modinfo.ModInfoUtils;
import dev.architectury.injectables.annotations.ExpectPlatform;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.common.claims.CadmusDataHandler;
import earth.terrarium.cadmus.mixins.common.GameRulesAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class ModGameRules {
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_CLAIMED_CHUNKS = register("maxClaimedChunks", GameRules.Category.MISC, createIntRule(1089));
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_CHUNK_LOADED = register("maxChunkLoaded", GameRules.Category.MISC, createIntRule(64));

    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_BLOCK_BREAKING = register("doClaimedBlockBreaking", GameRules.Category.MISC, createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_BLOCK_PLACING = register("doClaimedBlockPlacing", GameRules.Category.MISC, createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_BLOCK_EXPLOSIONS = register("doClaimedBlockExplosions", GameRules.Category.MISC, createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_BLOCK_INTERACTIONS = register("doClaimedBlockInteractions", GameRules.Category.MISC, createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_ENTITY_INTERACTIONS = register("doClaimedEntityInteractions", GameRules.Category.MISC, createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_CLAIMED_DAMAGE_ENTITIES = register("doClaimedDamageEntities", GameRules.Category.MISC, createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_CLAIMED_MOB_GRIEFING = register("claimedMobGriefing", GameRules.Category.MISC, createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_CAN_PICKUP_CLAIMED_ITEMS = register("canPickupClaimedItems", GameRules.Category.MISC, createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanValue> DO_COMBINED_CLAIM_LIMIT = registerIfPrometheusInstalled("doCombinedClaimLimit", GameRules.Category.MISC, createBooleanRule(false, (server, rule) ->
        CadmusDataHandler.getMaxTeamClaims(server).keySet().forEach(id -> MaxClaimProviderApi.API.getSelected().calculate(id, server))));

    public static void init() {
    }

    public static <T extends GameRules.Value<T>> GameRules.Key<T> registerIfPrometheusInstalled(String name, GameRules.Category category, GameRules.Type<T> type) {
        return ModInfoUtils.isModLoaded("prometheus") ? register(name, category, type) : null;
    }

    @ExpectPlatform
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        throw new NotImplementedException();
    }

    @ExpectPlatform
    public static GameRules.Type<GameRules.IntegerValue> createIntRule(int defaultValue) {
        throw new NotImplementedException();
    }

    public static GameRules.Type<GameRules.BooleanValue> createBooleanRule(boolean defaultValue) {
        return createBooleanRule(defaultValue, (server, rule) -> {});
    }

    @ExpectPlatform
    public static GameRules.Type<GameRules.BooleanValue> createBooleanRule(boolean defaultValue, BiConsumer<MinecraftServer, GameRules.BooleanValue> biConsumer) {
        throw new NotImplementedException();
    }

    public static int getOrCreateIntGameRule(Level level, GameRules.Key<GameRules.IntegerValue> key) {
        GameRulesAccessor gameRules = (GameRulesAccessor) level.getGameRules();
        if (!gameRules.rules().containsKey(key)) {
            copyAndAddRule(gameRules, key);
        }
        return ((GameRules.IntegerValue) gameRules.rules().get(key)).get();
    }

    public static boolean getOrCreateBooleanGameRule(Level level, GameRules.Key<GameRules.BooleanValue> key) {
        GameRulesAccessor gameRules = (GameRulesAccessor) level.getGameRules();
        if (!gameRules.rules().containsKey(key)) {
            copyAndAddRule(gameRules, key);
        }
        return ((GameRules.BooleanValue) gameRules.rules().get(key)).get();
    }

    private static void copyAndAddRule(GameRulesAccessor gameRules, GameRules.Key<?> key) {
        HashMap<GameRules.Key<?>, GameRules.Value<?>> rules = new HashMap<>(gameRules.rules());
        rules.put(key, GameRulesAccessor.getAllRules().get(key).createRule());
        gameRules.setRules(rules);
    }
}
