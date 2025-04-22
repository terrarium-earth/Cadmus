package earth.terrarium.cadmus.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.client.neoforge.CadmusClientNeoForge;
import earth.terrarium.cadmus.common.commands.CadmusCommands;
import earth.terrarium.cadmus.common.flags.Flags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(Cadmus.MOD_ID)
public class CadmusNeoForge {
    public CadmusNeoForge() {
        Cadmus.init();
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onEnterSection);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onRightClick);
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Cadmus.onPlayerJoin(player);
        }
    }

    private void onServerStarted(ServerStartedEvent event) {
        Cadmus.onServerStarted(event.getServer());
    }

    private void onEnterSection(EntityEvent.EnteringSection event) {
        if (event.getEntity() instanceof Player player) {
            Cadmus.onEnterSection(player, event.getOldPos().chunk());
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CadmusCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    private void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide()) {
            ClaimApi.API.getClaim(event.getLevel(), event.getEntity().chunkPosition()).ifPresent(claim -> {
                if (!Flags.USE.get(event.getLevel().getServer(), claim.team().id())) {
                    event.setCanceled(true);
                }
            });
        }
    }
}
