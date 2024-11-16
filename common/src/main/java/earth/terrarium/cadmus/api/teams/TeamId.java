package earth.terrarium.cadmus.api.teams;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record TeamId(ResourceLocation providerId, UUID teamId) {
    public static final Codec<TeamId> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("providerId").forGetter(TeamId::providerId),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("teamId").forGetter(TeamId::teamId)
    ).apply(instance, TeamId::new));

    public static final ByteCodec<TeamId> BYTE_CODEC = ObjectByteCodec.create(
        ExtraByteCodecs.RESOURCE_LOCATION.fieldOf(TeamId::providerId),
        ByteCodec.UUID.fieldOf(TeamId::teamId),
        TeamId::new
    );

    public static TeamId ofNew(ResourceLocation providerId) {
        return new TeamId(providerId, UUID.randomUUID());
    }
}
