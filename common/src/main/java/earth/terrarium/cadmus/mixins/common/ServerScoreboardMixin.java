package earth.terrarium.cadmus.mixins.common;

import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.teams.VanillaTeamProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "addPlayerToTeam", at = @At("RETURN"))
    private void cadmus$addPlayerToTeam(String playerName, PlayerTeam playerTeam, CallbackInfoReturnable<Boolean> cir) {
        if (TeamApi.API.getSelected() instanceof VanillaTeamProvider team) {
            team.transferClaims(server, playerTeam, playerName);
            UUID id = team.gerOrCreateId(playerTeam);
            team.onPlayerAdded(server, id, server.getPlayerList().getPlayerByName(playerName));
        }
    }

    @Inject(
        method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
        at = @At("RETURN")
    )
    private void cadmus$removePlayerFromTeam(String username, PlayerTeam playerTeam, CallbackInfo ci) {
        if (TeamApi.API.getSelected() instanceof VanillaTeamProvider team) {
            UUID id = team.gerOrCreateId(playerTeam);
            team.onPlayerRemoved(server, id, server.getPlayerList().getPlayerByName(username));
        }
    }

    @Inject(method = "onTeamAdded", at = @At("HEAD"))
    private void cadmus$onTeamAdded(PlayerTeam playerTeam, CallbackInfo ci) {
        if (TeamApi.API.getSelected() instanceof VanillaTeamProvider team) {
            UUID id = team.gerOrCreateId(playerTeam);
            team.onCreate(server, id);
        }
    }

    @Inject(method = "onTeamRemoved", at = @At("HEAD"))
    private void cadmus$onTeamRemoved(PlayerTeam playerTeam, CallbackInfo ci) {
        if (TeamApi.API.getSelected() instanceof VanillaTeamProvider team) {
            UUID id = team.remove(playerTeam);
            team.onRemove(server, id);
        }
    }

    @Inject(method = "onTeamChanged", at = @At("HEAD"))
    private void cadmus$onTeamChanged(PlayerTeam playerTeam, CallbackInfo ci) {
        if (TeamApi.API.getSelected() instanceof VanillaTeamProvider team) {
            UUID id = team.gerOrCreateId(playerTeam);
            team.onChange(server, id);
        }
    }
}
