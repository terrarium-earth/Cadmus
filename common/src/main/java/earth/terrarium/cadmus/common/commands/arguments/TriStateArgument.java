package earth.terrarium.cadmus.common.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class TriStateArgument implements ArgumentType<TriState> {
    private static final Collection<String> EXAMPLES = Arrays.asList("true", "false", "default");
    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(o -> Component.translatable("argument.tri_state.invalid"));

    public static TriStateArgument triState() {
        return new TriStateArgument();
    }

    public static TriState getTriState(CommandContext<?> context, String name) {
        return context.getArgument(name, TriState.class);
    }

    public TriState parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString().toLowerCase(Locale.ROOT);
        return switch (string) {
            case "true" -> TriState.TRUE;
            case "false" -> TriState.FALSE;
            case "default" -> TriState.UNDEFINED;
            default -> throw ERROR_INVALID_VALUE.create(string);
        };
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return builder
            .suggest("true")
            .suggest("false")
            .suggest("default")
            .buildFuture();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
