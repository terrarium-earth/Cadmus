package earth.terrarium.cadmus.mixin.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin extends Block {
    public FireBlockMixin(Properties properties) {
        super(properties);
    }

    // Prevent fire from spreading in protected chunks
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cadmus$tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (ClaimApi.API.isClaimed(level, pos)) {
            level.scheduleTick(pos, this, invokeGetFireTickDelay(level.random));
            ci.cancel();
        }
    }

    @Invoker
    private static int invokeGetFireTickDelay(RandomSource random) {
        throw new AssertionError();
    }
}
