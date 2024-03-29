package earth.terrarium.cadmus.api.claims.admin;

import earth.terrarium.cadmus.api.ApiHelper;
import earth.terrarium.cadmus.api.claims.admin.flags.Flag;

import java.util.Map;

public interface FlagApi {
    FlagApi API = ApiHelper.load(FlagApi.class);

    /**
     * Registers a new flag.
     *
     * @param name The name of the flag.
     * @param flag The value of the flag.
     */
    void register(String name, Flag<?> flag);

    /**
     * Gets the value of a flag.
     *
     * @param name The name of the flag.
     * @return The value of the flag.
     */
    Flag<?> get(String name);

    /**
     * Gets all registered flags.
     *
     * @return All registered flags.
     */
    Map<String, Flag<?>> getAll();
}
