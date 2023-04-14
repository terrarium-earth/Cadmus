package earth.terrarium.cadmus.mixin.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PointedDripstoneBlock.class, TargetBlock.class})
public abstract class PointedDripstoneBlockMixin {
    // Prevent tridents from destroying dripstone in protected chunks
    @Inject(method = "onProjectileHit", at = @At("HEAD"), cancellable = true)
    private void cadmus$onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile, CallbackInfo ci) {
        if (ClaimUtils.inProtectedChunk(level, projectile.blockPosition())) {
            if (projectile.getOwner() instanceof Player owner && ClaimUtils.inProtectedChunk(owner)) {
                ci.cancel();
            }
        }
    }
}
