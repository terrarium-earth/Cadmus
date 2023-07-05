package earth.terrarium.cadmus.mixins.fabric.common.chunkprotection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
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
    private void cadmus$triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir) {
        Direction direction = state.getValue(PistonBaseBlock.FACING);
        ChunkPos chunkPos = new ChunkPos(pos.relative(direction));

        // TODO
    }
}
