package earth.terrarium.cadmus.common.flags;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.flags.Flag;
import earth.terrarium.cadmus.api.flags.FlagApi;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FlagApiImpl implements FlagApi {

    private static final Map<String, Flag<?>> FLAGS = new HashMap<>();

    @Override
    public <T extends Flag<?>> T register(T defaultValue) {
        FLAGS.put(defaultValue.id(), defaultValue);
        return defaultValue;
    }

    @Override
    public Flag<?> getDefaultValue(String flag) {
        return FLAGS.get(flag);
    }

    @Override
    public Map<String, Flag<?>> getAllDefaults() {
        return FLAGS;
    }

    @Override
    public boolean isAdminTeam(MinecraftServer server, UUID id) {
        return FlagSaveData.isAdminClaim(server, id);
    }

    @Override
    public boolean isAdminTeam(MinecraftServer server, String name) {
        return getIdFromName(server, name).map(id -> FlagSaveData.isAdminClaim(server, id)).orElse(false);
    }

    @Override
    public Map<UUID, Map<String, Flag<?>>> getAllAdminTeams(MinecraftServer server) {
        return FlagSaveData.getAll(server);
    }

    @Override
    public Collection<String> getAllAdminTeamNames(MinecraftServer server) {
        return FlagSaveData.getAllAdminTeamNames(server);
    }

    @Override
    public UUID createAdminTeam(MinecraftServer server, String name) {
        return FlagSaveData.createAdminClaim(server, name);
    }

    @Override
    public void removeAdminTeam(MinecraftServer server, UUID id) {
        FlagSaveData.removeAdminClaim(server, id);
    }

    @Override
    public Optional<UUID> getIdFromName(MinecraftServer server, String name) {
        return FlagSaveData.getIdFromName(server, name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Flag<T>> getFlag(ServerLevel level, ChunkPos pos, String flagName) {
        if (!FLAGS.containsKey(flagName)) throw new IllegalArgumentException("Flag not registered: " + flagName);
        return Optional.ofNullable((Flag<T>) ClaimApi.API.getClaim(level, pos).filter(claim -> claim.left().isAdmin()).map(claim ->
            getFlag(level.getServer(), claim.first().id(), flagName)).orElse(null));
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> Flag<T> getFlag(MinecraftServer server, UUID id, String flagName) {
        if (!FLAGS.containsKey(flagName)) throw new IllegalArgumentException("Flag not registered: " + flagName);
        return (Flag<T>) FlagSaveData.getFlag(server, id, flagName);
    }

    @Override
    public void setFlag(MinecraftServer server, UUID id, String flagName, Flag<?> flag) {
        FlagSaveData.setFlag(server, id, flagName, flag);
    }

    @Override
    public void removeFlag(MinecraftServer server, UUID id, String flagName) {
        FlagSaveData.removeFlag(server, id, flagName);
    }

    @Override
    public void clearAll(MinecraftServer server) {
        FlagSaveData.clearAll(server);
    }
}
