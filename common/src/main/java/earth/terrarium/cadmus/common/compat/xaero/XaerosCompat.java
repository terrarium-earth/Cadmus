package earth.terrarium.cadmus.common.compat.xaero;

import earth.terrarium.cadmus.client.ClientClaims;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xaero.map.highlight.HighlighterRegistry;

import java.util.Map;

public class XaerosCompat {
    private static final String LISTENER_ID = "xaeros";
    private static final XaeroClaimsManager manager = new XaeroClaimsManager();

    public static void registerListener(ResourceKey<Level> dimension) {
        ClientClaims.get(dimension).addListener(LISTENER_ID, claims ->
                update(dimension, claims)
        );
    }

    public static void removeListener(ResourceKey<Level> dimension) {
        ClientClaims.get(dimension).removeListener(LISTENER_ID);
        manager.clear(dimension);
    }

    private static void update(ResourceKey<Level> dimension, Map<ChunkPos, ClientClaims.Entry> claims) {
        manager.put(dimension, claims);
    }

    public static void registerHighlighters(HighlighterRegistry registry) {
        registry.register(new CadmusChunkHighlighter(manager));
    }
}
