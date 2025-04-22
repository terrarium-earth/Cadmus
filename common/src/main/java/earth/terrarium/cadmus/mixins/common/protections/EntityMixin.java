package earth.terrarium.cadmus.mixins.common.protections;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.Protections;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyReturnValue(method = "canRide", at = @At("RETURN"))
    private boolean cadmus$canRide(boolean original, Entity vehicle) {
        return original && !vehicle.level().isClientSide() ?
            ClaimApi.API.getClaim(vehicle.level(), vehicle.chunkPosition())
                .map(claim -> Flags.USE_VEHICLES.get(vehicle.getServer(), claim.team().id()))
                .orElse(true) :
            original;
    }

    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void cadmus$mayInteract(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var entity = (Entity) (Object) (this);
        if (entity instanceof Player player) {
            if (!Protections.BLOCK_INTERACTIONS.canInteractWithBlock(player, pos, level.getBlockState(pos))) {
                cir.setReturnValue(false);
            }
        } else {
            if (!Protections.MOB_GRIEFING.canMobGrief(entity, pos)) {
                cir.setReturnValue(false);
            }
        }
    }
}
