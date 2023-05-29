package earth.terrarium.cadmus.common.util;

import com.teamresourceful.resourcefullib.common.lib.Constants;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ModUtils {
    public static void displayTeamName(ServerPlayer player, ChunkPos pos) {
        if (!(player instanceof LastMessageHolder holder)) return;

        var claimData = ClaimHandler.getClaim(player.serverLevel(), player.chunkPosition());
        Component displayName = null;
        if (claimData != null) {
            displayName = TeamProviderApi.API.getSelected().getTeamName(claimData.getFirst(), player.server);
        }

        Component lastMessage = holder.cadmus$getLastMessage();
        if (lastMessage == null) {
            Component greeting = AdminClaimHandler.getFlag((ServerLevel) player.level(), player.chunkPosition(), ModFlags.GREETING);
            if (!greeting.getString().isBlank()) {
                player.displayClientMessage(greeting, false);
            }
        }
        if (Objects.equals(displayName, lastMessage)) return;
        holder.cadmus$setLastMessage(displayName);

        if (displayName == null) {
            player.displayClientMessage(ConstantComponents.WILDERNESS, true);
            Component farewell = AdminClaimHandler.getFlag(player.serverLevel(), pos, ModFlags.FAREWELL);
            if (!farewell.getString().isBlank()) {
                player.displayClientMessage(farewell, false);
            }
        } else {
            boolean isMember = TeamProviderApi.API.getSelected().isMember(claimData.getFirst(), player.server, player.getUUID());
            ChatFormatting teamColor = TeamProviderApi.API.getSelected().getTeamColor(claimData.getFirst(), player.getServer());

            ChatFormatting color = isMember ? teamColor : ChatFormatting.DARK_RED;
            player.displayClientMessage(displayName.copy().withStyle(color), true);
        }
    }

    public static boolean tryClaim(ServerLevel level, ServerPlayer player, Map<ChunkPos, ClaimType> addedChunks, Map<ChunkPos, ClaimType> removedChunks) {
        String id = TeamProviderApi.API.getSelected().getTeamId(player.getServer(), player.getUUID());

        // Check if the player is claiming more chunks than allowed
        var teamClaims = ClaimHandler.getTeamClaims(level, id);
        int maxClaims = MaxClaimProviderApi.API.getSelected().getMaxClaims(id, player.getServer(), player);
        if (!addedChunks.isEmpty() && (teamClaims == null ? 0 : teamClaims.values().size()) + addedChunks.size() - removedChunks.size() > maxClaims) {
            Constants.LOGGER.warn("Player {} tried to claim more chunks than allowed! ({} > {})", player.getName().getString(), teamClaims == null ? 0 : teamClaims.values().size() + addedChunks.size() - removedChunks.size(), maxClaims);
            return false;
        }

        // Check if the player is claiming more chunk loaded chunks than allowed
        int maxChunkLoaded = MaxClaimProviderApi.API.getSelected().getMaxChunkLoaded(id, player.getServer(), player);
        int currentChunkLoaded = 0;
        int addedChunkLoaded = addedChunks.values().stream().filter(claim -> claim == ClaimType.CHUNK_LOADED).toArray().length;
        int removedChunkLoaded = removedChunks.values().stream().filter(claim -> claim == ClaimType.CHUNK_LOADED).toArray().length;
        if (teamClaims != null) {
            currentChunkLoaded = teamClaims.values().stream().filter(claim -> claim == ClaimType.CHUNK_LOADED).toArray().length;
        }
        if (currentChunkLoaded + addedChunkLoaded - removedChunkLoaded > maxChunkLoaded) {
            Constants.LOGGER.warn("Player {} tried to claim more chunk loaded chunks than allowed! ({} > {})", player.getName().getString(), currentChunkLoaded + addedChunkLoaded - removedChunkLoaded, maxChunkLoaded);
            return false;
        }

        claim(id, level, addedChunks, removedChunks.keySet());
        return true;
    }

    public static void claim(String id, ServerLevel level, Map<ChunkPos, ClaimType> addedChunks, Set<ChunkPos> removedChunks) {
        ClaimHandler.updateChunkLoaded(level, id, false);

        ClaimHandler.addClaims(level, id, addedChunks);
        ClaimHandler.removeClaims(level, id, removedChunks);

        ClaimHandler.updateChunkLoaded(level, id, true);
        level.players().forEach(player -> displayTeamName(player, player.chunkPosition()));
    }

    public static Component serverTranslation(String translation, Object... args) {
        if (args.length == 0) {
            Component component = Component.translatable(translation);
            return Component.translatableWithFallback(translation, component.getString());
        }
        Component component = Component.translatable(translation, args);
        return Component.translatableWithFallback(translation, component.getString(), args);
    }
}
