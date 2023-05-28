package earth.terrarium.cadmus.common.claims.admin;

import earth.terrarium.cadmus.api.claims.admin.FlagApi;
import earth.terrarium.cadmus.api.claims.admin.flags.Flag;

import java.util.HashMap;
import java.util.Map;

public class FlagApiImpl implements FlagApi {
    private static final Map<String, Flag<?>> FLAGS = new HashMap<>();

    @Override
    public void register(String name, Flag<?> flag) {
        FLAGS.put(name, flag);
    }

    @Override
    public Flag<?> get(String flag) {
        return FLAGS.get(flag);
    }

    @Override
    public Map<String, Flag<?>> getAll() {
        return FLAGS;
    }
}
