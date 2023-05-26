package earth.terrarium.cadmus.api.claims.admin.flags;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public record IntegerFlag(int value) implements Flag<Integer> {
    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, Integer> createArgument(String string) {
        return Commands.argument(string, IntegerArgumentType.integer());
    }

    @Override
    public Flag<Integer> getFromArgument(CommandContext<?> context, String string) {
        return new IntegerFlag(IntegerArgumentType.getInteger(context, string));
    }

    @Override
    public Flag<Integer> create(String value) {
        return new IntegerFlag(Integer.parseInt(value));
    }
}
