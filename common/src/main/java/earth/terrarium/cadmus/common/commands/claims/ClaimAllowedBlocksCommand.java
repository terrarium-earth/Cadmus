package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ClaimAllowedBlocksCommand {

    private static final SimpleCommandExceptionType BLOCK_NOT_ADDED = new SimpleCommandExceptionType(ConstantComponents.BLOCK_NOT_ADDED);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("settings")
                .then(Commands.argument("provider", ResourceLocationArgument.id()).suggests(TeamId.TEAM_PROVIDER_SUGGESTION_PROVIDER)
                    .then(Commands.argument("id", UuidArgument.uuid()).suggests(TeamId.TEAM_UUID_SUGGESTION_PROVIDER)
                        .then(Commands.literal("allowedBlocks")
                            .then(Commands.literal("add")
                                .then(Commands.argument("value", BlockStateArgument.block(buildContext))
                                    .executes(context -> {
                                        BlockState block = BlockStateArgument.getBlock(context, "value").getState();
                                        TeamId id = TeamId.fromCommand(context);
                                        addBlock(context.getSource(), block, id);
                                        return 1;
                                    })
                                )
                            )
                            .then(Commands.literal("remove")
                                .then(Commands.argument("value", BlockStateArgument.block(buildContext))
                                    .executes(context -> {
                                        BlockState block = BlockStateArgument.getBlock(context, "value").getState();
                                        TeamId id = TeamId.fromCommand(context);
                                        removeBlock(context.getSource(), block, id);
                                        return 1;
                                    })
                                )
                            )
                            .then(Commands.literal("list")
                                .executes(context -> {
                                    TeamId id = TeamId.fromCommand(context);
                                    listBlocks(context.getSource(), id);
                                    return 1;
                                })
                            )
                            .executes(context -> {
                                TeamId id = TeamId.fromCommand(context);
                                listBlocks(context.getSource(), id);
                                return 1;
                            })
                        )
                    )
                )
            ));
    }

    private static void addBlock(CommandSourceStack source, BlockState block, TeamId teamId) throws CommandSyntaxException {
        CadmusSaveData.addAllowedBlock(source.getServer(), teamId, block.getBlock());
        source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.setting.add_allowed_block", block.getBlock().getName()), false);
    }

    private static void removeBlock(CommandSourceStack source, BlockState block, TeamId teamId) throws CommandSyntaxException {
        if (!CadmusSaveData.isBlockAllowed(source.getServer(), teamId, block.getBlock())) throw BLOCK_NOT_ADDED.create();
        CadmusSaveData.removeAllowedBlock(source.getServer(), teamId, block.getBlock());
        source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.setting.remove_allowed_block", block.getBlock().getName()), false);
    }

    private static void listBlocks(CommandSourceStack source, TeamId teamId) throws CommandSyntaxException {
        CadmusSaveData.getAllowedBlocks(source.getServer(), teamId).forEach(key -> {
            Block block = BuiltInRegistries.BLOCK.get(key);
            source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.setting.list_allowed_blocks", block == Blocks.AIR ? key : block.getName()), false);
        });
    }
}
