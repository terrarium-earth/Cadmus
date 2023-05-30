package earth.terrarium.cadmus.common.teams;

import com.mojang.authlib.GameProfile;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class TeamHelper {
    public static Set<GameProfile> getTeamMembers(String id, MinecraftServer server) {
        return TeamProviderApi.API.getSelected().getTeamMembers(getTrueTeamId(id), server);
    }

    @Nullable
    public static Component getTeamName(String id, MinecraftServer server) {
        if (id.startsWith(ClaimHandler.ADMIN_PREFIX)) {
            return AdminClaimHandler.getFlag(server, getTrueTeamId(id), ModFlags.DISPLAY_NAME);
        }
        return TeamProviderApi.API.getSelected().getTeamName(getTrueTeamId(id), server);
    }

    public static String getTeamId(MinecraftServer server, UUID player) {
        return TeamProviderApi.API.getSelected().getTeamId(server, player);
    }

    public static boolean isMember(String id, MinecraftServer server, UUID player) {
        if (id.startsWith(ClaimHandler.ADMIN_PREFIX)) return true;
        if (id.startsWith(ClaimHandler.PLAYER_PREFIX)) return getTrueTeamId(id).equals(player.toString());
        return TeamProviderApi.API.getSelected().isMember(getTrueTeamId(id), server, player);
    }

    public static ChatFormatting getTeamColor(String id, MinecraftServer server) {
        return TeamProviderApi.API.getSelected().getTeamColor(getTrueTeamId(id), server);
    }

    private static String getTrueTeamId(String id) {
        return id.split(":")[1];
    }
}
