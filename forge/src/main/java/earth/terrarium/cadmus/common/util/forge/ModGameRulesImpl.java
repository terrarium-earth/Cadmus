package earth.terrarium.cadmus.common.util.forge;

import earth.terrarium.cadmus.mixins.forge.common.GameRulesBooleanValueInvoker;
import earth.terrarium.cadmus.mixins.forge.common.GameRulesIntegerValueInvoker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

import java.util.function.BiConsumer;

public class ModGameRulesImpl {
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRules.register(name, category, type);
    }

    public static GameRules.Type<GameRules.IntegerValue> createIntRule(int defaultValue) {
        return GameRulesIntegerValueInvoker.invokeCreate(defaultValue);
    }

    public static GameRules.Type<GameRules.BooleanValue> createBooleanRule(boolean defaultValue, BiConsumer<MinecraftServer, GameRules.BooleanValue> biConsumer) {
        return GameRulesBooleanValueInvoker.invokeCreate(defaultValue, biConsumer);
    }
}
