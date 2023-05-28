package earth.terrarium.cadmus.api.claims.admin.flags;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public record FloatFlag(float value) implements Flag<Float> {
    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, Float> createArgument(String string) {
        return Commands.argument(string, FloatArgumentType.floatArg());
    }

    @Override
    public Flag<Float> getFromArgument(CommandContext<CommandSourceStack> context, String string) {
        return new FloatFlag(FloatArgumentType.getFloat(context, string));
    }

    @Override
    public Flag<Float> create(String value) {
        return new FloatFlag(Float.parseFloat(value));
    }
}
