package earth.terrarium.cadmus.compat.fabric.cpa;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
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
            return ClaimApi.API.canBreakBlock(level, pos, player);
        }
        return ClaimApi.API.canBreakBlock(level, pos, profile.getId());
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return ClaimApi.API.canExplodeBlock(level, pos, explosion, player);
        }
        return ClaimApi.API.canExplodeBlock(level, pos, explosion, profile.getId());
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return ClaimApi.API.canPlaceBlock(level, pos, player);
        }
        return ClaimApi.API.canPlaceBlock(level, pos, profile.getId());
    }

    @Override
    public boolean canInteractBlock(Level level, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return ClaimApi.API.canInteractWithBlock(level, pos, InteractionType.USE, player);
        }
        return ClaimApi.API.canInteractWithBlock(level, pos, InteractionType.USE, profile.getId());
    }

    @Override
    public boolean canInteractEntity(Level level, Entity entity, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return ClaimApi.API.canInteractWithEntity(level, entity, player);
        }
        return ClaimApi.API.canInteractWithEntity(level, entity, profile.getId());
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, GameProfile profile, @Nullable Player player) {
        if (player != null) {
            return ClaimApi.API.canDamageEntity(level, entity, player);
        }
        return ClaimApi.API.canDamageEntity(level, entity, profile.getId());
    }
}
