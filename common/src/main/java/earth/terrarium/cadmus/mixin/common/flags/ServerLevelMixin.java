package earth.terrarium.cadmus.mixin.common.flags;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @WrapWithCondition(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 2))
    private boolean cadmus$tickChunk(ServerLevel level, BlockPos pos, BlockState state, LevelChunk chunk, int randomTickSpeed) {
        return AdminClaimHandler.<Boolean>getFlag(level, new ChunkPos(pos), ModFlags.SNOW_FALL);
    }

    @WrapWithCondition(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean cadmus$tickChunkIce(ServerLevel level, BlockPos pos, BlockState state, LevelChunk chunk, int randomTickSpeed) {
        return AdminClaimHandler.<Boolean>getFlag(level, new ChunkPos(pos), ModFlags.ICE_FORM);
    }
}
