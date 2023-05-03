package earth.terrarium.cadmus.common.util.forge;

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

    public static GameRules.Type<GameRules.BooleanValue> createBooleanRule(boolean defaultValue) {
        return GameRulesBooleanValueInvoker.invokeCreate(defaultValue);
    }
}
