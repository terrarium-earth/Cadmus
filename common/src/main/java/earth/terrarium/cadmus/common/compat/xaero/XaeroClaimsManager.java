package earth.terrarium.cadmus.common.compat.xaero;

import earth.terrarium.cadmus.client.ClientClaims;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class XaeroClaimsManager {
    private final Map<ResourceKey<Level>, Map<ChunkPos, ClientClaims.Entry>> map = new HashMap<>();

    public @Nullable ClientClaims.Entry get(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        var claims = map.get(dimension);
        if (claims == null) return null;
        return claims.get(new ChunkPos(chunkX, chunkZ));
    }

    public Map<ChunkPos, ClientClaims.Entry> put(ResourceKey<Level> dimension, Map<ChunkPos, ClientClaims.Entry> claims) {
        return map.put(dimension, claims);
    }

    public void clear(ResourceKey<Level> dimension) {
        map.remove(dimension);
    }
}
