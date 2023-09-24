package earth.terrarium.cadmus.mixins.common.create;

import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase.Context;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TrainRelocationPacket.class, remap = false)
public class TrainRelocationPacketMixin {
    @Shadow int entityId;

    @Inject(method = "lambda$handle$2", at = @At("HEAD"), cancellable = true)
    private void onHandle(Context context, CallbackInfo ci) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        Level level = player.getCommandSenderWorld();
        Entity entity = level.getEntity(entityId);
        if (entity == null) return;
        if (!ClaimApi.API.canInteractWithEntity(level, entity, player)
            || !ClaimApi.API.canInteractWithBlock(level, entity.getOnPos(), InteractionType.WORLD, player)) {
            ci.cancel();
        }
    }
}
