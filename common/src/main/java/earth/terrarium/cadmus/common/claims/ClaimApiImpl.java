package earth.terrarium.cadmus.common.claims;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.teams.Team;
import earth.terrarium.cadmus.common.teams.TeamSaveData;
import earth.terrarium.cadmus.common.util.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

public class ClaimApiImpl implements ClaimApi {
    @Override
    public boolean isClaimed(Level level, ChunkPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            return ClaimSaveData.get(serverLevel, pos) != null;
        }
        return false;
    }

    @Override
    public boolean isChunkLoaded(Level level, ChunkPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            ClaimInfo info = ClaimSaveData.get(serverLevel, pos);
            if (info == null) return false;
            return info.type() == ClaimType.CHUNK_LOADED;
        }
        return false;
    }

    public boolean canBreakBlock(Level level, BlockPos pos, UUID player) {
        return canAccess(level, pos, player, ModGameRules.RULE_DO_CLAIMED_BLOCK_BREAKING, (team, server) ->
            TeamProviderApi.API.getSelected().canBreakBlock(team.name(), server, pos, player));
    }

    @Override
    public boolean canBreakBlock(Level level, BlockPos pos, Player player) {
        return canBreakBlock(level, pos, player.getUUID());
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, UUID player) {
        return canAccess(level, pos, player, ModGameRules.RULE_DO_CLAIMED_BLOCK_PLACING, (team, server) ->
            TeamProviderApi.API.getSelected().canPlaceBlock(team.name(), server, pos, player));
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, Player player) {
        return canPlaceBlock(level, pos, player.getUUID());
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID player) {
        return canAccess(level, pos, player, ModGameRules.RULE_DO_CLAIMED_BLOCK_EXPLOSIONS, (team, server) ->
            TeamProviderApi.API.getSelected().canExplodeBlock(team.name(), server, pos, explosion, player));
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, Player player) {
        return canExplodeBlock(level, pos, explosion, player.getUUID());
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, UUID player) {
        return canAccess(level, pos, player, ModGameRules.RULE_DO_CLAIMED_BLOCK_INTERACTIONS, (team, server) ->
            TeamProviderApi.API.getSelected().canInteractWithBlock(team.name(), server, pos, type, player));
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, Player player) {
        return canInteractWithBlock(level, pos, type, player.getUUID());
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, UUID player) {
        return canAccess(level, entity.blockPosition(), player, ModGameRules.RULE_DO_CLAIMED_ENTITY_INTERACTIONS, (team, server) ->
            TeamProviderApi.API.getSelected().canInteractWithEntity(team.name(), server, entity, player));
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, Player player) {
        return canInteractWithEntity(level, entity, player.getUUID());
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, UUID player) {
        return canAccess(level, entity.blockPosition(), player, ModGameRules.RULE_CLAIMED_DAMAGE_ENTITIES, (team, server) ->
            TeamProviderApi.API.getSelected().canDamageEntity(team.name(), server, entity, player));
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, Player player) {
        return canDamageEntity(level, entity, player.getUUID());
    }

    @Override
    public boolean canEntityGrief(Level level, Entity entity) {
        return ModGameRules.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CLAIMED_MOB_GRIEFING)
            || !isClaimed(level, entity.chunkPosition());
    }

    @Override
    public boolean canEntityGrief(Level level, BlockPos pos, Entity entity) {
        return ModGameRules.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CLAIMED_MOB_GRIEFING)
            || !isClaimed(level, pos);
    }

    @Override
    public boolean canPickupItem(Level level, BlockPos pos, ItemEntity item, Entity picker) {
        if (ModGameRules.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CAN_PICKUP_CLAIMED_ITEMS)) {
            return true;
        }
        if (Objects.equals(item.getOwner(), picker)) return true;
        if (picker instanceof Player player) {
            return canInteractWithEntity(level, item, player);
        }
        return canEntityGrief(level, picker);
    }

    private boolean canAccess(Level level, BlockPos pos, UUID player, GameRules.Key<GameRules.BooleanValue> rule, BiFunction<Team, MinecraftServer, Boolean> checkTeamPermission) {
        if (!(level instanceof ServerLevel serverLevel)) return true;
        MinecraftServer server = serverLevel.getServer();
        if (ModGameRules.getOrCreateBooleanGameRule(level, rule)) return true;
        if (!isClaimed(level, pos)) return true;

        Team team = TeamSaveData.getPlayerTeam(server, player);
        if (team == null) return false;

        if (!checkTeamPermission.apply(team, server)) return false;

        ClaimInfo info = ClaimSaveData.get(serverLevel, new ChunkPos(pos));
        if (info == null) return true;
        Team chunkTeam = TeamSaveData.get(server, info.teamId());
        if (chunkTeam == null) return true;
        return TeamProviderApi.API.getSelected().isMember(chunkTeam.name(), serverLevel.getServer(), player);
    }
}
