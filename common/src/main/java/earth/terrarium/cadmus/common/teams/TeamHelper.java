package earth.terrarium.cadmus.common.teams;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamHelper {

    private static final ChatFormatting[] COLORS = new ChatFormatting[]{
        ChatFormatting.DARK_GREEN,
        ChatFormatting.GREEN,
        ChatFormatting.AQUA,
        ChatFormatting.DARK_AQUA,
        ChatFormatting.DARK_BLUE,
        ChatFormatting.BLUE,
        ChatFormatting.LIGHT_PURPLE,
        ChatFormatting.DARK_PURPLE,
    };

    public static Set<GameProfile> getTeamMembers(String id, MinecraftServer server) {
        if (ModUtils.isAdmin(id) || ModUtils.isPlayer(id)) return new HashSet<>();
        return TeamProviderApi.API.getSelected().getTeamMembers(teamId(id), server);
    }

    public static @NotNull Component getTeamName(String id, MinecraftServer server) {
        Component name;
        if (ModUtils.isAdmin(id)) {
            name = AdminClaimHandler.getFlag(server, teamId(id), ModFlags.DISPLAY_NAME);
        } else if (ModUtils.isPlayer(id)) {
            var profile = ModUtils.getProfileCache(server).get(UUID.fromString(teamId(id)));
            name = profile.map(p -> Component.literal(p.getName())).orElse(Component.literal(id));
        } else {
            name = TeamProviderApi.API.getSelected().getTeamName(teamId(id), server);
        }
        return name == null ? Component.literal(id) : name;
    }

    public static String getTeamId(MinecraftServer server, UUID player) {
        String id = TeamProviderApi.API.getSelected().getTeamId(server, player);
        return id == null ? ClaimHandler.PLAYER_PREFIX + player.toString() : id;
    }

    public static boolean isMember(String id, MinecraftServer server, UUID player) {
        if (ModUtils.isAdmin(id)) return true;
        if (ModUtils.isPlayer(id)) return teamId(id).equals(player.toString());
        return TeamProviderApi.API.getSelected().isMember(teamId(id), server, player);
    }

    public static ChatFormatting getTeamColor(String id, MinecraftServer server) {
        if (ModUtils.isAdmin(id)) return ChatFormatting.LIGHT_PURPLE;
        if (ModUtils.isPlayer(id)) {
            int uniqueishId = id.isEmpty() ? 0 : id.charAt(id.length() - 1);
            return COLORS[Math.abs(uniqueishId) % COLORS.length];
        }
        return TeamProviderApi.API.getSelected().getTeamColor(teamId(id), server);
    }

    public static String teamId(String id) {
        return id.substring(2);
    }
}
