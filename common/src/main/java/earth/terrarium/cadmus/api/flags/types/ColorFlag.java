package earth.terrarium.cadmus.api.flags.types;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.api.flags.Flag;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public record ColorFlag(String id, Color value) implements Flag<Color> {
    public static final SimpleCommandExceptionType INVALID_COLOR = new SimpleCommandExceptionType(() -> "Invalid color");

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> createArgument(String argument) {
        return Commands.argument(argument, StringArgumentType.word());
    }

    @Override
    public Flag<Color> getFromArgument(String argument, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var color = context.getArgument(argument, String.class);
        Color parsedColor = Color.parse(color);
        if (parsedColor == null) throw INVALID_COLOR.create();
        return new ColorFlag(id, parsedColor);
    }

    @Override
    public void serialize(CompoundTag tag) {
        Color.CODEC.encode(value, NbtOps.INSTANCE, tag);
    }

    @Override
    public Flag<Color> deserialize(CompoundTag tag) {
        return Color.CODEC.decode(NbtOps.INSTANCE, tag).mapOrElse(color -> new ColorFlag(id, color.getFirst()), (e) -> new ColorFlag(id, Color.DEFAULT));
    }
}
