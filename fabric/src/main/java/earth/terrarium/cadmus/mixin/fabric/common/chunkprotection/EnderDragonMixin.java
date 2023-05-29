package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin {
    // Prevent the ender dragon from destroying blocks in protected chunks
    @Inject(method = "checkWalls", at = @At("HEAD"), cancellable = true)
    private void cadmus$checkWalls(AABB area, CallbackInfoReturnable<Boolean> ci) {
        EnderDragon enderDragon = (EnderDragon) (Object) this;
        if (!ClaimApi.API.canEntityGrief(enderDragon.level(), enderDragon)) {
            ci.setReturnValue(false);
        }
    }
}
