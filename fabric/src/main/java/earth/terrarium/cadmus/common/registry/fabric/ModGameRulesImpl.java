package earth.terrarium.cadmus.common.registry.fabric;

import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.client.SyncGameRulePacket;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public class ModGameRulesImpl {

    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRuleRegistry.register(name, category, type);
    }

    public static GameRules.Type<GameRules.IntegerValue> createIntRule(int defaultValue) {
        return GameRuleFactory.createIntRule(defaultValue);
    }

    public static GameRules.Type<GameRules.BooleanValue> createBooleanRule(boolean defaultValue, byte packetId) {
        return GameRuleFactory.createBooleanRule(defaultValue, (server, rule) ->
            NetworkHandler.CHANNEL.sendToAllPlayers(new SyncGameRulePacket(packetId, rule.get()), server));
    }
}
