package earth.terrarium.cadmus.fabric;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import earth.terrarium.cadmus.compat.fabric.cpa.CommonProtectionApiCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;

public class CadmusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Cadmus.init();

        if (FabricLoader.getInstance().isModLoaded("common-protection-api")) {
            CommonProtectionApiCompat.init();
        }

        // Send chunk data to client
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ClaimUtils.sendSyncPacket(handler.player));
        registerChunkProtectionEvents();
    }

    private static void registerChunkProtectionEvents() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, blockPos, blockState, blockEntity) -> !ClaimUtils.inProtectedChunk(player, blockPos));

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (ClaimUtils.inProtectedChunk(player, hitResult.getBlockPos())) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (ClaimUtils.inProtectedChunk(player, entity.blockPosition())) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, level, hand, blockPos, direction) -> {
            if (ClaimUtils.inProtectedChunk(player, blockPos)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (ClaimUtils.inProtectedChunk(player, entity.blockPosition())) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }
}