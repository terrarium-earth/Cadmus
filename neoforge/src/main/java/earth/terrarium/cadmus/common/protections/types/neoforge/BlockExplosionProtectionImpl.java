package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = Cadmus.MOD_ID)
final class BlockExplosionProtectionImpl {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onExplode(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos ->
            !Protections.BLOCK_EXPLOSIONS.canExplodeBlock(event.getLevel(), pos, event.getExplosion()));
    }
}
