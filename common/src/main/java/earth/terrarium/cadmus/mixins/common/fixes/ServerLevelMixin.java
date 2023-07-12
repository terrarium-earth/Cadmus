package earth.terrarium.cadmus.mixins.common.fixes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import earth.terrarium.cadmus.Cadmus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * <p>
 * Fix chunk loading not working when no players are on the server.
 * </p><p>
 * Cadmus uses {@link ServerChunkCache#updateChunkForced} directly, which skips adding the force chunk to the list
 * which is used when checking if any force loaded chunks are currently active on the server.
 * </p><p>
 * Cadmus does this to prevent saving the force loaded chunks using the vanilla code. So to fix this issue, we simply
 * count the amount of active cadmus force loaded chunks there are and add a check below.
 * </p>
 *
 * @author Fx Morin
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @ModifyExpressionValue(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/longs/LongSet;isEmpty()Z",
                    ordinal = 0
            )
    )
    private boolean cadmus$fixOfflineChunkLoading(boolean isEmpty) {
        return isEmpty && Cadmus.FORCE_LOADED_CHUNK_COUNT <= 0;
    }
}
