package earth.terrarium.cadmus.mixin.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ArmorStand.class, ItemFrame.class, HangingEntity.class})
public abstract class HangingEntityMixin extends Entity {
    public HangingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // Prevent armor stands and hanging entities from being damaged by projectiles and explosions in protected chunks
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void cadmus$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof Projectile projectile) {
            if (projectile.getOwner() instanceof Player player) {
                if (!ClaimApi.API.canDamageEntity(player.level(), this, player)) {
                    cir.setReturnValue(false);
                }
            } else if (projectile.getOwner() != null && !ClaimApi.API.canEntityGrief(level(), projectile.getOwner())) {
                cir.setReturnValue(false);
            }
        } else if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            if (!ClaimApi.API.isClaimed(level(), blockPosition())) {
                cir.setReturnValue(false);
            }
        }
    }
}
