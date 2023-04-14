package earth.terrarium.cadmus.mixin.fabric.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    @Final
    private ObjectArrayList<BlockPos> toBlow;

    @Shadow
    @Final
    private Level level;

    // Prevent explosions from destroying blocks in protected chunks
    @Inject(method = "finalizeExplosion", at = @At("HEAD"))
    private void cadmus$finalizeExplosion(boolean spawnParticles, CallbackInfo ci) {
        for (var blockPos : toBlow) {
            if (ClaimUtils.inProtectedChunk(level, blockPos)) {
                toBlow.clear();
                break;
            }
        }
    }
}
