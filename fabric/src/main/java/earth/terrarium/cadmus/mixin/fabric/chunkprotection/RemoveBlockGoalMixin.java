package earth.terrarium.cadmus.mixin.fabric.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RemoveBlockGoal.class)
public abstract class RemoveBlockGoalMixin {
    @Final
    @Shadow
    private Mob removerMob;

    // Prevent zombies from breaking down doors in protected chunks
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void cadmus$canUse(CallbackInfoReturnable<Boolean> ci) {
        if (ClaimUtils.inProtectedChunk(removerMob)) {
            ci.setReturnValue(false);
        }
    }
}
