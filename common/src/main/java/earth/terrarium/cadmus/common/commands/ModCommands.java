package earth.terrarium.cadmus.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import earth.terrarium.cadmus.common.claims.ClaimAllowedBlocksCommand;
import earth.terrarium.cadmus.common.commands.claims.ClaimCommand;
import earth.terrarium.cadmus.common.commands.claims.ClaimInfoCommand;
import earth.terrarium.cadmus.common.commands.claims.ClaimSettingsCommand;
import earth.terrarium.cadmus.common.commands.claims.UnclaimCommand;
import earth.terrarium.cadmus.common.commands.claims.admin.AdminClaimCommands;
import earth.terrarium.cadmus.common.commands.claims.admin.AdminCommands;
import earth.terrarium.cadmus.common.commands.claims.admin.AdminDefaultSettingsCommand;
import earth.terrarium.cadmus.common.commands.claims.admin.AdminFlagCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection environment) {
        ClaimCommand.register(dispatcher);
        UnclaimCommand.register(dispatcher);
        ClaimInfoCommand.register(dispatcher);
        ClaimSettingsCommand.register(dispatcher);
        ClaimAllowedBlocksCommand.register(dispatcher, ctx);

        AdminCommands.register(dispatcher);
        AdminClaimCommands.register(dispatcher);
        AdminFlagCommands.register(dispatcher);
        AdminDefaultSettingsCommand.register(dispatcher);
    }
}
