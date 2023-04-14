package earth.terrarium.cadmus.mixin.fabric.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {
    @Shadow
    private int destroyBlocksTick;

    // Prevent withers from destroying blocks in protected chunks
    @Inject(method = "customServerAiStep", at = @At("HEAD"))
    private void cadmus$customServerAiStep(CallbackInfo ci) {
        var entity = (WitherBoss) (Object) this;
        if (ClaimUtils.inProtectedChunk(entity)) {
            destroyBlocksTick = 20;
        }
    }
}
