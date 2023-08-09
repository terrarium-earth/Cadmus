package earth.terrarium.cadmus.common.commands.claims.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.common.claims.CadmusDataHandler;
import earth.terrarium.cadmus.common.claims.ClaimSettings;
import earth.terrarium.cadmus.common.commands.claims.CommandHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdminDefaultSettingsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cadmus")
            .requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
            .then(Commands.literal("admin")
                .then(Commands.literal("defaultsettings")
                    .then(canBreak())
                    .then(canPlace())
                    .then(canExplode())
                    .then(canInteractWithBlocks())
                    .then(canInteractWithEntities())
                    .then(canDamageEntities())
                )));
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canBreak() {
        return Commands.literal("canBreak")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        boolean canBreak = BoolArgumentType.getBool(context, "value");
                        CadmusDataHandler.getDefaultClaimSettings(player.server).setCanBreak(TriState.of(canBreak));
                        player.displayClientMessage(setCurrentComponent("canBreak", canBreak), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canBreak = defaultSettings.canBreak(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canBreak", canBreak), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canPlace() {
        return Commands.literal("canPlace")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        boolean canPlace = BoolArgumentType.getBool(context, "value");
                        CadmusDataHandler.getDefaultClaimSettings(player.server).setCanPlace(TriState.of(canPlace));
                        player.displayClientMessage(setCurrentComponent("canPlace", canPlace), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canPlace = defaultSettings.canPlace(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canPlace", canPlace), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canExplode() {
        return Commands.literal("canExplode")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        boolean canExplode = BoolArgumentType.getBool(context, "value");
                        CadmusDataHandler.getDefaultClaimSettings(player.server).setCanExplode(TriState.of(canExplode));
                        player.displayClientMessage(setCurrentComponent("canExplode", canExplode), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canExplode = defaultSettings.canExplode(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canExplode", canExplode), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canInteractWithBlocks() {
        return Commands.literal("canInteractWithBlocks")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        boolean canInteractWithBlocks = BoolArgumentType.getBool(context, "value");
                        CadmusDataHandler.getDefaultClaimSettings(player.server).setCanInteractWithBlocks(TriState.of(canInteractWithBlocks));
                        player.displayClientMessage(setCurrentComponent("canInteractWithBlocks", canInteractWithBlocks), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canInteractWithBlocks = defaultSettings.canInteractWithBlocks(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canInteractWithBlocks", canInteractWithBlocks), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canInteractWithEntities() {
        return Commands.literal("canInteractWithEntities")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        boolean canInteractWithEntities = BoolArgumentType.getBool(context, "value");
                        CadmusDataHandler.getDefaultClaimSettings(player.server).setCanInteractWithEntities(TriState.of(canInteractWithEntities));
                        player.displayClientMessage(setCurrentComponent("canInteractWithEntities", canInteractWithEntities), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canInteractWithEntities = defaultSettings.canInteractWithEntities(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canInteractWithEntities", canInteractWithEntities), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canDamageEntities() {
        return Commands.literal("canDamageEntities")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        boolean canDamageEntities = BoolArgumentType.getBool(context, "value");
                        CadmusDataHandler.getDefaultClaimSettings(player.server).setDamagingEntities(TriState.of(canDamageEntities));
                        player.displayClientMessage(setCurrentComponent("canDamageEntities", canDamageEntities), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canDamageEntities = defaultSettings.canDamageEntities(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canDamageEntities", canDamageEntities), false);
                });
                return 1;
            });
    }

    private static Component getCurrentComponent(String command, Object value) {
        return CommonUtils.serverTranslatable("text.cadmus.settings.current", command, value);
    }

    private static Component setCurrentComponent(String command, Object value) {
        return CommonUtils.serverTranslatable("text.cadmus.settings.set", command, value);
    }
}
