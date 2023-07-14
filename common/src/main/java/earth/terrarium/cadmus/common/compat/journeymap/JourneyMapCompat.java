package earth.terrarium.cadmus.common.compat.journeymap;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.ClientClaims;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.RegistryEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.Map;

@ClientPlugin
public class JourneyMapCompat implements IClientPlugin {

    private static final String LISTENER_ID = "journeymap";

    private IClientAPI api;
    private ClaimedChunkOptions options;

    @Override
    public void initialize(IClientAPI api) {
        this.api = api;
        this.api.subscribe(getModId(),
            EnumSet.of(
                ClientEvent.Type.DISPLAY_UPDATE,
                ClientEvent.Type.MAPPING_STARTED,
                ClientEvent.Type.MAPPING_STOPPED,
                ClientEvent.Type.REGISTRY
            )
        );
    }

    @Override
    public String getModId() {
        return Cadmus.MOD_ID;
    }

    @Override
    public void onEvent(ClientEvent event) {
        switch (event.type) {
            case DISPLAY_UPDATE, MAPPING_STARTED -> {
                if (options != null && Boolean.TRUE.equals(options.showClaimedChunks.get())) {
                    ClientClaims.get(event.dimension).addListener(LISTENER_ID, claims -> update(claims, event.dimension));
                } else {
                    ClientClaims.get(event.dimension).removeListener(LISTENER_ID);
                    clear();
                }
            }
            case MAPPING_STOPPED -> {
                ClientClaims.get(event.dimension).removeListener(LISTENER_ID);
                clear();
            }
            case REGISTRY -> {
                if (event instanceof RegistryEvent.OptionsRegistryEvent) {
                    this.options = new ClaimedChunkOptions();
                }
            }
        }
    }

    private void clear() {
        api.removeAll(getModId());
    }

    private void update(Map<ChunkPos, ClientClaims.Entry> claims, ResourceKey<Level> dimension) {
        clear();
        claims.forEach((chunkPos, entry) -> update(chunkPos, entry, dimension));
    }

    private void update(ChunkPos pos, ClientClaims.Entry entry, ResourceKey<Level> dimension) {
        try {
            api.show(ClaimedChunkDisplay.create(pos, entry, dimension));
        } catch (Exception ignored) {}
    }
}
