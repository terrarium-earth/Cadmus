package earth.terrarium.cadmus.mixin.common;

import earth.terrarium.cadmus.common.claims.LastMessageHolder;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements LastMessageHolder {
    @Nullable
    private String cadmus$lastMessage;

    @Override
    public String cadmus$getLastMessage() {
        return cadmus$lastMessage;
    }

    @Override
    public void cadmus$setLastMessage(String message) {
        cadmus$lastMessage = message;
    }
}
