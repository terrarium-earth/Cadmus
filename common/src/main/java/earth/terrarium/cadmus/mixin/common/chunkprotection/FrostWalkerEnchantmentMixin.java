package earth.terrarium.cadmus.mixin.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FrostWalkerEnchantment.class)
public abstract class FrostWalkerEnchantmentMixin {
    @Inject(method = "onEntityMoved", at = @At("HEAD"), cancellable = true)
    private static void cadmus$onEntityMoved(LivingEntity living, Level level, BlockPos pos, int levelConflicting, CallbackInfo ci) {
        if (living instanceof Player player && !ClaimApi.API.canPlaceBlock(level, living.blockPosition(), player)) {
            ci.cancel();
        } else if (!ClaimApi.API.canEntityGrief(level, living)) {
            ci.cancel();
        }
    }
}
