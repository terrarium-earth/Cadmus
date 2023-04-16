package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
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

import java.util.Set;
import java.util.UUID;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBlockMixin {
    // Prevent pistons from pushing blocks into protected chunks
    @Inject(method = "triggerEvent", at = @At("HEAD"), cancellable = true)
    private void cadmus$triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir) {
        Direction direction = state.getValue(PistonBaseBlock.FACING);
        ChunkPos chunkPos = new ChunkPos(pos.relative(direction));
        Set<UUID> currentChunkMembers = ClaimApi.API.getClaimMembers(level, new ChunkPos(pos));
        if (!ClaimApi.API.isClaimed(level, chunkPos) && !currentChunkMembers.equals(ClaimApi.API.getClaimMembers(level, chunkPos))) {
            cir.setReturnValue(false);
        }
    }
}
