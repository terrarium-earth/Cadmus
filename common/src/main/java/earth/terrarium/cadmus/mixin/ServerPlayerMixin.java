package earth.terrarium.cadmus.mixin;

import earth.terrarium.cadmus.common.claiming.LastMessageHolder;
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
