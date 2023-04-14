package earth.terrarium.cadmus.common.claiming;

import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.message.client.SyncClaimedChunksPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class ClaimUtils {
    public static boolean inProtectedChunk(Entity entity) {
        return entity != null && inProtectedChunk(entity, entity.blockPosition());
    }

    public static boolean inProtectedChunk(Entity entity, BlockPos pos) {
        if (entity == null) return false;
        if (entity instanceof Player player) {
            return playerInProtectedChunk(player, pos);
        } else {
            return inProtectedChunk(entity.level, pos);
        }
    }

    public static boolean inProtectedChunk(Level level, BlockPos pos) {
        var teams = ClaimChunkSaveData.getTeams(level);
        for (var team : teams.entrySet()) {
            if (isInProtectedChunks(pos, team.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static boolean playerInProtectedChunk(Player player, BlockPos pos) {
        // Don't do chunk protection for creative players
        if (player.isCreative()) return false;
        var playerTeam = getTeamName(player);
        var team = getChunkTeam(player.level, new ChunkPos(pos));
        return team != null && !playerTeam.equals(team);
    }

    private static boolean isInProtectedChunks(BlockPos pos, Set<ClaimedChunk> chunks) {
        for (var chunk : chunks) {
            ChunkPos chunkPos = chunk.pos();
            if (chunkPos.x == pos.getX() >> 4 && chunkPos.z == pos.getZ() >> 4) {
                return true;
            }
        }
        return false;
    }

    public static String getTeamName(Player player) {
        return player.getTeam() == null ? player.getName().getString() : player.getTeam().getName();
    }

    public static void sendSyncPacket(ServerPlayer player) {
        var teams = ClaimChunkSaveData.getTeams(player.level);
        if (teams.isEmpty()) return;
        NetworkHandler.CHANNEL.sendToPlayer(new SyncClaimedChunksPacket(teams), player);
    }

    @Nullable
    public static String getChunkTeam(Level level, ChunkPos pos) {
        var teams = ClaimChunkSaveData.getTeams(level);
        for (var team : teams.entrySet()) {
            for (ClaimedChunk chunk : team.getValue()) {
                if (chunk.pos().equals(pos)) {
                    return team.getKey();
                }
            }
        }
        return null;
    }

    public static void displayTeamName(ServerPlayer player) {
        if (!(player instanceof LastMessageHolder holder)) return;

        var team = getChunkTeam(player.level, player.chunkPosition());
        var lastMessage = holder.cadmus$getLastMessage();

        if (Objects.equals(team, lastMessage)) return;
        holder.cadmus$setLastMessage(team);
        var playerTeam = getTeamName(player);
        if (team == null) {
            player.displayClientMessage(Component.translatable("message.cadmus.wilderness"), true);
        } else {
            player.displayClientMessage(Component.literal(team).withStyle(playerTeam.equals(team) ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED), true);
        }
    }
}
