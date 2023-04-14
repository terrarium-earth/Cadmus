package earth.terrarium.cadmus.mixin.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BasePressurePlateBlock.class)
public abstract class BasePressurePlateBlockMixin {
    // Prevent players from activating pressure plates in protected chunks
    @Inject(method = "checkPressed", at = @At("HEAD"), cancellable = true)
    private void cadmus$checkPressed(@Nullable Entity entity, Level level, BlockPos pos, BlockState state, int currentSignal, CallbackInfo ci) {
        if (ClaimUtils.inProtectedChunk(entity, pos)) {
            ci.cancel();
        }
    }
}
