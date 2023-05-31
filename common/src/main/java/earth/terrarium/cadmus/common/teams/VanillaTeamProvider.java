package earth.terrarium.cadmus.common.teams;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.api.teams.TeamProvider;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanillaTeamProvider implements TeamProvider {

    @Override
    public Set<GameProfile> getTeamMembers(String id, MinecraftServer server) {
        PlayerTeam team = server.getScoreboard().getPlayerTeam(id);
        Set<GameProfile> profiles = new HashSet<>();
        if (team == null) return profiles;
        team.getPlayers().forEach(p -> ModUtils.getProfileCache(server).get(p).ifPresent(profiles::add));
        return profiles;
    }

    @Override
    @Nullable
    public Component getTeamName(String id, MinecraftServer server) {
        var playerTeam = server.getScoreboard().getPlayerTeam(id);
        return Optionull.map(playerTeam, PlayerTeam::getDisplayName);
    }

    @Override
    @Nullable
    public String getTeamId(MinecraftServer server, UUID player) {
        var profile = ModUtils.getProfileCache(server).get(player);
        if (profile.isEmpty()) return null;
        var playerTeam = server.getScoreboard().getPlayersTeam(profile.get().getName());
        return Optionull.map(playerTeam, t -> ClaimHandler.TEAM_PREFIX + t.getName());
    }

    @Override
    public boolean isMember(String id, MinecraftServer server, UUID player) {
        var profile = ModUtils.getProfileCache(server).get(player).orElse(null);
        if (profile == null) return false;
        var playerTeam = server.getScoreboard().getPlayerTeam(id);
        if (playerTeam == null) return id.equals(player.toString());
        return playerTeam.getPlayers().contains(profile.getName());
    }

    @Override
    public ChatFormatting getTeamColor(String id, MinecraftServer server) {
        var playerTeam = server.getScoreboard().getPlayerTeam(id);
        var result = Optionull.mapOrDefault(playerTeam, PlayerTeam::getColor, ChatFormatting.AQUA);
        return result == ChatFormatting.RESET ? ChatFormatting.AQUA : result;
    }

    @Override
    public boolean canBreakBlock(String id, MinecraftServer server, BlockPos pos, UUID player) {
        return isMember(id, server, player);
    }

    @Override
    public boolean canPlaceBlock(String id, MinecraftServer server, BlockPos pos, UUID player) {
        return isMember(id, server, player);
    }

    @Override
    public boolean canExplodeBlock(String id, MinecraftServer server, BlockPos pos, Explosion explosion, UUID player) {
        return isMember(id, server, player);
    }

    @Override
    public boolean canInteractWithBlock(String id, MinecraftServer server, BlockPos pos, InteractionType type, UUID player) {
        return isMember(id, server, player);
    }

    @Override
    public boolean canInteractWithEntity(String id, MinecraftServer server, Entity entity, UUID player) {
        return isMember(id, server, player);
    }

    @Override
    public boolean canDamageEntity(String id, MinecraftServer server, Entity entity, UUID player) {
        return isMember(id, server, player);
    }

    public void onTeamChanged(String id, MinecraftServer server, String playerName) {
        TeamProvider.super.onTeamChanged(server, id);
        var profile = ModUtils.getProfileCache(server).get(playerName).orElse(null);
        if (profile == null) return;
        server.getAllLevels().forEach(l -> ClaimHandler.clear(l, ClaimHandler.PLAYER_PREFIX + profile.getId().toString()));
    }

    public void onTeamRemoved(MinecraftServer server, PlayerTeam playerTeam) {
        TeamProvider.super.onTeamRemoved(server, playerTeam.getName());
        server.getAllLevels().forEach(l -> ClaimHandler.clear(l, ClaimHandler.TEAM_PREFIX + playerTeam.getName()));
    }
}
