package earth.terrarium.cadmus.mixins.common.fabric.protections;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.Protections;
import earth.terrarium.cadmus.common.teams.AdminTeamProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin extends Entity {

    public LightningBoltMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // Prevent entities from being affected by lightning in protected chunks
    @WrapWithCondition(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V"
        )
    )
    private boolean cadmus$onThunderHit(Entity entity, ServerLevel level, LightningBolt lightning) {
        if ((lightning.getCause() != null &&
            !Protections.ENTITY_DAMAGE.canDamageEntity(lightning.getCause(), lightning.getCause())) ||
            !Protections.MOB_GRIEFING.canMobGrief(lightning)) {
            return false;
        }

        return ClaimApi.API.getClaim(level, entity.chunkPosition()).map(claim ->
            claim.team().isAdmin() &&
                Flags.LIGHTNING.get(level.getServer(), claim.team().id())
        ).orElse(true);
    }
}
