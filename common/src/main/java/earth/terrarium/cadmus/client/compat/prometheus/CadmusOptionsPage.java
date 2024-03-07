package earth.terrarium.cadmus.client.compat.prometheus;

import earth.terrarium.cadmus.common.compat.prometheus.CadmusOptions;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.olympus.client.components.textbox.IntTextBox;
import earth.terrarium.prometheus.api.roles.client.Page;
import earth.terrarium.prometheus.client.utils.UiUtils;
import earth.terrarium.prometheus.common.handlers.role.Role;
import earth.terrarium.prometheus.common.menus.content.RoleEditContent;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;

public class CadmusOptionsPage implements Page {

    private final RoleEditContent content;

    private IntTextBox maxClaimsBox;
    private IntTextBox maxChunkLoaded;

    public CadmusOptionsPage(RoleEditContent content, Runnable ignored) {
        this.content = content;
    }

    @Override
    public Layout getContents(int width, int height) {
        GridLayout layout = new GridLayout().rowSpacing(5);

        Role role = content.selected();
        CadmusOptions options = role.getNonNullOption(CadmusOptions.SERIALIZER);

        maxClaimsBox = UiUtils.addLine(
            layout, 0, width,
            ConstantComponents.CLAIMS_MAX,
            (w) -> new IntTextBox(
                maxClaimsBox,
                w, 20,
                options.maxClaims(), i -> {}
            )
        );

        maxChunkLoaded = UiUtils.addLine(
            layout, 1, width,
            ConstantComponents.CHUNK_LOADED_MAX,
            (w) -> new IntTextBox(
                maxChunkLoaded,
                w, 20,
                options.maxChunkLoaded(), i -> {}
            )
        );

        return layout;
    }

    @Override
    public void save(Role role) {
        CadmusOptions options = role.getNonNullOption(CadmusOptions.SERIALIZER);
        CadmusOptions newOptions = new CadmusOptions(
            maxClaimsBox.getIntValue().orElse(options.maxClaims()),
            maxChunkLoaded.getIntValue().orElse(options.maxChunkLoaded())
        );
        if (!newOptions.equals(options)) {
            role.setData(newOptions);
        }
    }
}
