package earth.terrarium.cadmus.common.compat.prometheus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.prometheus.Prometheus;
import earth.terrarium.prometheus.api.roles.options.RoleOption;
import earth.terrarium.prometheus.api.roles.options.RoleOptionSerializer;
import net.minecraft.resources.ResourceLocation;

public record CadmusOptions(int maxClaims, int maxChunkLoaded) implements RoleOption<CadmusOptions> {

    public static final RoleOptionSerializer<CadmusOptions> SERIALIZER = RoleOptionSerializer.of(
        ResourceLocation.fromNamespaceAndPath(Prometheus.MOD_ID, Cadmus.MOD_ID),
        1,
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("maxClaims").orElse(Cadmus.DEFAULT_MAX_CLAIMS).forGetter(CadmusOptions::maxClaims),
            Codec.INT.fieldOf("maxChunkLoaded").orElse(Cadmus.DEFAULT_MAX_CHUNK_LOADED_CLAIMS).forGetter(CadmusOptions::maxChunkLoaded)
        ).apply(instance, CadmusOptions::new)),
        ObjectByteCodec.create(
            ByteCodec.INT.fieldOf(CadmusOptions::maxClaims),
            ByteCodec.INT.fieldOf(CadmusOptions::maxChunkLoaded),
            CadmusOptions::new
        ),
        new CadmusOptions(Cadmus.DEFAULT_MAX_CLAIMS, Cadmus.DEFAULT_MAX_CHUNK_LOADED_CLAIMS)
    );

    @Override
    public RoleOptionSerializer<CadmusOptions> serializer() {
        return SERIALIZER;
    }
}
