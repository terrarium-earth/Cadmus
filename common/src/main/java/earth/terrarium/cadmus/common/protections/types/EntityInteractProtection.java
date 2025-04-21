package earth.terrarium.cadmus.common.protections.types;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.flags.types.BooleanFlag;
import earth.terrarium.cadmus.api.protections.Protection;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.ClaimSettings;
import earth.terrarium.cadmus.common.tags.ModEntityTypeTags;
import earth.terrarium.cadmus.common.utils.CadmusGameRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class EntityInteractProtection implements Protection {

    @Override
    public String setting() {
        return ClaimSettings.CAN_INTERACT_WITH_ENTITIES;
    }

    @Override
    public String permission() {
        return "cadmus.entity_interactions";
    }

    @Override
    public String personalPermission() {
        return "cadmus.personal.entity_interactions";
    }

    @Override
    public BooleanFlag flag() {
        return Flags.ENTITY_INTERACTIONS;
    }

    @Override
    public GameRules.Key<GameRules.BooleanValue> gameRule() {
        return CadmusGameRules.DO_CLAIMED_ENTITY_INTERACTIONS;
    }

    public boolean canInteractWithEntity(Player player, Entity entity) {
        return canInteractWithEntity(player.level(), player.getGameProfile(), entity);
    }

    public boolean canInteractWithEntity(Level level, GameProfile player, Entity entity) {
        if (entity.getType().is(ModEntityTypeTags.ALLOWS_CLAIM_INTERACTIONS_ENTITIES)) return true;
        return level.isClientSide() || getId(level, entity.chunkPosition()).map(id ->
            isPlayerAllowed(level, player, id)).orElse(true);
    }
}
