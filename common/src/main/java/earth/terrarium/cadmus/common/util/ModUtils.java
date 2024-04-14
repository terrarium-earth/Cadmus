package earth.terrarium.cadmus.common.util;

import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import earth.terrarium.cadmus.common.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

public class ModUtils {

    public static void displayTeamName(ServerPlayer player, ChunkPos pos) {
        if (!(player instanceof LastMessageHolder holder)) return;

        var claimData = ClaimHandler.getClaim(player.serverLevel(), player.chunkPosition());
        Component displayName = null;
        if (claimData != null) {
            displayName = TeamHelper.getTeamName(claimData.getFirst(), player.server);
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
            boolean isMember = TeamHelper.isMember(claimData.getFirst(), player.server, player.getUUID());
            ChatFormatting teamColor = TeamHelper.getTeamColor(claimData.getFirst(), player.getServer());

            ChatFormatting color = isMember ? teamColor : ChatFormatting.DARK_RED;
            player.displayClientMessage(displayName.copy().withStyle(color), true);
        }
    }

    public static GameProfileCache getProfileCache(MinecraftServer server) {
        return Objects.requireNonNull(server.getProfileCache());
    }

    public static Component translatableWithStyle(String key, Object... args) {
        for (int i = 0; i < args.length; ++i) {
            if (!(args[i] instanceof MutableComponent component)) continue;
            if (component.getStyle().getColor() == null) continue;

            ChatFormatting color = ChatFormatting.getByName(component.getStyle().getColor().toString());
            if (color != null) {
                args[i] = "§" + color.getChar() + component.getString();
            }
        }

        return Component.literal(CommonUtils.serverTranslatable(key, args).getString());
    }

    public static boolean isAdmin(String id) {
        return id.charAt(0) == 'a';
    }

    public static boolean isPlayer(String id) {
        return id.charAt(0) == 'p';
    }

    public static boolean isTeam(String id) {
        return id.charAt(0) == 't';
    }
}
