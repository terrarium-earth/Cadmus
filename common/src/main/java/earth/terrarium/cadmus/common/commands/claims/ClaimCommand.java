package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ClaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                .then(Commands.argument("chunkload", BoolArgumentType.bool())
                    .executes(context -> {
                        ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                        boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                        claim(context.getSource(), pos, chunkload);
                        return 1;
                    }))
                .executes(context -> {
                    ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                    claim(context.getSource(), pos, false);
                    return 1;
                }))
            .executes(context -> {
                claim(context.getSource(), context.getSource().getPlayerOrException().chunkPosition(), false);
                return 1;
            })
        );
    }

    private static void claim(CommandSourceStack source, ChunkPos pos, boolean chunkload) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int claimsCount = getClaimsCount(player, chunkload) + 1;
        int maxClaims = chunkload ? ClaimLimitApi.API.getMaxChunkLoadedClaims(player) : ClaimLimitApi.API.getMaxClaims(player);
        if (claimsCount > maxClaims) {
            throw new SimpleCommandExceptionType(ModUtils.translatableWithStyle(
                "command.cadmus.exception.maxed_out_claims",
                claimsCount, maxClaims
            )).create();
        }

        checkClaimed(source.getLevel(), pos);

        ClaimApi.API.claim(player, pos, chunkload);

        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            chunkload ?
                "command.cadmus.info.chunk_loaded_chunk_at" :
                "command.cadmus.info.claimed_chunk_at",
            pos.x, pos.z,
            claimsCount, maxClaims
        ), false);
    }

    public static void checkClaimed(ServerLevel level, ChunkPos pos) throws CommandSyntaxException {
        var claim = ClaimApi.API.getClaim(level, pos);
        if (claim.isPresent()) {
            Component name = TeamApi.API.getName(level, claim.get().left());
            throw new SimpleCommandExceptionType(ModUtils.translatableWithStyle(
                "command.cadmus.exception.already_claimed",
                name
            )).create();
        }
    }

    public static int getClaimsCount(Player player, boolean chunkload) {
        return getClaimsCount(player.level(), TeamApi.API.getTeams(player), chunkload);
    }

    public static int getClaimsCount(Level level, UUID id, boolean chunkload) {
        var claims = ClaimApi.API.getOwnedClaims(level, id).orElse(null);
        if (claims == null) return 0;
        return chunkload ?
            (int) claims.values().stream().filter(loaded -> loaded).count() :
            claims.size();
    }
}
