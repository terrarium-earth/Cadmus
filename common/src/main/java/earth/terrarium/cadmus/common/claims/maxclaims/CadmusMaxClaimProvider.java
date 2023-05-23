package earth.terrarium.cadmus.common.claims.maxclaims;

import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProvider;
import earth.terrarium.cadmus.common.util.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public class CadmusMaxClaimProvider implements MaxClaimProvider {

    @Override
    public void calculate(String id, MinecraftServer server) {
    }

    @Override
    public void removeTeam(String id, MinecraftServer server) {
    }

    // The Cadmus one just uses the game rule for the max claims
    @Override
    public int getMaxClaims(String id, MinecraftServer server, Player player) {
        return ModGameRules.getOrCreateIntGameRule(player.level, ModGameRules.RULE_MAX_CLAIMED_CHUNKS);
    }

    @Override
    public int getMaxChunkLoaded(String id, MinecraftServer server, Player player) {
        return ModGameRules.getOrCreateIntGameRule(player.level, ModGameRules.RULE_MAX_CHUNK_LOADED);
    }
}
