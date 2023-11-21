package earth.terrarium.cadmus.common.util.neoforge;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

    @ModifyVariable(method = "dealExplosionDamage", at = @At("STORE"))
    private Iterator<Entity> cadmus$dealExplosionDamage(Iterator<Entity> entities) {
        FireworkRocketEntity entity = ((FireworkRocketEntity) (Object) this);
        Player player = entity.getOwner() instanceof Player ? (Player) entity.getOwner() : null;

        List<Entity> entityList = new ArrayList<>();
        entities.forEachRemaining(entityList::add);

        entityList.removeIf(next ->
            (ClaimApi.API.isClaimed(entity.level(), next.chunkPosition()) &&
                (player == null || !ClaimApi.API.canDamageEntity(entity.level(), next, player))));

        return entityList.iterator();
    }
}
