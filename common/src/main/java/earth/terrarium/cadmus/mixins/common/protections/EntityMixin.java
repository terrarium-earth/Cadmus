package earth.terrarium.cadmus.mixins.common.protections;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.flags.Flags;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyReturnValue(method = "canRide", at = @At("RETURN"))
    private boolean cadmus$canRide(boolean original, Entity vehicle) {
        return original && !vehicle.level().isClientSide() ?
            ClaimApi.API.getClaim(vehicle.level(), vehicle.chunkPosition())
                .map(claim -> Flags.USE_VEHICLES.get(vehicle.getServer(), claim.first().id()))
                .orElse(true) :
            original;
    }
}
