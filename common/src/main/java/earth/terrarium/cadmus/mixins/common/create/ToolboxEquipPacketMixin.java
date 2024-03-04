package earth.terrarium.cadmus.mixins.common.create;

import com.simibubi.create.content.equipment.toolbox.ToolboxEquipPacket;
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

@Mixin(value = ToolboxEquipPacket.class, remap = false)
public class ToolboxEquipPacketMixin {
    @Shadow private BlockPos toolboxPos;

    @Inject(method = "lambda$handle$1", at = @At("HEAD"), cancellable = true)
    private void onHandle(Context context, CallbackInfo ci) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        Level level = player.getCommandSenderWorld();
        if (!ClaimApi.API.canInteractWithBlock(level, toolboxPos, InteractionType.USE, player)) {
            ci.cancel();
        }
    }
}
