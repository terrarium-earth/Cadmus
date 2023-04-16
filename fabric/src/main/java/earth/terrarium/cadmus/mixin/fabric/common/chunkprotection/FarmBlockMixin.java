package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public abstract class FarmBlockMixin {
    // Prevent players from trampling crops in protected chunks
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void cadmus$fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        if (entity instanceof Player player) {
            if (!ClaimApi.API.canBreakBlock(player.level, pos, player)) {
                ci.cancel();
            }
        } else if (!ClaimApi.API.canEntityGrief(level, pos, entity)) {
            ci.cancel();
        }
    }
}
