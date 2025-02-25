package earth.terrarium.cadmus.fabric;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.commands.CadmusCommands;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.types.fabric.BlockBreakProtectionImpl;
import earth.terrarium.cadmus.common.protections.types.fabric.BlockInteractProtectionImpl;
import earth.terrarium.cadmus.common.protections.types.fabric.EntityDamageProtectionImpl;
import earth.terrarium.cadmus.common.protections.types.fabric.EntityInteractProtectionImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

public class CadmusFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Cadmus.init();
        ServerLifecycleEvents.SERVER_STARTED.register(Cadmus::onServerStarted);
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> CadmusCommands.register(dispatcher, context));

        BlockBreakProtectionImpl.register();
        BlockInteractProtectionImpl.register();
        EntityInteractProtectionImpl.register();
        EntityDamageProtectionImpl.register();

        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide()) {
                var claim = ClaimApi.API.getClaim(level, player.chunkPosition());
                if (claim.isPresent()) {
                    if (!Flags.USE.get(level.getServer(), claim.get().first().id())) {
                        return InteractionResultHolder.fail(stack);
                    }
                }
            }
            return InteractionResultHolder.pass(stack);
        });
    }
}