package earth.terrarium.cadmus.common.commands.claims;

import net.minecraft.network.chat.Component;

public class ClaimException extends Exception {

    public static final ClaimException CHUNK_IS_ALREADY_CLAIMED = new ClaimException(Component.translatable("command.cadmus.exception.chunk_is_already_claimed"));
    public static final ClaimException YOUVE_ALREADY_CLAIMED_CHUNK = new ClaimException(Component.translatable("command.cadmus.exception.youve_already_claimed_chunk"));
    public static final ClaimException YOUVE_MAXED_OUT_YOUR_CLAIMS = new ClaimException(Component.translatable("command.cadmus.exception.youve_maxed_out_your_claims"));
    public static final ClaimException THIS_CHUNK_IS_NOT_CLAIMED = new ClaimException(Component.translatable("command.cadmus.exception.this_chunk_is_not_claimed"));
    public static final ClaimException YOU_DONT_OWN_THIS_CHUNK = new ClaimException(Component.translatable("command.cadmus.exception.you_dont_own_this_chunk"));

    private final Component message;

    private ClaimException(Component message) {
        super(message.getString());
        this.message = message;
    }

    public Component message() {
        return message;
    }
}
