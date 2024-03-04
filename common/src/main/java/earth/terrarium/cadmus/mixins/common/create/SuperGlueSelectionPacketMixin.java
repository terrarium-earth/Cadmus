package earth.terrarium.cadmus.mixins.common.create;

import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase.Context;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SuperGlueSelectionPacket.class)
public class SuperGlueSelectionPacketMixin {
    @Shadow private BlockPos from;
    @Shadow private BlockPos to;

    @Inject(method = "lambda$handle$0", at = @At("HEAD"), cancellable = true)
    private void handle(Context context, CallbackInfo ci) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        Level level = player.getCommandSenderWorld();
        if (!ClaimApi.API.canInteractWithBlock(level, from, InteractionType.WORLD, player)
            || !ClaimApi.API.canInteractWithBlock(level, to, InteractionType.WORLD, player)) {
            ci.cancel();
        }
    }
}
