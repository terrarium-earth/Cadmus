package earth.terrarium.cadmus.api.claims.admin.flags;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * This API is being heavily reworked in 1.21.
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.21")
public interface Flag<T> {

    T getValue();

    RequiredArgumentBuilder<CommandSourceStack, T> createArgument(String string);

    Flag<T> getFromArgument(CommandContext<CommandSourceStack> context, String string);

    Flag<T> create(String value);

    default String serialize() {
        return String.valueOf(getValue());
    }
}