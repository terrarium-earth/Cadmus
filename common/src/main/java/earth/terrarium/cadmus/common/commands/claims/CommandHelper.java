package earth.terrarium.cadmus.common.commands.claims;

import net.minecraft.commands.CommandRuntimeException;

public class CommandHelper {
    public static void runAction(Action action) throws CommandRuntimeException {
        try {
            action.run();
        } catch (ClaimException e) {
            throw new CommandRuntimeException(e.message());
        }
    }

    @FunctionalInterface
    public interface Action {
        void run() throws ClaimException;
    }
}
