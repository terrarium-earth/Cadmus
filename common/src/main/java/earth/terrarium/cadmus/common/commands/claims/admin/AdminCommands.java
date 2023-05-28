package earth.terrarium.cadmus.common.commands.claims.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.claims.admin.flags.ComponentFlag;
import earth.terrarium.cadmus.api.claims.admin.flags.Flag;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.commands.claims.ClaimException;
import earth.terrarium.cadmus.common.commands.claims.CommandHelper;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AdminCommands {
    public static final SuggestionProvider<CommandSourceStack> ADMIN_CLAIM_SUGGESTION_PROVIDER = (context, builder) -> {
        Map<String, Map<String, Flag<?>>> claims = AdminClaimHandler.getAll(context.getSource().getServer());
        return SharedSuggestionProvider.suggest((claims.keySet().stream()), builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("admin")
                .requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("create")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(context, "id");
                            CommandHelper.runAction(() -> create(player, id));
                            return 1;
                        })))
                .then(Commands.literal("remove")
                    .then(Commands.argument("adminClaim", StringArgumentType.string())
                        .suggests(ADMIN_CLAIM_SUGGESTION_PROVIDER)
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(context, "adminClaim");
                            CommandHelper.runAction(() -> remove(player, id));
                            return 1;
                        })))
                .then(Commands.literal("claim")
                    .then(Commands.argument("adminClaim", StringArgumentType.string())
                        .suggests(ADMIN_CLAIM_SUGGESTION_PROVIDER)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .then(Commands.argument("chunkload", BoolArgumentType.bool())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                                    String id = StringArgumentType.getString(context, "adminClaim");
                                    boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                                    CommandHelper.runAction(() -> claim(player, id, pos, chunkload));
                                    return 1;
                                })))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(context, "adminClaim");
                            CommandHelper.runAction(() -> claim(player, id, player.blockPosition(), false));
                            return 1;
                        })))));
    }

    public static void create(ServerPlayer player, String id) {
        AdminClaimHandler.create(player.server, id, new HashMap<>());
        AdminClaimHandler.setFlag(player.server, id, ModFlags.DISPLAY_NAME, new ComponentFlag(Component.literal(id)));
        player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.admin.create", id), false);
    }

    public static void remove(ServerPlayer player, String id) throws ClaimException {
        if (AdminClaimHandler.get(player.server, id) == null) {
            throw ClaimException.CLAIM_DOES_NOT_EXIST;
        }
        AdminClaimHandler.remove(player.server, id);
        player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.admin.remove", id), false);
    }

    public static void claim(ServerPlayer player, String id, BlockPos pos, boolean chunkloaded) throws ClaimException {
        if (AdminClaimHandler.get(player.server, id) == null) {
            throw ClaimException.CLAIM_DOES_NOT_EXIST;
        }
        Pair<String, ClaimType> claimData = ClaimHandler.getClaim(player.getLevel(), new ChunkPos(pos));
        if (claimData != null) {
            boolean isMember = TeamProviderApi.API.getSelected().isMember(claimData.getFirst(), player.server, player.getUUID());
            throw isMember ? ClaimException.YOUVE_ALREADY_CLAIMED_CHUNK : ClaimException.CHUNK_IS_ALREADY_CLAIMED;
        }
        var claim = Map.of(new ChunkPos(pos), chunkloaded ? ClaimType.CHUNK_LOADED : ClaimType.CLAIMED);
        ModUtils.claim(ClaimHandler.ADMIN_PREFIX + id, player.getLevel(), claim, Set.of());
        if (chunkloaded) {
            player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.claiming.chunk_loaded_chunk_at", SectionPos.blockToSectionCoord(pos.getX()) * 16, SectionPos.blockToSectionCoord(pos.getZ()) * 16), false);
        } else {
            player.displayClientMessage(ModUtils.serverTranslation("text.cadmus.claiming.claimed_chunk_at", SectionPos.blockToSectionCoord(pos.getX()) * 16, SectionPos.blockToSectionCoord(pos.getZ()) * 16), false);
        }
    }
}
