package earth.terrarium.cadmus.client.compat.prometheus;

import earth.terrarium.cadmus.common.compat.prometheus.CadmusAutoCompletes;
import earth.terrarium.cadmus.common.compat.prometheus.CadmusOptions;
import earth.terrarium.prometheus.api.permissions.PermissionApi;
import earth.terrarium.prometheus.api.roles.client.PageApi;

public class PrometheusClientCompat {

    public static void init() {
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.BLOCK_BREAKING);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.BLOCK_PLACING);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.BLOCK_EXPLOSIONS);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.BLOCK_INTERACTIONS);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.ENTITY_INTERACTIONS);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.ENTITY_DAMAGE);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.PERSONAL_BLOCK_BREAKING);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.PERSONAL_BLOCK_PLACING);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.PERSONAL_BLOCK_EXPLOSIONS);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.PERSONAL_BLOCK_INTERACTIONS);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.PERSONAL_ENTITY_INTERACTIONS);
        PermissionApi.API.addAutoComplete(CadmusAutoCompletes.PERSONAL_ENTITY_DAMAGE);

        PageApi.API.register(CadmusOptions.SERIALIZER.id(), CadmusOptionsPage::new);
    }
}
