package earth.terrarium.cadmus.mixin.common;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {

    @Inject(method = "addPlayerToTeam", at = @At("HEAD"))
    private void cadmus$addPlayerToTeam(String playerName, PlayerTeam team, CallbackInfoReturnable<Boolean> ci) {
        // TODO
    }

    @Inject(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V", at = @At("HEAD"))
    private void cadmus$removePlayerFromTeam(String string, PlayerTeam playerTeam, CallbackInfo ci) {
        // TODO
    }
}
