package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(Cadmus.MOD_ID)
final class BlockInteractProtectionImpl {
    public BlockInteractProtectionImpl() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!Protections.BLOCK_INTERACTIONS.canInteractWithBlock(event.getEntity(), event.getPos(), event.getLevel().getBlockState(event.getPos()))) {
            event.setUseItem(TriState.DEFAULT);
            event.setUseBlock(TriState.FALSE);
        }
    }
}
