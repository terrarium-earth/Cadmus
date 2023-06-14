package earth.terrarium.cadmus.mixin.common.xaero;

import earth.terrarium.cadmus.common.compat.xaero.XaerosCompat;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xaero.map.WorldMapSession;
import xaero.map.biome.BiomeColorCalculator;
import xaero.map.biome.BiomeGetter;
import xaero.map.cache.BlockStateShortShapeCache;
import xaero.map.cache.BrokenBlockTintCache;
import xaero.map.executor.Executor;
import xaero.map.file.MapSaveLoad;
import xaero.map.file.worldsave.WorldDataHandler;
import xaero.map.file.worldsave.WorldDataReader;
import xaero.map.file.worldsave.biome.WorldDataBiomeManager;
import xaero.map.graphics.TextureUploader;
import xaero.map.highlight.HighlighterRegistry;

@Pseudo
@Mixin(value = WorldMapSession.class, remap = false)
public abstract class WorldMapSessionMixin {
    @Inject(
            method = "init",
            at = @At(value = "INVOKE", target = "Lxaero/map/highlight/HighlighterRegistry;end()V"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void cadmus$registerHighlighters(ClientPacketListener connection, long biomeZoomSeed, CallbackInfo ci, BlockStateShortShapeCache blockStateShortShapeCache, MapSaveLoad mapSaveLoad, TextureUploader textureUploader, BiomeGetter biomeGetter, BrokenBlockTintCache brokenBlockTintCache, BiomeColorCalculator biomeColorCalculator, WorldDataBiomeManager worldDataBiomeManager, WorldDataReader worldDataReader, Executor worldDataRenderExecutor, WorldDataHandler worldDataHandler, HighlighterRegistry highlightRegistry) {
        XaerosCompat.registerHighlighters(highlightRegistry);
    }
}
