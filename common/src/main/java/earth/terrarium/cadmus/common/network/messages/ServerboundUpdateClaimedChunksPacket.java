package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.function.Consumer;

public record ServerboundUpdateClaimedChunksPacket(Map<ChunkPos, ClaimType> addedChunks,
                                                   Map<ChunkPos, ClaimType> removedChunks) implements Packet<ServerboundUpdateClaimedChunksPacket> {

    public static final ServerboundPacketType<ServerboundUpdateClaimedChunksPacket> TYPE = new Type();

    @Override
    public PacketType<ServerboundUpdateClaimedChunksPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ServerboundUpdateClaimedChunksPacket> implements ServerboundPacketType<ServerboundUpdateClaimedChunksPacket> {

        public Type() {
            super(
                ServerboundUpdateClaimedChunksPacket.class,
                new ResourceLocation(Cadmus.MOD_ID, "update_claimed_chunks"),
                ObjectByteCodec.create(
                    new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ClaimType.CODEC).fieldOf(ServerboundUpdateClaimedChunksPacket::addedChunks),
                    new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ClaimType.CODEC).fieldOf(ServerboundUpdateClaimedChunksPacket::removedChunks),
                    ServerboundUpdateClaimedChunksPacket::new
                )
            );
        }

        @Override
        public Consumer<Player> handle(ServerboundUpdateClaimedChunksPacket packet) {
            return player -> {
                ServerLevel level = (ServerLevel) player.level();
                packet.addedChunks().forEach((pos, type) -> {
                    if (ClaimApi.API.canClaim(level, pos, type == ClaimType.CHUNK_LOADED, (ServerPlayer) player)) {
                        ClaimApi.API.claim(level, pos, type == ClaimType.CHUNK_LOADED, (ServerPlayer) player);
                    }
                });

                packet.removedChunks().forEach((pos, type) ->
                    ClaimApi.API.unclaim(level, pos, (ServerPlayer) player));
            };
        }
    }
}
