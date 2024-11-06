package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnclaimAreaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unclaim")
            .then(Commands.literal("area")
                .then(Commands.argument("startPos", ColumnPosArgument.columnPos())
                    .then(Commands.argument("endPos", ColumnPosArgument.columnPos())
                        .executes(context -> {
                            ChunkPos startPos = ColumnPosArgument.getColumnPos(context, "startPos").toChunkPos();
                            ChunkPos endPos = ColumnPosArgument.getColumnPos(context, "endPos").toChunkPos();
                            unclaim(context.getSource(), startPos, endPos);
                            return 1;
                        })
                    )
                )
            )
        );
    }

    private static void unclaim(CommandSourceStack source, ChunkPos startPos, ChunkPos endPos) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int dist = startPos.getChessboardDistance(endPos);
        if (dist > 50) {
            throw new SimpleCommandExceptionType(ModUtils.translatableWithStyle(
                "command.cadmus.exception.area_too_large",
                dist, 50
            )).create();
        }

        Set<ChunkPos> finalPositions = new HashSet<>();
        Set<ChunkPos> positions = ChunkPos.rangeClosed(startPos, endPos).collect(Collectors.toUnmodifiableSet());

        UUID id = TeamApi.API.getTeams(player);
        positions.forEach(pos ->
            ClaimApi.API.getClaim(source.getLevel(), pos).ifPresent(claim -> {
                if (claim.left().equals(id)) {
                    finalPositions.add(pos);
                }
            })
        );

        ClaimApi.API.unclaim(player, finalPositions);

        int claimsCount = ClaimCommand.getClaimsCount(player, false);
        int maxClaims = ClaimLimitApi.API.getMaxClaims(player);
        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            "command.cadmus.info.unclaimed_chunks_area",
            startPos.x, startPos.z,
            endPos.x, endPos.z,
            claimsCount, maxClaims
        ), false);
    }
}
