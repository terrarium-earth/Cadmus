package earth.terrarium.cadmus.common.compat.prometheus.roles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.prometheus.Prometheus;
import earth.terrarium.prometheus.api.roles.options.RoleOption;
import earth.terrarium.prometheus.api.roles.options.RoleOptionSerializer;
import net.minecraft.resources.ResourceLocation;

public record CadmusOptions(int maxClaims, int maxChunkLoaded) implements RoleOption<CadmusOptions> {

    public static final RoleOptionSerializer<CadmusOptions> SERIALIZER = RoleOptionSerializer.of(
        new ResourceLocation(Prometheus.MOD_ID, Cadmus.MOD_ID),
        1,
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("maxClaims").orElse(1089).forGetter(CadmusOptions::maxClaims),
            Codec.INT.fieldOf("maxChunkLoaded").orElse(64).forGetter(CadmusOptions::maxChunkLoaded)
        ).apply(instance, CadmusOptions::new)),
        new CadmusOptions(1089, 64)
    );

    @Override
    public RoleOptionSerializer<CadmusOptions> serializer() {
        return SERIALIZER;
    }
}
