package earth.terrarium.cadmus.common.claims;

import com.teamresourceful.bytecodecs.base.ByteCodec;

public enum ClaimType {
    CLAIMED,
    CHUNK_LOADED,
    ;

    public static final ByteCodec<ClaimType> CODEC = ByteCodec.ofEnum(ClaimType.class);
}
