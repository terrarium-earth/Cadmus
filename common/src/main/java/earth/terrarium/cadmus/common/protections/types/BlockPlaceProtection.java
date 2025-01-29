package earth.terrarium.cadmus.common.protections.types;

import earth.terrarium.cadmus.api.flags.types.BooleanFlag;
import earth.terrarium.cadmus.api.protections.Protection;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.ClaimSettings;
import earth.terrarium.cadmus.common.utils.CadmusGameRules;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class BlockPlaceProtection implements Protection {

    @Override
    public String setting() {
        return ClaimSettings.CAN_PLACE_BLOCKS;
    }

    @Override
    public String permission() {
        return "cadmus.block_placing";
    }

    @Override
    public String personalPermission() {
        return "cadmus.personal.block_placing";
    }

    @Override
    public BooleanFlag flag() {
        return Flags.BLOCK_PLACE;
    }

    @Override
    public GameRules.Key<GameRules.BooleanValue> gameRule() {
        return CadmusGameRules.DO_CLAIMED_BLOCK_PLACING;
    }

    public boolean canPlaceBlock(@NotNull Entity entity, BlockPos pos, BlockState state) {
        if (entity.level().isClientSide()) return true;
        TeamId id = getId(entity.level(), pos).orElse(null);
        if (id == null) return true;
        if (isBlockAllowed(entity.level(), id, state)) return true;

        return entity instanceof Player player ?
            isPlayerAllowed(player, id) :
            isEntityAllowed(entity, id);
    }

    public boolean canPlaceBlock(Level level, UUID player, BlockPos pos, BlockState state) {
        Player playerEntity = level.getPlayerByUUID(player);
        return playerEntity == null || canPlaceBlock(playerEntity, pos, state);
    }

    public boolean canPlaceBlock(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return true;
        TeamId id = getId(level, pos).orElse(null);
        if (id == null) return true;
        if (isBlockAllowed(level, id, state)) return true;
        return CadmusSaveData.getClaimSettingOrDefault(level.getServer(), id, ClaimSettings.CAN_NON_PLAYERS_PLACE_BLOCKS);
    }
}
