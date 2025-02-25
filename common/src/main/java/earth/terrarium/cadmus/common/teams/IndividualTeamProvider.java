package earth.terrarium.cadmus.common.teams;

import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.api.teams.TeamProvider;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class IndividualTeamProvider implements TeamProvider {
    public static final ResourceLocation ID = Cadmus.id("individual");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Optional<Component> getName(Level level, UUID id) {
        return Optional.ofNullable(level.getServer())
            .map(MinecraftServer::getProfileCache)
            .flatMap(cache -> cache.get(id))
            .map(gameProfile -> Component.literal(gameProfile.getName()));
    }

    @Override
    public Optional<Color> getColor(Level level, UUID id) {
        return Optional.ofNullable(level.getServer())
            .map(server -> CadmusSaveData.getTeamColor(server, new TeamId(ID, id)));
    }

    @Override
    public Set<UUID> getMembers(Level level, UUID id) {
        return Set.of(id);
    }

    @Override
    public boolean isMember(Level level, UUID id, Player player) {
        return id.equals(player.getUUID());
    }

    @Override
    public Set<UUID> getTeams(Player player) {
        return Set.of(player.getUUID());
    }

    @Override
    public boolean canModifySettings(Player player, UUID teamId) {
        return teamId.equals(player.getUUID());
    }

    @Override
    public Set<UUID> getAllTeams(MinecraftServer server) {
        return CadmusSaveData.getUniquePlayers(server);
    }
}
