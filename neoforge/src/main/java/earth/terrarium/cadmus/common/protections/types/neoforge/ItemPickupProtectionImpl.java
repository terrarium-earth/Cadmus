package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@Mod(Cadmus.MOD_ID)
final class ItemPickupProtectionImpl {
    public ItemPickupProtectionImpl() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!Protections.ITEM_PICKUP.canPickupItem(event.getPlayer(), event.getItemEntity())) {
            event.setCanPickup(TriState.FALSE);
        }
    }
}
