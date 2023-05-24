package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;

public class UnclaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unclaim")
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                    CommandHelper.runAction(() -> unclaim(player, pos));
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> unclaim(player, player.blockPosition()));
                return 1;
            }));
    }

    public static void unclaim(ServerPlayer player, BlockPos pos) throws ClaimException {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.getLevel(), new ChunkPos(pos));
        if (claimData == null) {
            throw ClaimException.THIS_CHUNK_IS_NOT_CLAIMED;
        }
        boolean isMember = TeamProviderApi.API.getSelected().isMember(claimData.getFirst(), player.server, player.getUUID());
        if (!isMember) {
            throw ClaimException.YOU_DONT_OWN_THIS_CHUNK;
        }
        ModUtils.tryClaim(player.getLevel(), player, Map.of(), Map.of(new ChunkPos(pos), ClaimType.CLAIMED));
        player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.unclaiming.unclaimed_chunk_at", pos.getX(), pos.getY(), pos.getZ()), false);
    }
}
