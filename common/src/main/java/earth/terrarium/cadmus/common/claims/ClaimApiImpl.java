package earth.terrarium.cadmus.common.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.compat.prometheus.CadmusAutoCompletes;
import earth.terrarium.cadmus.common.compat.prometheus.PrometheusIntegration;
import earth.terrarium.cadmus.common.util.ModGameRules;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

public class ClaimApiImpl implements ClaimApi {
    @Override
    public boolean isClaimed(Level level, ChunkPos pos) {
        if (!level.isClientSide()) {
            return ClaimHandler.getClaim((ServerLevel) level, pos) != null;
        }
        return false;
    }

    @Override
    public boolean isChunkLoaded(Level level, ChunkPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            var claim = ClaimHandler.getClaim(serverLevel, pos);
            if (claim == null) return false;
            return claim.getSecond() == ClaimType.CHUNK_LOADED;
        }
        return false;
    }

    public boolean canBreakBlock(Level level, BlockPos pos, UUID player) {
        return canAccess(level, pos, player, CadmusAutoCompletes.BLOCK_BREAKING, ModGameRules.RULE_DO_CLAIMED_BLOCK_BREAKING,
            (id, server) -> getBoolFlag(server, id, ModFlags.BLOCK_BREAK),
            (id, server) -> TeamProviderApi.API.getSelected().canBreakBlock(id, server, pos, player));
    }

    @Override
    public boolean canBreakBlock(Level level, BlockPos pos, Player player) {
        return canBreakBlock(level, pos, player.getUUID());
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, UUID player) {
        return canAccess(level, pos, player, CadmusAutoCompletes.BLOCK_PLACING, ModGameRules.RULE_DO_CLAIMED_BLOCK_PLACING,
            (id, server) -> getBoolFlag(server, id, ModFlags.BLOCK_PLACE),
            (id, server) -> TeamProviderApi.API.getSelected().canPlaceBlock(id, server, pos, player));
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, Player player) {
        return canPlaceBlock(level, pos, player.getUUID());
    }

    @Override
    public boolean canExplodeBlock(Level level, ChunkPos pos) {
        if (level.isClientSide()) return false;

        Pair<String, ClaimType> claim = ClaimHandler.getClaim((ServerLevel) level, pos);
        boolean isAdmin = claim != null && claim.getFirst().startsWith(ClaimHandler.ADMIN_PREFIX);

        return (claim != null) && (!isAdmin || !AdminClaimHandler.<Boolean>getFlag(level.getServer(), claim.getFirst(), ModFlags.BLOCK_EXPLOSIONS));
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID player) {
        return canAccess(level, pos, player, CadmusAutoCompletes.BLOCK_EXPLOSIONS, ModGameRules.RULE_DO_CLAIMED_BLOCK_EXPLOSIONS,
            (id, server) -> getBoolFlag(server, id, ModFlags.BLOCK_EXPLOSIONS),
            (id, server) -> TeamProviderApi.API.getSelected().canExplodeBlock(id, server, pos, explosion, player));
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, Player player) {
        return canExplodeBlock(level, pos, explosion, player.getUUID());
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, UUID player) {
        return canAccess(level, pos, player, CadmusAutoCompletes.BLOCK_INTERACTIONS, ModGameRules.RULE_DO_CLAIMED_BLOCK_INTERACTIONS,
            (id, server) -> {
                Block block = level.getBlockState(pos).getBlock();
                if (block instanceof DoorBlock || block instanceof TrapDoorBlock) {
                    return getBoolFlag(server, id, ModFlags.USE_DOORS);
                }

                if (block instanceof AbstractChestBlock<?>) {
                    return getBoolFlag(server, id, ModFlags.USE_CHESTS);
                }

                if (block instanceof LeverBlock || block instanceof ButtonBlock || block instanceof PressurePlateBlock) {
                    return getBoolFlag(server, id, ModFlags.USE_REDSTONE);
                }
                return getBoolFlag(server, id, ModFlags.BLOCK_INTERACTIONS);
            },
            (id, server) -> TeamProviderApi.API.getSelected().canInteractWithBlock(id, server, pos, type, player));
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, Player player) {
        return canInteractWithBlock(level, pos, type, player.getUUID());
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, UUID player) {
        return canAccess(level, entity.blockPosition(), player, CadmusAutoCompletes.ENTITY_INTERACTIONS, ModGameRules.RULE_DO_CLAIMED_ENTITY_INTERACTIONS,
            (id, server) -> getBoolFlag(server, id, ModFlags.ENTITY_INTERACTIONS),
            (id, server) -> TeamProviderApi.API.getSelected().canInteractWithEntity(id, server, entity, player));
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, Player player) {
        return canInteractWithEntity(level, entity, player.getUUID());
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, UUID player) {
        return canAccess(level, entity.blockPosition(), player, CadmusAutoCompletes.ENTITY_DAMAGE, ModGameRules.RULE_CLAIMED_DAMAGE_ENTITIES,
            (id, server) -> {
                if (entity.getType().getCategory() == MobCategory.CREATURE
                    || entity.getType().getCategory() == MobCategory.AMBIENT
                    || entity.getType().getCategory() == MobCategory.AXOLOTLS
                    || entity.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE
                    || entity.getType().getCategory() == MobCategory.WATER_AMBIENT
                    || entity instanceof Animal) {
                    return getBoolFlag(server, id, ModFlags.CREATURE_DAMAGE);
                }

                if (entity.getType().getCategory() == MobCategory.MONSTER || entity instanceof Monster) {
                    return getBoolFlag(server, id, ModFlags.MONSTER_DAMAGE);
                }
                if (entity instanceof Player) {
                    return getBoolFlag(server, id, ModFlags.PVP);
                }
                return getBoolFlag(server, id, ModFlags.ENTITY_DAMAGE);
            },
            (id, server) -> TeamProviderApi.API.getSelected().canDamageEntity(id, server, entity, player));
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, Player player) {
        return canDamageEntity(level, entity, player.getUUID());
    }

    @Override
    public boolean canEntityGrief(Level level, Entity entity) {
        return canEntityGrief(level, entity.blockPosition(), entity);
    }

    @Override
    public boolean canEntityGrief(Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide()) {
            var claim = ClaimHandler.getClaim((ServerLevel) level, new ChunkPos(pos));
            if (claim == null) return true;
            if (claim.getFirst().startsWith(ClaimHandler.ADMIN_PREFIX)) {
                return AdminClaimHandler.<Boolean>getFlag((ServerLevel) level, new ChunkPos(pos), ModFlags.MOB_GRIEFING);
            }
        }
        return ModGameRules.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CLAIMED_MOB_GRIEFING);
    }

    @Override
    public boolean canPickupItem(Level level, BlockPos pos, ItemEntity item, Entity picker) {
        if (!level.isClientSide()) {
            if (!AdminClaimHandler.<Boolean>getFlag((ServerLevel) level, new ChunkPos(pos), ModFlags.ITEM_PICKUP)) {
                return false;
            }
        }

        if (ModGameRules.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CAN_PICKUP_CLAIMED_ITEMS)) {
            return true;
        }
        if (Objects.equals(item.getOwner(), picker)) return true;
        if (picker instanceof Player player) {
            return canInteractWithEntity(level, item, player);
        }
        return canEntityGrief(level, picker);
    }

    private boolean canAccess(Level level, BlockPos pos, UUID player, String
        permission, GameRules.Key<GameRules.BooleanValue> rule, BiFunction<String, MinecraftServer, Boolean> checkFlags, BiFunction<String, MinecraftServer, Boolean> checkTeamPermission) {
        if (!(level instanceof ServerLevel serverLevel)) return true;

        var claim = ClaimHandler.getClaim(serverLevel, new ChunkPos(pos));
        if (claim == null) return true;
        if (claim.getFirst().startsWith(ClaimHandler.ADMIN_PREFIX) && !checkFlags.apply(claim.getFirst(), serverLevel.getServer())) {
            return false;
        }

        if (ModUtils.isModLoaded("prometheus") && PrometheusIntegration.hasPermission(serverLevel.getPlayerByUUID(player), permission)) {
            return true;
        } else if (!ModUtils.isModLoaded("prometheus") && ModGameRules.getOrCreateBooleanGameRule(level, rule)) {
            return true;
        }

        if (!isClaimed(level, pos)) return true;

        return checkTeamPermission.apply(claim.getFirst(), serverLevel.getServer());
    }

    private boolean getBoolFlag(MinecraftServer server, String id, String flag) {
        return AdminClaimHandler.<Boolean>getFlag(server, id, flag);
    }
}
