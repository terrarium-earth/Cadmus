package earth.terrarium.cadmus.common.commands.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.flags.types.ColorFlag;
import earth.terrarium.cadmus.api.flags.types.StringFlag;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.commands.claims.ClaimCommand;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.teams.AdminTeamProvider;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
import java.util.UUID;

public class AdminClaimCommands {

    private static final SimpleCommandExceptionType ADMIN_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType(ConstantComponents.ADMIN_TEAM_ALREADY_EXISTS);
    public static final SimpleCommandExceptionType ADMIN_TEAM_DOES_NOT_EXIST = new SimpleCommandExceptionType(ConstantComponents.ADMIN_TEAM_DOES_NOT_EXIST);

    public static final SuggestionProvider<CommandSourceStack> ADMIN_TEAM_SUGGESTION_PROVIDER = (context, builder) -> {
        Collection<String> names = FlagApi.API.getAllAdminTeamNames(context.getSource().getServer());
        return SharedSuggestionProvider.suggest(names, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cadmus")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("adminclaims")
                .then(Commands.literal("create")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .executes(context -> {
                            String team = StringArgumentType.getString(context, "id");
                            create(context.getSource(), team);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .suggests(ADMIN_TEAM_SUGGESTION_PROVIDER)
                        .executes(context -> {
                            String team = StringArgumentType.getString(context, "id");
                            remove(context.getSource(), team);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("claim")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .suggests(ADMIN_TEAM_SUGGESTION_PROVIDER)
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .executes(context -> {
                                ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                                String team = StringArgumentType.getString(context, "id");
                                claim(context.getSource(), pos, team);
                                return 1;
                            })
                        )
                        .executes(context -> {
                            String team = StringArgumentType.getString(context, "id");
                            claim(context.getSource(), context.getSource().getPlayerOrException().chunkPosition(), team);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("unclaim")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .suggests(ADMIN_TEAM_SUGGESTION_PROVIDER)
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .executes(context -> {
                                ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                                String team = StringArgumentType.getString(context, "id");
                                unclaim(context.getSource(), pos, team);
                                return 1;
                            })
                        )
                        .executes(context -> {
                            String team = StringArgumentType.getString(context, "id");
                            unclaim(context.getSource(), context.getSource().getPlayerOrException().chunkPosition(), team);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("clear")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .suggests(ADMIN_TEAM_SUGGESTION_PROVIDER)
                        .executes(context -> {
                            String team = StringArgumentType.getString(context, "id");
                            unclaimAll(context.getSource(), team);
                            return 1;
                        })
                    )
                )
            )
        );
    }

    private static void create(CommandSourceStack source, String team) throws CommandSyntaxException {
        if (FlagApi.API.isAdminTeam(source.getServer(), team)) throw ADMIN_TEAM_ALREADY_EXISTS.create();
        UUID id = FlagApi.API.createAdminTeam(source.getServer(), team);
        FlagApi.API.setFlag(source.getServer(), id, Flags.DISPLAY_NAME.id(), new StringFlag(Flags.DISPLAY_NAME.id(), team));
        FlagApi.API.setFlag(source.getServer(), id, Flags.COLOR.id(), new ColorFlag(Flags.COLOR.id(), Color.DEFAULT));
        source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.admin.create", team), false);
        TeamApi.API.syncTeamInfo(source.getServer(), new TeamId(AdminTeamProvider.ID, id), true);
    }

    private static void remove(CommandSourceStack source, String team) throws CommandSyntaxException {
        if (!FlagApi.API.isAdminTeam(source.getServer(), team)) throw ADMIN_TEAM_DOES_NOT_EXIST.create();
        UUID id = FlagApi.API.getIdFromName(source.getServer(), team).orElse(null);
        if (id == null) throw ADMIN_TEAM_DOES_NOT_EXIST.create();

        FlagApi.API.removeAdminTeam(source.getServer(), id);
        source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.admin.remove", team), false);
    }

    private static void claim(CommandSourceStack source, ChunkPos pos, String team) throws CommandSyntaxException {
        UUID id = FlagApi.API.getIdFromName(source.getServer(), team).orElse(null);
        if (id == null) throw AdminClaimCommands.ADMIN_TEAM_DOES_NOT_EXIST.create();

        TeamId teamId = new TeamId(AdminTeamProvider.ID, id);
        ClaimCommand.checkClaimed(source.getLevel(), pos, teamId);

        ClaimApi.API.claim(source.getLevel(), teamId, pos, false);

        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            "command.cadmus.info.claimed_admin_chunk_at",
            pos.x, pos.z
        ), false);
    }

    private static void unclaim(CommandSourceStack source, ChunkPos pos, String team) throws CommandSyntaxException {
        UUID id = FlagApi.API.getIdFromName(source.getServer(), team).orElse(null);
        if (id == null) throw AdminClaimCommands.ADMIN_TEAM_DOES_NOT_EXIST.create();

        ClaimApi.API.unclaim(source.getLevel(), new TeamId(AdminTeamProvider.ID, id), pos);

        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            "command.cadmus.info.unclaimed_admin_chunk_at",
            pos.x, pos.z
        ), false);
    }

    private static void unclaimAll(CommandSourceStack source, String team) throws CommandSyntaxException {
        UUID id = FlagApi.API.getIdFromName(source.getServer(), team).orElse(null);
        TeamId adminID = TeamId.ofAdmin(id);
        if (id == null) throw AdminClaimCommands.ADMIN_TEAM_DOES_NOT_EXIST.create();

        int oldClaimsCount = ClaimCommand.getClaimsCount(source.getLevel(), adminID, false);
        ClaimApi.API.clear(source.getLevel(), adminID);
        int diff = oldClaimsCount - ClaimCommand.getClaimsCount(source.getLevel(), adminID, false);
        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            "command.cadmus.info.unclaimed_all_admin",
            diff
        ), false);
    }
}
