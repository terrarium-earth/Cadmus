package earth.terrarium.cadmus.mixin.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterlilyBlock.class)
public abstract class WaterlilyBlockMixin {
    // Prevent boats from destroying lily pads in protected chunks
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void cadmus$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof Boat boat) {
            if (boat.getFirstPassenger() instanceof Player player) {
                if (!ClaimApi.API.canBreakBlock(level, pos, player)) {
                    ci.cancel();
                }
            } else if (!ClaimApi.API.canEntityGrief(level, pos, entity)) {
                ci.cancel();
            }
        }
    }
}
