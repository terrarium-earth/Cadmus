package earth.terrarium.cadmus.common.commands.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.commands.claims.ClaimCommand;
import earth.terrarium.cadmus.common.commands.claims.UnclaimCommand;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class AdminCommands {

    public static final SimpleCommandExceptionType TEAM_DOES_NOT_EXIST = new SimpleCommandExceptionType(ConstantComponents.TEAM_DOES_NOT_EXIST);

    public static final SuggestionProvider<CommandSourceStack> TEAM_PROVIDER_SUGGESTION_PROVIDER = (context, builder) ->
        SharedSuggestionProvider.suggest(
            TeamApi.API.getAllProviders(),
            builder,
            ResourceLocation::toString,
            id -> Component.translatable(id.toLanguageKey("provider"))
        );

    public static final SuggestionProvider<CommandSourceStack> TEAM_ID_SUGGESTION_PROVIDER = (context, builder) -> {
        var provider = TeamApi.API.getProvider(context.getArgument("provider", ResourceLocation.class));
        return SharedSuggestionProvider.suggest(
            provider.getAllTeams(context.getSource().getServer()),
            builder,
            UUID::toString,
            id -> provider.getName(context.getSource().getLevel(), id).orElse(CommonComponents.EMPTY)
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cadmus")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("admin")
                .then(Commands.literal("claim")
                    .then(Commands.argument("provider", ResourceLocationArgument.id())
                        .suggests(TEAM_PROVIDER_SUGGESTION_PROVIDER)
                        .then(Commands.argument("id", UuidArgument.uuid()))
                        .suggests(TEAM_ID_SUGGESTION_PROVIDER)
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .then(Commands.argument("chunkload", BoolArgumentType.bool())
                                .executes(context -> {
                                    ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                                    boolean chunkload = BoolArgumentType.getBool(context, "chunkload");
                                    ResourceLocation provider = ResourceLocationArgument.getId(context, "provider");
                                    UUID id = UuidArgument.getUuid(context, "id");
                                    claim(context.getSource(), pos, chunkload, provider, id);
                                    return 1;
                                }))
                            .executes(context -> {
                                ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                                ResourceLocation provider = ResourceLocationArgument.getId(context, "provider");
                                UUID id = UuidArgument.getUuid(context, "id");
                                claim(context.getSource(), pos, false, provider, id);
                                return 1;
                            }))
                        .executes(context -> {
                            ResourceLocation provider = ResourceLocationArgument.getId(context, "provider");
                            UUID id = UuidArgument.getUuid(context, "id");
                            claim(context.getSource(), context.getSource().getPlayerOrException().chunkPosition(), false, provider, id);
                            return 1;
                        })
                    )
                )

                .then(Commands.literal("unclaim")
                    .then(Commands.argument("provider", ResourceLocationArgument.id())
                        .suggests(TEAM_PROVIDER_SUGGESTION_PROVIDER)
                        .then(Commands.argument("id", UuidArgument.uuid()))
                        .suggests(TEAM_ID_SUGGESTION_PROVIDER)
                        .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                            .executes(context -> {
                                ChunkPos pos = ColumnPosArgument.getColumnPos(context, "pos").toChunkPos();
                                ResourceLocation provider = ResourceLocationArgument.getId(context, "provider");
                                UUID id = UuidArgument.getUuid(context, "id");
                                unclaim(context.getSource(), pos, provider, id);
                                return 1;
                            })
                        )
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ChunkPos pos = player.chunkPosition();
                            ResourceLocation provider = ResourceLocationArgument.getId(context, "provider");
                            UUID id = UuidArgument.getUuid(context, "id");
                            unclaim(context.getSource(), pos, provider, id);
                            return 1;
                        })
                    )
                    .executes(context -> {
                        unclaim(context.getSource());
                        return 1;
                    })
                )
                .then(Commands.literal("clear")
                    .then(Commands.argument("provider", ResourceLocationArgument.id())
                        .suggests(TEAM_PROVIDER_SUGGESTION_PROVIDER)
                        .then(Commands.argument("id", UuidArgument.uuid()))
                        .suggests(TEAM_ID_SUGGESTION_PROVIDER)
                        .executes(context -> {
                            ResourceLocation provider = ResourceLocationArgument.getId(context, "provider");
                            UUID id = UuidArgument.getUuid(context, "id");
                            unclaimAll(context.getSource(), provider, id);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("clearall")
                    .executes(context -> {
                        clearAll(context.getSource());
                        return 1;
                    })
                )
            )
        );
    }

    private static void claim(CommandSourceStack source, ChunkPos pos, boolean chunkload, ResourceLocation provider, UUID id) throws CommandSyntaxException {
        TeamId teamId = new TeamId(provider, id);
        if (!TeamApi.API.teamExists(source.getServer(), teamId)) throw TEAM_DOES_NOT_EXIST.create();
        ClaimCommand.checkClaimed(source.getLevel(), pos);

        ClaimApi.API.claim(source.getLevel(), teamId, pos, chunkload);

        int claimsCount = ClaimCommand.getClaimsCount(source.getLevel(), teamId, chunkload);
        int maxClaims = chunkload ? ClaimLimitApi.API.getMaxChunkLoadedClaims(teamId) : ClaimLimitApi.API.getMaxClaims(teamId);

        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            chunkload ?
                "command.cadmus.info.chunk_loaded_chunk_at" :
                "command.cadmus.info.claimed_chunk_at",
            pos.x, pos.z,
            claimsCount, maxClaims
        ), false);
    }

    private static void unclaim(CommandSourceStack source, ChunkPos pos, ResourceLocation provider, UUID id) throws CommandSyntaxException {
        TeamId teamId = new TeamId(provider, id);
        if (!TeamApi.API.teamExists(source.getServer(), teamId)) throw TEAM_DOES_NOT_EXIST.create();

        var claim = ClaimApi.API.getClaim(source.getLevel(), pos);
        if (claim.isEmpty()) throw UnclaimCommand.NOT_CLAIMED.create();

        ClaimApi.API.unclaim(source.getLevel(), teamId, pos);

        int claimsCount = ClaimCommand.getClaimsCount(source.getLevel(), teamId, false);
        int maxClaims = ClaimLimitApi.API.getMaxClaims(teamId);
        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            "command.cadmus.info.unclaimed_chunk_at",
            pos.x, pos.z,
            claimsCount, maxClaims
        ), false);
    }

    private static void unclaim(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var claim = ClaimApi.API.getClaim(source.getLevel(), player.chunkPosition());
        if (claim.isEmpty()) throw UnclaimCommand.NOT_CLAIMED.create();
        TeamId teamId = claim.get().left();
        unclaim(source, player.chunkPosition(), teamId.provider(), teamId.id());
    }

    private static void unclaimAll(CommandSourceStack source, ResourceLocation provider, UUID id) throws CommandSyntaxException {
        TeamId teamId = new TeamId(provider, id);
        if (!TeamApi.API.teamExists(source.getServer(), teamId)) throw TEAM_DOES_NOT_EXIST.create();

        int oldClaimsCount = ClaimCommand.getClaimsCount(source.getLevel(), teamId, false);
        ClaimApi.API.clear(source.getLevel(), teamId);
        int diff = oldClaimsCount - ClaimCommand.getClaimsCount(source.getLevel(), teamId, false);
        source.sendSuccess(() -> ModUtils.translatableWithStyle(
            "command.cadmus.info.unclaimed_all",
            diff
        ), false);
    }

    private static void clearAll(CommandSourceStack source) {
        ClaimApi.API.clearAll(source.getServer());
        source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.info.admin_clear"), false);
    }
}
