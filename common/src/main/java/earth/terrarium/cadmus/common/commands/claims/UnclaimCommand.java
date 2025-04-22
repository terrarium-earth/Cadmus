package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class UnclaimCommand {

    public static final SimpleCommandExceptionType NOT_CLAIMED = new SimpleCommandExceptionType(ConstantComponents.NOT_CLAIMED);
    private static final SimpleCommandExceptionType NOT_OWNER = new SimpleCommandExceptionType(ConstantComponents.NOT_OWNER);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unclaim")
            .then(Commands.literal("all")
                .executes(context -> {
                    unclaimAll(context.getSource());
                    return 1;
                }))
            .then(Commands.argument("provider", ResourceLocationArgument.id()).suggests(TeamId.TEAM_PROVIDER_SUGGESTION_PROVIDER)
                .then(Commands.argument("id", UuidArgument.uuid()).suggests(TeamId.TEAM_UUID_SUGGESTION_PROVIDER)
                    .executes(context -> {
                        unclaimAll(context.getSource(), TeamId.fromCommand(context));
                        return 1;
                    })
                )
            )
            .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                .executes(context -> {
                    ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                    unclaim(context.getSource(), pos);
                    return 1;
                })
            )
            .executes(context -> {
                unclaim(context.getSource(), context.getSource().getPlayerOrException().chunkPosition());
                return 1;
            })
        );
    }

    private static void unclaim(CommandSourceStack source, ChunkPos pos) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var claim = ClaimApi.API.getClaim(source.getLevel(), pos);
        if (claim.isEmpty()) throw NOT_CLAIMED.create();
        else {
            var claims = ClaimApi.API.getOwnedClaims(player).orElse(null);
            if (claims == null || !claims.containsKey(pos)) throw NOT_OWNER.create();
        }

        ClaimApi.API.getClaim(source.getLevel(), pos).ifPresent(team -> {
            ClaimApi.API.unclaim(source.getLevel(), team.team(), pos);
            int claimsCount = ClaimCommand.getClaimsCount(source.getLevel(), team.team(), false);
            int maxClaims = ClaimLimitApi.API.getMaxClaims(team.team());
            source.sendSuccess(() -> ModUtils.translatableWithStyle(
                "command.cadmus.info.unclaimed_chunk_at",
                pos.x, pos.z,
                claimsCount, maxClaims
            ), false);
        });
    }

    private static void unclaimAll(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int oldClaimsCount = ClaimCommand.getClaimsCount(player, false);
        ClaimApi.API.clear(source.getPlayer());
        int diff = oldClaimsCount - ClaimCommand.getClaimsCount(player, false);
        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            "command.cadmus.info.unclaimed_all",
            diff
        ), false);
    }

    private static void unclaimAll(CommandSourceStack source, TeamId id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int oldClaimsCount = ClaimCommand.getClaimsCount(player, false);
        ClaimApi.API.clear(source.getLevel(), id);
        int diff = oldClaimsCount - ClaimCommand.getClaimsCount(player, false);
        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            "command.cadmus.info.unclaimed_all",
            diff
        ), false);
    }
}
