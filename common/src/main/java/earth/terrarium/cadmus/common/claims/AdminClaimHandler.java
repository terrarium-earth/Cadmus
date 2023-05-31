package earth.terrarium.cadmus.common.claims;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.utils.SaveHandler;
import earth.terrarium.cadmus.api.claims.admin.FlagApi;
import earth.terrarium.cadmus.api.claims.admin.flags.Flag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AdminClaimHandler extends SaveHandler {
    private final Map<String, Map<String, Flag<?>>> flagsById = new HashMap<>();

    @Override
    public void loadData(CompoundTag tag) {
        tag.getAllKeys().forEach(id -> {
            CompoundTag adminTag = tag.getCompound(id);
            CompoundTag flagsTag = adminTag.getCompound("flags");
            Map<String, Flag<?>> flags = new HashMap<>();
            flagsTag.getAllKeys().forEach(flag -> {
                Flag<?> value = FlagApi.API.get(flag).create(flagsTag.getString(flag));
                flags.put(flag, value);
            });
            flagsById.put(id, flags);
        });
    }

    @Override
    public void saveData(CompoundTag tag) {
        flagsById.forEach((id, claimData) -> {
            CompoundTag adminTag = new CompoundTag();
            CompoundTag flagsTag = new CompoundTag();
            claimData.forEach((flag, value) -> flagsTag.putString(flag, value.serialize()));
            adminTag.put("flags", flagsTag);
            tag.put(id, adminTag);
        });
    }

    public static AdminClaimHandler read(MinecraftServer server) {
        return read(server.overworld().getDataStorage(), AdminClaimHandler::new, "cadmus_admin_claims");
    }

    public static void create(MinecraftServer server, String id, Map<String, Flag<?>> claim) {
        var data = read(server);
        data.flagsById.put(id, claim);
    }

    public static void remove(MinecraftServer server, String id) {
        var data = read(server);
        data.flagsById.remove(id);
    }

    @Nullable
    public static Map<String, Flag<?>> get(MinecraftServer server, String id) {
        var data = read(server);
        return data.flagsById.get(id);
    }

    public static Map<String, Map<String, Flag<?>>> getAll(MinecraftServer server) {
        return read(server).flagsById;
    }

    public static boolean contains(MinecraftServer server, String id) {
        var data = read(server);
        return data.flagsById.containsKey(id);
    }

    public static boolean getBooleanFlag(ServerLevel level, ChunkPos pos, String flag) {
        return getFlag(level, pos, flag);
    }

    public static boolean getBooleanFlag(MinecraftServer server, String id, String flag) {
        return getFlag(server, id, flag);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFlag(ServerLevel level, ChunkPos pos, String flag) {
        Pair<String, ClaimType> claim = ClaimHandler.getClaim(level, pos);
        if (claim == null) return (T) FlagApi.API.get(flag).getValue();
        return getFlag(level.getServer(), claim.getFirst(), flag);
    }


    @SuppressWarnings("unchecked")
    public static <T> T getFlag(MinecraftServer server, String id, String flag) {
        var data = read(server);
        var claim = data.flagsById.get(id);
        if (claim == null) return (T) FlagApi.API.get(flag).getValue();
        var value = claim.get(flag);
        var result = value == null ? (Flag<T>) FlagApi.API.get(flag) : (Flag<T>) value;
        return result.getValue();
    }

    public static Map<String, Flag<?>> getAllFlags(MinecraftServer server, String id) {
        var data = read(server);
        return data.flagsById.get(id);
    }

    public static void setFlag(MinecraftServer server, String id, String flag, Flag<?> value) {
        var data = read(server);
        var claim = data.flagsById.get(id);
        claim.put(flag, value);
    }

    public static void removeFlag(MinecraftServer server, String id, String flag) {
        var data = read(server);
        var claim = data.flagsById.get(id);
        claim.remove(flag);
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
