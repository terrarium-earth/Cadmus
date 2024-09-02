package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@Mod(Cadmus.MOD_ID)
final class EntityExplosionProtectionImpl {
    public EntityExplosionProtectionImpl() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onExplode(ExplosionEvent.Detonate event) {
        event.getAffectedEntities().removeIf(entity ->
            !Protections.ENTITY_EXPLOSIONS.canExplodeEntity(entity, event.getExplosion()));
    }
}
