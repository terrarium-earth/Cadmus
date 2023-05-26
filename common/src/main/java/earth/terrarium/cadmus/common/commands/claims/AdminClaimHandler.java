package earth.terrarium.cadmus.common.commands.claims;

import earth.terrarium.cadmus.api.claims.admin.FlagApi;
import earth.terrarium.cadmus.api.claims.admin.flags.Flag;
import earth.terrarium.cadmus.common.commands.claims.admin.AdminClaim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AdminClaimHandler extends SavedData {
    private final Map<String, AdminClaim> claimsById = new HashMap<>();

    public AdminClaimHandler() {
    }

    public AdminClaimHandler(CompoundTag tag) {
        tag.getAllKeys().forEach(id -> {
            CompoundTag adminTag = tag.getCompound(id);
            Component displayName = Component.Serializer.fromJson(adminTag.getString("displayName"));
            CompoundTag flagsTag = adminTag.getCompound("flags");
            Map<String, Flag<?>> flags = new HashMap<>();
            flagsTag.getAllKeys().forEach(flag -> {
                Flag<?> value = FlagApi.API.get(flag).create(flagsTag.getString(flag));
                flags.put(flag, value);
            });
            AdminClaim adminClaim = new AdminClaim(displayName, flags);
            claimsById.put(id, adminClaim);
        });
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        claimsById.forEach((id, claimData) -> {
            CompoundTag adminTag = new CompoundTag();
            adminTag.putString("displayName", Component.Serializer.toJson(claimData.displayName()));
            CompoundTag flagsTag = new CompoundTag();
            claimData.flags().forEach((flag, value) -> flagsTag.putString(flag, String.valueOf(value.getValue())));
            adminTag.put("flags", flagsTag);
            tag.put(id, adminTag);
        });
        return tag;
    }

    public static AdminClaimHandler read(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(AdminClaimHandler::new, AdminClaimHandler::new, "cadmus_admin_claims");
    }

    public static void create(MinecraftServer server, String id, AdminClaim claim) {
        var data = read(server);
        data.claimsById.put(id, claim);
    }

    public static void remove(MinecraftServer server, String id) {
        var data = read(server);
        data.claimsById.remove(id);
    }

    public static AdminClaim get(MinecraftServer server, String id) {
        var data = read(server);
        return data.claimsById.get(id);
    }

    public static Map<String, AdminClaim> getAll(MinecraftServer server) {
        return read(server).claimsById;
    }

    public static Flag<?> getFlag(MinecraftServer server, String id, String flag) {
        var data = read(server);
        var claim = data.claimsById.get(id);
        var value = claim.flags().get(flag);
        return value == null ? FlagApi.API.get(flag) : value;
    }

    public static Map<String, Flag<?>> getAllFlags(MinecraftServer server, String id) {
        var data = read(server);
        var claim = data.claimsById.get(id);
        return claim.flags();
    }

    public static void setFlag(MinecraftServer server, String id, String flag, Flag<?> value) {
        var data = read(server);
        var claim = data.claimsById.get(id);
        claim.flags().put(flag, value);
    }

    public static void removeFlag(MinecraftServer server, String id, String flag) {
        var data = read(server);
        var claim = data.claimsById.get(id);
        claim.flags().remove(flag);
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
