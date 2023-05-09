package earth.terrarium.cadmus.mixin.common;

import earth.terrarium.cadmus.common.claims.LastMessageHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements LastMessageHolder {
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
}
