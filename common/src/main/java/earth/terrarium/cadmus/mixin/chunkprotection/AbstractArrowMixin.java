package earth.terrarium.cadmus.mixin.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractArrow.class, ThrownTrident.class})
public abstract class AbstractArrowMixin {
    // Prevent player-thrown projectiles from hurting entities in protected chunks
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void cadmus$onHitEntity(EntityHitResult result, CallbackInfo ci) {
        if (ClaimUtils.inProtectedChunk(((AbstractArrow) (Object) this).getOwner(), result.getEntity().blockPosition())) {
            ci.cancel();
        }
    }
}
