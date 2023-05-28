package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

    @ModifyVariable(method = "dealExplosionDamage", at = @At("STORE"))
    private List<LivingEntity> cadmus$dealExplosionDamage(List<LivingEntity> entities) {
        FireworkRocketEntity entity = ((FireworkRocketEntity) (Object) this);
        Player player = entity.getOwner() instanceof Player p ? p : null;
        entities.removeIf(next -> (ClaimApi.API.isClaimed(entity.level, next.chunkPosition()) && (player == null || !ClaimApi.API.canDamageEntity(entity.level, next, player))));
        return entities;
    }
}
