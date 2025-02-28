package earth.terrarium.cadmus.api.teams;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import earth.terrarium.cadmus.common.teams.AdminTeamProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public record TeamId(ResourceLocation provider, UUID id) {
    public static final Codec<TeamId> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("provider").forGetter(TeamId::provider),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("id").forGetter(TeamId::id)
    ).apply(instance, TeamId::new));

    public static final ByteCodec<TeamId> BYTE_CODEC = ObjectByteCodec.create(
        ExtraByteCodecs.RESOURCE_LOCATION.fieldOf(TeamId::provider),
        ByteCodec.UUID.fieldOf(TeamId::id),
        TeamId::new
    );

    public static final SuggestionProvider<CommandSourceStack> TEAM_PROVIDER_SUGGESTION_PROVIDER = (context, builder) ->
        SharedSuggestionProvider.suggest(
            TeamApi.API.getTeams(Objects.requireNonNull(context.getSource().getPlayer())).keySet(),
            builder,
            ResourceLocation::toString,
            id -> Component.translatable(id.toLanguageKey("provider"))
        );

    public static final SuggestionProvider<CommandSourceStack> TEAM_UUID_SUGGESTION_PROVIDER = (context, builder) -> {
        ResourceLocation providerName = context.getArgument("provider", ResourceLocation.class);
        var provider = TeamApi.API.getProvider(providerName);
        return SharedSuggestionProvider.suggest(
            TeamApi.API.getProvider(providerName).getTeams(context.getSource().getPlayer()),
            builder,
            UUID::toString,
            id -> provider.getName(context.getSource().getLevel(), id).orElse(CommonComponents.EMPTY)
        );
    };

    public static TeamId fromCommand(CommandContext<CommandSourceStack> context) {
        TeamId teamId = new TeamId(context.getArgument("provider", ResourceLocation.class), context.getArgument("id", UUID.class));
        if(!TeamApi.API.getProvider(teamId.provider).canModifySettings(context.getSource().getPlayer(), teamId.id)) {
            throw new IllegalArgumentException("Member cannot modify team settings");
        };
        return teamId;
    }

    public static TeamId ofNew(ResourceLocation providerId) {
        return new TeamId(providerId, UUID.randomUUID());
    }

    public static TeamId ofAdmin(UUID id) {
        return new TeamId(AdminTeamProvider.ID, id);
    }

    public boolean isAdmin() {
        return AdminTeamProvider.ID.equals(provider);
    }
}
