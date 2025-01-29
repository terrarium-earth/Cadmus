package earth.terrarium.cadmus.common.teams;

import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.teams.TeamProvider;
import earth.terrarium.cadmus.common.flags.Flags;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AdminTeamProvider implements TeamProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Cadmus.MOD_ID, "admin");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Optional<Component> getName(Level level, UUID id) {
        return Optional.of(Component.literal(FlagApi.API.<String>getFlag(level.getServer(), id, Flags.DISPLAY_NAME.id()).value()));
    }

    @Override
    public Optional<Color> getColor(Level level, UUID id) {
        return Optional.of(FlagApi.API.<Color>getFlag(level.getServer(), id, Flags.COLOR.id()).value());
    }

    @Override
    public Set<UUID> getMembers(Level level, UUID id) {
        return Set.of();
    }

    @Override
    public boolean isMember(Level level, UUID id, Player player) {
        return false;
    }

    @Override
    public Set<UUID> getTeams(Player player) {
        if (!player.hasPermissions(2)) return Set.of();
        return getAllTeams(player.getServer());
    }

    @Override
    public boolean canModifySettings(Player player, UUID teamId) {
        return player.hasPermissions(2);
    }

    @Override
    public Set<UUID> getAllTeams(MinecraftServer server) {
        return FlagApi.API.getAllAdminTeams(server).keySet();
    }
}
