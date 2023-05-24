package earth.terrarium.cadmus.common.constants;

import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.network.chat.Component;

public class ConstantComponents {
    public static final Component TITLE = Component.translatable("gui.cadmus.claim_map.title");
    public static final Component LOADING = Component.translatable("gui.cadmus.claim_map.loading");
    public static final Component CLEAR_CLAIMED_CHUNKS = Component.translatable("tooltip.cadmus.claim_map.clear_claimed_chunks");
    public static final Component CHUNK_LOADED = Component.translatable("tooltip.cadmus.claim_map.chunk_loaded");
    public static final Component WILDERNESS = ModUtils.serverTranslation("message.cadmus.wilderness");

    public static final Component OPEN_CLAIM_MAP_KEY = Component.translatable("key.cadmus.open_claim_map");
    public static final Component ODYSSEY_CATEGORY = Component.translatable("key.categories.odyssey");

    public static final Component CADMUS_TITLE = Component.translatable("cadmus.options.cadmus_options.title");
    public static final Component CLAIMS_MAX = Component.translatable("cadmus.options.cadmus_options.max_claims");
    public static final Component CHUNK_LOADED_MAX = Component.translatable("cadmus.options.cadmus_options.max_loaded_chunks");
}
