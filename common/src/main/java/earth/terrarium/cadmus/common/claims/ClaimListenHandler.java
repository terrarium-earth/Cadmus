package earth.terrarium.cadmus.common.claims;

import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.ClientboundUpdateListeningChunksPacket;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Supplier;

public final class ClaimListenHandler {

    private static final int MAX_CHUNKS_PER_PACKET = 500;

    // Players in which are listening to updates.
    private final Set<UUID> listeners = new HashSet<>();
    private final ResourceKey<Level> dimension;

    public ClaimListenHandler(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public void addListener(Player player) {
        listeners.add(player.getUUID());
        var server = player.getServer();
        if (server == null) return;
        var level = server.getLevel(dimension);
        if (level == null) return;
        for (var entry : ClaimHandler.getAllTeamClaims(level).entrySet()) {
            String id = entry.getKey();
            Object2BooleanMap<ChunkPos> claims = new Object2BooleanOpenHashMap<>(entry.getValue().size());
            entry.getValue().keySet().forEach(c -> claims.put(c, true));
            sendOrSplitPacket(List.of((ServerPlayer) player), level, id, claims);
        }
    }

    public void removeListener(Player player) {
        listeners.remove(player.getUUID());
    }

    public void addClaims(ServerLevel level, String id, Set<ChunkPos> claimData) {
        sendPacket(level, id, () -> {
            Object2BooleanMap<ChunkPos> claims = new Object2BooleanOpenHashMap<>(claimData.size());
            claimData.forEach(c -> claims.put(c, true));
            return claims;
        });
    }

    public void removeClaims(ServerLevel level, String id, Set<ChunkPos> claimData) {
        sendPacket(level, id, () -> {
            Object2BooleanMap<ChunkPos> claims = new Object2BooleanOpenHashMap<>(claimData.size());
            claimData.forEach(c -> claims.put(c, false));
            return claims;
        });
    }

    private void sendPacket(ServerLevel level, String id, Supplier<Object2BooleanMap<ChunkPos>> getter) {
        if (listeners.isEmpty()) return;
        List<ServerPlayer> players = level.getServer().getPlayerList()
            .getPlayers()
            .stream()
            .filter(p -> listeners.contains(p.getUUID()))
            .toList();
        if (players.isEmpty()) return;
        Object2BooleanMap<ChunkPos> claims = getter.get();
        if (claims.isEmpty()) return;
        sendOrSplitPacket(players, level, id, claims);
    }

    private void sendClaims(List<ServerPlayer> players, Component displayName, int color, Object2BooleanMap<ChunkPos> claims) {
        List<Object2BooleanMap<ChunkPos>> split = new ArrayList<>();
        Object2BooleanMap<ChunkPos> current = new Object2BooleanOpenHashMap<>();
        int count = 0;
        for (Object2BooleanMap.Entry<ChunkPos> entry : claims.object2BooleanEntrySet()) {
            current.put(entry.getKey(), entry.getBooleanValue());
            count++;
            if (count >= MAX_CHUNKS_PER_PACKET) {
                split.add(current);
                current = new Object2BooleanOpenHashMap<>();
                count = 0;
            }
        }
        if (!current.isEmpty()) {
            split.add(current);
        }
        for (Object2BooleanMap<ChunkPos> chunkPosObject2BooleanMap : split) {
            NetworkHandler.CHANNEL.sendToPlayers(new ClientboundUpdateListeningChunksPacket(
                this.dimension,
                displayName,
                color,
                chunkPosObject2BooleanMap
            ), players);
        }
    }

    private void sendOrSplitPacket(List<ServerPlayer> players, ServerLevel level, String id, Object2BooleanMap<ChunkPos> claims) {
        Component displayName = TeamHelper.getTeamName(id, level.getServer());
        ChatFormatting color = TeamHelper.getTeamColor(id, level.getServer());

        sendClaims(players, displayName, color.getColor() == null ? -1 : color.getColor(), claims);
    }
}
