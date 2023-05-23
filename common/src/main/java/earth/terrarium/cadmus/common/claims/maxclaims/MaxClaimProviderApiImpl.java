package earth.terrarium.cadmus.common.claims.maxclaims;

import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProvider;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MaxClaimProviderApiImpl implements MaxClaimProviderApi {
    public final Map<ResourceLocation, MaxClaimProvider> providers = new HashMap<>();
    @Nullable
    private ResourceLocation selected;

    @Override
    public void register(ResourceLocation id, MaxClaimProvider provider) {
        if (providers.containsKey(id)) {
            throw new IllegalArgumentException("Provider already registered: " + id);
        }
        providers.put(id, provider);
    }

    @Override
    @Nullable
    public MaxClaimProvider get(ResourceLocation id) {
        return providers.get(id);
    }

    @Override
    public MaxClaimProvider getSelected() {
        return Objects.requireNonNull(providers.get(selected));
    }

    @Override
    @Nullable
    public ResourceLocation getSelectedId() {
        return selected;
    }

    @Override
    public void setSelected(@Nullable ResourceLocation id) {
        if (id != null && !providers.containsKey(id)) {
            throw new IllegalArgumentException("No provider registered for: " + id);
        }
        this.selected = id;
    }

    @Override
    public Collection<ResourceLocation> getIds() {
        return providers.keySet();
    }
}
