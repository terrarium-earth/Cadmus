package earth.terrarium.cadmus.common.claims;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.defaults.EnumCodec;

public enum ClaimType {
    CLAIMED,
    CHUNK_LOADED,
    ;

    public static final ByteCodec<ClaimType> CODEC = new EnumCodec<>(ClaimType.class);
}
