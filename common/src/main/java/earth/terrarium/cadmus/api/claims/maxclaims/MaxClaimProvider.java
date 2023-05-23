package earth.terrarium.cadmus.api.claims.maxclaims;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public interface MaxClaimProvider {
    void calculate(String id, MinecraftServer server);

    void removeTeam(String id, MinecraftServer server);

    int getMaxClaims(String id, MinecraftServer server, Player player);

    int getMaxChunkLoaded(String id, MinecraftServer server, Player player);
}
