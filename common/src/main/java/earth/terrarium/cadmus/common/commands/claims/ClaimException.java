package earth.terrarium.cadmus.common.commands.claims;

import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import net.minecraft.network.chat.Component;

public class ClaimException extends Exception {

    public static final ClaimException CHUNK_ALREADY_CLAIMED = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.chunk_already_claimed"));
    public static final ClaimException ALREADY_CLAIMED_CHUNK = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.already_claimed_chunk"));
    public static final ClaimException MAXED_OUT_CLAIMS = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.maxed_out_claims"));
    public static final ClaimException CHUNK_NOT_CLAIMED = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.chunk_not_claimed"));
    public static final ClaimException CANT_UNLCLAIM_ADMIN = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.cant_unclaim_admin"));
    public static final ClaimException DONT_OWN_CHUNK = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.dont_own_chunk"));
    public static final ClaimException CLAIM_HAS_NO_FLAGS = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.claim_has_no_flags"));
    public static final ClaimException CLAIM_ALREADY_EXISTS = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.claim_already_exists"));
    public static final ClaimException CLAIM_DOES_NOT_EXIST = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.claim_does_not_exist"));
    public static final ClaimException NOT_ALLOWED_TO_MANAGE_TEAM_SETTINGS = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.not_allowed_to_manage_team_setting"));
    public static final ClaimException NO_PERMISSION_SETTINGS = new ClaimException(CommonUtils.serverTranslatable("command.cadmus.exception.no_permission_settings"));

    private final Component message;

    private ClaimException(Component message) {
        super(message.getString());
        this.message = message;
    }

    public Component message() {
        return message;
    }
}
