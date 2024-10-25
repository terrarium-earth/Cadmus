package earth.terrarium.cadmus.api.flags;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface Flag<T> {

    /**
     * Gets the value of the flag.
     *
     * @return The value of the flag.
     */
    T value();

    /**
     * Gets the id of the flag.
     *
     * @return The id of the flag.
     */
    String id();

    /**
     * Creates a brigadier argument for the flag.
     *
     * @param argument The name of the argument.
     * @return The brigadier argument.
     */
    ArgumentBuilder<CommandSourceStack, ?> createArgument(String argument);

    /**
     * Gets the flag from a brigadier argument.
     *
     * @param argument The name of the argument.
     * @param context  The command context.
     * @return The flag.
     * @throws CommandSyntaxException If the flag could not be created.
     */
    Flag<T> getFromArgument(String argument, CommandContext<CommandSourceStack> context) throws CommandSyntaxException;

    /**
     * Serializes the flag to NBT.
     *
     * @param tag The tag.
     */
    void serialize(CompoundTag tag);

    /**
     * Deserializes the flag from NBT.
     *
     * @param tag The tag.
     * @return The flag.
     */
    Flag<T> deserialize(CompoundTag tag);

    /**
     * Gets the value of the flag for a specific admin chunk.
     *
     * @param level The level.
     * @param pos   The position of the chunk.
     * @return The value of the flag.
     */
    default T get(Level level, ChunkPos pos) {
        return level instanceof ServerLevel serverLevel ?
            FlagApi.API.<T>getFlag(serverLevel, pos, id())
                .map(Flag::value)
                .orElse(this.value()) :
            this.value();
    }

    /**
     * Gets the value of the flag for a specific admin team.
     *
     * @param server The server.
     * @param id     The id of the admin team.
     * @return The value of the flag.
     */
    default T get(MinecraftServer server, UUID id) {
        return FlagApi.API.<T>getFlag(server, id, id()).value();
    }
}