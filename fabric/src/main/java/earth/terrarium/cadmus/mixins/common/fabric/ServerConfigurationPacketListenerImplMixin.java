package earth.terrarium.cadmus.mixins.common.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import earth.terrarium.cadmus.Cadmus;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin {

    @Inject(
        method = "handleConfigurationFinished",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    // The fabric event is called too early before the player is added to the player list which cadmus needs to send packets so invoke here instead.
    private void cadmus$handleConfigurationFinished(ServerboundFinishConfigurationPacket packet, CallbackInfo ci, @Local ServerPlayer serverPlayer) {
        Cadmus.onPlayerJoin(serverPlayer);
    }
}
