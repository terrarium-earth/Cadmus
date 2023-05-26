package earth.terrarium.cadmus.api.claims.admin.flags;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public record BooleanFlag(boolean value) implements Flag<Boolean> {
    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, Boolean> createArgument(String string) {
        return Commands.argument(string, BoolArgumentType.bool());
    }

    @Override
    public Flag<Boolean> getFromArgument(CommandContext<?> context, String string) {
        return new BooleanFlag(BoolArgumentType.getBool(context, string));
    }

    @Override
    public Flag<Boolean> create(String value) {
        return new BooleanFlag(Boolean.parseBoolean(value));
    }
}
