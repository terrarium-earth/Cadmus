package earth.terrarium.cadmus.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClaimsScreen extends Screen {
    public ClaimsScreen() {
        super(Component.literal("TEST"));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.renderBackground(poseStack);
        Minecraft.getInstance().font.draw(poseStack, "TEST", 10, 10, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
