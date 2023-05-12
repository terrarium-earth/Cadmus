package earth.terrarium.cadmus.common.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.LastMessageHolder;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Objects;

public class ModUtils {
    public static void displayTeamName(ServerPlayer player) {
        if (!(player instanceof LastMessageHolder holder)) return;

        var claimData = ClaimHandler.getClaim(player.getLevel(), player.chunkPosition());
        Component displayName = null;
        if (claimData != null) {
            displayName = TeamProviderApi.API.getSelected().getTeamName(claimData.getFirst(), player.server);
        }

        Component lastMessage = holder.cadmus$getLastMessage();
        if (Objects.equals(displayName, lastMessage)) return;
        holder.cadmus$setLastMessage(displayName);

        if (displayName == null) {
            player.displayClientMessage(ConstantComponents.WILDERNESS, true);
        } else {
            boolean isMember = TeamProviderApi.API.getSelected().isMember(claimData.getFirst(), player.server, player.getUUID());
            ChatFormatting teamColor = TeamProviderApi.API.getSelected().getTeamColor(claimData.getFirst(), player.getServer());

            ChatFormatting color = isMember ? teamColor : ChatFormatting.DARK_RED;
            player.displayClientMessage(displayName.copy().withStyle(color), true);
        }
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new NotImplementedException();
    }
}
