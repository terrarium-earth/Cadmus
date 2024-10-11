package earth.terrarium.cadmus.common.tags;

import earth.terrarium.cadmus.Cadmus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class ModEntityTypeTags {

    public static final TagKey<EntityType<?>> ALLOWS_CLAIM_INTERACTIONS_ENTITIES = tag("allows_claim_interactions");
    public static final TagKey<EntityType<?>> ALLOWS_CLAIM_DAMAGE_ENTITIES = tag("allows_claim_damage");
    public static final TagKey<EntityType<?>> CAN_GRIEF_ENTITIES = tag("can_grief");

    public static final TagKey<EntityType<?>> MONSTERS = tag("monsters");
    public static final TagKey<EntityType<?>> CREATURES = tag("creatures");

    private static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Cadmus.id(name));
    }
}
