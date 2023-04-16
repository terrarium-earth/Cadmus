package earth.terrarium.cadmus.mixin.fabric.common.chunkprotection;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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
        if (this.getCause() != null) {
            return ClaimApi.API.canDamageEntity(level, entity, this.getCause());
        }
        return ClaimApi.API.canEntityGrief(level, lightningBolt);
    }
}
