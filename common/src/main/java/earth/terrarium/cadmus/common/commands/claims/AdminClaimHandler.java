package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.claims.admin.FlagApi;
import earth.terrarium.cadmus.api.claims.admin.flags.Flag;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AdminClaimHandler extends SavedData {
    private final Map<String, Map<String, Flag<?>>> flagsById = new HashMap<>();

    public AdminClaimHandler() {
    }

    public AdminClaimHandler(CompoundTag tag) {
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
    public @NotNull CompoundTag save(CompoundTag tag) {
        flagsById.forEach((id, claimData) -> {
            CompoundTag adminTag = new CompoundTag();
            CompoundTag flagsTag = new CompoundTag();
            claimData.forEach((flag, value) -> flagsTag.putString(flag, value.serialize()));
            adminTag.put("flags", flagsTag);
            tag.put(id, adminTag);
        });
        return tag;
    }

    public static AdminClaimHandler read(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(AdminClaimHandler::new, AdminClaimHandler::new, "cadmus_admin_claims");
    }

    public static void create(MinecraftServer server, String id, Map<String, Flag<?>> claim) {
        var data = read(server);
        data.flagsById.put(id, claim);
    }

    public static void remove(MinecraftServer server, String id) {
        var data = read(server);
        data.flagsById.remove(id);
    }

    public static Map<String, Flag<?>> get(MinecraftServer server, String id) {
        var data = read(server);
        return data.flagsById.get(id);
    }

    public static Map<String, Map<String, Flag<?>>> getAll(MinecraftServer server) {
        return read(server).flagsById;
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
        var claim = data.flagsById.get(id.replace("a:", ""));
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
