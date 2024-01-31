package earth.terrarium.cadmus.common.commands.claims;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public class CommandHelper {
    public static void runAction(Action action) throws CommandSyntaxException {
        try {
            action.run();
        } catch (ClaimException e) {
            throw new SimpleCommandExceptionType(e.message()).create();
        }
    }

    @FunctionalInterface
    public interface Action {
        void run() throws ClaimException, CommandSyntaxException;
    }
}
