package earth.terrarium.cadmus.mixin.fabric.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
    // Prevent armor stands from being affected by explosions in protected chunks
    @Inject(method = "ignoreExplosion", at = @At("HEAD"), cancellable = true)
    private void cadmus$ignoreExplosion(CallbackInfoReturnable<Boolean> cir) {
        if (ClaimUtils.inProtectedChunk((ArmorStand) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
