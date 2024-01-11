package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class ClaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                .then(Commands.argument("chunkload", BoolArgumentType.bool())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ColumnPos pos = ColumnPosArgument.getColumnPos(context, "pos");
                        boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                        CommandHelper.runAction(() -> claim(player, pos.toChunkPos(), chunkload));
                        return 1;
                    })))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> claim(player, player.chunkPosition(), false));
                return 1;
            }));
    }

    public static void claim(ServerPlayer player, ChunkPos pos, boolean chunkloaded) throws ClaimException {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.serverLevel(), pos);
        if (claimData != null) {
            boolean isMember = TeamHelper.isMember(claimData.getFirst(), player.server, player.getUUID());
            throw isMember ? ClaimException.ALREADY_CLAIMED_CHUNK : ClaimException.CHUNK_ALREADY_CLAIMED;
        }
        if (!ClaimApi.API.canClaim(player.serverLevel(), pos, chunkloaded, player)) {
            throw ClaimException.MAXED_OUT_CLAIMS;
        }
        ClaimApi.API.claim(player.serverLevel(), pos, chunkloaded, player);
        if (chunkloaded) {
            player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.claiming.chunk_loaded_chunk_at", pos.x, pos.z), false);
        } else {
            player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.claiming.claimed_chunk_at", pos.x, pos.z), false);
        }
    }
}

