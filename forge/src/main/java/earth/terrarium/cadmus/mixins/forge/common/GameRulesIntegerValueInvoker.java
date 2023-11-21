package earth.terrarium.cadmus.mixins.forge.common;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.IntegerValue.class)
public interface GameRulesIntegerValueInvoker {
    @Invoker
    static GameRules.Type<GameRules.IntegerValue> invokeCreate(int i) {
        throw new AssertionError();
    }
}
