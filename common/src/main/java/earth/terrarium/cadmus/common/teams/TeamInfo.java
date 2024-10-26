package earth.terrarium.cadmus.common.teams;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.color.Color;

public record TeamInfo(String name, Color color) {
    public static final Codec<TeamInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(TeamInfo::name),
        Color.CODEC.fieldOf("color").forGetter(TeamInfo::color)
    ).apply(instance, TeamInfo::new));

    public static final ByteCodec<TeamInfo> BYTE_CODEC = ObjectByteCodec.create(
        ByteCodec.STRING.fieldOf(TeamInfo::name),
        Color.BYTE_CODEC.fieldOf(TeamInfo::color),
        TeamInfo::new
    );
}
