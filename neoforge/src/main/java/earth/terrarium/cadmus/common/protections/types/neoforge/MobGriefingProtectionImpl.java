package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(Cadmus.MOD_ID)
final class MobGriefingProtectionImpl {
    public MobGriefingProtectionImpl() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onEntityMobGriefing(EntityMobGriefingEvent event) {
        if (!Protections.MOB_GRIEFING.canMobGrief(event.getEntity(), event.getEntity().chunkPosition())) {
            event.setCanGrief(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onFarmLandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (!(event.getEntity() instanceof Player) && !Protections.MOB_GRIEFING.canMobGrief(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
        if (!Protections.MOB_GRIEFING.canMobGrief(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        if (event.getLightning().getCause() == null && !Protections.MOB_GRIEFING.canMobGrief(event.getLightning())) {
            event.setCanceled(true);
        }
    }
}
