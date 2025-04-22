package earth.terrarium.cadmus.common.commands.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import earth.terrarium.cadmus.api.flags.Flag;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.teams.AdminTeamProvider;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.UUID;

public class FlagCommands {

    private static final SimpleCommandExceptionType TEAM_HAS_NO_FLAGS = new SimpleCommandExceptionType(ConstantComponents.ADMIN_TEAM_HAS_NO_FLAGS);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("cadmus")
            .requires(source -> source.hasPermission(2));

        FlagApi.API.getAllDefaults().forEach((name, flag) ->
            dispatcher.register(command
                .then(Commands.literal("adminclaims")
                    .then(Commands.literal("flags")
                        .then(Commands.literal("set")
                            .then(Commands.argument("team", StringArgumentType.string())
                                .suggests(AdminClaimCommands.ADMIN_TEAM_SUGGESTION_PROVIDER)
                                .then(Commands.literal(name)
                                    .then(flag.createArgument("value")
                                        .executes(context -> {
                                            String team = StringArgumentType.getString(context, "team");
                                            Flag<?> value = flag.getFromArgument("value", context);
                                            set(context.getSource(), team, name, value);
                                            return 1;
                                        })
                                    )
                                )
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("team", StringArgumentType.string())
                                .suggests(AdminClaimCommands.ADMIN_TEAM_SUGGESTION_PROVIDER)
                                .then(Commands.literal(name)
                                    .executes(context -> {
                                        String team = StringArgumentType.getString(context, "team");
                                        remove(context.getSource(), name, team);
                                        return 1;
                                    })
                                )
                            )
                        )
                        .then(Commands.literal("list")
                            .then(Commands.argument("team", StringArgumentType.string())
                                .suggests(AdminClaimCommands.ADMIN_TEAM_SUGGESTION_PROVIDER)
                                .executes(context -> {
                                    String team = StringArgumentType.getString(context, "team");
                                    list(context.getSource(), team);
                                    return 1;
                                })
                            )
                        )
                    )
                )
            )
        );
    }

    private static void set(CommandSourceStack source, String team, String flagName, Flag<?> flag) throws CommandSyntaxException {
        if (!FlagApi.API.isAdminTeam(source.getServer(), team))
            throw AdminClaimCommands.ADMIN_TEAM_DOES_NOT_EXIST.create();

        FlagApi.API.getIdFromName(source.getServer(), team).ifPresent(id -> {
            Flag<?> oldFlagValue = FlagApi.API.getFlag(source.getServer(), id, flagName);
            FlagApi.API.setFlag(source.getServer(), id, flagName, flag);
            source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.admin.set_flag", flagName, oldFlagValue, flag), false);
            TeamApi.API.syncTeamInfo(source.getServer(), TeamId.ofAdmin(id), true);
        });
    }

    private static void remove(CommandSourceStack source, String flagName, String team) throws CommandSyntaxException {
        if (!FlagApi.API.isAdminTeam(source.getServer(), team))
            throw AdminClaimCommands.ADMIN_TEAM_DOES_NOT_EXIST.create();

        FlagApi.API.getIdFromName(source.getServer(), team).ifPresent(id -> {
            FlagApi.API.removeFlag(source.getServer(), id, flagName);
            source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.admin.remove_flag", flagName), false);
            TeamApi.API.syncTeamInfo(source.getServer(), TeamId.ofAdmin(id), true);
        });
    }

    private static void list(CommandSourceStack source, String team) throws CommandSyntaxException {
        if (!FlagApi.API.isAdminTeam(source.getServer(), team))
            throw AdminClaimCommands.ADMIN_TEAM_DOES_NOT_EXIST.create();

        UUID id = FlagApi.API.getIdFromName(source.getServer(), team).orElse(null);
        if (id == null) throw AdminClaimCommands.ADMIN_TEAM_DOES_NOT_EXIST.create();
        var flags = FlagApi.API.getAllAdminTeams(source.getServer()).get(id);
        if (flags.isEmpty()) throw TEAM_HAS_NO_FLAGS.create();
        flags.forEach((name, flag) -> source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.admin.list_flags", name, flag), false));
    }
}
