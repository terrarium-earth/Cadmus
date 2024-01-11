package earth.terrarium.cadmus.common.commands.claims.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.CadmusDataHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.commands.claims.ClaimException;
import earth.terrarium.cadmus.common.commands.claims.CommandHelper;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class AdminCommands {
    public static final SuggestionProvider<CommandSourceStack> TEAM_SUGGESTION_PROVIDER = (context, builder) -> {
        var player = context.getSource().getPlayerOrException();
        var teams = ClaimHandler.getAllTeamClaims(player.serverLevel());
        return SharedSuggestionProvider.suggest((teams.keySet().stream().map(s -> "\"" + s + "\"")), builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cadmus")
            .requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
            .then(Commands.literal("admin")
                .then(Commands.literal("bypass")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        CommandHelper.runAction(() -> bypass(player));
                        return 1;
                    }))
                .then(Commands.literal("clear")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .suggests(TEAM_SUGGESTION_PROVIDER)
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(context, "id");
                            CommandHelper.runAction(() -> clear(player, id));
                            return 1;
                        })))
                .then(Commands.literal("claim")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .suggests(TEAM_SUGGESTION_PROVIDER)
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .then(Commands.argument("chunkload", BoolArgumentType.bool())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ColumnPos pos = ColumnPosArgument.getColumnPos(context, "pos");
                                    String id = StringArgumentType.getString(context, "id");
                                    boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                                    CommandHelper.runAction(() -> claim(player, pos.toChunkPos(), id, chunkload));
                                    return 1;
                                })))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(context, "id");
                            CommandHelper.runAction(() -> claim(player, player.chunkPosition(), id, false));
                            return 1;
                        })))
                .then(Commands.literal("clearall")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        CommandHelper.runAction(() -> clearAll(player));
                        return 1;
                    }))
                .then(Commands.literal("unclaim")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .suggests(TEAM_SUGGESTION_PROVIDER)
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                ColumnPos pos = ColumnPosArgument.getColumnPos(context, "pos");
                                String id = StringArgumentType.getString(context, "id");
                                CommandHelper.runAction(() -> unclaim(player, pos.toChunkPos(), id));
                                return 1;
                            }))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(context, "id");
                            CommandHelper.runAction(() -> unclaim(player, player.chunkPosition(), id));
                            return 1;
                        }))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        CommandHelper.runAction(() -> unclaim(player, player.chunkPosition()));
                        return 1;
                    }))
                .then(Commands.literal("list")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        CommandHelper.runAction(() -> list(player));
                        return 1;
                    }))
            ));
    }

    public static void bypass(ServerPlayer player) {
        CadmusDataHandler.toggleBypass(player.server, player.getUUID());
        if (CadmusDataHandler.canBypass(player.server, player.getUUID())) {
            player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.bypass.enable", player.getGameProfile().getName()), false);
        } else {
            player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.bypass.disable", player.getGameProfile().getName()), false);
        }
    }

    public static void clear(ServerPlayer player, String id) {
        player.server.getAllLevels().forEach(l -> ClaimHandler.clear(l, id));
        Component name = TeamHelper.getTeamName(id, player.server);
        if (name == null) return;
        player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.clear", name.getString()), false);
    }

    public static void clearAll(ServerPlayer player) {
        player.server.getAllLevels().forEach(ClaimHandler::clearAll);
        player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.clear_all"), false);
    }

    public static void claim(ServerPlayer player, ChunkPos pos, String id, boolean chunkloaded) throws ClaimException {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.serverLevel(), pos);
        if (claimData != null) {
            boolean isMember = TeamHelper.isMember(claimData.getFirst(), player.server, player.getUUID());
            throw isMember ? ClaimException.ALREADY_CLAIMED_CHUNK : ClaimException.CHUNK_ALREADY_CLAIMED;
        }
        ClaimApi.API.claim(player.serverLevel(), pos, id, chunkloaded);
        if (chunkloaded) {
            player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.claiming.chunk_loaded_chunk_at", pos.x, pos.z), false);
        } else {
            player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.claiming.claimed_chunk_at", pos.x, pos.z), false);
        }
    }

    public static void unclaim(ServerPlayer player, ChunkPos pos) throws ClaimException {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.serverLevel(), pos);
        if (claimData == null) throw ClaimException.CHUNK_NOT_CLAIMED;
        ClaimApi.API.unclaim(player.serverLevel(), pos, claimData.getFirst());
        player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.unclaiming.unclaimed_chunk_at", pos.x, pos.z), false);
    }

    public static void unclaim(ServerPlayer player, ChunkPos pos, String id) throws ClaimException {
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.serverLevel(), pos);
        if (claimData == null) throw ClaimException.CHUNK_NOT_CLAIMED;
        if (!claimData.getFirst().equals(id)) throw ClaimException.DONT_OWN_CHUNK;
        ClaimApi.API.unclaim(player.serverLevel(), pos, id);
        player.displayClientMessage(CommonUtils.serverTranslatable("text.cadmus.unclaiming.unclaimed_chunk_at", pos.x, pos.z), false);
    }

    public static void list(ServerPlayer player) {
        ClaimHandler.getAllTeamClaims(player.serverLevel()).forEach((id, chunks) -> {
            Component name = TeamHelper.getTeamName(id, player.server);
            if (name == null) return;
            player.displayClientMessage(Component.empty(), false);
            player.displayClientMessage(name, false);
            player.displayClientMessage(Component.literal(id), false);
        });
    }
}
