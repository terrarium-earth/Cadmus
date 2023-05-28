package earth.terrarium.cadmus.api.claims.admin.flags;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public record StringFlag(String value) implements Flag<String> {
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, String> createArgument(String string) {
        return Commands.argument(string, StringArgumentType.string());
    }

    @Override
    public Flag<String> getFromArgument(CommandContext<CommandSourceStack> context, String string) {
        return new StringFlag(StringArgumentType.getString(context, string));
    }

    @Override
    public Flag<String> create(String value) {
        return new StringFlag(value);
    }
}
