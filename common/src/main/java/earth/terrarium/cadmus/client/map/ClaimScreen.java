package earth.terrarium.cadmus.client.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import com.teamresourceful.resourcefullib.client.utils.RenderUtils;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.claim.ClaimButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ClaimScreen extends Screen {
    public static final ResourceLocation MAP_TEXTURE = new ResourceLocation(Cadmus.MOD_ID, "textures/gui/map.png");
    public static final int MAP_SIZE = 200;
    private static final ClaimMapRenderer MAP_RENDERER = new ClaimMapRenderer();

    public ClaimScreen() {
        super(Component.empty());
        update();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        super.renderBackground(poseStack);
        int fillLeft = (this.height - 200) / 2;
        int fillTop = (this.height - 200) / 2;
        fill(poseStack, fillLeft, fillTop, 200, 200, 0x000000);

        MAP_RENDERER.render(poseStack);
        renderPlayerAvatar(poseStack);

        int left = (this.width - 218) / 2;
        int top = (this.height - 227) / 2 - 4;
        RenderSystem.enableBlend();
        RenderUtils.bindTexture(MAP_TEXTURE);
        blit(poseStack, left, top, 0, 0, 218, 227);
    }

    @Override
    protected void init() {
        super.init();
        addButtons();
    }

    private void renderPlayerAvatar(PoseStack poseStack) {
        if (Minecraft.getInstance().player == null) return;
        float left = (this.width) / 2f;
        float top = (this.height) / 2f;
        double x = (Minecraft.getInstance().player.getX() % 16) + 8;
        double y = (Minecraft.getInstance().player.getZ() % 16) + 8;
        float scale = Mth.clamp(Minecraft.getInstance().options.renderDistance().get(), 4, 24) * 4;
        scale -= scale % 16;
        scale = ClaimMapUpdater.getPixelScale((int) scale);
        scale = MAP_SIZE / scale;
        x *= scale;
        y *= scale;
        RenderUtils.bindTexture(Minecraft.getInstance().player.getSkinTextureLocation());
        try (var ignored = new CloseablePoseStack(poseStack)) {
            poseStack.translate(left + x - 4, top + y - 4, 0);
            blit(poseStack, 0, 0, 8, 8, 8, 8, 64, 64);
        }
    }

    private void addButtons() {
        int x = (this.width - MAP_SIZE) / 2;
        int y = (this.height - MAP_SIZE) / 2;
        ClaimButton claimButton = new ClaimButton(x, y, 10, 10, button -> {
        });
        this.addRenderableWidget(claimButton);
    }

    public void update() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ClaimMapUpdater.update(false, this, player, (ClientLevel) player.level);
    }

    public void update(ClaimMapData mapData, int scale) {
        MAP_RENDERER.update(mapData, scale);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
