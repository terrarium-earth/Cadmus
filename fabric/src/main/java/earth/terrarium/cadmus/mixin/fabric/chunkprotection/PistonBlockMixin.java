package earth.terrarium.cadmus.mixin.fabric.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBlockMixin {
    // Prevent pistons from pushing blocks into protected chunks
    @Inject(method = "triggerEvent", at = @At("HEAD"), cancellable = true)
    private void cadmus$triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> ci) {
        Direction direction = state.getValue(PistonBaseBlock.FACING);
        if (ClaimUtils.inProtectedChunk(level, new BlockPos(
                pos.getX() + direction.getStepX() * 16,
                pos.getY() + direction.getStepY() * 16,
                pos.getZ() + direction.getStepZ() * 16))
        ) {
            ci.setReturnValue(false);
        }
    }
}
