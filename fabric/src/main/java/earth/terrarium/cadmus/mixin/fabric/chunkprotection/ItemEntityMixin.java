package earth.terrarium.cadmus.mixin.fabric.chunkprotection;

import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    // Prevent players from picking up items in protected chunks unless they dropped them
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void cadmus$playerTouch(Player player, CallbackInfo ci) {
        Entity owner = ((ItemEntity) (Object) this).getOwner();
        if (ClaimUtils.inProtectedChunk(player) && !Objects.equals(owner, player)) {
            ci.cancel();
        }
    }
}
