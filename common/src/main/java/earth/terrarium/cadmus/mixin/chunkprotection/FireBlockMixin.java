package earth.terrarium.cadmus.mixin.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    // Prevent fire from spreading in protected chunks
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cadmus$tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (ClaimUtils.inProtectedChunk(level, pos)) {
            level.scheduleTick(pos, (FireBlock) (Object) this, invokeGetFireTickDelay(level.random));
            ci.cancel();
        }
    }

    @Invoker
    private static int invokeGetFireTickDelay(RandomSource random) {
        throw new AssertionError();
    }
}
