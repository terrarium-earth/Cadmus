package earth.terrarium.cadmus.common.commands.claims;

import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.network.chat.Component;

public class ClaimException extends Exception {

    public static final ClaimException CHUNK_IS_ALREADY_CLAIMED = new ClaimException(ModUtils.serverTranslation("command.cadmus.exception.chunk_is_already_claimed"));
    public static final ClaimException YOUVE_ALREADY_CLAIMED_CHUNK = new ClaimException(ModUtils.serverTranslation("command.cadmus.exception.youve_already_claimed_chunk"));
    public static final ClaimException YOUVE_MAXED_OUT_YOUR_CLAIMS = new ClaimException(ModUtils.serverTranslation("command.cadmus.exception.youve_maxed_out_your_claims"));
    public static final ClaimException THIS_CHUNK_IS_NOT_CLAIMED = new ClaimException(ModUtils.serverTranslation("command.cadmus.exception.this_chunk_is_not_claimed"));
    public static final ClaimException YOU_DONT_OWN_THIS_CHUNK = new ClaimException(ModUtils.serverTranslation("command.cadmus.exception.you_dont_own_this_chunk"));
    public static final ClaimException CLAIM_HAS_NO_FLAGS = new ClaimException(ModUtils.serverTranslation("command.cadmus.exception.claim_has_no_flags"));
    public static final ClaimException CLAIM_DOES_NOT_EXIST = new ClaimException(ModUtils.serverTranslation("command.cadmus.exception.claim_does_not_exist"));

    private final Component message;

    private ClaimException(Component message) {
        super(message.getString());
        this.message = message;
    }

    public Component message() {
        return message;
    }
}
