package earth.terrarium.cadmus.client.compat.journeymap;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.client.events.CadmusClientEvents;
import earth.terrarium.cadmus.api.events.CadmusEvents;
import earth.terrarium.cadmus.client.CadmusClient;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.event.MappingEvent;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.event.impl.ClientEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneyMapCompat implements IClientPlugin {

    private IClientAPI api;
    private ClaimedChunkOptions options;

    @Override
    public void initialize(IClientAPI api) {
        this.api = api;

        CadmusEvents.AddClaimsEvent.register((level, id, positions) -> update(level.dimension()));
        CadmusEvents.RemoveClaimsEvent.register((level, id, positions) -> update(level.dimension()));
        CadmusEvents.ClearClaimsEvent.register((level, id) -> update(level.dimension()));
        CadmusClientEvents.UpdateTeamInfo.register((id, name, color, updateMaps) -> {
            if (updateMaps) update(CadmusClient.level().dimension());
        });

        ClientEventRegistry.DISPLAY_UPDATE_EVENT.subscribe(getModId(), this::updateOrClear);

        ClientEventRegistry.MAPPING_EVENT.subscribe(getModId(), event -> {
            if (event.getStage() == MappingEvent.Stage.MAPPING_STOPPED) {
                clear();
            } else {
                updateOrClear(event);
            }
        });

        ClientEventRegistry.OPTIONS_REGISTRY_EVENT_EVENT.subscribe(getModId(), event -> {
            this.options = new ClaimedChunkOptions();
        });
    }

    public void updateOrClear(ClientEvent event) {
        if (options != null && Boolean.TRUE.equals(options.showClaimedChunks.get())) {
            update(event.dimension);
        } else {
            clear();
        }
    }

    @Override
    public String getModId() {
        return Cadmus.MOD_ID;
    }

    private void clear() {
        api.removeAll(getModId());
    }

    private void update(ResourceKey<Level> dimension) {
        if (options != null && options.showClaimedChunks.get()) {
            show(dimension);
        } else {
            clear();
        }
    }

    private void show(ResourceKey<Level> dimension) {
        clear();
        ClaimApi.API.getAllClientClaims(dimension).forEach((pos, entry) -> {
            try {
                api.show(ClaimedChunkDisplay.create(pos, entry.left(), entry.rightBoolean(), dimension));
            } catch (Exception ignored) {}
        });
    }
}
