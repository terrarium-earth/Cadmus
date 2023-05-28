package earth.terrarium.cadmus.mixin.common.flags;

import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public abstract class IceBlockMixin {
    @Inject(method = "melt", at = @At(value = "HEAD"), cancellable = true)
    private void cadmus$melt(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (!level.isClientSide()) {
            if (!AdminClaimHandler.<Boolean>getFlag((ServerLevel) level, new ChunkPos(pos), ModFlags.ICE_MELT)) {
                ci.cancel();
            }
        }
    }
}
