package earth.terrarium.cadmus.mixins.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.common.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void cadmus$mayInteract(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var entity = (Entity) (Object) (this);
        if (entity instanceof Player player) {
            if (!ClaimApi.API.canInteractWithBlock(level, pos, InteractionType.WORLD, player)) {
                cir.setReturnValue(false);
            }
        } else {
            if (!ClaimApi.API.canEntityGrief(level, pos, entity)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "canRide", at = @At("RETURN"), cancellable = true)
    private void cadmus$canRide(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && !entity.level().isClientSide) {
            var id = ClaimHandler.getClaim((ServerLevel) entity.level(), entity.chunkPosition());
            if (id == null) return;
            if (!AdminClaimHandler.getBooleanFlag(entity.level().getServer(), id.getFirst(), ModFlags.USE_VEHICLES)) {
                cir.setReturnValue(false);
            }
        }
    }
}
