package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.networking.base.CodecPacketHandler;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;

public record ServerboundUpdateClaimedChunksPacket(Map<ChunkPos, ClaimType> addedChunks,
                                                   Map<ChunkPos, ClaimType> removedChunks) implements Packet<ServerboundUpdateClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "update_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ServerboundUpdateClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler extends CodecPacketHandler<ServerboundUpdateClaimedChunksPacket> {
        public Handler() {
            super(ObjectByteCodec.create(
                new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ClaimType.CODEC).fieldOf(ServerboundUpdateClaimedChunksPacket::addedChunks),
                new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ClaimType.CODEC).fieldOf(ServerboundUpdateClaimedChunksPacket::removedChunks),
                ServerboundUpdateClaimedChunksPacket::new
            ));
        }

        @Override
        public PacketContext handle(ServerboundUpdateClaimedChunksPacket message) {
            return (player, level) -> {
                message.addedChunks.forEach((pos, type) -> {
                    if (ClaimApi.API.canClaim((ServerLevel) level, pos, type == ClaimType.CHUNK_LOADED, (ServerPlayer) player)) {
                        ClaimApi.API.claim((ServerLevel) level, pos, type == ClaimType.CHUNK_LOADED, (ServerPlayer) player);
                    }
                });

                message.removedChunks.forEach((pos, type) ->
                    ClaimApi.API.unclaim((ServerLevel) level, pos, (ServerPlayer) player));
            };
        }
    }
}
