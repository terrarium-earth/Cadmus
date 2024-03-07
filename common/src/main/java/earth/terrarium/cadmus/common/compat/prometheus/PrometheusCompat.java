package earth.terrarium.cadmus.common.compat.prometheus;

import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.prometheus.Prometheus;
import earth.terrarium.prometheus.api.permissions.PermissionApi;
import earth.terrarium.prometheus.api.roles.RoleApi;
import earth.terrarium.prometheus.api.roles.options.RoleOptionsApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class PrometheusCompat {
    public static final ResourceLocation PROMETHEUS_ID = new ResourceLocation(Prometheus.MOD_ID, Prometheus.MOD_ID);

    public static void init() {
        RoleOptionsApi.API.register(CadmusOptions.SERIALIZER);
        MaxClaimProviderApi.API.register(PROMETHEUS_ID, new PrometheusMaxClaimProvider());
        MaxClaimProviderApi.API.setSelected(PROMETHEUS_ID);
        PermissionApi.API.addDefaultPermission(CadmusAutoCompletes.PERSONAL_BLOCK_BREAKING, TriState.TRUE);
        PermissionApi.API.addDefaultPermission(CadmusAutoCompletes.PERSONAL_BLOCK_PLACING, TriState.TRUE);
        PermissionApi.API.addDefaultPermission(CadmusAutoCompletes.PERSONAL_BLOCK_EXPLOSIONS, TriState.TRUE);
        PermissionApi.API.addDefaultPermission(CadmusAutoCompletes.PERSONAL_BLOCK_INTERACTIONS, TriState.TRUE);
        PermissionApi.API.addDefaultPermission(CadmusAutoCompletes.PERSONAL_ENTITY_INTERACTIONS, TriState.TRUE);
        PermissionApi.API.addDefaultPermission(CadmusAutoCompletes.PERSONAL_ENTITY_DAMAGE, TriState.TRUE);
    }

    public static boolean hasPermission(Player player, String permission) {
        return PermissionApi.API.getPermission(player, permission).map(false);
    }

    public static int getMaxClaims(Player player) {
        return RoleApi.API.getNonNullOption(player, CadmusOptions.SERIALIZER).maxClaims();
    }

    public static int getMaxClaims(Level level, UUID player) {
        return RoleApi.API.forceGetNonNullOption(level, player, CadmusOptions.SERIALIZER).maxClaims();
    }

    public static int getMaxChunkLoaded(Player player) {
        return RoleApi.API.getNonNullOption(player, CadmusOptions.SERIALIZER).maxChunkLoaded();
    }

    public static int getMaxChunkLoaded(Level level, UUID player) {
        return RoleApi.API.forceGetNonNullOption(level, player, CadmusOptions.SERIALIZER).maxChunkLoaded();
    }
}
