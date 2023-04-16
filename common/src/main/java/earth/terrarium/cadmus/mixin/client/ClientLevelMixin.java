package earth.terrarium.cadmus.mixin.client;

import earth.terrarium.cadmus.client.claims.ChunkHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements ChunkHolder {
    @Nullable
    private ChunkPos cadmus$chunkPos;

    @Override
    public ChunkPos cadmus$getChunkPos() {
        return cadmus$chunkPos;
    }

    @Override
    public void cadmus$setChunkPos(ChunkPos chunkPos) {
        cadmus$chunkPos = chunkPos;
    }
}
