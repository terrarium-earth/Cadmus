package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class ClaimInfoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("info")
                .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ColumnPos pos = ColumnPosArgument.getColumnPos(context, "pos");
                        CommandHelper.runAction(() -> claimInfo(player, pos.toChunkPos()));
                        return 1;
                    }))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> claimInfo(player, player.chunkPosition()));
                    return 1;
                })
            ));
    }

    public static void claimInfo(ServerPlayer player, ChunkPos pos) {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.serverLevel(), pos);
        Component status = null;
        if (claimData == null) {
            status = ConstantComponents.UNCLAIMED;
        } else {
            Component displayName = TeamHelper.getTeamName(claimData.getFirst(), player.server);
            boolean isMember = TeamHelper.isMember(claimData.getFirst(), player.server, player.getUUID());
            ChatFormatting color = isMember ? TeamHelper.getTeamColor(claimData.getFirst(), player.server) : ChatFormatting.DARK_RED;
            if (displayName != null && color != null) {
                Component type = switch (claimData.getFirst().charAt(0)) {
                    case 't' -> ConstantComponents.TEAM;
                    case 'p' -> ConstantComponents.PLAYER;
                    case 'a' -> ConstantComponents.ADMIN;
                    default -> ConstantComponents.UNKNOWN;
                };
                status = switch (claimData.getSecond()) {
                    case CLAIMED ->
                        CommonUtils.serverTranslatable("text.cadmus.info.claimed_by", displayName.getString(), type.getString());
                    case CHUNK_LOADED ->
                        CommonUtils.serverTranslatable("text.cadmus.info.chunk_loaded_by", displayName.getString(), type.getString());
                };
                status = status.copy().withStyle(Style.EMPTY.withColor(color).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(claimData.getFirst()).withStyle(color))));
            }
        }
        Component location = CommonUtils.serverTranslatable("text.cadmus.info.location", pos.x, pos.z);
        if (status != null) {
            player.displayClientMessage(status, false);
        }
        player.displayClientMessage(location, false);
    }
}
