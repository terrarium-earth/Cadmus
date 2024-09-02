package earth.terrarium.cadmus.client.claimmap;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import com.teamresourceful.resourcefullib.client.components.CursorWidget;
import com.teamresourceful.resourcefullib.client.screens.CursorScreen;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class MapWidget extends AbstractWidget implements CursorWidget, AutoCloseable {

    private static final ResourceLocation TEXTURE = Cadmus.id("claimmaptextures");

    private final int scale;
    private final DynamicTexture texture;

    public MapWidget(int x, int y, int width, int height, int scale) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.scale = scale;
        this.texture = new DynamicTexture(scale, scale, false);

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.register(TEXTURE, this.texture);
    }

    public void updateTexture(int[][] colors) {
        NativeImage pixels = texture.getPixels();
        if (pixels == null) return;
        for (int i = 0; i < scale; ++i) {
            for (int j = 0; j < scale; ++j) {
                pixels.setPixelRGBA(i, j, colors[i][j]);
            }
        }

        this.texture.upload();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFF000000);
        graphics.drawCenteredString(Minecraft.getInstance().font, ConstantComponents.LOADING, getX() + getWidth() / 2, getY() + getHeight() / 2 - 4, 0xFFFFFF);

        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        try (var pose = new CloseablePoseStack(graphics)) {
            pose.translate(this.getX(), this.getY(), 1);

            int scale = ClaimMapScreen.MAP_SIZE;
            builder.addVertex( 0, scale, 0).setUv(0, 1);
            builder.addVertex( scale, scale, 0).setUv(1, 1);
            builder.addVertex( scale, 0, 0).setUv(1, 0);
            builder.addVertex( 0, 0, 0).setUv(0, 0);

            var toDraw = builder.build();
            if (toDraw != null) {
                BufferUploader.drawWithShader(toDraw);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public CursorScreen.Cursor getCursor() {
        return CursorScreen.Cursor.CROSSHAIR;
    }

    @Override
    public void close() {
        this.texture.close();
    }
}
