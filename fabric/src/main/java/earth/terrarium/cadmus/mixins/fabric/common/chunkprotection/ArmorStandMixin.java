package earth.terrarium.cadmus.mixins.fabric.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
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
        ArmorStand armorStand = (ArmorStand) (Object) this;
        if (!ClaimApi.API.canEntityGrief(armorStand.level(), armorStand)) {
            cir.setReturnValue(true);
        }
    }
}
