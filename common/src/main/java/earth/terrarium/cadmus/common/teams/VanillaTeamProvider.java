package earth.terrarium.cadmus.common.teams;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.api.teams.TeamProvider;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class VanillaTeamProvider implements TeamProvider {
    @Override
    public Set<GameProfile> getTeamMembers(String id, MinecraftServer server) {
        PlayerTeam team = server.getScoreboard().getPlayersTeam(id);
        Set<GameProfile> profiles = new HashSet<>();
        if (team == null) return profiles;
        for (String player : team.getPlayers()) {
            server.getProfileCache().get(player).ifPresent(profiles::add);
        }
        return profiles;
    }

    @Override
    public Component getTeamName(String id, MinecraftServer server) {
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(id);
        if (playerTeam != null) return playerTeam.getDisplayName();
        Team team = TeamSaveData.get(server, UUID.fromString(id));
        if (team == null) return null;
        PlayerTeam playerTeam1 = server.getScoreboard().getPlayerTeam(team.name());
        if (playerTeam1 != null) return playerTeam1.getDisplayName();
        Optional<UUID> player = team.members().stream().findFirst();
        if (player.isPresent()) {
            var profile = server.getProfileCache().get(player.get());
            return profile.map(gameProfile -> Component.literal(gameProfile.getName())).orElse(null);
        }
        return null;
    }

    @Override
    @Nullable
    public String getTeamId(ServerPlayer player) {
        return Optionull.map(player.getTeam(), net.minecraft.world.scores.Team::getName);
    }

    @Override
    public boolean isMember(String id, MinecraftServer server, UUID player) {
        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player);
        if (serverPlayer == null) return true;
        if (serverPlayer.getTeam() == null) {
            return id.equals(player.toString());
        }
        return serverPlayer.getTeam().getName().equals(id);
    }

    @Override
    public ChatFormatting getTeamColor(String id, MinecraftServer server) {
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(id);
        return Optionull.mapOrDefault(playerTeam, PlayerTeam::getColor, ChatFormatting.AQUA);
    }

    @Override
    public boolean canBreakBlock(String id, MinecraftServer server, BlockPos pos, UUID player) {
        return hasPermission(id, server, player);
    }

    @Override
    public boolean canPlaceBlock(String id, MinecraftServer server, BlockPos pos, UUID player) {
        return hasPermission(id, server, player);
    }

    @Override
    public boolean canExplodeBlock(String id, MinecraftServer server, BlockPos pos, Explosion explosion, UUID player) {
        return hasPermission(id, server, player);
    }

    @Override
    public boolean canInteractWithBlock(String id, MinecraftServer server, BlockPos pos, InteractionType type, UUID player) {
        return hasPermission(id, server, player);
    }

    @Override
    public boolean canInteractWithEntity(String id, MinecraftServer server, Entity entity, UUID player) {
        return hasPermission(id, server, player);
    }

    @Override
    public boolean canDamageEntity(String id, MinecraftServer server, Entity entity, UUID player) {
        return hasPermission(id, server, player);
    }

    private boolean hasPermission(String id, MinecraftServer server, UUID player) {
        if (isMember(id, server, player)) return true;
        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player);
        if (serverPlayer == null) return false;
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(id);
        return serverPlayer.isAlliedTo(playerTeam);
    }

    public void addPlayerToTeam(MinecraftServer server, String playerName, PlayerTeam scoreboardTeam) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) return;

        Set<ChunkPos> removed = new HashSet<>();
        for (Team team : new HashSet<>(TeamSaveData.getTeams(server))) {
            if (team.name().equals(scoreboardTeam.getName())) {
                TeamSaveData.addTeamMember(player, team);
                return;
            }
            removed.addAll(TeamSaveData.removeTeamMember(player, team));
        }
        Team team = TeamSaveData.getOrCreateTeam(player, scoreboardTeam.getName());
        // Transfer chunks to new team if the old team was removed
        removed.forEach(chunkPos -> ClaimSaveData.set(player.getLevel(), chunkPos, new ClaimInfo(team.teamId(), ClaimType.CLAIMED)));
    }

    public void removePlayerFromTeam(MinecraftServer server, String playerName, PlayerTeam scoreboardTeam) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) return;

        for (Team team : new HashSet<>(TeamSaveData.getTeams(server))) {
            if (team.name().equals(scoreboardTeam.getName())) {
                TeamSaveData.removeTeamMember(player, team);
                return;
            }
        }
    }
}
