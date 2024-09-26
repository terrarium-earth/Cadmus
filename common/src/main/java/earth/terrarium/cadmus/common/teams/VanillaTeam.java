package earth.terrarium.cadmus.common.teams;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.color.ConstantColors;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.teams.Team;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class VanillaTeam implements Team {

    private static final BiMap<String, UUID> TEAM_CACHE = HashBiMap.create();

    @Override
    public Optional<Component> getName(Level level, UUID id) {
        String name = TEAM_CACHE.inverse().get(id);
        if (name == null) return Optional.empty();
        PlayerTeam playerTeam = level.getScoreboard().getPlayerTeam(name);
        if (playerTeam == null) return Optional.empty();
        return Optional.of(playerTeam.getDisplayName());
    }

    @Override
    public Optional<Color> getColor(Level level, UUID id) {
        String name = TEAM_CACHE.inverse().get(id);
        if (name == null) return Optional.empty();
        PlayerTeam playerTeam = level.getScoreboard().getPlayerTeam(name);
        ChatFormatting color = Optionull.map(playerTeam, PlayerTeam::getColor);
        return Optional.ofNullable(color == ChatFormatting.RESET ? ConstantColors.aqua : (color != null ? new Color(color.getColor()) : null));
    }

    @Override
    public Set<UUID> getMembers(Level level, UUID id) {
        if (!(level instanceof ServerLevel serverLevel)) return Set.of();
        String name = TEAM_CACHE.inverse().get(id);
        if (name == null) return Set.of();

        PlayerTeam playerTeam = level.getScoreboard().getPlayerTeam(name);
        GameProfileCache profileCache = serverLevel.getServer().getProfileCache();
        if (playerTeam == null || profileCache == null) return Set.of();

        Set<UUID> members = new HashSet<>();
        playerTeam.getPlayers().forEach(member -> profileCache.get(member).ifPresent(profile -> members.add(profile.getId())));
        return members;
    }

    @Override
    public boolean isMember(Level level, UUID id, Player player) {
        String name = TEAM_CACHE.inverse().get(id);
        if (name == null) return false;
        PlayerTeam playerTeam = level.getScoreboard().getPlayerTeam(name);
        if (playerTeam == null) return false;
        return playerTeam.getPlayers().contains(player.getGameProfile().getName());
    }

    @Override
    public Optional<UUID> getId(Player player) {
        PlayerTeam team = player.getScoreboard().getPlayersTeam(player.getGameProfile().getName());
        if (team == null) return Optional.empty();
        return Optional.of(gerOrCreateId(team));
    }

    @Override
    public boolean canModifySettings(Player player) {
        return true;
    }

    @Override
    public Set<UUID> getAllTeams(MinecraftServer server) {
        return new HashSet<>(TEAM_CACHE.values());
    }

    public UUID gerOrCreateId(PlayerTeam team) {
        return TEAM_CACHE.computeIfAbsent(team.getName(), ModUtils::stringToUUID);
    }

    public UUID remove(PlayerTeam team) {
        return TEAM_CACHE.remove(team.getName());
    }

    public void transferClaims(MinecraftServer server, PlayerTeam team, String playerName) {
        UUID id = TEAM_CACHE.get(team.getName());
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) return;

        server.getAllLevels().forEach(level ->
            ClaimApi.API.getOwnedClaims(level, player.getUUID()).ifPresent(claims ->
                ClaimApi.API.claim(level, id, claims)));

        server.getAllLevels().forEach(level -> ClaimApi.API.clear(level, player.getUUID()));
    }
}
