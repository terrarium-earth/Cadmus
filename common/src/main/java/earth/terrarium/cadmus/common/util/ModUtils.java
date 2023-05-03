package earth.terrarium.cadmus.common.util;

import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.claims.LastMessageHolder;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.teams.Team;
import earth.terrarium.cadmus.common.teams.TeamSaveData;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
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
        Component displayName = Optionull.mapOrDefault(team, Team::displayName, ConstantComponents.WILDERNESS);

        String lastMessage = holder.cadmus$getLastMessage();
        if (Objects.equals(displayName.getString(), lastMessage)) return;
        holder.cadmus$setLastMessage(displayName.getString());

        if (team == null) {
            player.displayClientMessage(ConstantComponents.WILDERNESS, true);
        } else {
            boolean isMember = TeamProviderApi.API.getSelected().isMember(team.name(), player.server, player.getUUID());
            ChatFormatting color = isMember ? ChatFormatting.AQUA : ChatFormatting.DARK_RED;
            player.displayClientMessage(team.displayName().copy().withStyle(color), true);
        }
    }
}
