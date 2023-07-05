package earth.terrarium.cadmus.mixins.common;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GameRules.class)
public interface GameRulesAccessor {

    @Accessor("rules")
    Map<GameRules.Key<?>, GameRules.Value<?>> rules();

    @Final
    @Mutable
    @Accessor("rules")
    void setRules(Map<GameRules.Key<?>, GameRules.Value<?>> rules);

    @Accessor("GAME_RULE_TYPES")
    static Map<GameRules.Key<?>, GameRules.Type<?>> getAllRules() {
        throw new AssertionError();
    }
}
