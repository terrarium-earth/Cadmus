package earth.terrarium.cadmus.api.teams;

import earth.terrarium.cadmus.api.ApiHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "1.21")
public interface TeamProviderApi {

    TeamProviderApi API = ApiHelper.load(TeamProviderApi.class);

    /**
     * Registers a team provider
     *
     * @param id       the id of the provider
     * @param provider the provider
     * @throws IllegalArgumentException if the id is already registered
     */
    void register(ResourceLocation id, TeamProvider provider);

    /**
     * Gets a team provider
     *
     * @param id the id of the provider
     * @return the provider, returns null if no provider is registered for the id
     */
    @Nullable
    TeamProvider get(ResourceLocation id);

    /**
     * Gets the selected team provider
     *
     * @return the selected team provider
     */
    TeamProvider getSelected();

    /**
     * Gets the selected team provider id
     *
     * @return the selected team provider id
     */
    @Nullable
    ResourceLocation getSelectedId();

    /**
     * Sets the selected team provider
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
