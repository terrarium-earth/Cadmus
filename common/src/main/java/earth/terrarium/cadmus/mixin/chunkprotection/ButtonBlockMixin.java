package earth.terrarium.cadmus.mixin.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ButtonBlock.class)
public abstract class ButtonBlockMixin {
    // Prevent projectiles from pressing wooden buttons in protected chunks
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void cadmus$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (ClaimUtils.inProtectedChunk(level, pos)) {
            if (entity instanceof Projectile projectile) {
                if (projectile.getOwner() instanceof Player owner && ClaimUtils.inProtectedChunk(owner, pos)) {
                    ci.cancel();
                }
            }
        }
    }
}
