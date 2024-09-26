package earth.terrarium.cadmus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.serverbound.BulkClaimSettingsPacket;
import earth.terrarium.cadmus.common.utils.ModUtils;
import earth.terrarium.olympus.client.components.Widgets;
import earth.terrarium.olympus.client.components.base.BaseParentWidget;
import earth.terrarium.olympus.client.components.base.ListWidget;
import earth.terrarium.olympus.client.components.compound.CompoundWidget;
import earth.terrarium.olympus.client.components.compound.radio.RadioState;
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers;
import earth.terrarium.olympus.client.constants.MinecraftColors;
import earth.terrarium.olympus.client.ui.UIConstants;
import earth.terrarium.olympus.client.ui.modals.BaseModal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClaimConfigModal extends BaseModal {
    public static final Map<Color, Component> COLOR_NAMES = new HashMap<>();

    private final Map<String, RadioState<TriState>> settings = new HashMap<>();

    static {
        Arrays.stream(ModUtils.COLORS).forEach(color -> COLOR_NAMES.put(color, Component.literal(getName(color))));
    }

    protected ClaimConfigModal(ClaimMapScreen background) {
        super(ConstantComponents.SETTINGS, background);

        background.getSettings().forEach((setting, value) -> settings.put(setting, RadioState.of(value, switch (value) {
            case TRUE -> 0;
            case UNDEFINED -> 1;
            case FALSE -> 2;
        })));
    }

    @Override
    protected void init() {
        super.init();
        ListWidget settings = new ListWidget(modalContentWidth, modalContentHeight - 22) {{ this.gap = 4; }};
        settings.setPosition(modalContentLeft, modalContentTop - 4);

        settings.add(new BaseParentWidget(0, 0) {});

        // settings.add(new LabelledEntry(modalWidth, font, Component.translatable("gui.cadmus.claim_map.color"), new Dropdown<>(null, modalWidth / 2, 20, COLOR_NAMES, ConstantColors.aqua)));

        this.settings.forEach((setting, state) -> {
            var entry = new CompoundWidget();
            entry.withContents(layout -> {
                layout.addChild(new StringWidget(Component.literal(" " + setting), font), layoutSettings -> layoutSettings.alignVerticallyMiddle().alignHorizontallyLeft());
                layout.addChild(Widgets.tristate(state), layoutSettings -> layoutSettings.alignVerticallyMiddle().alignHorizontallyRight());
            });
            settings.add(entry);
        });

        FrameLayout footer = new FrameLayout(modalContentWidth, 20 + INNER_PADDING * 2);
        footer.setPosition(modalContentLeft, top + modalHeight - 21 - INNER_PADDING * 2);

        footer.addChild(Widgets.button(button -> {
            button.withSize(100, 20);
            button.withRenderer(WidgetRenderers.text(ConstantComponents.SAVE).withColor(MinecraftColors.WHITE));
            button.withTexture(UIConstants.PRIMARY_BUTTON);
        }), layoutSettings -> {
            layoutSettings.alignHorizontallyRight();
            layoutSettings.alignVerticallyMiddle();
        });

        this.addRenderableWidget(settings);
        settings.visitWidgets(this::addWidget);
        footer.arrangeElements();
        footer.visitWidgets(this::addRenderableWidget);
    }

    public static String getName(Color color) {
        try {
            Field specialName = Color.class.getDeclaredField("specialName");
            specialName.setAccessible(true);
            return StringUtils.capitalize((String) specialName.get(color));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Cadmus.LOGGER.warn("Failed to get special name for color {}. {}", color, e);
            return "Unknown";
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(UIConstants.MODAL_FOOTER, left + 1, top + modalHeight - 21 - INNER_PADDING * 2, modalWidth - 2, 20 + INNER_PADDING * 2);
    }
}
