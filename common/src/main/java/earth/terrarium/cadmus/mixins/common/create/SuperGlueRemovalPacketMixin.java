package earth.terrarium.cadmus.mixins.common.create;

import com.simibubi.create.content.contraptions.glue.SuperGlueRemovalPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SuperGlueRemovalPacket.class)
public class SuperGlueRemovalPacketMixin {
    @Shadow private int entityId;

    @Inject(method = "lambda$handle$0", at = @At("HEAD"), cancellable = true)
    private void handle(SimplePacketBase.Context context, CallbackInfo ci) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        Level level = player.getCommandSenderWorld();
        Entity entity = level.getEntity(entityId);
        if (!ClaimApi.API.canInteractWithEntity(level, entity, player)) {
            ci.cancel();
        }
    }
}
