package earth.terrarium.cadmus.mixin.fabric.common;

import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Nullable
    private ChunkPos cadmus$lastChunkPos;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void cadmus$travel(Vec3 vec3, CallbackInfo ci) {
        if (!this.level.isClientSide) {
            var pos = this.chunkPosition();
            // check if player has entered new chunk
            if (!Objects.equals(pos, cadmus$lastChunkPos)) {
                cadmus$lastChunkPos = pos;
                ModUtils.enterChunkSection((ServerPlayer) (Object) this);
            }
        }
    }
}
