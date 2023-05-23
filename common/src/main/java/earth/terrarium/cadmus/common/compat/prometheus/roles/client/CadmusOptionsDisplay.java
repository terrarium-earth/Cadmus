package earth.terrarium.cadmus.common.compat.prometheus.roles.client;

import com.teamresourceful.resourcefullib.client.components.selection.ListEntry;
import com.teamresourceful.resourcefullib.client.components.selection.SelectionList;
import earth.terrarium.cadmus.common.compat.prometheus.roles.CadmusOptions;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.prometheus.api.roles.client.OptionDisplay;
import earth.terrarium.prometheus.client.screens.roles.options.entries.NumberBoxListEntry;
import earth.terrarium.prometheus.client.screens.roles.options.entries.TextListEntry;
import earth.terrarium.prometheus.common.handlers.role.Role;

import java.util.List;

public record CadmusOptionsDisplay(List<ListEntry> entries) implements OptionDisplay {

    public static CadmusOptionsDisplay create(Role role, SelectionList<ListEntry> ignored) {
        CadmusOptions cadmusOptions = role.getNonNullOption(CadmusOptions.SERIALIZER);
        return new CadmusOptionsDisplay(List.of(
            new TextListEntry(ConstantComponents.CADMUS_TITLE),
            new NumberBoxListEntry(cadmusOptions.maxClaims(), false, ConstantComponents.CLAIMS_MAX),
            new NumberBoxListEntry(cadmusOptions.maxChunkLoaded(), false, ConstantComponents.CHUNK_LOADED_MAX)
        ));
    }

    @Override
    public List<ListEntry> getDisplayEntries() {
        return entries;
    }

    @Override
    public boolean save(Role role) {
        NumberBoxListEntry maxClaims = (NumberBoxListEntry) entries.get(1);
        NumberBoxListEntry maxChunkLoaded = (NumberBoxListEntry) entries.get(2);
        if (maxClaims.getIntValue().isPresent() && maxChunkLoaded.getIntValue().isPresent()) {
            role.setData(new CadmusOptions(maxClaims.getIntValue().getAsInt(), maxChunkLoaded.getIntValue().getAsInt()));
            return true;
        }
        return false;
    }
}
