package earth.terrarium.cadmus.common.util;

import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.claims.LastMessageHolder;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.teams.Team;
import earth.terrarium.cadmus.common.teams.TeamSaveData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModUtils {
    public static <T> T generate(Predicate<T> validator, Supplier<T> getter) {
        T value;
        do {
            value = getter.get();
        } while (!validator.test(value));
        return value;
    }

    public static void displayTeamName(ServerPlayer player) {
        if (!(player instanceof LastMessageHolder holder)) return;

        ClaimInfo info = ClaimSaveData.get(player);
        Team team = TeamSaveData.get(player.server, info == null ? null : info.teamId());
        String name = info == null ? player.getStringUUID() : info.teamId().toString();
        Component displayName = TeamProviderApi.API.getSelected().getTeamName(name, player.server);

        Component lastMessage = holder.cadmus$getLastMessage();
        if (Objects.equals(displayName, lastMessage)) return;
        holder.cadmus$setLastMessage(displayName);

        if (team == null || displayName == null) {
            player.displayClientMessage(ConstantComponents.WILDERNESS, true);
        } else {
            boolean isMember = TeamProviderApi.API.getSelected().isMember(team.name(), player.server, player.getUUID());
            ChatFormatting teamColor = TeamProviderApi.API.getSelected().getTeamColor(team.name(), player.getServer());
            ChatFormatting color = isMember ? teamColor : ChatFormatting.DARK_RED;
            player.displayClientMessage(displayName.copy().withStyle(color), true);
        }
    }
}
