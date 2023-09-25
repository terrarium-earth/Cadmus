package earth.terrarium.cadmus.mixins.common.create;

import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotatoProjectileEntity.class)
public class PotatoProjectileEntityMixin {
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onHitEntity(EntityHitResult ray, CallbackInfo ci) {
        PotatoProjectileEntity entity = (PotatoProjectileEntity) (Object) this;
        Level level = entity.getCommandSenderWorld();
        if (!ClaimApi.API.canEntityGrief(level, entity)) {
            ci.cancel();
        }
    }
}
