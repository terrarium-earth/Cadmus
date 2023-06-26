package earth.terrarium.cadmus.common.commands.claims.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import earth.terrarium.cadmus.api.claims.admin.FlagApi;
import earth.terrarium.cadmus.api.claims.admin.flags.Flag;
import earth.terrarium.cadmus.common.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.commands.claims.ClaimException;
import earth.terrarium.cadmus.common.commands.claims.CommandHelper;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class AdminFlagCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("claim")
            .requires((commandSourceStack) -> commandSourceStack.hasPermission(2));

        FlagApi.API.getAll().forEach((id, flag) ->
            dispatcher.register(command.then(Commands.literal("admin")
                .then(Commands.literal("flag")
                    .then(Commands.literal("set")
                        .then(Commands.argument("id", StringArgumentType.string())
                            .suggests(AdminCommands.ADMIN_CLAIM_SUGGESTION_PROVIDER)
                            .then(Commands.literal(id)
                                .then(flag.createArgument("value")
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        String adminClaim = StringArgumentType.getString(context, "id");
                                        CommandHelper.runAction(() -> flag(player, adminClaim, id, flag.getFromArgument(context, "value")));
                                        return 1;
                                    })))))
                    .then(Commands.literal("remove")
                        .then(Commands.argument("id", StringArgumentType.string())
                            .suggests(AdminCommands.ADMIN_CLAIM_SUGGESTION_PROVIDER)
                            .then(Commands.literal(id)
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String adminClaim = StringArgumentType.getString(context, "id");
                                    CommandHelper.runAction(() -> remove(player, adminClaim, id));
                                    return 1;
                                }))))
                    .then(Commands.literal("list")
                        .then(Commands.argument("id", StringArgumentType.string())
                            .suggests(AdminCommands.ADMIN_CLAIM_SUGGESTION_PROVIDER)
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                String adminClaim = StringArgumentType.getString(context, "id");
                                CommandHelper.runAction(() -> list(player, adminClaim));
                                return 1;
                            })))))));
    }

    public static void flag(ServerPlayer player, String id, String flagName, Flag<?> flag) throws ClaimException {
        if (AdminClaimHandler.get(player.server, id) == null) {
            throw ClaimException.CLAIM_DOES_NOT_EXIST;
        }
        var oldVal = AdminClaimHandler.getFlag(player.server, id, flagName);
        AdminClaimHandler.setFlag(player.server, id, flagName, flag);
        player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.admin.set_flag", flagName, oldVal, flag.getValue()), false);
    }

    public static void remove(ServerPlayer player, String id, String flagName) throws ClaimException {
        if (AdminClaimHandler.get(player.server, id) == null) {
            throw ClaimException.CLAIM_DOES_NOT_EXIST;
        }
        AdminClaimHandler.removeFlag(player.server, id, flagName);
        player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.admin.remove_flag", flagName), false);
    }

    public static void list(ServerPlayer player, String id) throws ClaimException {
        if (AdminClaimHandler.get(player.server, id) == null) {
            throw ClaimException.CLAIM_DOES_NOT_EXIST;
        }
        Map<String, Flag<?>> flags = AdminClaimHandler.getAllFlags(player.server, id);
        if (flags.isEmpty()) {
            throw ClaimException.CLAIM_HAS_NO_FLAGS;
        }
        flags.forEach((name, value) -> player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.admin.list", name, value.getValue()), false));
    }
}

// ColumnPosArgument