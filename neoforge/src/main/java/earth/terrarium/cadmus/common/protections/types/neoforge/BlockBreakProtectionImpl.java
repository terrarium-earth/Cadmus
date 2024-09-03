package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Cadmus.MOD_ID)
final class BlockBreakProtectionImpl {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onAttackBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!Protections.BLOCK_BREAKING.canBreakBlock(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!Protections.BLOCK_BREAKING.canBreakBlock(event.getPlayer(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onFarmLandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof Player player && !Protections.BLOCK_BREAKING.canBreakBlock(player, event.getPos())) {
            event.setCanceled(true);
        } else if (!Protections.MOB_GRIEFING.canMobGrief(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onFillBucket(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().getItem() != Items.BUCKET) return;
        if (!Protections.BLOCK_BREAKING.canBreakBlock(event.getEntity(), event.getPos())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
}
