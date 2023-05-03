package earth.terrarium.cadmus.forge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.client.forge.CadmusClientForge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Cadmus.MOD_ID)
public class CadmusForge {
    public CadmusForge() {
        Cadmus.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CadmusClientForge::init);

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(CadmusForge::onEnterSection);
        registerChunkProtectionEvents(bus);
    }

    private static void registerChunkProtectionEvents(IEventBus bus) {
        bus.addListener(EventPriority.LOWEST, CadmusForge::onBlockPlace);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onBlockBreak);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onBlockInteract);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onEntityInteract);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onAttackBlock);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onAttackEntity);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onFillBucket);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onExplode);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onFarmLandTrample);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onEntityMobGriefing);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onLivingDestroyBlock);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onItemPickup);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onEntityStruckByLightning);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onProjectileImpact);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onLivingAttack);
        bus.addListener(EventPriority.LOWEST, CadmusForge::onPistonPush);
    }

    private static void onEnterSection(EntityEvent.EnteringSection event) {
        if (event.getEntity() instanceof Player player) {
            Cadmus.enterChunkSection(player);
        }
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!ClaimApi.API.canBreakBlock(event.getPlayer().getLevel(), event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!ClaimApi.API.canPlaceBlock(player.getLevel(), event.getPos(), player)) {
                event.setCanceled(true);
            }
        } else if (event.getEntity() != null && !ClaimApi.API.canEntityGrief(event.getEntity().getLevel(), event.getPos(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!ClaimApi.API.canInteractWithBlock(event.getEntity().getLevel(), event.getPos(), InteractionType.USE, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!ClaimApi.API.canInteractWithEntity(event.getEntity().getLevel(), event.getTarget(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onAttackBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!ClaimApi.API.canInteractWithBlock(event.getEntity().getLevel(), event.getPos(), InteractionType.ATTACK, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onAttackEntity(AttackEntityEvent event) {
        if (!ClaimApi.API.canDamageEntity(event.getEntity().getLevel(), event.getTarget(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Prevent players from using buckets in protected chunks
    private static void onFillBucket(FillBucketEvent event) {
        var target = event.getTarget();
        if (target != null && ClaimApi.API.canBreakBlock(event.getLevel(), BlockPos.containing(target.getLocation()), event.getEntity())) {
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    // Prevent explosions from destroying blocks in protected chunks
    private static void onExplode(ExplosionEvent.Detonate event) {
        Player player = event.getExplosion().getIndirectSourceEntity() instanceof Player p ? p : null;
        event.getAffectedBlocks().removeIf(next -> (ClaimApi.API.isClaimed(event.getLevel(), next) && (player == null || !ClaimApi.API.canPlaceBlock(event.getLevel(), next, player))));
        event.getAffectedEntities().removeIf(next -> (ClaimApi.API.isClaimed(event.getLevel(), next.chunkPosition()) && (player == null || !ClaimApi.API.canDamageEntity(event.getLevel(), next, player))));
    }

    // Prevent players from trampling crops in protected chunks
    private static void onFarmLandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!ClaimApi.API.canBreakBlock(player.level, event.getPos(), player)) {
                event.setCanceled(true);
            }
        } else if (!ClaimApi.API.canEntityGrief(event.getEntity().getLevel(), event.getPos(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onEntityMobGriefing(EntityMobGriefingEvent event) {
        if (!ClaimApi.API.canEntityGrief(event.getEntity().getLevel(), event.getEntity())) {
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    // Prevent mobs destroying blocks in protected chunks
    private static void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
        if (!ClaimApi.API.canEntityGrief(event.getEntity().getLevel(), event.getPos(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Prevent players from picking up items in protected chunks unless they dropped them
    private static void onItemPickup(EntityItemPickupEvent event) {
        if (!ClaimApi.API.canPickupItem(event.getItem().level, event.getItem().blockPosition(), event.getItem(), event.getEntity())) {
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    // Prevent entities from being affected by lightning in protected chunks
    private static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        if (event.getLightning().getCause() != null) {
            if (!ClaimApi.API.canDamageEntity(event.getEntity().level, event.getEntity(), event.getLightning().getCause())) {
                event.setCanceled(true);
            }
        } else if (!ClaimApi.API.canEntityGrief(event.getLightning().level, event.getLightning())) {
            event.setCanceled(true);
        }
    }

    private static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile().getOwner() instanceof Player player) {
            if (!ClaimApi.API.canDamageEntity(event.getEntity().level, event.getEntity(), player)) {
                event.setCanceled(true);
            }
        }
    }

    // Prevent mobs from taking damage from players in protected chunks
    private static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (!ClaimApi.API.canDamageEntity(event.getEntity().level, event.getEntity(), player)) {
                event.setCanceled(true);
            }
        }
    }

    private static void onPistonPush(PistonEvent.Pre event) {
        // TODO
    }
}