package earth.terrarium.cadmus.mixin.common;

import earth.terrarium.cadmus.api.teams.TeamProviderApi;
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

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "addPlayerToTeam", at = @At("HEAD"))
    private void cadmus$addPlayerToTeam(String playerName, PlayerTeam team, CallbackInfoReturnable<Boolean> ci) {
        if (TeamProviderApi.API.getSelected() instanceof VanillaTeamProvider provider) {
            provider.addPlayerToTeam(server, playerName, team);
        }
    }

    @Inject(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V", at = @At("HEAD"))
    private void cadmus$removePlayerFromTeam(String username, PlayerTeam playerTeam, CallbackInfo ci) {
        if (TeamProviderApi.API.getSelected() instanceof VanillaTeamProvider provider) {
            provider.removePlayerFromTeam(server, username, playerTeam);
        }
    }
}
