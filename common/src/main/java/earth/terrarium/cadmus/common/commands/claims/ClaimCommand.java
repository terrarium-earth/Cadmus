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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicInteger;

public class ClaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.argument("provider", ResourceLocationArgument.id()).suggests(TeamId.TEAM_PROVIDER_SUGGESTION_PROVIDER)
                .then(Commands.argument("id", UuidArgument.uuid()).suggests(TeamId.TEAM_UUID_SUGGESTION_PROVIDER)
                    .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                        .then(Commands.argument("chunkload", BoolArgumentType.bool())
                            .executes(context -> {
                                ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                                boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                                TeamId id = TeamId.fromCommand(context);
                                claim(context.getSource(), id, pos, chunkload);
                                return 1;
                            })
                            .executes(context -> {
                                ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                                TeamId id = TeamId.fromCommand(context);
                                claim(context.getSource(), id, pos, false);
                                return 1;
                            }))
                        .executes(context -> {
                            TeamId id = TeamId.fromCommand(context);
                            claim(context.getSource(), id, context.getSource().getPlayerOrException().chunkPosition(), false);
                            return 1;
                        })
                    )
                )
            )
        );
    }

    private static void claim(CommandSourceStack source, TeamId id, ChunkPos pos, boolean chunkload) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int claimsCount = getClaimsCount(player.level(), id, chunkload) + 1;
        int maxClaims = chunkload ? ClaimLimitApi.API.getMaxChunkLoadedClaims(id) : ClaimLimitApi.API.getMaxClaims(id);
        if (claimsCount > maxClaims) {
            throw new SimpleCommandExceptionType(ModUtils.translatableWithStyle(
                "command.cadmus.exception.maxed_out_claims",
                claimsCount, maxClaims
            )).create();
        }

        checkClaimed(source.getLevel(), pos);

        ClaimApi.API.claim(source.getLevel(), id, pos, chunkload);

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

    public static int getClaimsCount(Level level, TeamId id, boolean chunkload) {
        var claims = ClaimApi.API.getOwnedClaims(level, id).orElse(null);
        if (claims == null) return 0;
        return chunkload ?
            (int) claims.values().stream().filter(loaded -> loaded).count() :
            claims.size();
    }

    public static int getClaimsCount(Player player, boolean chunkload) {
        AtomicInteger count = new AtomicInteger();
        TeamApi.API.getTeamsList(player).forEach(team -> {
            var claims = ClaimApi.API.getOwnedClaims(player.level(), team).orElse(null);
            if (claims != null) {
                if (chunkload) {
                    count.addAndGet((int) claims.values().stream().filter(loaded -> loaded).count());
                } else {
                    count.addAndGet(claims.size());
                }
            }
        });
        return count.get();
    }
}
