package earth.terrarium.cadmus.client;

import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.ServerboundListenToChunksPacket;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class ClientClaims {

    public static final Map<ResourceKey<Level>, ClientClaims> CLAIMS = new HashMap<>();

    private final ResourceKey<Level> dimension;
    private final Map<ChunkPos, Entry> claims = new HashMap<>();
    private final Map<String, Consumer<Map<ChunkPos, Entry>>> listeners = new HashMap<>();

    public ClientClaims(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public void addListener(String id, Consumer<Map<ChunkPos, Entry>> listener) {
        if (listeners.isEmpty()) {
            claims.clear();
            NetworkHandler.CHANNEL.sendToServer(new ServerboundListenToChunksPacket(dimension, true));
        }
        listeners.put(id, listener);
    }

    public void removeListener(String id) {
        listeners.remove(id);
        if (listeners.isEmpty()) {
            NetworkHandler.CHANNEL.sendToServer(new ServerboundListenToChunksPacket(dimension, false));
            claims.clear();
        }
    }

    public void update(Component displayName, int color, Object2BooleanMap<ChunkPos> claims) {
        Entry entry = new Entry(displayName, color);
        claims.forEach((chunkPos, isClaimed) -> {
            if (isClaimed) {
                this.claims.put(chunkPos, entry);
            } else {
                this.claims.remove(chunkPos);
            }
        });
        listeners.values().forEach(l -> l.accept(this.claims));
    }

    public record Entry(Component name, int color) {}

    public static ClientClaims get(ResourceKey<Level> dimension) {
        return CLAIMS.computeIfAbsent(dimension, ClientClaims::new);
    }
}
