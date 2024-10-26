package earth.terrarium.cadmus.client.compat.prometheus;

import earth.terrarium.cadmus.common.compat.prometheus.CadmusOptions;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.olympus.client.components.Widgets;
import earth.terrarium.olympus.client.utils.State;
import earth.terrarium.prometheus.api.roles.client.Page;
import earth.terrarium.prometheus.client.utils.UiUtils;
import earth.terrarium.prometheus.common.handlers.role.Role;
import earth.terrarium.prometheus.common.menus.content.RoleEditContent;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;

public class CadmusOptionsPage implements Page {

    private final RoleEditContent content;

    private final State<Integer> maxClaimsBox;
    private final State<Integer> maxChunkLoaded;

    public CadmusOptionsPage(RoleEditContent content, Runnable ignored) {
        this.content = content;

        Role role = content.selected();
        CadmusOptions options = role.getNonNullOption(CadmusOptions.SERIALIZER);

        maxClaimsBox = State.of(options.maxClaims());
        maxChunkLoaded = State.of(options.maxChunkLoaded());
    }

    @Override
    public Layout getContents(int width, int height) {
        GridLayout layout = new GridLayout().rowSpacing(5);

        Role role = content.selected();
        CadmusOptions options = role.getNonNullOption(CadmusOptions.SERIALIZER);

        UiUtils.addLine(
            layout, 0, width,
            ConstantComponents.MAX_CLAIMS,
            (w) -> Widgets.intInput(maxClaimsBox, textBox -> textBox.withSize(w, 20))
        );

        UiUtils.addLine(
            layout, 1, width,
            ConstantComponents.MAX_CHUNK_LOADED_CLAIMS,
            (w) -> Widgets.intInput(maxChunkLoaded, textBox -> textBox.withSize(w, 20))
        );

        return layout;
    }

    @Override
    public void save(Role role) {
        CadmusOptions options = role.getNonNullOption(CadmusOptions.SERIALIZER);
        CadmusOptions newOptions = new CadmusOptions(
            maxClaimsBox.get(),
            maxChunkLoaded.get()
        );
        if (!newOptions.equals(options)) {
            role.setData(newOptions);
        }
    }
}
