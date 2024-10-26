package earth.terrarium.cadmus.common.commands.admin;

import com.mojang.brigadier.CommandDispatcher;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class BypassCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cadmus")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("bypass")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> {
                        bypass(context.getSource(), EntityArgument.getPlayer(context, "player"));
                        return 1;
                    })
                )
                .executes(context -> {
                    bypass(context.getSource(), context.getSource().getPlayerOrException());
                    return 1;
                })
            )
        );
    }

    private static void bypass(CommandSourceStack source, ServerPlayer player) {
        UUID id = player.getUUID();
        CadmusSaveData.toggleBypass(source.getServer(), id);
        Component name = Component.literal(player.getGameProfile().getName()).withStyle(TeamApi.API.getColor(source.getLevel(), id).getAsStyle());
        if (CadmusSaveData.canBypass(source.getServer(), id)) {
            source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.bypass.enable", name), false);
        } else {
            source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.bypass.disable", name), false);
        }
    }
}
