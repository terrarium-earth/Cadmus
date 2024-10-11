package earth.terrarium.cadmus.mixins.common.protections;

import earth.terrarium.cadmus.common.protections.Protections;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ReplaceBlock;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.item.enchantment.effects.SetBlockProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ReplaceDisk.class, ReplaceBlock.class, SetBlockProperties.class})
public abstract class EnchantmentEffectMixins {

    // Prevent frost walker from creating frosted ice in protected chunks
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void cadmus$cancelReplacement(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 origin, CallbackInfo ci) {
        if (entity instanceof Player player && !Protections.BLOCK_PLACING.canPlaceBlock(player, player.blockPosition(), Blocks.FROSTED_ICE.defaultBlockState())) {
            ci.cancel();
        } else if (!Protections.MOB_GRIEFING.canMobGrief(entity)) {
            ci.cancel();
        }
    }
}
