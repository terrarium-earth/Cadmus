package earth.terrarium.cadmus.common.teams;

import com.mojang.authlib.GameProfile;
import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.api.teams.TeamProvider;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.clientbound.SyncAllTeamInfoPacket;
import earth.terrarium.cadmus.common.network.packets.clientbound.SyncTeamInfo;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TeamApiImpl implements TeamApi {

    private static final Map<Player, Component> LAST_MESSAGE = new WeakHashMap<>();

    private final HashMap<ResourceLocation, TeamProvider> teams = new HashMap<>();

    @Override
    public void register(ResourceLocation id, TeamProvider team) {
        this.teams.put(id, team);
    }

    @Override
    public TeamProvider getProvider(ResourceLocation id) {
        return this.teams.get(id);
    }

    @Override
    public Set<ResourceLocation> getAllProviders() {
        return teams.keySet();
    }

    @Override
    public Set<TeamId> getAllTeams(MinecraftServer server) {
        Set<TeamId> teams = new HashSet<>();
        this.teams.forEach((id, team) -> team.getAllTeams(server).forEach(uuid -> teams.add(new TeamId(id, uuid))));
        return teams;
    }

    @Override
    public Component getName(Level level, TeamId id) {
        return teams.get(id.provider()).getName(level, id.id()).orElseGet(() -> {
            if (!level.isClientSide()) {
                MinecraftServer server = level.getServer();
                if (server == null) return ConstantComponents.UNKNOWN;

                if (FlagApi.API.isAdminTeam(server, id.id())) {
                    return Component.literal(Flags.DISPLAY_NAME.get(server, id.id()));
                }

                GameProfileCache cache = server.getProfileCache();
                if (cache == null) return ConstantComponents.UNKNOWN;
                GameProfile profile = cache.get(id.id()).orElse(null);
                if (profile == null) return ConstantComponents.UNKNOWN;
                return Component.literal(profile.getName());
            } else if (CadmusClient.TEAM_INFO.containsKey(id)) {
                return Component.literal(CadmusClient.TEAM_INFO.get(id).name());
            }

            return ConstantComponents.UNKNOWN;
        }).copy().withStyle(getColor(level, id).getAsStyle());
    }

    @Override
    public Component getName(MinecraftServer server, TeamId id) {
        return getName(server.overworld(), id);
    }

    @Override
    public Color getColor(Level level, TeamId id) {
        return getProvider(id.provider()).getColor(level, id.id()).orElseGet(() -> ModUtils.uuidToColor(id.id()));
    }

    @Override
    public Color getColor(MinecraftServer server, TeamId id) {
        return getColor(server.overworld(), id);
    }

    @Override
    public boolean isMember(Level level, TeamId id, Player player) {
        return getProvider(id.provider()).isMember(level, id.id(), player);
    }

    @Override
    public Set<UUID> getMembers(Level level, TeamId id) {
        return getProvider(id.provider()).getMembers(level, id.id());
    }

    @Override
    public Set<TeamId> getTeamsList(@NotNull Player player) {
        return getTeams(player).entrySet().stream().flatMap(entry -> entry.getValue().stream().map(uuid -> new TeamId(entry.getKey(), uuid))).collect(Collectors.toSet());
    }

    @Override
    public Map<ResourceLocation, Set<UUID>> getTeams(@NotNull Player player) {
        Map<ResourceLocation, Set<UUID>> teams = new HashMap<>();
        this.teams.forEach((resourceLocation, teamProvider) -> {
            Set<UUID> teamIds = teamProvider.getTeams(player);
            if (!teamIds.isEmpty()) teams.put(resourceLocation, teamIds);
        });
        return teams;
    }

    @Override
    public boolean isOnTeam(@NotNull Player player) {
        return !this.getTeamsList(player).isEmpty();
    }

    @Override
    public boolean canModifySettings(@NotNull Player player, TeamId id) {
        return this.getProvider(id.provider()).canModifySettings(player, id.id());
    }

    @Override
    public void removeTeam(MinecraftServer server, TeamId id) {
        server.getAllLevels().forEach(level -> ClaimApi.API.clear(level, id));
        CadmusSaveData.removeTeam(server, id);
    }

    @Override
    public void syncAllTeamInfo(MinecraftServer server) {
        Map<TeamId, TeamInfo> teamInfo = new HashMap<>();

        getAllTeams(server).forEach(id -> {
            String name = getName(server, id).getString();
            Color color = getColor(server, id);
            teamInfo.put(id, new TeamInfo(name, color));
        });

        NetworkHandler.sendToAllClientPlayers(new SyncAllTeamInfoPacket(teamInfo), server);
    }

    @Override
    public void syncAllTeamInfo(ServerPlayer player) {
        if (NetworkHandler.CHANNEL.canSendToPlayer(player, SyncAllTeamInfoPacket.TYPE)) {
            Map<TeamId, TeamInfo> teamInfo = new HashMap<>();

            getAllTeams(player.server).forEach(id -> {
                String name = getName(player.server, id).getString();
                Color color = getColor(player.server, id);
                teamInfo.put(id, new TeamInfo(name, color));
            });

            NetworkHandler.CHANNEL.sendToPlayer(new SyncAllTeamInfoPacket(teamInfo), player);
        }
    }

    @Override
    public void syncTeamInfo(MinecraftServer server, TeamId id, boolean updateMaps) {
        String name = getName(server, id).getString();
        Color color = getColor(server, id);
        NetworkHandler.sendToAllClientPlayers(new SyncTeamInfo(id, name, color, updateMaps), server);
    }

    @Override
    public void displayTeamName(ServerPlayer player, ChunkPos pos) {
        if (player == null) return;
        Component message = ClaimApi.API.getClaim(player.level(), player.chunkPosition()).map(claim -> {
            String greeting = Flags.GREETING.get(player.serverLevel(), player.chunkPosition());
            return greeting.isBlank() ?
                getName(player.level(), claim.left()) :
                Component.literal(greeting).withStyle(ChatFormatting.GOLD);
        }).orElseGet(() -> {
            String farewell = Flags.FAREWELL.get(player.serverLevel(), pos);
            return farewell.isBlank() ?
                ConstantComponents.WILDERNESS :
                Component.literal(farewell).withStyle(ChatFormatting.GOLD);
        });

        if (message.equals(LAST_MESSAGE.get(player))) return;
        LAST_MESSAGE.put(player, message);
        player.displayClientMessage(message, true);
    }

    @Override
    public void displayTeamNameToAll(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(this::displayTeamName);
    }
}
