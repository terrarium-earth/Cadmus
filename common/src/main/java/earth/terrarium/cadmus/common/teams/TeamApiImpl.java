package earth.terrarium.cadmus.common.teams;

import com.mojang.authlib.GameProfile;
import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.flags.Flag;
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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    private final ResourceLocation individual = Cadmus.id("individual");
    private final ResourceLocation admin = Cadmus.id("admin");

    @Override
    public void register(ResourceLocation id, TeamProvider team) {
        this.teams.put(id, team);
    }

    public TeamProvider getProvider(ResourceLocation id) {
        return this.teams.get(id);
    }

    @Override
    public Set<TeamId> getAllTeams(MinecraftServer server) {
        Set<TeamId> teams = new HashSet<>();
        this.teams.forEach((id, team) -> team.getAllTeams(server).forEach(uuid -> teams.add(new TeamId(id, uuid))));
        server.getAllLevels().forEach(level -> ClaimApi.API.getAllClaimsByOwner(level).keySet().forEach(uuid -> teams.add(new TeamId(individual, uuid))));
        server.getPlayerList().getPlayers().forEach(player -> teams.add(new TeamId(individual, player.getUUID())));
        FlagApi.API.getAllAdminTeams(server).keySet().forEach(uuid -> teams.add(new TeamId(admin, uuid)));
        return teams;
    }

    @Override
    public Component getName(Level level, TeamId id) {
        return teams.get(id.providerId()).getName(level, id.teamId()).orElseGet(() -> {
            if (!level.isClientSide()) {
                MinecraftServer server = level.getServer();
                if (server == null) return ConstantComponents.UNKNOWN;

                if (FlagApi.API.isAdminTeam(server, id.teamId())) {
                    return Component.literal(Flags.DISPLAY_NAME.get(server, id.teamId()));
                }

                GameProfileCache cache = server.getProfileCache();
                if (cache == null) return ConstantComponents.UNKNOWN;
                GameProfile profile = cache.get(id.teamId()).orElse(null);
                if (profile == null) return ConstantComponents.UNKNOWN;
                return Component.literal(profile.getName());
            } else if (CadmusClient.TEAM_INFO.containsKey(id.teamId())) {
                return Component.literal(CadmusClient.TEAM_INFO.get(id.teamId()).name());
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
        return getProvider(id.providerId()).getColor(level, id.teamId()).orElseGet(() -> {
            if (!level.isClientSide()) {
                MinecraftServer server = level.getServer();
                if (server == null) return ModUtils.uuidToColor(id);

                if (FlagApi.API.isAdminTeam(server, id)) {
                    return Flags.COLOR.get(server, id);
                } else {
                    return CadmusSaveData.getTeamColor(server, id);
                }
            } else {
                if (CadmusClient.TEAM_INFO.containsKey(id)) {
                    return CadmusClient.TEAM_INFO.get(id).color();
                } else {
                    return ModUtils.uuidToColor(id);
                }
            }
        });
    }

    @Override
    public Color getColor(MinecraftServer server, UUID id) {
        return getColor(server.overworld(), id);
    }

    @Override
    public boolean isMember(Level level, UUID id, Player player) {
        return player.getUUID().equals(id) || getSelected().isMember(level, id, player);
    }

    @Override
    public UUID getTeams(@NotNull Player player) {
        return getSelected().getId(player).orElse(player.getUUID());
    }

    @Override
    public boolean isOnTeam(@NotNull Player player) {
        return getSelected().getId(player).isPresent();
    }

    @Override
    public boolean canModifySettings(@NotNull Player player) {
        return !isOnTeam(player) || getSelected().canModifySettings(player);
    }

    @Override
    public void removeTeam(MinecraftServer server, UUID id) {
        server.getAllLevels().forEach(level -> ClaimApi.API.clear(level, id));
        CadmusSaveData.removeTeam(server, id);
    }

    @Override
    public void syncAllTeamInfo(MinecraftServer server) {
        Map<UUID, TeamInfo> teamInfo = new HashMap<>();

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
            Map<UUID, TeamInfo> teamInfo = new HashMap<>();

            getAllTeams(player.server).forEach(id -> {
                String name = getName(player.server, id).getString();
                Color color = getColor(player.server, id);
                teamInfo.put(id, new TeamInfo(name, color));
            });

            NetworkHandler.CHANNEL.sendToPlayer(new SyncAllTeamInfoPacket(teamInfo), player);
        }
    }

    @Override
    public void syncTeamInfo(MinecraftServer server, UUID id, boolean updateMaps) {
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
