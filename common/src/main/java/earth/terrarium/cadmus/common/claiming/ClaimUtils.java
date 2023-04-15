package earth.terrarium.cadmus.common.claiming;

import earth.terrarium.cadmus.common.team.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;

// TODO make API
public class ClaimUtils {
    public static boolean inProtectedChunk(Entity entity) {
        return entity != null && inProtectedChunk(entity, entity.blockPosition());
    }

    public static boolean inProtectedChunk(Entity entity, BlockPos pos) {
        if (entity == null) return false;
        if (entity instanceof Player player) {
            return isPlayerInProtectedChunk(player, pos);
        } else {
            return inProtectedChunk(entity.level, pos);
        }
    }

    public static boolean inProtectedChunk(Level level, BlockPos pos) {
        var claimedChunks = ClaimChunkSaveData.getAll(level);
        for (var entry : claimedChunks.entrySet()) {
            if (isInProtectedChunks(pos, entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerInProtectedChunk(Player player, BlockPos pos) {
        var playerTeam = getTeamName(player);
        var info = ClaimChunkSaveData.get(player.level, new ChunkPos(pos));
        if (info == null) return false;
        return info.team() != null && !playerTeam.equals(info.team().teamId());
    }

    private static boolean isInProtectedChunks(BlockPos pos, ChunkPos chunk) {
        return chunk.x == pos.getX() >> 4 && chunk.z == pos.getZ() >> 4;
    }

    public static UUID getTeamName(Player player) {
//        return player.getTeam() == null ? player.getUUID() : player.getTeam().
        return player.getUUID();
    }

    public static void sendSyncPacket(ServerPlayer player) {
//        var teams = ClaimChunkSaveData.getTeams(player.level);
//        if (teams.isEmpty()) return;
//        NetworkHandler.CHANNEL.sendToPlayer(new SyncClaimedChunksPacket(teams), player);
    }

    public static void displayTeamName(ServerPlayer player) {
        if (!(player instanceof LastMessageHolder holder)) return;

        var team = Optionull.mapOrDefault(ClaimChunkSaveData.get(player), ClaimInfo::team, new Team(null, null, null, ""));
        String name = team.name();
        String lastMessage = holder.cadmus$getLastMessage();

        if (Objects.equals(team.name(), lastMessage)) return;
        holder.cadmus$setLastMessage(team.name());
        var playerTeam = getTeamName(player);
        if (team.creator() == null) {
            player.displayClientMessage(Component.translatable("message.cadmus.wilderness"), true);
        } else {
            player.displayClientMessage(Component.nullToEmpty(name).copy().withStyle(playerTeam.equals(team.teamId()) ? ChatFormatting.AQUA : ChatFormatting.DARK_RED), true);
        }
    }
}
