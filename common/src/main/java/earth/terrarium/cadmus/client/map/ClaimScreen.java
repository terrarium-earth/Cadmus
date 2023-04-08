package earth.terrarium.cadmus.client.map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamresourceful.resourcefullib.client.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ClaimScreen extends Screen {
    private static final ClaimMapRenderer MAP_RENDERER = new ClaimMapRenderer();

    public ClaimScreen() {
        super(Component.empty());
        update();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(poseStack);
        MAP_RENDERER.render(poseStack);

        if (Minecraft.getInstance().player == null) return;
        float left = (this.width) / 2f;
        float top = (this.height) / 2f;
        double x = (Minecraft.getInstance().player.getX() % 16) + 8;
        double y = (Minecraft.getInstance().player.getZ() % 16) + 8;
        float scale = Mth.clamp(Minecraft.getInstance().options.renderDistance().get(), 4, 24) * 4;
        scale -= scale % 16;
        scale *= 2;
        scale += 16;
        scale = 200 / scale;
        x *= scale;
        y *= scale;
        RenderUtils.bindTexture(Minecraft.getInstance().player.getSkinTextureLocation());
        poseStack.pushPose();
        poseStack.translate(left + x - 4, top + y - 4, 0);
        blit(poseStack, 0, 0, 8, 8, 8, 8, 64, 64);
        poseStack.popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void update() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ClaimMapUpdater.update(false, this, player, (ClientLevel) player.level);
    }

    public void update(ClaimMapData mapData, int scale) {
        MAP_RENDERER.update(mapData, scale);
    }
}
