package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class ClaimInfoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("info")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                        CommandHelper.runAction(() -> claimInfo(player, pos));
                        return 1;
                    }))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> claimInfo(player, player.blockPosition()));
                    return 1;
                })
            ));
    }

    public static void claimInfo(ServerPlayer player, BlockPos pos) {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.getLevel(), new ChunkPos(pos));
        Component status = null;
        if (claimData == null) {
            status = ConstantComponents.UNCLAIMED;
        } else {
            Component displayName = TeamProviderApi.API.getSelected().getTeamName(claimData.getFirst(), player.server);
            boolean isMember = TeamProviderApi.API.getSelected().isMember(claimData.getFirst(), player.server, player.getUUID());
            ChatFormatting color = isMember ? TeamProviderApi.API.getSelected().getTeamColor(claimData.getFirst(), player.server) : ChatFormatting.DARK_RED;
            if (displayName != null && color != null) {
                Component type = switch (claimData.getFirst().split(":")[0]) {
                    case ClaimHandler.TEAM_PREFIX -> ConstantComponents.TEAM;
                    case ClaimHandler.PLAYER_PREFIX -> ConstantComponents.PLAYER;
                    case ClaimHandler.ADMIN_PREFIX -> ConstantComponents.ADMIN;
                    default -> ConstantComponents.UNKNOWN;
                };
                status = switch (claimData.getSecond()) {
                    case CLAIMED ->
                        ModUtils.serverTranslation("text.cadmus.info.claimed_by", displayName.getString(), type.getString());
                    case CHUNK_LOADED ->
                        ModUtils.serverTranslation("text.cadmus.info.chunk_loaded_by", displayName.getString(), type.getString());
                };
                status = status.copy().withStyle(Style.EMPTY.withColor(color).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(claimData.getFirst()).withStyle(color))));
            }
        }
        Component location = ModUtils.serverTranslation("text.cadmus.info.location", SectionPos.blockToSectionCoord(pos.getX()) * 16, SectionPos.blockToSectionCoord(pos.getZ()) * 16);
        if (status != null) {
            player.displayClientMessage(status, false);
        }
        player.displayClientMessage(location, false);
    }
}
