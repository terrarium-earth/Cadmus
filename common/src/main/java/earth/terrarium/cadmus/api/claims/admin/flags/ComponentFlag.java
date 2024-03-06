package earth.terrarium.cadmus.api.claims.admin.flags;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public record ComponentFlag(Component value) implements Flag<Component> {
    @Override
    public Component getValue() {
        return value;
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, Component> createArgument(String string) {
        return Commands.argument(string, ComponentArgument.textComponent());
    }

    @Override
    public Flag<Component> getFromArgument(CommandContext<CommandSourceStack> context, String string) {
        return new ComponentFlag(ComponentArgument.getComponent(context, string));
    }

    @Override
    public Flag<Component> create(String value) {
        var component = Component.Serializer.fromJson(value);
        return new ComponentFlag(component == null ? CommonComponents.EMPTY : component);
    }

    @Override
    public String serialize() {
        return Component.Serializer.toJson(value);
    }
}
