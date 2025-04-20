package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

public class ClaimInfoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("info")
                .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                    .executes(context -> {
                        ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                        getInfo(context.getSource(), pos);
                        return 1;
                    }))
                .executes(context -> {
                    getInfo(context.getSource(), context.getSource().getPlayerOrException().chunkPosition());
                    return 1;
                })
            )
        );
    }

    private static void getInfo(CommandSourceStack source, ChunkPos pos) {
        ClaimApi.API.getClaim(source.getLevel(), pos).ifPresentOrElse(claim -> {
            boolean chunkLoaded = claim.isChunkLoaded();
            Component name = TeamApi.API.getName(source.getLevel(), claim.team());

            source.sendSuccess(() -> CommonComponents.joinLines(
                ModUtils.translatableWithStyle("command.cadmus.info.claimed_by", name.getString()).copy().withStyle(name.getStyle()),
                ModUtils.translatableWithStyle("command.cadmus.info.id", claim.team()),
                ModUtils.translatableWithStyle("command.cadmus.info.position", pos.x, pos.z),
                chunkLoaded ? ConstantComponents.CHUNK_LOADED_TRUE : ConstantComponents.CHUNK_LOADED_FALSE
            ), false);
        }, () -> source.sendFailure(ConstantComponents.NOT_CLAIMED));
    }
}
