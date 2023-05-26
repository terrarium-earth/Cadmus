package earth.terrarium.cadmus.api.claims.admin.flags;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public interface Flag<T> {
    T getValue();

    RequiredArgumentBuilder<CommandSourceStack, T> createArgument(String string);

    Flag<T> getFromArgument(CommandContext<?> context, String string);

    Flag<T> create(String value);
}