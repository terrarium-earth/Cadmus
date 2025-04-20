package earth.terrarium.cadmus.common.protections.types;

import earth.terrarium.cadmus.api.flags.types.BooleanFlag;
import earth.terrarium.cadmus.api.protections.Protection;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.ClaimSettings;
import earth.terrarium.cadmus.common.tags.ModBlockTags;
import earth.terrarium.cadmus.common.teams.AdminTeamProvider;
import earth.terrarium.cadmus.common.utils.CadmusGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public final class BlockInteractProtection implements Protection {

    @Override
    public String setting() {
        return ClaimSettings.CAN_INTERACT_WITH_BLOCKS;
    }

    @Override
    public String permission() {
        return "cadmus.block_interactions";
    }

    @Override
    public String personalPermission() {
        return "cadmus.personal.block_interactions";
    }

    @Override
    public BooleanFlag flag() {
        return Flags.BLOCK_INTERACTIONS;
    }

    @Override
    public GameRules.Key<GameRules.BooleanValue> gameRule() {
        return CadmusGameRules.DO_CLAIMED_BLOCK_INTERACTIONS;
    }

    public boolean canInteractWithBlock(Player player, BlockPos pos, BlockState state) {
        if (state.is(ModBlockTags.ALLOWS_CLAIM_INTERACTIONS)) return true;
        return player.level().isClientSide() || getId(player.level(), pos).map(id ->
            checkFlags((ServerLevel) player.level(), pos, id) && isPlayerAllowed(player, id) || isBlockAllowed(player.level(), id, pos)).orElse(true);
    }

    public boolean canInteractWithBlock(Level level, UUID player, BlockPos pos, BlockState state) {
        Player playerEntity = level.getPlayerByUUID(player);
        return playerEntity == null || canInteractWithBlock(playerEntity, pos, state);
    }

    private boolean checkFlags(ServerLevel level, BlockPos pos, TeamId id) {
        MinecraftServer server = level.getServer();
        if (!id.isAdmin()) return true;

        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlockTags.DOOR_LIKE)) return Flags.USE_DOORS.get(server, id.id());
        if (state.is(ModBlockTags.INTERACTABLE_STORAGE)) return Flags.USE_CHESTS.get(server, id.id());
        if (state.is(ModBlockTags.REDSTONE)) return Flags.USE_REDSTONE.get(server, id.id());
        return true;
    }
}
