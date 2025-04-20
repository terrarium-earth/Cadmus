package earth.terrarium.cadmus.common.claims;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.ClaimData;
import earth.terrarium.cadmus.api.events.CadmusEvents;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.clientbound.*;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class ClaimApiImpl implements ClaimApi {

    @Override
    public void claim(Level level, TeamId id, ChunkPos pos, boolean chunkLoad) {
        if (chunkLoad) {
            level.getChunkSource().updateChunkForced(pos, true);
            Cadmus.FORCE_LOADED_CHUNK_COUNT++;
        }

        var data = ClaimSaveData.read(level);
        data.claims().put(pos, ObjectBooleanPair.of(id, chunkLoad));
        data.claimsById().computeIfAbsent(id, uuid -> new Object2BooleanOpenHashMap<>()).put(pos, chunkLoad);

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new AddClaimPacket(id, pos, chunkLoad), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.AddClaimsEvent.fire(level, id, Object2BooleanMaps.singleton(pos, chunkLoad));
    }

    @Override
    public void claim(Level level, TeamId id, Object2BooleanMap<ChunkPos> positions) {
        positions.forEach((pos, chunkLoad) -> {
            if (chunkLoad) {
                level.getChunkSource().updateChunkForced(pos, true);
                Cadmus.FORCE_LOADED_CHUNK_COUNT++;
            }
        });

        var data = ClaimSaveData.read(level);
        for (var entry : positions.object2BooleanEntrySet()) {
            ChunkPos pos = entry.getKey();
            boolean chunkLoad = entry.getBooleanValue();
            data.claims().put(pos, ObjectBooleanPair.of(id, chunkLoad));
            data.claimsById().computeIfAbsent(id, uuid -> new Object2BooleanOpenHashMap<>()).put(pos, chunkLoad);
        }

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new AddBulkClaimsPacket(id, positions), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.AddClaimsEvent.fire(level, id, positions);
    }

    @Override
    public void unclaim(Level level, Player player, ChunkPos pos) {
        var claim = getClaim(level, pos);
        if (claim.isEmpty() || !TeamApi.API.isMember(level, claim.get().team(), player)) return;
        unclaim(level, claim.get().team(), pos);
    }

    @Override
    public void unclaim(Level level, TeamId id, ChunkPos pos) {
        if (getClaim(level, pos).map(ClaimData::isChunkLoaded).orElse(false)) {
            level.getChunkSource().updateChunkForced(pos, false);
            Cadmus.FORCE_LOADED_CHUNK_COUNT--;
        }

        var data = ClaimSaveData.read(level);
        data.claims().remove(pos);
        data.claimsById().get(id).removeBoolean(pos);

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new RemoveClaimPacket(id, pos), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.RemoveClaimsEvent.fire(level, id, Set.of(pos));
    }

    @Override
    public void unclaim(Level level, TeamId id, Set<ChunkPos> positions) {
        for (var pos : positions) {
            if (getClaim(level, pos).map(ClaimData::isChunkLoaded).orElse(false)) {
                level.getChunkSource().updateChunkForced(pos, false);
                Cadmus.FORCE_LOADED_CHUNK_COUNT--;
            }
        }

        var data = ClaimSaveData.read(level);
        for (var pos : positions) {
            data.claims().remove(pos);
            data.claimsById().get(id).removeBoolean(pos);
        }

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new RemoveBulkClaimsPacket(id, positions), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.RemoveClaimsEvent.fire(level, id, positions);
    }

    @Override
    public void clear(Level level, TeamId id) {
        getOwnedClaims(level, id).ifPresent(claims -> claims.forEach((pos, chunkLoad) -> {
            if (chunkLoad) {
                level.getChunkSource().updateChunkForced(pos, false);
                Cadmus.FORCE_LOADED_CHUNK_COUNT--;
            }
        }));

        var data = ClaimSaveData.read(level);
        data.claims().values().removeIf(claim -> claim.left().equals(id));
        data.claimsById().remove(id);

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new ClearClaimsPacket(id), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.ClearClaimsEvent.fire(level, id);
    }

    @Override
    public void clear(Player player) {
        TeamApi.API.getTeamsList(player).forEach(team -> clear(player.level(), team));
    }

    @Override
    public void clearAll(MinecraftServer server) {
        CadmusSaveData.clearAll(server);
        FlagApi.API.clearAll(server);
        server.getAllLevels().forEach(level -> {
            getAllClaimsByOwner(level).forEach((id, claims) -> {
                claims.forEach((pos, chunkLoad) -> {
                    if (chunkLoad) {
                        level.getChunkSource().updateChunkForced(pos, false);
                        Cadmus.FORCE_LOADED_CHUNK_COUNT--;
                    }
                });
                NetworkHandler.sendToAllClientPlayers(new ClearClaimsPacket(id), server);
                CadmusEvents.ClearClaimsEvent.fire(level, id);
            });

            var data = ClaimSaveData.read(level);
            data.claims().clear();
            data.claimsById().clear();
            data.setDirty();
        });
        TeamApi.API.displayTeamNameToAll(server);
        TeamApi.API.syncAllTeamInfo(server);
    }

    @Override
    public Optional<ClaimData> getClaim(Level level, ChunkPos pos) {
        var data = ClaimSaveData.read(level);
        ObjectBooleanPair<TeamId> teamIdObjectBooleanPair = data.claims().get(pos);
        return Optional.of(new ClaimData(teamIdObjectBooleanPair.left(), teamIdObjectBooleanPair.rightBoolean()));
    }

    @Override
    public List<ObjectBooleanPair<TeamId>> getClaims(Level level, Collection<ChunkPos> positions) {
        var data = ClaimSaveData.read(level);
        List<ObjectBooleanPair<TeamId>> results = new ArrayList<>();

        for (var pos : positions) {
            var claim = data.claims().get(pos);
            if (claim != null) {
                results.add(claim);
            }
        }
        return results;
    }

    @Override
    public Optional<Object2BooleanMap<ChunkPos>> getOwnedClaims(Level level, TeamId id) {
        var data = ClaimSaveData.read(level);
        return Optional.ofNullable(data.claimsById().get(id));
    }

    @Override
    public Object2ObjectMap<ChunkPos, ObjectBooleanPair<TeamId>> getAllClaims(ServerLevel level) {
        return ClaimSaveData.read(level).claims();
    }

    @Override
    public Object2ObjectMap<TeamId, Object2BooleanMap<ChunkPos>> getAllClaimsByOwner(ServerLevel level) {
        return ClaimSaveData.read(level).claimsById();
    }

    @Override
    public Optional<ObjectBooleanPair<TeamId>> getClientClaim(ResourceKey<Level> level, ChunkPos pos) {
        return Optional.ofNullable(ClaimSaveData.readClient(level).claims().get(pos));
    }

    @Override
    public Object2ObjectMap<ChunkPos, ObjectBooleanPair<TeamId>> getAllClientClaims(ResourceKey<Level> level) {
        return ClaimSaveData.readClient(level).claims();
    }
}
