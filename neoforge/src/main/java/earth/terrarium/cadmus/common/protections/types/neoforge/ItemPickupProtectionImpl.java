package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = Cadmus.MOD_ID)
final class ItemPickupProtectionImpl {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!Protections.ITEM_PICKUP.canPickupItem(event.getPlayer(), event.getItemEntity())) {
            event.setCanPickup(TriState.FALSE);
        }
    }
}
