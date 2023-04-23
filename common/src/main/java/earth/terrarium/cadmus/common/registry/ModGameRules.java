package earth.terrarium.cadmus.common.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import earth.terrarium.cadmus.common.network.messages.client.SyncGameRulePacket;
import net.minecraft.world.level.GameRules;
import org.apache.commons.lang3.NotImplementedException;

public class ModGameRules {
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_CLAIMED_CHUNKS = register("maxClaimedChunks", GameRules.Category.MISC, createIntRule(1089));
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_CHUNK_LOADED = register("maxChunkLoaded", GameRules.Category.MISC, createIntRule(64));

    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_BLOCK_BREAKING = register("doClaimedBlockBreaking", GameRules.Category.MISC, createBooleanRule(false, SyncGameRulePacket.DO_CLAIMED_BLOCK_BREAKING));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_BLOCK_PLACING = register("doClaimedBlockPlacing", GameRules.Category.MISC, createBooleanRule(false, SyncGameRulePacket.DO_CLAIMED_BLOCK_PLACING));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_BLOCK_EXPLOSIONS = register("doClaimedBlockExplosions", GameRules.Category.MISC, createBooleanRule(false, SyncGameRulePacket.DO_CLAIMED_BLOCK_EXPLOSIONS));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_BLOCK_INTERACTIONS = register("doClaimedBlockInteractions", GameRules.Category.MISC, createBooleanRule(false, SyncGameRulePacket.DO_CLAIMED_BLOCK_INTERACTIONS));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_CLAIMED_ENTITY_INTERACTIONS = register("doClaimedEntityInteractions", GameRules.Category.MISC, createBooleanRule(false, SyncGameRulePacket.DO_CLAIMED_ENTITY_INTERACTIONS));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_CLAIMED_DAMAGE_ENTITIES = register("doClaimedDamageEntities", GameRules.Category.MISC, createBooleanRule(false, SyncGameRulePacket.CLAIMED_DAMAGE_ENTITIES));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_CLAIMED_MOB_GRIEFING = register("claimedMobGriefing", GameRules.Category.MISC, createBooleanRule(false, SyncGameRulePacket.CLAIMED_MOB_GRIEFING));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_CAN_PICKUP_CLAIMED_ITEMS = register("canPickupClaimedItems", GameRules.Category.MISC, createBooleanRule(false, SyncGameRulePacket.CAN_PICKUP_CLAIMED_ITEMS));

    public static void init() {
    }

    @ExpectPlatform
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        throw new NotImplementedException();
    }

    @ExpectPlatform
    public static GameRules.Type<GameRules.IntegerValue> createIntRule(int defaultValue) {
        throw new NotImplementedException();
    }

    @ExpectPlatform
    public static GameRules.Type<GameRules.BooleanValue> createBooleanRule(boolean defaultValue, byte packetId) {
        throw new NotImplementedException();
    }
}
