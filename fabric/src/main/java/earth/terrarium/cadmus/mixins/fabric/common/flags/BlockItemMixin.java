package earth.terrarium.cadmus.mixins.fabric.common.flags;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.CadmusDataHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void cadmus$place(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (blockPlaceContext.getPlayer() instanceof ServerPlayer player) {
            if (!ClaimApi.API.canPlaceBlock(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), player)) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        } else if (blockPlaceContext.getLevel() instanceof ServerLevel level) {
            if (ClaimApi.API.isClaimed(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
                var claim = ClaimHandler.getClaim(level, new ChunkPos(blockPlaceContext.getClickedPos()));
                if (claim == null) return;
                ClaimSettings settings = CadmusDataHandler.getClaimSettings(level.getServer(), claim.getFirst());
                ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(level.getServer());
                if (!settings.canNonPlayersPlace(defaultSettings)) {
                    cir.setReturnValue(InteractionResult.FAIL);
                }
            }
        }
    }
}
