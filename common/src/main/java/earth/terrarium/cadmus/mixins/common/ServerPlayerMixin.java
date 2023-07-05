package earth.terrarium.cadmus.mixins.common;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.common.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.util.LastMessageHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements LastMessageHolder {

    @Shadow
    public abstract boolean hurt(DamageSource arg, float f);

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Nullable
    private Component cadmus$lastMessage;

    @Override
    public Component cadmus$getLastMessage() {
        return cadmus$lastMessage;
    }

    @Override
    public void cadmus$setLastMessage(Component message) {
        cadmus$lastMessage = message;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.level().getGameTime() % 20 == 0) {
            float healRate = AdminClaimHandler.<Float>getFlag((ServerLevel) this.level(), this.chunkPosition(), ModFlags.HEAL_RATE);
            if (healRate > 0) {
                this.heal(healRate);
            } else if (healRate < 0) {
                this.hurt(this.damageSources().generic(), -healRate);
            }

            float feedRate = AdminClaimHandler.<Float>getFlag((ServerLevel) this.level(), this.chunkPosition(), ModFlags.FEED_RATE);
            if (feedRate > 0) {
                if (feedRate > this.random.nextFloat()) {
                    this.getFoodData().eat((int) Math.ceil(feedRate), feedRate);
                }
            }
        }
    }

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true)
    private void cadmus$teleportTo(double x, double y, double z, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (!AdminClaimHandler.getBooleanFlag(player.serverLevel(), new ChunkPos(BlockPos.containing(x, y, z)), ModFlags.ALLOW_ENTRY)) {
            Component message = AdminClaimHandler.getFlag(player.serverLevel(), player.chunkPosition(), ModFlags.ENTRY_DENY_MESSAGE);
            if (!message.getString().isBlank()) {
                player.displayClientMessage(message.copy().withStyle(ChatFormatting.RED), false);
            }
            ci.cancel();
        }

        if (!AdminClaimHandler.getBooleanFlag(player.serverLevel(), player.chunkPosition(), ModFlags.ALLOW_EXIT)) {
            ci.cancel();
        }
    }

    @Inject(method = "restoreFrom", at = @At(value = "HEAD", target = "Lnet/minecraft/server/level/ServerPlayer;onUpdateAbilities()V"))
    private void cadmus$restoreFrom(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        if (!keepEverything && AdminClaimHandler.getBooleanFlag(that.serverLevel(), that.chunkPosition(), ModFlags.KEEP_INVENTORY)) {
            this.getInventory().replaceWith(that.getInventory());
            this.experienceLevel = that.experienceLevel;
            this.totalExperience = that.totalExperience;
            this.experienceProgress = that.experienceProgress;
            this.setScore(that.getScore());
        }
    }
}
