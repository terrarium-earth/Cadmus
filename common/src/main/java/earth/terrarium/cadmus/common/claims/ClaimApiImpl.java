package earth.terrarium.cadmus.common.claims;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.api.team.TeamProviderApi;
import earth.terrarium.cadmus.common.registry.ModGameRules;
import earth.terrarium.cadmus.common.team.Team;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ClaimApiImpl implements ClaimApi {

    @Override
    public boolean isClaimed(Level level, ChunkPos pos) {
        return ClaimChunkSaveData.get(level, pos) != null;
    }

    @Override
    public Set<UUID> getClaimMembers(Level level, ChunkPos pos) {
        ClaimInfo info = ClaimChunkSaveData.get(level, pos);
        if (info == null) return Set.of();
        return Optionull.mapOrDefault(info.team(), Team::members, Set.of());
    }

    @Override
    public boolean canBreakBlock(Level level, BlockPos pos, UUID id) {
        if (!ModUtils.getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_BLOCK_BREAKING)) {
            if (!TeamProviderApi.API.getSelected().canBreakBlock(level, pos, id)) {
                return false;
            }
            Set<UUID> claimMembers = getClaimMembers(level, new ChunkPos(pos));
            return !isClaimed(level, pos) || claimMembers.contains(id);
        }
        return true;
    }

    @Override
    public boolean canBreakBlock(Level level, BlockPos pos, Player id) {
        return canBreakBlock(level, pos, id.getUUID());
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, UUID id) {
        if (!ModUtils.getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_BLOCK_PLACING)) {
            if (!TeamProviderApi.API.getSelected().canPlaceBlock(level, pos, id)) {
                return false;
            }
            Set<UUID> claimMembers = getClaimMembers(level, new ChunkPos(pos));
            return !isClaimed(level, pos) || claimMembers.contains(id);
        }
        return true;
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, Player id) {
        return canPlaceBlock(level, pos, id.getUUID());
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID id) {
        if (!ModUtils.getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_BLOCK_EXPLOSIONS)) {
            if (!TeamProviderApi.API.getSelected().canExplodeBlock(level, pos, explosion, id)) {
                return false;
            }
            Set<UUID> claimMembers = getClaimMembers(level, new ChunkPos(pos));
            return !isClaimed(level, pos) || claimMembers.contains(id);
        }
        return true;
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, Player id) {
        return canExplodeBlock(level, pos, explosion, id.getUUID());
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, UUID id) {
        if (!ModUtils.getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_BLOCK_INTERACTIONS)) {
            if (!TeamProviderApi.API.getSelected().canInteractWithBlock(level, pos, type, id)) {
                return false;
            }
            Set<UUID> claimMembers = getClaimMembers(level, new ChunkPos(pos));
            return !isClaimed(level, pos) || claimMembers.contains(id);
        }
        return true;
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, Player id) {
        return canInteractWithBlock(level, pos, type, id.getUUID());
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, UUID id) {
        if (!ModUtils.getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_ENTITY_INTERACTIONS)) {
            if (!TeamProviderApi.API.getSelected().canInteractWithEntity(level, entity, id)) {
                return false;
            }
            Set<UUID> claimMembers = getClaimMembers(level, entity.chunkPosition());
            return !isClaimed(level, entity.chunkPosition()) || claimMembers.contains(id);
        }
        return true;
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, Player id) {
        return canInteractWithEntity(level, entity, id.getUUID());
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, UUID id) {
        if (!ModUtils.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CLAIMED_DAMAGE_ENTITIES) && TeamProviderApi.API.getSelected().canDamageEntity(level, entity, id)) {
            Set<UUID> claimMembers = getClaimMembers(level, entity.chunkPosition());
            return !isClaimed(level, entity.chunkPosition()) || claimMembers.contains(id);
        }
        return true;
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, Player id) {
        return canDamageEntity(level, entity, id.getUUID());
    }

    @Override
    public boolean canEntityGrief(Level level, Entity entity) {
        if (!ModUtils.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CLAIMED_MOB_GRIEFING)) {
            return !isClaimed(level, entity.chunkPosition());
        }
        return true;
    }

    @Override
    public boolean canEntityGrief(Level level, BlockPos pos, Entity entity) {
        return !isClaimed(level, pos);
    }

    @Override
    public boolean canPickupItem(Level level, BlockPos pos, ItemEntity item, Entity picker) {
        if (!ModUtils.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CAN_PICKUP_CLAIMED_ITEMS)) {
            if (Objects.equals(item.getOwner(), picker)) {
                return true;
            }
            if (picker instanceof Player player) {
                return canInteractWithEntity(level, item, player);
            }
            return canEntityGrief(level, picker);
        }
        return true;
    }
}
