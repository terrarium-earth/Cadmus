package earth.terrarium.cadmus.client;

import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.serverbound.BulkClaimSettingsPacket;
import earth.terrarium.cadmus.common.network.packets.serverbound.ClaimColorPacket;
import earth.terrarium.olympus.client.components.Widgets;
import earth.terrarium.olympus.client.components.base.BaseParentWidget;
import earth.terrarium.olympus.client.components.base.ListWidget;
import earth.terrarium.olympus.client.components.compound.radio.RadioState;
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers;
import earth.terrarium.olympus.client.constants.MinecraftColors;
import earth.terrarium.olympus.client.ui.OverlayAlignment;
import earth.terrarium.olympus.client.ui.UIConstants;
import earth.terrarium.olympus.client.ui.modals.BaseModal;
import earth.terrarium.olympus.client.utils.State;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class ClaimConfigModal extends BaseModal {
    private final Map<String, RadioState<TriState>> settings = new HashMap<>();
    private final State<Color> color;
    private final State<TeamId> selectedTeam;
    private final boolean canModifyColor;

    protected ClaimConfigModal(ClaimMapScreen background) {
        super(ConstantComponents.SETTINGS, background);

        background.getSettings().forEach((setting, value) -> settings.put(setting, RadioState.of(value, switch (value) {
            case TRUE -> 0;
            case UNDEFINED -> 1;
            case FALSE -> 2;
        })));

        this.color = State.of(background.teamColor);
        this.selectedTeam = State.of(background.selected);
        this.canModifyColor = background.canModifyColor;
    }

    @Override
    protected void init() {
        super.init();
        ListWidget renderedSettings = new ListWidget(modalContentWidth, modalContentHeight - 22) {{ this.gap = 4; }};
        renderedSettings.setPosition(modalContentLeft, modalContentTop - 4);

        renderedSettings.add(new BaseParentWidget(0, 0) {});

        if (canModifyColor) {
            renderedSettings.add(Widgets.labelled(font, Component.literal("Color"), Widgets.carousel(widget -> {
                widget.withSize(100, 20);
                widget.withContents(colorLayout -> {
                    colorLayout.withChild(Widgets.colorInput(color, textBox -> {
                        textBox.withSize(80, 20);
                    }));
                    colorLayout.withChild(Widgets.colorPicker(color, false, button -> {
                        button.withSize(20);
                    }, colorPickerOverlay -> {
                        colorPickerOverlay.withAlignment(OverlayAlignment.BOTTOM_RIGHT);
                    }));
                });
            })));
        }

        this.settings.forEach((setting, state) -> renderedSettings.add(Widgets.labelled(font, Component.literal(setting), Widgets.tristate(state))));

        FrameLayout footer = new FrameLayout(modalContentWidth, 20 + INNER_PADDING * 2);
        footer.setPosition(modalContentLeft, top + modalHeight - 21 - INNER_PADDING * 2);

        footer.addChild(Widgets.button(button -> {
            button.withSize(100, 20);
            button.withRenderer(WidgetRenderers.text(ConstantComponents.SAVE).withColor(MinecraftColors.WHITE));
            button.withTexture(UIConstants.PRIMARY_BUTTON);
            button.withCallback(() -> {
                var finalSettings = new HashMap<String, TriState>();
                this.settings.forEach((key, value) -> finalSettings.put(key, value.get()));
                var packet = new BulkClaimSettingsPacket(selectedTeam.get(), finalSettings);
                NetworkHandler.CHANNEL.sendToServer(packet);

                if (canModifyColor) {
                    NetworkHandler.CHANNEL.sendToServer(new ClaimColorPacket(selectedTeam.get(), color.get()));
                    ((ClaimMapScreen) this.background).updateColor(color.get());
                }
            });
        }), layoutSettings -> {
            layoutSettings.alignHorizontallyRight();
            layoutSettings.alignVerticallyMiddle();
        });

        this.addRenderableWidget(renderedSettings);
        renderedSettings.visitWidgets(this::addWidget);
        footer.arrangeElements();
        footer.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(UIConstants.MODAL_FOOTER, left + 1, top + modalHeight - 21 - INNER_PADDING * 2, modalWidth - 2, 20 + INNER_PADDING * 2);
    }
}
