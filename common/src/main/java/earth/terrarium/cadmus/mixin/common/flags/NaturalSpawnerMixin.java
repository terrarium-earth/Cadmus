package earth.terrarium.cadmus.mixin.common.flags;

import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @Inject(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V", at = @At("HEAD"), cancellable = true)
    private static void cadmus$spawnCategoryForPosition(MobCategory category, ServerLevel level, ChunkAccess chunk, BlockPos pos, NaturalSpawner.SpawnPredicate filter, NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci) {
        if ((category == MobCategory.CREATURE
            || category == MobCategory.AMBIENT
            || category == MobCategory.AXOLOTLS
            || category == MobCategory.UNDERGROUND_WATER_CREATURE
            || category == MobCategory.WATER_AMBIENT)
            && !AdminClaimHandler.<Boolean>getFlag(level, chunk.getPos(), ModFlags.CREATURE_SPAWNING)) {
            ci.cancel();
        } else if (category == MobCategory.MONSTER && !AdminClaimHandler.<Boolean>getFlag(level, chunk.getPos(), ModFlags.MONSTER_SPAWNING)) {
            ci.cancel();
        } else if (!AdminClaimHandler.<Boolean>getFlag(level, chunk.getPos(), ModFlags.MOB_SPAWNING)) {
            ci.cancel();
        }
    }
}
