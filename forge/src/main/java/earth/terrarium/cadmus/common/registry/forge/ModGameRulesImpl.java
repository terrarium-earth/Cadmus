package earth.terrarium.cadmus.common.registry.forge;

import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.client.SyncGameRulePacket;
import earth.terrarium.cadmus.mixin.forge.common.GameRulesBooleanValueInvoker;
import earth.terrarium.cadmus.mixin.forge.common.GameRulesIntegerValueInvoker;
import net.minecraft.world.level.GameRules;

public class ModGameRulesImpl {
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRules.register(name, category, type);
    }

    public static GameRules.Type<GameRules.IntegerValue> createIntRule(int defaultValue) {
        return GameRulesIntegerValueInvoker.invokeCreate(defaultValue);
    }

    public static GameRules.Type<GameRules.BooleanValue> createBooleanRule(boolean defaultValue, byte packetId) {
        return GameRulesBooleanValueInvoker.invokeCreate(defaultValue, (server, rule) ->
            NetworkHandler.CHANNEL.sendToAllPlayers(new SyncGameRulePacket(packetId, rule.get()), server));
    }
}