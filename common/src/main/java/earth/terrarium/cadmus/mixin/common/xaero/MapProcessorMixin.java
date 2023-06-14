package earth.terrarium.cadmus.mixin.common.xaero;

import earth.terrarium.cadmus.common.compat.xaero.XaerosCompat;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.MapProcessor;
import xaero.map.world.MapWorld;

@Pseudo
@Mixin(value = MapProcessor.class, remap = false)
public abstract class MapProcessorMixin {
    @Shadow
    private MapWorld mapWorld;

    @Inject(method = "updateWorld", at = @At("TAIL"))
    private void cadmus$addListeners(CallbackInfo ci) {
        ResourceKey<Level> dimId = mapWorld.getCurrentDimensionId();
        if (dimId != null) XaerosCompat.registerListener(dimId);
    }

    @Inject(method = "onWorldUnload", at = @At("TAIL"))
    private void cadmus$removeListeners(CallbackInfo ci) {
        ResourceKey<Level> dimId = mapWorld.getCurrentDimensionId();
        if (dimId != null) XaerosCompat.removeListener(dimId);
    }
}
