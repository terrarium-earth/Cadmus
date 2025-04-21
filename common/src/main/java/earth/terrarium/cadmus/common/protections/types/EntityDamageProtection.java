package earth.terrarium.cadmus.common.protections.types;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.flags.types.BooleanFlag;
import earth.terrarium.cadmus.api.protections.Protection;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.ClaimSettings;
import earth.terrarium.cadmus.common.tags.ModEntityTypeTags;
import earth.terrarium.cadmus.common.utils.CadmusGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class EntityDamageProtection implements Protection {

    @Override
    public String setting() {
        return ClaimSettings.CAN_DAMAGE_ENTITIES;
    }

    @Override
    public String permission() {
        return "cadmus.entity_damage";
    }

    @Override
    public String personalPermission() {
        return "cadmus.personal.entity_damage";
    }

    @Override
    public BooleanFlag flag() {
        return Flags.ENTITY_DAMAGE;
    }

    @Override
    public GameRules.Key<GameRules.BooleanValue> gameRule() {
        return CadmusGameRules.DO_CLAIMED_ENTITY_DAMAGE;
    }

    public boolean canDamageEntity(Player player, Entity entity) {
        return canDamageEntity(player.level(), player.getGameProfile(), entity);
    }

    public boolean canDamageEntity(Level level, GameProfile player, Entity entity) {
        if (entity.getType().is(ModEntityTypeTags.ALLOWS_CLAIM_DAMAGE_ENTITIES)) return true;
        return level.isClientSide() || getId(level, entity.chunkPosition()).map(team ->
            checkFlags(level.getServer(), entity, team.id()) && isPlayerAllowed(level, player, team)).orElse(true);
    }

    private boolean checkFlags(MinecraftServer server, Entity entity, UUID teamId) {
        if (!FlagApi.API.isAdminTeam(server, teamId)) return true;

        if (entity instanceof Player) return Flags.PVP.get(server, teamId);

        if (entity instanceof Enemy || entity.getType().is(ModEntityTypeTags.MONSTERS)) {
            return Flags.MONSTER_DAMAGE.get(server, teamId);
        } else {
            if (entity instanceof Mob || entity.getType().is(ModEntityTypeTags.CREATURES)) {
                return Flags.CREATURE_DAMAGE.get(server, teamId);
            }

            return Flags.ENTITY_DAMAGE.get(server, teamId);
        }
    }
}
