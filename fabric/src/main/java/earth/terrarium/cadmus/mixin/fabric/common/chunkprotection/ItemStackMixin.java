package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import earth.terrarium.cadmus.common.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void cadmus$use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!level.isClientSide()) {
            var id = ClaimHandler.getClaim((ServerLevel) level, player.chunkPosition());
            if (id == null) return;
            if (!AdminClaimHandler.getBooleanFlag(level.getServer(), id.getFirst(), ModFlags.USE)) {
                cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(interactionHand)));
            }
        }
    }
}
