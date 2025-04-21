package earth.terrarium.cadmus.compat.fabric.cpa;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.protections.Protections;
import eu.pb4.common.protection.api.ProtectionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class CadmusProtectionProvider implements ProtectionProvider {

    @Override
    public boolean isProtected(Level level, BlockPos pos) {
        return ClaimApi.API.isClaimed(level, pos);
    }

    @Override
    public boolean isAreaProtected(Level level, AABB area) {
        ChunkPos min = new ChunkPos(((int) area.minX) >> 4, ((int) area.minZ) >> 4);
        ChunkPos max = new ChunkPos(((int) area.maxX) >> 4, ((int) area.maxZ) >> 4);
        for (int x = min.x; x <= max.x; x++) {
            for (int z = min.z; z <= max.z; z++) {
                if (!ClaimApi.API.isClaimed(level, new ChunkPos(x, z))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canBreakBlock(Level level, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return Protections.BLOCK_BREAKING.canBreakBlock(player, pos);
        }
        return Protections.BLOCK_BREAKING.canBreakBlock(level, profile, pos);
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, GameProfile profile, @Nullable Player player) {
        return Protections.BLOCK_EXPLOSIONS.canExplodeBlock(level, pos, explosion);
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return Protections.BLOCK_PLACING.canPlaceBlock(player, pos, level.getBlockState(pos));
        }
        return Protections.BLOCK_PLACING.canPlaceBlock(level, profile, pos, level.getBlockState(pos));
    }

    @Override
    public boolean canInteractBlock(Level level, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return Protections.BLOCK_INTERACTIONS.canInteractWithBlock(player, pos, level.getBlockState(pos));
        }
        return Protections.BLOCK_INTERACTIONS.canInteractWithBlock(level, profile, pos, level.getBlockState(pos));
    }

    @Override
    public boolean canInteractEntity(Level level, Entity entity, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return Protections.ENTITY_INTERACTIONS.canInteractWithEntity(player, entity);
        }
        return Protections.ENTITY_INTERACTIONS.canInteractWithEntity(level, profile, entity);
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return Protections.ENTITY_DAMAGE.canDamageEntity(player, entity);
        }
        return Protections.ENTITY_DAMAGE.canDamageEntity(level, profile, entity);
    }
}
