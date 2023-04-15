package earth.terrarium.cadmus.common.claiming;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.team.Team;
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

//TODO make each check a game rule
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
        return getClaimMembers(level, new ChunkPos(pos)).contains(id);
    }

    @Override
    public boolean canBreakBlock(Level level, BlockPos pos, Player id) {
        return canBreakBlock(level, pos, id.getUUID());
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, UUID id) {
        return getClaimMembers(level, new ChunkPos(pos)).contains(id);
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, Player id) {
        return canPlaceBlock(level, pos, id.getUUID());
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID id) {
        return getClaimMembers(level, new ChunkPos(pos)).contains(id);
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, Player id) {
        return canExplodeBlock(level, pos, explosion, id.getUUID());
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, UUID id) {
        return getClaimMembers(level, new ChunkPos(pos)).contains(id);
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, Player id) {
        return canInteractWithBlock(level, pos, id.getUUID());
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, UUID id) {
        return getClaimMembers(level, entity.chunkPosition()).contains(id);
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, Player id) {
        return canInteractWithEntity(level, entity, id.getUUID());
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, UUID id) {
        return getClaimMembers(level, entity.chunkPosition()).contains(id);
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, Player id) {
        return canDamageEntity(level, entity, id.getUUID());
    }

    @Override
    public boolean canEntityGrief(Level level, Entity entity) {
        return !isClaimed(level, entity.chunkPosition());
    }

    @Override
    public boolean canPickupItem(Level level, BlockPos pos, ItemEntity item, Entity picker) {
        if (Objects.equals(item.getOwner(), picker)) {
            return true;
        }
        if (picker instanceof Player) {
            return canInteractWithEntity(level, item, (Player) picker);
        }
        return canEntityGrief(level, picker);
    }
}
