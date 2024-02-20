package earth.terrarium.cadmus.common.util;

import earth.terrarium.cadmus.Cadmus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class ModEntityTags {
    public static final TagKey<EntityType<?>> ALLOWS_CLAIM_INTERACTIONS_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Cadmus.MOD_ID, "allows_claim_interactions"));
    public static final TagKey<EntityType<?>> ALLOWS_CLAIM_DAMAGE_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Cadmus.MOD_ID, "allows_claim_damage"));
    public static final TagKey<EntityType<?>> CAN_GRIEF_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Cadmus.MOD_ID, "can_grief"));

    public static final TagKey<EntityType<?>> MONSTERS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Cadmus.MOD_ID, "monsters"));
    public static final TagKey<EntityType<?>> CREATURES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Cadmus.MOD_ID, "creatures"));
}
