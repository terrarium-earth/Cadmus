package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.PistonEvent;

@Mod(Cadmus.MOD_ID)
final class BlockPlaceProtectionImpl {

    public BlockPlaceProtectionImpl() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) {
            if (!Protections.BLOCK_PLACING.canPlaceBlock(entity, event.getPos(), event.getPlacedBlock())) {
                event.setCanceled(true);
            }
        } else if (event.getLevel() instanceof Level level) {
            if (!Protections.BLOCK_PLACING.canPlaceBlock(level, event.getPos(), event.getPlacedBlock())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onPistonPush(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof Level level)) return;
        if (event.getPistonMoveType() == PistonEvent.PistonMoveType.RETRACT) return;
        if (!Protections.BLOCK_PLACING.canPlaceBlock(level, event.getPos(), event.getState())) {
            return;
        }

        PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver == null) return;
        resolver.resolve();

        BlockPos pos = event.getFaceOffsetPos();
        BlockPos target = pos.relative(event.getDirection(), resolver.getToPush().size());
        if (!Protections.BLOCK_PLACING.canPlaceBlock(level, target, level.getBlockState(pos))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onBonemeal(BonemealEvent event) {
        if (!Protections.BLOCK_PLACING.canPlaceBlock(event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos()))) {
            event.setCanceled(true);
        }
    }
}
