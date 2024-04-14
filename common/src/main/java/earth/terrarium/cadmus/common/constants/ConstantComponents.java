package earth.terrarium.cadmus.common.constants;

import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class ConstantComponents {
    public static final Component TITLE = Component.translatable("gui.cadmus.claim_map.title");
    public static final Component LOADING = Component.translatable("gui.cadmus.claim_map.loading");
    public static final Component CLOSE = Component.translatable("gui.cadmus.claim_map.close");
    public static final Component CLEAR_CLAIMED_CHUNKS = Component.translatable("tooltip.cadmus.claim_map.clear_claimed_chunks");
    public static final Component CLEAR_ALL_CLAIMED_CHUNKS = Component.translatable("tooltip.cadmus.claim_map.clear_all_claimed_chunks");
    public static final Component CHUNK_LOADED = Component.translatable("tooltip.cadmus.claim_map.chunk_loaded").withStyle(Style.EMPTY.withColor(0xfff59a22).withItalic(true));
    public static final Component ADMIN_CLAIM = Component.translatable("tooltip.cadmus.claim_map.admin_claim").withStyle(Style.EMPTY.withColor(0xfff59a22).withItalic(true));
    public static final Component WILDERNESS = CommonUtils.serverTranslatable("message.cadmus.wilderness");

    public static final Component OPEN_CLAIM_MAP_KEY = Component.translatable("key.cadmus.open_claim_map");
    public static final Component ODYSSEY_CATEGORY = Component.translatable("key.categories.project_odyssey");

    public static final Component CADMUS_TITLE = Component.translatable("cadmus.options.cadmus_options.title");
    public static final Component CLAIMS_MAX = Component.translatable("cadmus.options.cadmus_options.max_claims");
    public static final Component CHUNK_LOADED_MAX = Component.translatable("cadmus.options.cadmus_options.max_loaded_chunks");

    public static final Component UNCLAIMED = CommonUtils.serverTranslatable("text.cadmus.info.unclaimed");
    public static final Component TEAM = CommonUtils.serverTranslatable("text.cadmus.info.team");
    public static final Component PLAYER = CommonUtils.serverTranslatable("text.cadmus.info.player");
    public static final Component ADMIN = CommonUtils.serverTranslatable("text.cadmus.info.admin");
    public static final Component UNKNOWN = CommonUtils.serverTranslatable("text.cadmus.info.unknown");

    public static final Component INVALID_STATE = CommonUtils.serverTranslatable("command.cadmus.settings.invalid_state");

    public static final Component BLOCK_NOT_ADDED = CommonUtils.serverTranslatable("command.cadmus.exception.block_not_added");
}
