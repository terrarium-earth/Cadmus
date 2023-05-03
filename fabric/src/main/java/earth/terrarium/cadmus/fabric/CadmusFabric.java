package earth.terrarium.cadmus.fabric;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.compat.fabric.cpa.CommonProtectionApiCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;

public class CadmusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Cadmus.init();

        if (FabricLoader.getInstance().isModLoaded("common-protection-api")) {
            CommonProtectionApiCompat.init();
        }
        registerChunkProtectionEvents();
    }

    private static void registerChunkProtectionEvents() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, blockState, blockEntity) -> ClaimApi.API.canBreakBlock(level, pos, player));

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!ClaimApi.API.canInteractWithBlock(level, hitResult.getBlockPos(), InteractionType.USE, player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!ClaimApi.API.canInteractWithEntity(level, entity, player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, level, hand, blockPos, direction) -> {
            if (!ClaimApi.API.canInteractWithBlock(level, blockPos, InteractionType.ATTACK, player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!ClaimApi.API.canDamageEntity(level, entity, player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }
}