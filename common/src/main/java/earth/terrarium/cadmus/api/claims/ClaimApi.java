package earth.terrarium.cadmus.api.claims;

import earth.terrarium.cadmus.api.ApiHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.UUID;

public interface ClaimApi {

    ClaimApi API = ApiHelper.load(ClaimApi.class);

    boolean isClaimed(Level level, ChunkPos pos);

    default boolean isClaimed(Level level, BlockPos pos) {
        return this.isClaimed(level, new ChunkPos(pos));
    }

    Set<UUID> getClaimMembers(Level level, ChunkPos pos);

    boolean canBreakBlock(Level level, BlockPos pos, UUID id);

    boolean canBreakBlock(Level level, BlockPos pos, Player player);

    boolean canPlaceBlock(Level level, BlockPos pos, UUID id);

    boolean canPlaceBlock(Level level, BlockPos pos, Player player);

    boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID id);

    boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, Player player);

    boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, UUID id);

    boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, Player player);

    boolean canInteractWithEntity(Level level, Entity entity, UUID id);

    boolean canInteractWithEntity(Level level, Entity entity, Player player);

    boolean canDamageEntity(Level level, Entity entity, UUID id);

    boolean canDamageEntity(Level level, Entity entity, Player player);

    boolean canEntityGrief(Level level, Entity entity);

    boolean canEntityGrief(Level level, BlockPos pos, Entity entity);

    boolean canPickupItem(Level level, BlockPos pos, ItemEntity item, Entity picker);
}
