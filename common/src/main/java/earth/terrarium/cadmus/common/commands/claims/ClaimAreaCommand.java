package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.utils.ModUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClaimAreaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("area")
                .then(Commands.argument("startPos", ColumnPosArgument.columnPos())
                    .then(Commands.argument("endPos", ColumnPosArgument.columnPos())
                        .then(Commands.argument("chunkload", BoolArgumentType.bool())
                            .executes(context -> {
                                ChunkPos startPos = ColumnPosArgument.getColumnPos(context, "startPos").toChunkPos();
                                ChunkPos endPos = ColumnPosArgument.getColumnPos(context, "endPos").toChunkPos();
                                boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                                TeamId id = TeamId.getId(context);
                                claim(context.getSource(), id, startPos, endPos, chunkload);
                                return 1;
                            }))
                        .executes(context -> {
                            ChunkPos startPos = ColumnPosArgument.getColumnPos(context, "startPos").toChunkPos();
                            ChunkPos endPos = ColumnPosArgument.getColumnPos(context, "endPos").toChunkPos();
                            TeamId id = TeamId.getId(context);
                            claim(context.getSource(), id, startPos, endPos, false);
                            return 1;
                        })
                    )
                )
            )
        );
    }

    private static void claim(CommandSourceStack source, TeamId id, ChunkPos startPos, ChunkPos endPos, boolean chunkload) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int dist = startPos.getChessboardDistance(endPos);
        if (dist > 50) {
            throw new SimpleCommandExceptionType(ModUtils.translatableWithStyle(
                "command.cadmus.exception.area_too_large",
                dist, 50
            )).create();
        }

        Object2BooleanMap<ChunkPos> finalPositions = new Object2BooleanOpenHashMap<>();
        Set<ChunkPos> positions = ChunkPos.rangeClosed(startPos, endPos).collect(Collectors.toUnmodifiableSet());

        positions.forEach(pos ->
            ClaimApi.API.getClaim(source.getLevel(), pos).ifPresentOrElse(claim -> {
                if (claim.left().equals(id) && claim.rightBoolean()) {
                    finalPositions.put(pos, chunkload);
                }
            }, () -> finalPositions.put(pos, chunkload))
        );

        int claimsCount = ClaimCommand.getClaimsCount(player, chunkload) + finalPositions.size();
        int maxClaims = chunkload ? ClaimLimitApi.API.getMaxChunkLoadedClaims(id) : ClaimLimitApi.API.getMaxClaims(id);
        if (claimsCount > maxClaims) {
            throw new SimpleCommandExceptionType(ModUtils.translatableWithStyle(
                "command.cadmus.exception.not_enough_claims",
                claimsCount, maxClaims
            )).create();
        }

        ClaimApi.API.claim(source.getLevel(), id, finalPositions);

        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            chunkload ?
                "command.cadmus.info.chunk_loaded_chunks_area" :
                "command.cadmus.info.claimed_chunks_area",
            startPos.x, startPos.z,
            endPos.x, endPos.z,
            ClaimCommand.getClaimsCount(player, chunkload), maxClaims
        ), false);
    }
}
