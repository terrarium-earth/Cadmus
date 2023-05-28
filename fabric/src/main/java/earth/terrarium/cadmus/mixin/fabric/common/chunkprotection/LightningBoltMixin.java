package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin extends Entity {
    @Shadow
    public abstract @Nullable ServerPlayer getCause();

    public LightningBoltMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // Prevent entities from being affected by lightning in protected chunks
    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V"))
    private boolean cadmus$onThunderHit(Entity entity, ServerLevel level, LightningBolt lightningBolt) {
        var claim = ClaimHandler.getClaim(level, entity.chunkPosition());
        if (claim != null && claim.getFirst().startsWith(ClaimHandler.ADMIN_PREFIX)) {
            return AdminClaimHandler.<Boolean>getFlag(level.getServer(), claim.getFirst(), ModFlags.LIGHTNING);
        }
        if (this.getCause() != null) {
            return ClaimApi.API.canDamageEntity(level, entity, this.getCause());
        }
        return ClaimApi.API.canEntityGrief(level, lightningBolt);
    }
}
