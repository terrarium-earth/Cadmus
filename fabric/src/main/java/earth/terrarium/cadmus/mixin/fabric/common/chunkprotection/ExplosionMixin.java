package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import com.llamalad7.mixinextras.sugar.Local;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    @Final
    private ObjectArrayList<BlockPos> toBlow;

    @Shadow
    @Final
    private Level level;

    @Shadow
    public abstract @Nullable LivingEntity getIndirectSourceEntity();

    // Prevent explosions from destroying blocks in protected chunks
    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"))
    private void cadmus$explode(CallbackInfo ci, @Local(ordinal = 0) List<Entity> entities) {
        Player player = this.getIndirectSourceEntity() instanceof Player p ? p : null;
        toBlow.removeIf(next -> (ClaimApi.API.canExplodeBlock(level, new ChunkPos(next)) && (player == null || !ClaimApi.API.canExplodeBlock(level, next, (Explosion) (Object) this, player))));
        entities.removeIf(next -> (ClaimApi.API.canExplodeBlock(level, next.chunkPosition()) && (player == null || !ClaimApi.API.canDamageEntity(level, next, player))));
    }
}
