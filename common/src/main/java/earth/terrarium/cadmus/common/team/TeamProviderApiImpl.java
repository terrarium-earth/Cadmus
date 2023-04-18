package earth.terrarium.cadmus.common.team;

import earth.terrarium.cadmus.api.team.TeamProvider;
import earth.terrarium.cadmus.api.team.TeamProviderApi;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeamProviderApiImpl implements TeamProviderApi {
    public final Map<ResourceLocation, TeamProvider> providers = new HashMap<>();
    private ResourceLocation selected;

    @Override
    public void register(ResourceLocation id, TeamProvider provider) {
        if (providers.containsKey(id)) {
            throw new IllegalArgumentException("Provider already registered: " + id);
        }
        providers.put(id, provider);
    }

    @Override
    public @Nullable TeamProvider get(ResourceLocation id) {
        return providers.get(id);
    }

    @Override
    public TeamProvider getSelected() {
        return Objects.requireNonNull(providers.get(selected));
    }

    @Override
    public void setSelected(ResourceLocation id) {
        if (!providers.containsKey(id)) {
            throw new IllegalArgumentException("No provider registered for: " + id);
        }
        // TODO if updated, regenerate all team members
        this.selected = id;
    }

    @Override
    public Collection<ResourceLocation> getIds() {
        return providers.keySet();
    }

    @Override
    public void update(UUID uuid) {
        // TODO
    }
}
