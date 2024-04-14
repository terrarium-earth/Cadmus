package earth.terrarium.cadmus.common.claims;

import com.teamresourceful.resourcefullib.common.utils.SaveHandler;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;

import java.util.*;

/**
 * Data that should only have one save instance, the overworld save data.
 */
public class CadmusDataHandler extends SaveHandler {

    private final Map<String, IntIntPair> maxClaimsById = new HashMap<>();
    private final Map<String, ClaimSettings> settings = new HashMap<>();
    private final Set<UUID> bypassPlayers = new HashSet<>();
    private ClaimSettings defaultSettings = ClaimSettings.ofFalse();
    private final Map<String, Set<ResourceLocation>> allowedBlocks = new HashMap<>();

    @Override
    public void loadData(CompoundTag tag) {
        CompoundTag maxClaimsTag = tag.getCompound("max_claims");
        maxClaimsTag.getAllKeys().forEach(id -> {
            CompoundTag maxClaimTag = maxClaimsTag.getCompound(id);
            int maxClaims = maxClaimTag.getInt("maxClaims");
            int maxChunkLoaded = maxClaimTag.getInt("maxChunkLoaded");
            maxClaimsById.put(id, IntIntPair.of(maxClaims, maxChunkLoaded));
        });

        CompoundTag settings = tag.getCompound("settings");
        settings.getAllKeys().forEach(id -> this.settings.put(id, ClaimSettings.read(settings.getCompound(id))));

        CompoundTag bypassTag = tag.getCompound("bypass");
        bypassTag.getAllKeys().forEach(uuid -> bypassPlayers.add(UUID.fromString(uuid)));

        String teamProvider = tag.getString("team_provider");
        if (!teamProvider.isEmpty()) {
            TeamProviderApi.API.setSelected(new ResourceLocation(teamProvider));
        }

        String maxClaimProvider = tag.getString("max_claim_provider");
        if (!maxClaimProvider.isEmpty()) {
            MaxClaimProviderApi.API.setSelected(new ResourceLocation(maxClaimProvider));
        }

        if (tag.contains("defaultSettings")) {
            defaultSettings = ClaimSettings.read(tag.getCompound("defaultSettings"));
        }

        if (tag.contains("allowedBlocks")) {
            CompoundTag allowedBlocksTag = tag.getCompound("allowedBlocks");
            allowedBlocksTag.getAllKeys().forEach(id -> {
                ListTag blockTag = allowedBlocksTag.getList(id, Tag.TAG_STRING);
                Set<ResourceLocation> blocks = new HashSet<>();
                blockTag.forEach(tagEntry ->
                    blocks.add(new ResourceLocation(tagEntry.getAsString())));
                allowedBlocks.put(id, blocks);
            });
        }
    }

    @Override
    public void saveData(CompoundTag tag) {
        CompoundTag maxClaimsTag = new CompoundTag();
        maxClaimsById.forEach((id, maxClaims) -> {
            CompoundTag maxClaimTag = new CompoundTag();
            maxClaimTag.putInt("maxClaims", maxClaims.firstInt());
            maxClaimTag.putInt("maxChunkLoaded", maxClaims.secondInt());
            maxClaimsTag.put(id, maxClaimTag);
        });
        tag.put("max_claims", maxClaimsTag);

        CompoundTag settings = new CompoundTag();
        this.settings.forEach((id, claimSettings) -> settings.put(id, claimSettings.write(new CompoundTag())));
        tag.put("settings", settings);

        CompoundTag bypassTag = new CompoundTag();
        bypassPlayers.forEach(uuid -> bypassTag.put(uuid.toString(), new CompoundTag()));
        tag.put("bypass", bypassTag);

        ResourceLocation selectedId = TeamProviderApi.API.getSelectedId();
        if (selectedId != null) {
            tag.putString("team_provider", selectedId.toString());
        }

        ResourceLocation maxClaimSelectedId = MaxClaimProviderApi.API.getSelectedId();
        if (maxClaimSelectedId != null) {
            tag.putString("max_claim_provider", maxClaimSelectedId.toString());
        }

        tag.put("defaultSettings", defaultSettings.write(new CompoundTag()));

        CompoundTag allowedBlocksTag = new CompoundTag();
        this.allowedBlocks.forEach((id, blocks) -> {
            ListTag blockTag = new ListTag();
            blocks.forEach(block -> blockTag.add(StringTag.valueOf(block.toString())));
            allowedBlocksTag.put(id, blockTag);
        });
        tag.put("allowedBlocks", allowedBlocksTag);
    }

    public static boolean canBypass(MinecraftServer server, UUID player) {
        return read(server).bypassPlayers.contains(player);
    }

    public static void toggleBypass(MinecraftServer server, UUID player) {
        var data = read(server);
        if (data.bypassPlayers.contains(player)) {
            data.bypassPlayers.remove(player);
        } else {
            data.bypassPlayers.add(player);
        }
    }

    public static CadmusDataHandler read(MinecraftServer server) {
        return read(server.overworld().getDataStorage(), HandlerType.create(CadmusDataHandler::new), "cadmus_data");
    }

    public static Map<String, IntIntPair> getMaxTeamClaims(MinecraftServer server) {
        return read(server).maxClaimsById;
    }

    public static ClaimSettings getClaimSettings(MinecraftServer server, String id) {
        return read(server).settings.computeIfAbsent(id, ignored -> ClaimSettings.ofUndefined());
    }

    public static ClaimSettings getDefaultClaimSettings(MinecraftServer server) {
        return read(server).defaultSettings;
    }


    @Override
    public boolean isDirty() {
        return true;
    }

    public static void addAllowedBlock(MinecraftServer server, String player, Block block) {
        var data = read(server);
        data.allowedBlocks.computeIfAbsent(player, ignored -> new HashSet<>()).add(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static void removeAllowedBlock(MinecraftServer server, String player, Block block) {
        var data = read(server);
        data.allowedBlocks.computeIfAbsent(player, ignored -> new HashSet<>()).remove(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static boolean isBlockAllowed(MinecraftServer server, String player, Block block) {
        var data = read(server);
        return data.allowedBlocks.computeIfAbsent(player, ignored -> new HashSet<>()).contains(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static Set<ResourceLocation> getAllowedBlocks(MinecraftServer server, String player) {
        return read(server).allowedBlocks.computeIfAbsent(player, ignored -> new HashSet<>());
    }
}
