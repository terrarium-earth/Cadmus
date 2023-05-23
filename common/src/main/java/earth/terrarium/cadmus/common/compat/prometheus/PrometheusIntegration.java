package earth.terrarium.cadmus.common.compat.prometheus;

import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.common.compat.prometheus.roles.CadmusOptions;
import earth.terrarium.cadmus.common.compat.prometheus.roles.client.CadmusOptionsDisplay;
import earth.terrarium.prometheus.api.permissions.PermissionApi;
import earth.terrarium.prometheus.api.roles.RoleApi;
import earth.terrarium.prometheus.api.roles.client.OptionDisplayApi;
import earth.terrarium.prometheus.api.roles.options.RoleOptionsApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class PrometheusIntegration {
    public static final ResourceLocation PROMETHEUS_ID = new ResourceLocation("prometheus", "prometheus");

    public static void registerClient() {
        var api = PermissionApi.API;
        api.addAutoComplete("cadmus.block_breaking");
        api.addAutoComplete("cadmus.block_placing");
        api.addAutoComplete("cadmus.block_explosions");
        api.addAutoComplete("cadmus.block_interactions");
        api.addAutoComplete("cadmus.entity_interactions");
        api.addAutoComplete("cadmus.entity_damage");

        OptionDisplayApi.API.register(CadmusOptions.SERIALIZER.id(), CadmusOptionsDisplay::create);
    }

    public static void register() {
        RoleOptionsApi.API.register(CadmusOptions.SERIALIZER);
        MaxClaimProviderApi.API.register(PROMETHEUS_ID, new PrometheusMaxClaimProvider());
        MaxClaimProviderApi.API.setSelected(PROMETHEUS_ID);
    }

    public static boolean hasPermission(Player player, String permission) {
        return PermissionApi.API.getPermission(player, permission).map(false);
    }

    public static int getMaxClaims(Player player) {
        return RoleApi.API.getNonNullOption(player, CadmusOptions.SERIALIZER).maxClaims();
    }

    public static int getMaxChunkLoaded(Player player) {
        return RoleApi.API.getNonNullOption(player, CadmusOptions.SERIALIZER).maxChunkLoaded();
    }
}
