package earth.terrarium.cadmus.mixin.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import net.minecraft.core.BlockPos;
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
        var entity = (Object) (this);
        if (entity instanceof Player player) {
            if (!ClaimApi.API.canInteractWithBlock(level, pos, InteractionType.WORLD, player)) {
                cir.setReturnValue(false);
            }
        } else {
            if (!ClaimApi.API.canEntityGrief(level, pos, (Entity) (Object) this)) {
                cir.setReturnValue(false);
            }
        }
    }
}
