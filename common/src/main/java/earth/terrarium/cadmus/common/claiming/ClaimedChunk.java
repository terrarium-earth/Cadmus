package earth.terrarium.cadmus.common.claiming;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Set;

public record ClaimedChunk(ChunkPos pos, ClaimType type) {
    public static void encode(Set<ClaimedChunk> chunks, FriendlyByteBuf buf) {
        buf.writeVarInt(chunks.size());
        chunks.forEach(claimedChunks -> {
            buf.writeVarLong(claimedChunks.pos().toLong());
            buf.writeEnum(claimedChunks.type());
        });
    }

    public static Set<ClaimedChunk> decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<ClaimedChunk> chunks = new HashSet<>();
        for (int i = 0; i < size; i++) {
            ChunkPos chunkPos = new ChunkPos(buf.readVarLong());
            ClaimType claimType = buf.readEnum(ClaimType.class);
            chunks.add(new ClaimedChunk(chunkPos, claimType));
        }
        return chunks;
    }
}
