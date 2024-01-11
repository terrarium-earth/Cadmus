package earth.terrarium.cadmus.common.claims.maxclaims;

import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProvider;
import earth.terrarium.cadmus.common.util.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class CadmusMaxClaimProvider implements MaxClaimProvider {

    @Override
    public void calculate(String id, MinecraftServer server) {
    }

    @Override
    public void removeTeam(String id, MinecraftServer server) {
    }

    @Override
    public int getMaxClaims(String id, MinecraftServer server, Player player) {
        return getMaxClaims(id, (ServerLevel) player.level(), player.getUUID());
    }

    @Override
    public int getMaxClaims(String id, ServerLevel level, UUID player) {
        return ModGameRules.getOrCreateIntGameRule(level, ModGameRules.RULE_MAX_CLAIMED_CHUNKS);
    }

    @Override
    public int getMaxChunkLoaded(String id, MinecraftServer server, Player player) {
        return getMaxChunkLoaded(id, (ServerLevel) player.level(), player.getUUID());
    }

    @Override
    public int getMaxChunkLoaded(String id, ServerLevel level, UUID player) {
        return ModGameRules.getOrCreateIntGameRule(level, ModGameRules.RULE_MAX_CHUNK_LOADED);
    }
}
