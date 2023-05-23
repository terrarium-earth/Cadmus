package earth.terrarium.cadmus.common.util.fabric;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

import java.util.function.BiConsumer;

public class ModGameRulesImpl {
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRuleRegistry.register(name, category, type);
    }

    public static GameRules.Type<GameRules.IntegerValue> createIntRule(int defaultValue) {
        return GameRuleFactory.createIntRule(defaultValue);
    }

    public static GameRules.Type<GameRules.BooleanValue> createBooleanRule(boolean defaultValue, BiConsumer<MinecraftServer, GameRules.BooleanValue> biConsumer) {
        return GameRuleFactory.createBooleanRule(defaultValue, biConsumer);
    }
}
