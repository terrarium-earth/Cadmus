package earth.terrarium.cadmus.common.util;

import earth.terrarium.cadmus.common.claims.ClaimChunkSaveData;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.LastMessageHolder;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.team.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;
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

    public static UUID getTeamName(Player player) {
//        return player.getTeam() == null ? player.getUUID() : player.getTeam().
        return player.getUUID();
    }

    public static void sendSyncPacket(ServerPlayer player) {
//        var teams = ClaimChunkSaveData.getTeams(player.level);
//        if (teams.isEmpty()) return;
//        NetworkHandler.CHANNEL.sendToPlayer(new SyncClaimedChunksPacket(teams), player);
    }

    public static void displayTeamName(ServerPlayer player) {
        if (!(player instanceof LastMessageHolder holder)) return;

        var team = Optionull.mapOrDefault(ClaimChunkSaveData.get(player), ClaimInfo::team, new Team(null, null, null, ""));
        String name = team.name();
        String lastMessage = holder.cadmus$getLastMessage();

        if (Objects.equals(team.name(), lastMessage)) return;
        holder.cadmus$setLastMessage(team.name());
        var playerTeam = getTeamName(player);
        if (team.creator() == null) {
            player.displayClientMessage(ConstantComponents.WILDERNESS, true);
        } else {
            player.displayClientMessage(Component.nullToEmpty(name).copy().withStyle(playerTeam.equals(team.teamId()) ? ChatFormatting.AQUA : ChatFormatting.DARK_RED), true);
        }
    }
}
