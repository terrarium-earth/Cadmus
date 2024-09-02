package earth.terrarium.cadmus.client.compat.journeymap;

import earth.terrarium.cadmus.Cadmus;
import journeymap.api.v2.client.option.BooleanOption;
import journeymap.api.v2.client.option.OptionCategory;

public class ClaimedChunkOptions {

    private final OptionCategory category = new OptionCategory(Cadmus.MOD_ID, "Cadmus", "Cadmus Options");

    public final BooleanOption showClaimedChunks = new BooleanOption(
        category,
        "showClaimedChunks",
        "Show Claimed Chunks",
        true
    );
}
