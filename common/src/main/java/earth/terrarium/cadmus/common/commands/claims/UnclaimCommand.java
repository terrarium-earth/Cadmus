package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;

public class UnclaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unclaim")
            .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ColumnPos pos = ColumnPosArgument.getColumnPos(context, "pos");
                    CommandHelper.runAction(() -> unclaim(player, pos.toChunkPos()));
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> unclaim(player, player.chunkPosition()));
                return 1;
            }));
    }

    public static void unclaim(ServerPlayer player, ChunkPos pos) throws ClaimException {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.serverLevel(), pos);
        if (claimData == null) {
            throw ClaimException.CHUNK_NOT_CLAIMED;
        }
        boolean isMember = TeamHelper.isMember(claimData.getFirst(), player.server, player.getUUID());
        if (!isMember) {
            throw ClaimException.DONT_OWN_CHUNK;
        }
        ModUtils.tryClaim(player.serverLevel(), player, Map.of(), Map.of(pos, ClaimType.CLAIMED));
        player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.unclaiming.unclaimed_chunk_at", pos.x, pos.z), false);
    }
}
