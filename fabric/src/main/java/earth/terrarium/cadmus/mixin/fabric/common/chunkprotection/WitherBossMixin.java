package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
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
        WitherBoss wither = (WitherBoss) (Object) this;
        if (!ClaimApi.API.canEntityGrief(wither.level, wither)) {
            destroyBlocksTick = 20;
        }
    }
}
