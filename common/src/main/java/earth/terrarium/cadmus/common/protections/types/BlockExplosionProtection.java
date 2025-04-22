package earth.terrarium.cadmus.common.protections.types;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.flags.types.BooleanFlag;
import earth.terrarium.cadmus.api.protections.Protection;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.ClaimSettings;
import earth.terrarium.cadmus.common.utils.CadmusGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class BlockExplosionProtection implements Protection {

    @Override
    public String setting() {
        return ClaimSettings.CAN_EXPLODE_BLOCKS;
    }

    @Override
    public String permission() {
        return "cadmus.block_explosions";
    }

    @Override
    public String personalPermission() {
        return "cadmus.personal.block_explosions";
    }

    @Override
    public BooleanFlag flag() {
        return Flags.BLOCK_EXPLOSIONS;
    }

    @Override
    public GameRules.Key<GameRules.BooleanValue> gameRule() {
        return CadmusGameRules.DO_CLAIMED_BLOCK_EXPLOSIONS;
    }

    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion) {
        if (level.isClientSide()) return true;
        TeamId id = getId(level, pos).orElse(null);
        if (id == null) return true;
        if (isBlockAllowed(level, id, pos)) return true;
        LivingEntity entity = explosion.getIndirectSourceEntity();

        if (entity instanceof Player player) {
            return isPlayerAllowed(player, id);
        } else if (entity != null) {
            return isEntityAllowed(entity, id);
        }
        return !ClaimApi.API.isClaimed(level, pos);
    }
}
