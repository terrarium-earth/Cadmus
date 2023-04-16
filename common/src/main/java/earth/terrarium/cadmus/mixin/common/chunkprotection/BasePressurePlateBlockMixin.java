package earth.terrarium.cadmus.mixin.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
        if (entity instanceof Player player) {
            if (!ClaimApi.API.canInteractWithBlock(level, pos, InteractionType.WORLD, player)) {
                ci.cancel();
            }
        } else if (!ClaimApi.API.canEntityGrief(level, pos, entity)) {
            ci.cancel();
        }
    }
}
