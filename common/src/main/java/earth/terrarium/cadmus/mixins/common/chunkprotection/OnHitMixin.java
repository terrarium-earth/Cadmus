package earth.terrarium.cadmus.mixins.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractArrow.class, ThrownTrident.class})
public abstract class OnHitMixin {
    // Prevent player-thrown projectiles from hurting entities in protected chunks
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void cadmus$onHitEntity(EntityHitResult result, CallbackInfo ci) {
        if (((AbstractArrow) (Object) this).getOwner() instanceof Player player) {
            if (!ClaimApi.API.canDamageEntity(player.level(), result.getEntity(), player)) {
                ci.cancel();
            }
        }
    }
}
