package earth.terrarium.cadmus.forge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.forge.CadmusClientForge;
import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod(Cadmus.MOD_ID)
public class CadmusForge {
    public CadmusForge() {
        Cadmus.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CadmusClientForge::init);

        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(CadmusForge::onPlayerLoggedIn);
        bus.addListener(CadmusForge::onEnterSection);
        registerChunkProtectionEvents(bus);
    }

    private static void registerChunkProtectionEvents(IEventBus bus) {
        bus.addListener(CadmusForge::onBlockBreak);
        bus.addListener(CadmusForge::onBlockPlace);
        bus.addListener(CadmusForge::onBlockInteract);
        bus.addListener(CadmusForge::onEntityInteract);
        bus.addListener(CadmusForge::onAttackBlock);
        bus.addListener(CadmusForge::onAttackEntity);
        bus.addListener(CadmusForge::onFillBucket);
        bus.addListener(CadmusForge::onExplode);
        bus.addListener(CadmusForge::onFarmLandTrample);
        bus.addListener(CadmusForge::onEntityMobGriefing);
        bus.addListener(CadmusForge::onLivingDestroyBlock);
        bus.addListener(CadmusForge::onItemPickup);
        bus.addListener(CadmusForge::onEntityStruckByLightning);
        bus.addListener(CadmusForge::onProjectileImpact);
        bus.addListener(CadmusForge::onLivingAttack);
        bus.addListener(CadmusForge::onPistonPush);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ClaimUtils.sendSyncPacket((ServerPlayer) event.getEntity());
    }

    private static void onEnterSection(EntityEvent.EnteringSection event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ClaimUtils.displayTeamName(player);
        }
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (ClaimUtils.inProtectedChunk(event.getPlayer(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (ClaimUtils.inProtectedChunk(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (ClaimUtils.inProtectedChunk(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (ClaimUtils.inProtectedChunk(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void onAttackBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (ClaimUtils.inProtectedChunk(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void onAttackEntity(AttackEntityEvent event) {
        if (ClaimUtils.inProtectedChunk(event.getEntity(), event.getTarget().blockPosition())) {
            event.setCanceled(true);
        }
    }

    // Prevent players from using buckets in protected chunks
    private static void onFillBucket(FillBucketEvent event) {
        var target = event.getTarget();
        if (target != null && ClaimUtils.inProtectedChunk(event.getEntity(), BlockPos.containing(target.getLocation()))) {
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    // Prevent explosions from destroying blocks in protected chunks
    private static void onExplode(ExplosionEvent.Detonate event) { // TODO
        for (var blockPos : event.getAffectedBlocks()) {
            if (ClaimUtils.inProtectedChunk(event.getLevel(), blockPos)) {
                event.getAffectedBlocks().clear();
                event.getAffectedEntities().removeIf(entity -> entity instanceof HangingEntity || entity instanceof ArmorStand);
                break;
            }
        }
    }

    // Prevent players from trampling crops in protected chunks
    private static void onFarmLandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (ClaimUtils.inProtectedChunk(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void onEntityMobGriefing(EntityMobGriefingEvent event) {
        if (ClaimUtils.inProtectedChunk(event.getEntity())) {
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    // Prevent mobs destroying blocks in protected chunks
    private static void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
        if (ClaimUtils.inProtectedChunk(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    // Prevent players from picking up items in protected chunks unless they dropped them
    private static void onItemPickup(EntityItemPickupEvent event) {
        Entity owner = event.getItem().getOwner();
        if (ClaimUtils.inProtectedChunk(event.getEntity()) && !Objects.equals(owner, event.getEntity())) {
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    // Prevent entities from being affected by lightning in protected chunks
    private static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        if (ClaimUtils.inProtectedChunk(event.getLightning().getCause(), event.getEntity().blockPosition())) {
            event.setCanceled(true);
        }
    }

    private static void onProjectileImpact(ProjectileImpactEvent event) {
        if (ClaimUtils.inProtectedChunk(event.getProjectile().getOwner(), event.getEntity().blockPosition())) {
            event.setCanceled(true);
        }
    }

    // Prevent mobs from taking damage from players in protected chunks
    private static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player && ClaimUtils.inProtectedChunk((event.getSource().getEntity()))) {
            event.setCanceled(true);
        }
    }

    // Prevent pistons from pushing blocks into protected chunks
    private static void onPistonPush(PistonEvent.Pre event) {
        var pos = event.getPos();
        var direction = event.getDirection();
        if (event.getLevel() instanceof Level level) {
            if (ClaimUtils.inProtectedChunk(level, new BlockPos(
                    pos.getX() + direction.getStepX() * 16,
                    pos.getY() + direction.getStepY() * 16,
                    pos.getZ() + direction.getStepZ() * 16))
            ) {
                event.setCanceled(true);
            }
        }
    }
}