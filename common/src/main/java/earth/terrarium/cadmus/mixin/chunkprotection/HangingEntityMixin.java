package earth.terrarium.cadmus.mixin.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ArmorStand.class, ItemFrame.class, HangingEntity.class})
public abstract class HangingEntityMixin {
    // Prevent armor stands and hanging entities from being damaged by projectiles and explosions in protected chunks
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void cadmus$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        var entity = (Entity) (Object) this;
        if (source.getEntity() instanceof Projectile projectile && projectile.getOwner() instanceof Player player) {
            if (ClaimUtils.inProtectedChunk(player, entity.blockPosition())) {
                cir.setReturnValue(false);
            }
        } else if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            if (ClaimUtils.inProtectedChunk(entity)) {
                cir.setReturnValue(false);
            }
        }
    }
}
