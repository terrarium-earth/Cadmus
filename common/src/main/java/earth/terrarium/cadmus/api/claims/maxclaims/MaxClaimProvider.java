package earth.terrarium.cadmus.api.claims.maxclaims;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface MaxClaimProvider {
    void calculate(String id, MinecraftServer server);

    void removeTeam(String id, MinecraftServer server);

    int getMaxClaims(String id, MinecraftServer server, Player player);

    default int getMaxClaims(String id, ServerLevel level, UUID player) {
        return this.getMaxClaims(id, level.getServer(), level.getServer().getPlayerList().getPlayer(player));
    }

    int getMaxChunkLoaded(String id, MinecraftServer server, Player player);

    default int getMaxChunkLoaded(String id, ServerLevel level, UUID player) {
        return this.getMaxChunkLoaded(id, level.getServer(), level.getServer().getPlayerList().getPlayer(player));
    }
}
