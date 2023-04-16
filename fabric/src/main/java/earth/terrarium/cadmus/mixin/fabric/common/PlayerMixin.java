package earth.terrarium.cadmus.mixin.fabric.common;

import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Nullable
    private ChunkPos cadmus$lastChunkPos;

    @Inject(method = "travel", at = @At("HEAD"))
    private void cadmus$travel(Vec3 vec3, CallbackInfo ci) {
        var player = (Player) (Object) this;
        if (!player.level.isClientSide) {
            var pos = player.chunkPosition();
            // check if player has entered new chunk
            if (!Objects.equals(pos, cadmus$lastChunkPos)) {
                cadmus$lastChunkPos = pos;
                ModUtils.displayTeamName((ServerPlayer) player);
            }
        }
    }
}
