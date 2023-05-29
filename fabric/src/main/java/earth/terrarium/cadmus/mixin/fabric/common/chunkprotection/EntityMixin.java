package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    // Prevent entities from being damaged by players in protected chunks
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void cadmus$isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> ci) {
        if (source.getEntity() instanceof Player player) {
            if (!ClaimApi.API.canDamageEntity(player.level(), (Entity) (Object) this, player)) {
                ci.setReturnValue(false);
            }
        }
    }
}
