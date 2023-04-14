package earth.terrarium.cadmus.mixin.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void cadmus$mayInteract(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (ClaimUtils.inProtectedChunk((Entity) (Object) this, pos)) {
            cir.setReturnValue(false);
        }
    }
}
