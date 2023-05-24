package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;

public class ClaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("chunkload", BoolArgumentType.bool())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                        boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                        CommandHelper.runAction(() -> claim(player, pos, chunkload));
                        return 1;
                    })))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> claim(player, player.blockPosition(), false));
                return 1;
            }));
    }

    public static void claim(ServerPlayer player, BlockPos pos, boolean chunkloaded) throws ClaimException {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.getLevel(), new ChunkPos(pos));
        if (claimData != null) {
            boolean isMember = TeamProviderApi.API.getSelected().isMember(claimData.getFirst(), player.server, player.getUUID());
            throw isMember ? ClaimException.YOUVE_ALREADY_CLAIMED_CHUNK : ClaimException.CHUNK_IS_ALREADY_CLAIMED;
        }
        var claim = Map.of(new ChunkPos(pos), chunkloaded ? ClaimType.CHUNK_LOADED : ClaimType.CLAIMED);
        if (!ModUtils.tryClaim(player.getLevel(), player, claim, Map.of())) {
            throw ClaimException.YOUVE_MAXED_OUT_YOUR_CLAIMS;
        }
        if (chunkloaded) {
            player.displayClientMessage(Component.translatable("text.cadmus.claiming.chunk_loaded_chunk_at", pos.getX(), pos.getY(), pos.getZ()), false);
        } else {
            player.displayClientMessage(Component.translatable("text.cadmus.claiming.claimed_chunk_at", pos.getX(), pos.getY(), pos.getZ()), false);
        }
    }
}
