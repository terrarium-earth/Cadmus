package earth.terrarium.cadmus.api.claims.maxclaims;

import earth.terrarium.cadmus.api.ApiHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "1.21")
public interface MaxClaimProviderApi {

    MaxClaimProviderApi API = ApiHelper.load(MaxClaimProviderApi.class);

    /**
     * Registers a max claim provider
     *
     * @param id       the id of the provider
     * @param provider the provider
     * @throws IllegalArgumentException if the id is already registered
     */
    void register(ResourceLocation id, MaxClaimProvider provider);

    /**
     * Gets a max claim provider
     *
     * @param id the id of the provider
     * @return the provider, returns null if no provider is registered for the id
     */
    @Nullable
    MaxClaimProvider get(ResourceLocation id);

    /**
     * Gets the selected max claim provider
     *
     * @return the selected max claim provider
     */
    MaxClaimProvider getSelected();

    /**
     * Gets the selected max claim provider id
     *
     * @return the selected max claim provider id
     */
    @Nullable
    ResourceLocation getSelectedId();

    /**
     * Sets the selected max claim provider
     *
     * @param id the id of the provider
     * @throws IllegalArgumentException if no provider is registered for the id
     */
    void setSelected(ResourceLocation id);

    /**
     * Gets all registered ids
     *
     * @return the ids
     */
    Collection<ResourceLocation> getIds();
}
