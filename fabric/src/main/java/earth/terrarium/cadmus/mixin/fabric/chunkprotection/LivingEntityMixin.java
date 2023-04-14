package earth.terrarium.cadmus.mixin.fabric.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // Prevent entities from being damaged by players in protected chunks
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void cadmus$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        if (source.getEntity() instanceof Player && ClaimUtils.inProtectedChunk(source.getEntity())) {
            ci.setReturnValue(false);
        }
    }
}
