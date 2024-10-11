package earth.terrarium.cadmus.common.utils;

import com.teamresourceful.resourcefullib.common.utils.SaveHandler;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class CadmusSaveData extends SaveHandler {

    private final Map<UUID, Map<String, TriState>> settings = new HashMap<>();
    private final Object2BooleanMap<String> defaultSettings = new Object2BooleanArrayMap<>();
    private final Map<UUID, Set<ResourceLocation>> allowedBlocks = new HashMap<>();
    private final Set<UUID> bypassPlayers = new HashSet<>();

    @Override
    public void loadData(CompoundTag tag) {
        CompoundTag settingsTag = tag.getCompound("settings");
        settingsTag.getAllKeys().forEach(id -> {
            CompoundTag claimSettingsTag = settingsTag.getCompound(id);
            claimSettingsTag.getAllKeys().forEach(setting -> {
                TriState value = TriState.valueOf(claimSettingsTag.getString(setting));
                this.settings.computeIfAbsent(UUID.fromString(id), ignored -> new HashMap<>()).put(setting, value);
            });
        });

        CompoundTag defaultSettingsTag = tag.getCompound("defaultSettings");
        defaultSettingsTag.getAllKeys().forEach(setting ->
            defaultSettings.put(setting, defaultSettingsTag.getBoolean(setting)));

        CompoundTag allowedBlocksTag = tag.getCompound("allowedBlocks");
        allowedBlocksTag.getAllKeys().forEach(id -> {
            ListTag blockTag = allowedBlocksTag.getList(id, Tag.TAG_STRING);
            Set<ResourceLocation> blocks = new HashSet<>();
            blockTag.forEach(tagEntry ->
                blocks.add(ResourceLocation.parse(tagEntry.getAsString())));
            allowedBlocks.put(UUID.fromString(id), blocks);
        });

        CompoundTag bypassTag = tag.getCompound("bypass");
        bypassTag.getAllKeys().forEach(uuid -> bypassPlayers.add(UUID.fromString(uuid)));
    }

    @Override
    public void saveData(CompoundTag tag) {
        CompoundTag settingsTag = new CompoundTag();
        this.settings.forEach((id, claimSettings) -> {
            CompoundTag claimSettingsTag = new CompoundTag();
            claimSettings.forEach((setting, value) -> claimSettingsTag.putString(setting, value.name()));
            settingsTag.put(id.toString(), claimSettingsTag);
        });
        tag.put("settings", settingsTag);

        CompoundTag defaultSettingsTag = new CompoundTag();
        this.defaultSettings.forEach(defaultSettingsTag::putBoolean);
        tag.put("defaultSettings", defaultSettingsTag);

        CompoundTag allowedBlocksTag = new CompoundTag();
        this.allowedBlocks.forEach((id, blocks) -> {
            ListTag blockTag = new ListTag();
            blocks.forEach(block -> blockTag.add(StringTag.valueOf(block.toString())));
            allowedBlocksTag.put(id.toString(), blockTag);
        });
        tag.put("allowedBlocks", allowedBlocksTag);

        CompoundTag bypassTag = new CompoundTag();
        bypassPlayers.forEach(uuid -> bypassTag.put(uuid.toString(), new CompoundTag()));
        tag.put("bypass", bypassTag);
    }

    public static CadmusSaveData read(MinecraftServer server) {
        return read(server.overworld().getDataStorage(), SaveHandler.HandlerType.create(CadmusSaveData::new), "cadmus_data");
    }

    public static TriState getClaimSetting(MinecraftServer server, UUID id, String setting) {
        return read(server).settings
            .computeIfAbsent(id, ignored -> new HashMap<>())
            .getOrDefault(setting, TriState.UNDEFINED);
    }

    public static void setClaimSetting(MinecraftServer server, UUID id, String setting, TriState value) {
        var data = read(server);
        data.settings
            .computeIfAbsent(id, ignored -> new HashMap<>())
            .put(setting, value);
        data.setDirty();
    }

    public static boolean getClaimSettingOrDefault(MinecraftServer server, UUID id, String setting) {
        TriState value = getClaimSetting(server, id, setting);
        return value.isUndefined() ? getDefaultClaimSetting(server, setting) : value.isTrue();
    }

    public static boolean getDefaultClaimSetting(MinecraftServer server, String setting) {
        return read(server).defaultSettings.getBoolean(setting);
    }

    public static void setDefaultClaimSetting(MinecraftServer server, String setting, boolean value) {
        var data = read(server);
        data.defaultSettings.put(setting, value);
        data.setDirty();
    }


    public static boolean canBypass(MinecraftServer server, UUID player) {
        return read(server).bypassPlayers.contains(player);
    }

    public static boolean canBypass(ServerPlayer player) {
        return canBypass(player.getServer(), player.getUUID());
    }

    public static void toggleBypass(MinecraftServer server, UUID player) {
        var data = read(server);
        if (data.bypassPlayers.contains(player)) {
            data.bypassPlayers.remove(player);
        } else {
            data.bypassPlayers.add(player);
        }
    }

    public static void addAllowedBlock(MinecraftServer server, UUID player, Block block) {
        var data = read(server);
        data.allowedBlocks.computeIfAbsent(player, ignored -> new HashSet<>()).add(BuiltInRegistries.BLOCK.getKey(block));
        data.setDirty();
    }

    public static void removeAllowedBlock(MinecraftServer server, UUID player, Block block) {
        var data = read(server);
        data.allowedBlocks.computeIfAbsent(player, ignored -> new HashSet<>()).remove(BuiltInRegistries.BLOCK.getKey(block));
        data.setDirty();
    }

    public static boolean isBlockAllowed(MinecraftServer server, UUID player, Block block) {
        var data = read(server);
        return data.allowedBlocks.computeIfAbsent(player, ignored -> new HashSet<>()).contains(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static Set<ResourceLocation> getAllowedBlocks(MinecraftServer server, UUID player) {
        return read(server).allowedBlocks.computeIfAbsent(player, ignored -> new HashSet<>());
    }

    public static void removeTeam(MinecraftServer server, UUID id) {
        var data = read(server);
        data.settings.remove(id);
        data.allowedBlocks.remove(id);
        data.setDirty();
    }

    public static void clearAll(MinecraftServer server) {
        var data = read(server);
        data.settings.clear();
        data.allowedBlocks.clear();
        data.setDirty();
    }
}
