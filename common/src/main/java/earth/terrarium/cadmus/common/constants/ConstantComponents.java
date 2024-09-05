package earth.terrarium.cadmus.common.constants;

import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ConstantComponents {

    public static final Component UNKNOWN = Component.literal("Unknown");

    public static final Component PROJECT_ODYSSEY_CATEGORY = Component.translatable("key.categories.project_odyssey");
    public static final Component OPEN_CLAIM_MAP_KEY = Component.translatable("key.cadmus.open_claim_map");

    public static final Component CHUNK_LOADED_TRUE = CommonUtils.serverTranslatable("command.cadmus.info.chunk_loaded_true");
    public static final Component CHUNK_LOADED_FALSE = CommonUtils.serverTranslatable("command.cadmus.info.chunk_loaded_false");

    public static final Component NOT_CLAIMED = CommonUtils.serverTranslatable("command.cadmus.exception.not_claimed").copy().withStyle(ChatFormatting.RED);
    public static final Component NOT_OWNER = CommonUtils.serverTranslatable("command.cadmus.exception.not_owner").copy().withStyle(ChatFormatting.RED);

    public static final Component INVALID_STATE = CommonUtils.serverTranslatable("command.cadmus.exception.invalid_state");
    public static final Component BLOCK_NOT_ADDED = CommonUtils.serverTranslatable("command.cadmus.exception.block_not_added");

    public static final Component NO_PERMISSION_TEAM = CommonUtils.serverTranslatable("command.cadmus.exception.no_permission_team");
    public static final Component NO_PERMISSION_ROLE = CommonUtils.serverTranslatable("command.cadmus.exception.no_permission_role");

    public static final Component TEAM_DOES_NOT_EXIST = CommonUtils.serverTranslatable("command.cadmus.exception.team_does_not_exist");

    public static final Component ADMIN_TEAM_ALREADY_EXISTS = CommonUtils.serverTranslatable("command.cadmus.exception.admin_team_already_exists");
    public static final Component ADMIN_TEAM_DOES_NOT_EXIST = CommonUtils.serverTranslatable("command.cadmus.exception.admin_team_does_not_exist");
    public static final Component ADMIN_TEAM_HAS_NO_FLAGS = CommonUtils.serverTranslatable("command.cadmus.exception.admin_team_has_no_flags");

    public static final Component WILDERNESS = CommonUtils.serverTranslatable("message.cadmus.wilderness").copy().withStyle(ChatFormatting.GREEN);


    public static final Component MAP_TITLE = Component.translatable("gui.cadmus.claim_map.title");
    public static final Component UNCLAIM_ALL = Component.translatable("gui.cadmus.claim_map.clear_claimed_chunks");
    public static final Component CLOSE = Component.translatable("gui.cadmus.claim_map.close");
    public static final Component MAX_CLAIMS = Component.translatable("gui.cadmus.claim_map.max_claims");
    public static final Component MAX_CHUNK_LOADED_CLAIMS = Component.translatable("gui.cadmus.claim_map.max_chunk_loaded_claims");
    public static final Component SETTINGS = Component.translatable("gui.cadmus.claim_map.settings");

    public static final Component UNCLAIM_MODAL_TITLE = Component.translatable("gui.cadmus.unclaim_modal.title");
    public static final Component UNCLAIM_MODAL_DESCRIPTION = Component.translatable("gui.cadmus.unclaim_modal.description");
    public static final Component UNCLAIM_MODAL_CONFIRM = Component.translatable("gui.cadmus.unclaim_modal.confirm");
}
