package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import com.teamresourceful.resourcefullib.common.utils.modinfo.ModInfoUtils;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.CadmusDataHandler;
import earth.terrarium.cadmus.common.claims.ClaimSettings;
import earth.terrarium.cadmus.common.commands.arguments.TriStateArgument;
import earth.terrarium.cadmus.common.compat.prometheus.CadmusAutoCompletes;
import earth.terrarium.cadmus.common.compat.prometheus.PrometheusIntegration;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import earth.terrarium.cadmus.common.util.ModGameRules;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public class ClaimSettingsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("settings")
                .then(canBreak())
                .then(canPlace())
                .then(canExplode())
                .then(canInteractWithBlocks())
                .then(canInteractWithEntities())
                .then(canDamageEntities())
            ));
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canBreak() {
        return Commands.literal("canBreak")
            .then(Commands.argument("value", TriStateArgument.triState())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        if (!checkPrometheusPermissions(player, CadmusAutoCompletes.BLOCK_BREAKING, ModGameRules.RULE_DO_CLAIMED_BLOCK_BREAKING)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());

                        if (ModUtils.isTeam(id) && !TeamProviderApi.API.getSelected().canModifySettings(id, player)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        TriState canBreak = TriStateArgument.getTriState(context, "value");
                        CadmusDataHandler.getClaimSettings(player.server, id).setCanBreak(canBreak);
                        player.displayClientMessage(setCurrentComponent("canBreak", canBreak), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());
                    ClaimSettings settings = CadmusDataHandler.getClaimSettings(player.server, id);
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canBreak = settings.canBreak(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canBreak", canBreak), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canPlace() {
        return Commands.literal("canPlace")
            .then(Commands.argument("value", TriStateArgument.triState())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        if (!checkPrometheusPermissions(player, CadmusAutoCompletes.BLOCK_PLACING, ModGameRules.RULE_DO_CLAIMED_BLOCK_PLACING)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());

                        if (ModUtils.isTeam(id) && !TeamProviderApi.API.getSelected().canModifySettings(TeamHelper.teamId(id), player)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        TriState canPlace = TriStateArgument.getTriState(context, "value");
                        CadmusDataHandler.getClaimSettings(player.server, id).setCanPlace(canPlace);
                        player.displayClientMessage(setCurrentComponent("canPlace", canPlace), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());
                    ClaimSettings settings = CadmusDataHandler.getClaimSettings(player.server, id);
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canPlace = settings.canPlace(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canPlace", canPlace), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canExplode() {
        return Commands.literal("canExplode")
            .then(Commands.argument("value", TriStateArgument.triState())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        if (!checkPrometheusPermissions(player, CadmusAutoCompletes.BLOCK_EXPLOSIONS, ModGameRules.RULE_DO_CLAIMED_BLOCK_EXPLOSIONS)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());

                        if (ModUtils.isTeam(id) && !TeamProviderApi.API.getSelected().canModifySettings(id, player)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        TriState canExplode = TriStateArgument.getTriState(context, "value");
                        CadmusDataHandler.getClaimSettings(player.server, id).setCanExplode(canExplode);
                        player.displayClientMessage(setCurrentComponent("canExplode", canExplode), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());
                    ClaimSettings settings = CadmusDataHandler.getClaimSettings(player.server, id);
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canExplode = settings.canExplode(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canExplode", canExplode), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canInteractWithBlocks() {
        return Commands.literal("canInteractWithBlocks")
            .then(Commands.argument("value", TriStateArgument.triState())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        if (!checkPrometheusPermissions(player, CadmusAutoCompletes.BLOCK_INTERACTIONS, ModGameRules.RULE_DO_CLAIMED_BLOCK_INTERACTIONS)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());

                        if (ModUtils.isTeam(id) && !TeamProviderApi.API.getSelected().canModifySettings(id, player)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        TriState canInteractWithBlocks = TriStateArgument.getTriState(context, "value");
                        CadmusDataHandler.getClaimSettings(player.server, id).setCanInteractWithBlocks(canInteractWithBlocks);
                        player.displayClientMessage(setCurrentComponent("canInteractWithBlocks", canInteractWithBlocks), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());
                    ClaimSettings settings = CadmusDataHandler.getClaimSettings(player.server, id);
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canInteractWithBlocks = settings.canInteractWithBlocks(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canInteractWithBlocks", canInteractWithBlocks), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canInteractWithEntities() {
        return Commands.literal("canInteractWithEntities")
            .then(Commands.argument("value", TriStateArgument.triState())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        if (!checkPrometheusPermissions(player, CadmusAutoCompletes.ENTITY_INTERACTIONS, ModGameRules.RULE_DO_CLAIMED_ENTITY_INTERACTIONS)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());

                        if (ModUtils.isTeam(id) && !TeamProviderApi.API.getSelected().canModifySettings(id, player)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        TriState canInteractWithEntities = TriStateArgument.getTriState(context, "value");
                        CadmusDataHandler.getClaimSettings(player.server, id).setCanInteractWithEntities(canInteractWithEntities);
                        player.displayClientMessage(setCurrentComponent("canInteractWithEntities", canInteractWithEntities), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());
                    ClaimSettings settings = CadmusDataHandler.getClaimSettings(player.server, id);
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canInteractWithEntities = settings.canInteractWithEntities(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canInteractWithEntities", canInteractWithEntities), false);
                });
                return 1;
            });
    }

    private static ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> canDamageEntities() {
        return Commands.literal("canDamageEntities")
            .then(Commands.argument("value", TriStateArgument.triState())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CommandHelper.runAction(() -> {
                        if (!checkPrometheusPermissions(player, CadmusAutoCompletes.ENTITY_DAMAGE, ModGameRules.RULE_CLAIMED_DAMAGE_ENTITIES)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());

                        if (ModUtils.isTeam(id) && !TeamProviderApi.API.getSelected().canModifySettings(id, player)) {
                            throw ClaimException.NOT_ALLOWED_TO_MANAGE_SETTINGS;
                        }

                        TriState canDamageEntities = TriStateArgument.getTriState(context, "value");
                        CadmusDataHandler.getClaimSettings(player.server, id).setCanDamageEntities(canDamageEntities);
                        player.displayClientMessage(setCurrentComponent("canDamageEntities", canDamageEntities), false);
                    });
                    return 1;
                }))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                CommandHelper.runAction(() -> {
                    String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());
                    ClaimSettings settings = CadmusDataHandler.getClaimSettings(player.server, id);
                    ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(player.server);
                    boolean canDamageEntities = settings.canDamageEntities(defaultSettings);
                    player.displayClientMessage(getCurrentComponent("canDamageEntities", canDamageEntities), false);
                });
                return 1;
            });
    }

    private static Component getCurrentComponent(String command, Object value) {
        return CommonUtils.serverTranslatable("text.cadmus.settings.current", command, value);
    }

    private static Component setCurrentComponent(String command, TriState value) {
        String text = value.isTrue() ? "true" : value.isFalse() ? "false" : "default";
        return CommonUtils.serverTranslatable("text.cadmus.settings.set", command, text);
    }

    private static boolean checkPrometheusPermissions(ServerPlayer player, String permission, GameRules.Key<GameRules.BooleanValue> rule) {
        if (ModInfoUtils.isModLoaded("prometheus") && PrometheusIntegration.hasPermission(player, permission)) {
            return true;
        } else {
            return !ModInfoUtils.isModLoaded("prometheus") && ModGameRules.getOrCreateBooleanGameRule(player.serverLevel(), rule);
        }
    }
}
