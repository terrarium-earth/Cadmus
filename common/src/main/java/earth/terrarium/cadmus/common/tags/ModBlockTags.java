package earth.terrarium.cadmus.common.tags;

import earth.terrarium.cadmus.Cadmus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {

    public static final TagKey<Block> ALLOWS_CLAIM_INTERACTIONS = tag("allows_claim_interactions");
    public static final TagKey<Block> INTERACTABLE_STORAGE = tag("interactable_storage");
    public static final TagKey<Block> REDSTONE = tag("redstone");
    public static final TagKey<Block> DOOR_LIKE = tag("door_like");

    private static TagKey<Block> tag(String name) {
        return TagKey.create(Registries.BLOCK, Cadmus.id(name));
    }
}
