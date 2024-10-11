package earth.terrarium.cadmus.common.tags;

import earth.terrarium.cadmus.Cadmus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {

    public static final TagKey<Item> ALLOWS_CLAIM_PICKUP = tag("allows_claim_pickup");

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, Cadmus.id(name));
    }
}
