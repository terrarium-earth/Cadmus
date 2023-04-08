package earth.terrarium.cadmus.client.claim;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ClaimButton extends Button {
    public ClaimButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.literal("TEST"), onPress, Button.DEFAULT_NARRATION);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;
        try (var ignored = new CloseablePoseStack(poseStack)) {
            RenderSystem.setShaderColor(1, 1, 1, 0.2f);
            GuiComponent.fill(poseStack, getX(), getY(), getWidth(), getHeight(), 0xffffffff);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }
}
