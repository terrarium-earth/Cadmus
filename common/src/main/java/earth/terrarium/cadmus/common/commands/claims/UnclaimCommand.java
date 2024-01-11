package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class UnclaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unclaim")
            .then(Commands.literal("all")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> clear(player));
                    return 1;
                }))
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
        if (claimData == null) throw ClaimException.CHUNK_NOT_CLAIMED;
        boolean isMember = TeamHelper.isMember(claimData.getFirst(), player.server, player.getUUID());
        if (!isMember) throw ClaimException.DONT_OWN_CHUNK;
        if (ModUtils.isAdmin(claimData.getFirst())) throw ClaimException.CANT_UNLCLAIM_ADMIN;
        ClaimApi.API.unclaim(player.serverLevel(), pos, player);
        player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.unclaiming.unclaimed_chunk_at", pos.x, pos.z), false);
    }

    public static void clear(ServerPlayer player) {
        String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());
        player.server.getAllLevels().forEach(l -> ClaimHandler.clear(l, id));
        Component name = TeamHelper.getTeamName(id, player.server);
        player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.clear", name == null ? player.getDisplayName().getString() : name.getString()), false);
    }
}
