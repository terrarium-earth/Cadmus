package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Cadmus.MOD_ID)
final class EntityInteractProtectionImpl {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!Protections.ENTITY_INTERACTIONS.canInteractWithEntity(event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!Protections.ENTITY_INTERACTIONS.canInteractWithEntity(event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
        }
    }
}
