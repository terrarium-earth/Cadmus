package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.utils.ModUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimShapeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("shape")
                .then(Commands.argument("sides", IntegerArgumentType.integer(3, 10))
                    .then(Commands.argument("radius", IntegerArgumentType.integer(1, 10))
                        .then(Commands.argument("provider", ResourceLocationArgument.id())
                            .suggests(TeamId.TEAM_PROVIDER_SUGGESTION_PROVIDER)
                            .then(Commands.argument("id", UuidArgument.uuid()))
                            .suggests(TeamId.TEAM_UUID_SUGGESTION_PROVIDER)
                            .then(Commands.argument("chunkload", BoolArgumentType.bool())
                                .executes(context -> {
                                    int sides = IntegerArgumentType.getInteger(context, "sides");
                                    int radius = IntegerArgumentType.getInteger(context, "radius");
                                    boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                                    claim(context.getSource(), TeamId.fromCommand(context), sides, radius, chunkload);
                                    return 1;
                                }))
                            .executes(context -> {
                                int sides = IntegerArgumentType.getInteger(context, "sides");
                                int radius = IntegerArgumentType.getInteger(context, "radius");
                                claim(context.getSource(), TeamId.fromCommand(context), sides, radius, false);
                                return 1;
                            }))
                    ))));
    }

    private static void claim(CommandSourceStack source, TeamId id, int sides, int radius, boolean chunkload) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        BlockPos centerBlock = player.blockPosition();

        List<BlockPos> vertices = new ArrayList<>();
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            int dx = (int) (radius * 16 * Math.cos(angle));
            int dz = (int) (radius * 16 * Math.sin(angle));
            vertices.add(centerBlock.offset(dx, 0, dz));
        }

        int minX = centerBlock.getX() - radius * 16;
        int maxX = centerBlock.getX() + radius * 16;
        int minZ = centerBlock.getZ() - radius * 16;
        int maxZ = centerBlock.getZ() + radius * 16;

        Set<ChunkPos> chunksToClaim = new HashSet<>();

        for (int i = minX; i <= maxX; i += 16) {
            for (int j = minZ; j <= maxZ; j += 16) {
                BlockPos blockPos = new BlockPos(i, centerBlock.getY(), j);
                ChunkPos chunkPos = new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4);
                boolean claimChunk = false;
                for (int k = 0; k < 16 && !claimChunk; k++) {
                    for (int l = 0; l < 16 && !claimChunk; l++) {
                        BlockPos posToCheck = blockPos.offset(k, 0, l);
                        if (isInsideShape(posToCheck, vertices)) {
                            claimChunk = true;
                        }
                    }
                }
                if (claimChunk) {
                    chunksToClaim.add(chunkPos);
                }
            }
        }

        Object2BooleanMap<ChunkPos> finalPositions = new Object2BooleanOpenHashMap<>();
        chunksToClaim.forEach(pos -> {
            if (!ClaimApi.API.isClaimed(source.getLevel(), pos)) {
                finalPositions.put(pos, chunkload);
            }
        });

        int claimsCount = ClaimCommand.getClaimsCount(player, chunkload) + finalPositions.size();
        int maxClaims = chunkload ? ClaimLimitApi.API.getMaxChunkLoadedClaims(id) : ClaimLimitApi.API.getMaxClaims(id);
        if (claimsCount >= maxClaims) {
            throw new SimpleCommandExceptionType(ModUtils.translatableWithStyle(
                "command.cadmus.exception.not_enough_claims",
                claimsCount, maxClaims
            )).create();
        }

        if (!finalPositions.isEmpty()) {
            ClaimApi.API.claim(source.getLevel(), id, finalPositions);
        }

        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            chunkload ?
                "command.cadmus.info.chunk_loaded_chunks" :
                "command.cadmus.info.claimed_chunks",
            finalPositions.size(),
            ClaimCommand.getClaimsCount(player, chunkload), maxClaims
        ), false);
    }

    private static boolean isInsideShape(BlockPos pos, List<BlockPos> vertices) {
        int sides = vertices.size();
        int i, j;
        boolean result = false;
        for (i = 0, j = sides - 1; i < sides; j = i++) {
            if ((vertices.get(i).getZ() > pos.getZ()) != (vertices.get(j).getZ() > pos.getZ()) &&
                (pos.getX() < (vertices.get(j).getX() - vertices.get(i).getX()) * (pos.getZ() - vertices.get(i).getZ()) / (vertices.get(j).getZ() - vertices.get(i).getZ()) + vertices.get(i).getX())) {
                result = !result;
            }
        }
        return result;
    }
}
