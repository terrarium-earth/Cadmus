package earth.terrarium.cadmus.common.util;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.ClaimChunkSaveData;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.LastMessageHolder;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.client.SyncClaimedChunksPacket;
import earth.terrarium.cadmus.common.network.messages.client.SyncGameRulePacket;
import earth.terrarium.cadmus.common.registry.ModGameRules;
import earth.terrarium.cadmus.common.team.Team;
import earth.terrarium.cadmus.common.team.TeamSaveData;
import earth.terrarium.cadmus.mixin.common.GameRulesAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModUtils {
    public static final Map<GameRules.Key<GameRules.BooleanValue>, Boolean> CLIENT_GAME_RULES = new HashMap<>();

    public static <T> T generate(Predicate<T> validator, Supplier<T> getter) {
        T value;
        do {
            value = getter.get();
        } while (!validator.test(value));
        return value;
    }

    public static void onPlayerJoin(ServerPlayer player) {
        displayTeamName(player);
        enterChunkSection(player);
        Level level = player.level;
        NetworkHandler.CHANNEL.sendToPlayer(new SyncGameRulePacket(SyncGameRulePacket.DO_CLAIMED_BLOCK_BREAKING, getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_BLOCK_BREAKING)), player);
        NetworkHandler.CHANNEL.sendToPlayer(new SyncGameRulePacket(SyncGameRulePacket.DO_CLAIMED_BLOCK_PLACING, getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_BLOCK_PLACING)), player);
        NetworkHandler.CHANNEL.sendToPlayer(new SyncGameRulePacket(SyncGameRulePacket.DO_CLAIMED_BLOCK_EXPLOSIONS, getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_BLOCK_EXPLOSIONS)), player);
        NetworkHandler.CHANNEL.sendToPlayer(new SyncGameRulePacket(SyncGameRulePacket.DO_CLAIMED_BLOCK_INTERACTIONS, getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_BLOCK_INTERACTIONS)), player);
        NetworkHandler.CHANNEL.sendToPlayer(new SyncGameRulePacket(SyncGameRulePacket.DO_CLAIMED_ENTITY_INTERACTIONS, getOrCreateBooleanGameRule(level, ModGameRules.RULE_DO_CLAIMED_ENTITY_INTERACTIONS)), player);
        NetworkHandler.CHANNEL.sendToPlayer(new SyncGameRulePacket(SyncGameRulePacket.CLAIMED_DAMAGE_ENTITIES, getOrCreateBooleanGameRule(level, ModGameRules.RULE_CLAIMED_DAMAGE_ENTITIES)), player);
        NetworkHandler.CHANNEL.sendToPlayer(new SyncGameRulePacket(SyncGameRulePacket.CLAIMED_MOB_GRIEFING, getOrCreateBooleanGameRule(level, ModGameRules.RULE_CLAIMED_MOB_GRIEFING)), player);
        NetworkHandler.CHANNEL.sendToPlayer(new SyncGameRulePacket(SyncGameRulePacket.CAN_PICKUP_CLAIMED_ITEMS, getOrCreateBooleanGameRule(level, ModGameRules.RULE_CAN_PICKUP_CLAIMED_ITEMS)), player);
    }

    public static void enterChunkSection(ServerPlayer player) {
        displayTeamName(player);
        var info = ClaimChunkSaveData.get(player);
        if (info == null) return;
        NetworkHandler.CHANNEL.sendToPlayer(new SyncClaimedChunksPacket(player.chunkPosition(), info), player);
    }

    public static void displayTeamName(ServerPlayer player) {
        if (!(player instanceof LastMessageHolder holder)) return;

        var team = Optionull.mapOrDefault(ClaimChunkSaveData.get(player), ClaimInfo::team, new Team(null, null, null, ""));
        String name = team.name();
        String lastMessage = holder.cadmus$getLastMessage();

        if (Objects.equals(name, lastMessage)) return;
        holder.cadmus$setLastMessage(name);
        if (team.creator() == null) {
            player.displayClientMessage(ConstantComponents.WILDERNESS, true);
        } else {
            var members = ClaimApi.API.getClaimMembers(player.level, player.chunkPosition());
            ChatFormatting color = members.contains(player.getUUID()) ? ChatFormatting.AQUA : ChatFormatting.DARK_RED;
            player.displayClientMessage(Component.nullToEmpty(name).copy().withStyle(color), true);
        }
    }

    public static int getOrCreateIntGameRule(Level level, GameRules.Key<GameRules.IntegerValue> key) {
        GameRulesAccessor gameRules = (GameRulesAccessor) level.getGameRules();
        if (!gameRules.rules().containsKey(key)) {
            copyAndAddRule(gameRules, key);
        }
        return ((GameRules.IntegerValue) gameRules.rules().get(key)).get();
    }

    public static boolean getOrCreateBooleanGameRule(Level level, GameRules.Key<GameRules.BooleanValue> key) {
        if (level.isClientSide && CLIENT_GAME_RULES.containsKey(key)) {
            return CLIENT_GAME_RULES.get(key);
        }

        GameRulesAccessor gameRules = (GameRulesAccessor) level.getGameRules();
        if (!gameRules.rules().containsKey(key)) {
            copyAndAddRule(gameRules, key);
        }
        return ((GameRules.BooleanValue) gameRules.rules().get(key)).get();
    }

    private static void copyAndAddRule(GameRulesAccessor gameRules, GameRules.Key<?> key) {
        HashMap<GameRules.Key<?>, GameRules.Value<?>> rules = new HashMap<>(gameRules.rules());
        rules.put(key, GameRulesAccessor.getAllRules().get(key).createRule());
        gameRules.setRules(rules);
    }
}
