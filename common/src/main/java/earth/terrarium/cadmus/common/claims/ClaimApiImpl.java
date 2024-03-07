package earth.terrarium.cadmus.common.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.InteractionType;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.compat.prometheus.CadmusAutoCompletes;
import earth.terrarium.cadmus.common.compat.prometheus.PrometheusCompat;
import earth.terrarium.cadmus.common.util.ModEntityTags;
import earth.terrarium.cadmus.common.util.ModGameRules;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClaimApiImpl implements ClaimApi {
    @Override
    public boolean canClaim(ServerLevel level, ChunkPos pos, String id, boolean chunkLoad, UUID player) {
        Map<ChunkPos, ClaimType> currentClaims = ClaimHandler.getTeamClaims(level, id);
        if (currentClaims == null) return true;
        int maxClaims = MaxClaimProviderApi.API.getSelected().getMaxClaims(id, level, player);
        if (currentClaims.size() >= maxClaims) return false;
        if (chunkLoad) {
            int currentChunkLoaded = currentClaims.values().stream().filter(claim -> claim == ClaimType.CHUNK_LOADED).toArray().length;
            int maxChunkLoaded = MaxClaimProviderApi.API.getSelected().getMaxChunkLoaded(id, level, player);
            return currentChunkLoaded < maxChunkLoaded;
        }
        return true;
    }

    @Override
    public void claim(ServerLevel level, ChunkPos pos, String id, boolean chunkLoad) {
        ClaimHandler.claim(level, id, pos, chunkLoad ? ClaimType.CHUNK_LOADED : ClaimType.CLAIMED);
        level.players().forEach(player -> ModUtils.displayTeamName(player, player.chunkPosition()));
    }

    @Override
    public void unclaim(ServerLevel level, ChunkPos pos, String id) {
        ClaimHandler.unclaim(level, id, pos);
        level.players().forEach(player -> ModUtils.displayTeamName(player, player.chunkPosition()));
    }

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
            (id, server) -> AdminClaimHandler.getBooleanFlag(server, id, ModFlags.BLOCK_BREAK),
            ClaimSettings::canBreak,
            (id, server) -> TeamProviderApi.API.getSelected().canBreakBlock(id, server, pos, player));
    }

    @Override
    public boolean canBreakBlock(Level level, BlockPos pos, @NotNull Player player) {
        return canBreakBlock(level, pos, player.getUUID());
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, UUID player) {
        return canAccess(level, pos, player, CadmusAutoCompletes.BLOCK_PLACING, ModGameRules.RULE_DO_CLAIMED_BLOCK_PLACING,
            (id, server) -> AdminClaimHandler.getBooleanFlag(server, id, ModFlags.BLOCK_PLACE),
            ClaimSettings::canPlace,
            (id, server) -> TeamProviderApi.API.getSelected().canPlaceBlock(id, server, pos, player));
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, @NotNull Player player) {
        return canPlaceBlock(level, pos, player.getUUID());
    }

    @Override
    public boolean canExplodeBlock(Level level, ChunkPos pos) {
        if (level.isClientSide()) return false;

        Pair<String, ClaimType> claim = ClaimHandler.getClaim((ServerLevel) level, pos);
        boolean isAdmin = claim != null && ModUtils.isAdmin(claim.getFirst());

        return (claim != null) && (!isAdmin || !AdminClaimHandler.getBooleanFlag(level.getServer(), claim.getFirst(), ModFlags.BLOCK_EXPLOSIONS));
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, UUID player) {
        return canAccess(level, pos, player, CadmusAutoCompletes.BLOCK_EXPLOSIONS, ModGameRules.RULE_DO_CLAIMED_BLOCK_EXPLOSIONS,
            (id, server) -> AdminClaimHandler.getBooleanFlag(server, id, ModFlags.BLOCK_EXPLOSIONS),
            ClaimSettings::canExplode,
            (id, server) -> TeamProviderApi.API.getSelected().canExplodeBlock(id, server, pos, explosion, player));
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, @NotNull Player player) {
        return canExplodeBlock(level, pos, explosion, player.getUUID());
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, UUID player) {
        if (level.getBlockState(pos).is(Cadmus.ALLOWS_CLAIM_INTERACTIONS)) return true;
        return canAccess(level, pos, player, CadmusAutoCompletes.BLOCK_INTERACTIONS, ModGameRules.RULE_DO_CLAIMED_BLOCK_INTERACTIONS,
            (id, server) -> {
                BlockState state = level.getBlockState(pos);
                if (state.is(Cadmus.DOOR_LIKE)) return AdminClaimHandler.getBooleanFlag(server, id, ModFlags.USE_DOORS);
                if (state.is(Cadmus.INTERACTABLE_STORAGE)) return AdminClaimHandler.getBooleanFlag(server, id, ModFlags.USE_CHESTS);
                if (state.is(Cadmus.REDSTONE)) return AdminClaimHandler.getBooleanFlag(server, id, ModFlags.USE_REDSTONE);
                return AdminClaimHandler.getBooleanFlag(server, id, ModFlags.BLOCK_INTERACTIONS);
            },
            ClaimSettings::canInteractWithBlocks,
            (id, server) -> TeamProviderApi.API.getSelected().canInteractWithBlock(id, server, pos, type, player));
    }

    @Override
    public boolean canInteractWithBlock(Level level, BlockPos pos, InteractionType type, @NotNull Player player) {
        return canInteractWithBlock(level, pos, type, player.getUUID());
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, UUID player) {
        if (entity.getType().is(ModEntityTags.ALLOWS_CLAIM_INTERACTIONS_ENTITIES)) return true;
        return canAccess(level, entity.blockPosition(), player, CadmusAutoCompletes.ENTITY_INTERACTIONS, ModGameRules.RULE_DO_CLAIMED_ENTITY_INTERACTIONS,
            (id, server) -> AdminClaimHandler.getBooleanFlag(server, id, ModFlags.ENTITY_INTERACTIONS),
            ClaimSettings::canInteractWithEntities,
            (id, server) -> TeamProviderApi.API.getSelected().canInteractWithEntity(id, server, entity, player));
    }

    @Override
    public boolean canInteractWithEntity(Level level, Entity entity, @NotNull Player player) {
        return canInteractWithEntity(level, entity, player.getUUID());
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, UUID player) {
        if (entity.getType().is(ModEntityTags.ALLOWS_CLAIM_DAMAGE_ENTITIES)) return true;
        return canAccess(level, entity.blockPosition(), player, CadmusAutoCompletes.ENTITY_DAMAGE, ModGameRules.RULE_CLAIMED_DAMAGE_ENTITIES,
            (id, server) -> {
                if (entity instanceof Player) {
                    return AdminClaimHandler.getBooleanFlag(server, id, ModFlags.PVP);
                }

                if (entity instanceof Enemy || entity.getType().is(ModEntityTags.MONSTERS)) {
                    return AdminClaimHandler.getBooleanFlag(server, id, ModFlags.MONSTER_DAMAGE);
                } else {
                    if (entity instanceof Mob || entity.getType().is(ModEntityTags.CREATURES)) {
                        return AdminClaimHandler.getBooleanFlag(server, id, ModFlags.CREATURE_DAMAGE);
                    }

                    return AdminClaimHandler.getBooleanFlag(server, id, ModFlags.ENTITY_DAMAGE);
                }
            },
            ClaimSettings::canDamageEntities,
            (id, server) -> TeamProviderApi.API.getSelected().canDamageEntity(id, server, entity, player));
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, @NotNull Player player) {
        return canDamageEntity(level, entity, player.getUUID());
    }

    @Override
    public boolean canEntityGrief(Level level, @NotNull Entity entity) {
        return canEntityGrief(level, entity.blockPosition(), entity);
    }

    @Override
    public boolean canEntityGrief(Level level, BlockPos pos, @NotNull Entity entity) {
        if (entity.getType().is(ModEntityTags.CAN_GRIEF_ENTITIES)) return true;
        if (!level.isClientSide()) {
            var claim = ClaimHandler.getClaim((ServerLevel) level, new ChunkPos(pos));
            if (claim == null) return true;
            if (ModUtils.isAdmin(claim.getFirst())) {
                return AdminClaimHandler.getBooleanFlag((ServerLevel) level, new ChunkPos(pos), ModFlags.MOB_GRIEFING);
            }
        }
        return ModGameRules.getOrCreateBooleanGameRule(level, ModGameRules.RULE_CLAIMED_MOB_GRIEFING);
    }

    @Override
    public boolean canPickupItem(Level level, BlockPos pos, ItemEntity item, @NotNull Entity picker) {
        if (item.getItem().is(Cadmus.ALLOWS_CLAIM_PICKUP)) return true;
        if (!level.isClientSide()) {
            if (!AdminClaimHandler.getBooleanFlag((ServerLevel) level, new ChunkPos(pos), ModFlags.ITEM_PICKUP)) {
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

    private boolean canAccess(
        Level level, BlockPos pos, UUID player,
        String permission,
        GameRules.Key<GameRules.BooleanValue> rule,
        ToBooleanBiFunction<String, MinecraftServer> checkFlags,
        ToBooleanBiFunction<ClaimSettings, ClaimSettings> checkSettings,
        ToBooleanBiFunction<String, MinecraftServer> checkTeamPermission
    ) {
        if (!(level instanceof ServerLevel serverLevel)) return true;
        MinecraftServer server = serverLevel.getServer();

        if (CadmusDataHandler.canBypass(server, player)) return true;

        ChunkPos chunkPos = new ChunkPos(pos);
        var claim = ClaimHandler.getClaim(serverLevel, chunkPos);
        if (claim == null) return true;
        if (ModUtils.isAdmin(claim.getFirst()) && !checkFlags.applyAsBoolean(claim.getFirst(), server)) {
            return false;
        }

        if (Cadmus.IS_PROMETHEUS_LOADED && PrometheusCompat.hasPermission(serverLevel.getPlayerByUUID(player), permission)) {
            return true;
        } else if (!Cadmus.IS_PROMETHEUS_LOADED && ModGameRules.getOrCreateBooleanGameRule(level, rule)) {
            return true;
        }

        ClaimSettings settings = CadmusDataHandler.getClaimSettings(server, claim.getFirst());
        ClaimSettings defaultSettings = CadmusDataHandler.getDefaultClaimSettings(server);
        if (settings != null && checkSettings.applyAsBoolean(settings, defaultSettings)) {
            return true;
        }

        if (!isClaimed(level, pos)) return true;

        return checkTeamPermission.applyAsBoolean(claim.getFirst(), server);
    }
}
