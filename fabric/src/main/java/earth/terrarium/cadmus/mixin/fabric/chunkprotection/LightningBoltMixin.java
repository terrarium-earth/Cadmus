package earth.terrarium.cadmus.mixin.fabric.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin {
    // Prevent entities from being affected by lightning in protected chunks
    @Inject(method = "tick", at = @At("HEAD"))
    private void cadmus$tick(CallbackInfo ci) {
        var lightning = (LightningBolt) (Object) this;
        if (ClaimUtils.inProtectedChunk(lightning.getCause(), lightning.blockPosition())) {
            lightning.setVisualOnly(true);
        }
    }
}
