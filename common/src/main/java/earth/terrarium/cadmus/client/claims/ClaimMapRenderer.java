package earth.terrarium.cadmus.client.claims;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import earth.terrarium.cadmus.Cadmus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ClaimMapRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Cadmus.MOD_ID, "claimmaptextures");

    public void update(ClaimMapData data) {
        ClaimMapScreen.calculatingMap = false;
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        var dynamicTexture = new DynamicTexture(data.scale(), data.scale(), true);
        textureManager.register(TEXTURE, dynamicTexture);
        updateTexture(dynamicTexture, data.colors(), data.scale());
    }

    public void render(PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        try (var ignored = new CloseablePoseStack(poseStack)) {
            // render map at the center of the screen
            poseStack.translate(screenWidth / 2.0f - ClaimMapScreen.MAP_SIZE / 2.0f, screenHeight / 2.0f - ClaimMapScreen.MAP_SIZE / 2.0f, 1.0);

            Matrix4f matrix4f = poseStack.last().pose();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(matrix4f, 0.0F, ClaimMapScreen.MAP_SIZE, -0.01F).uv(0.0F, 1.0F).endVertex();
            builder.vertex(matrix4f, ClaimMapScreen.MAP_SIZE, ClaimMapScreen.MAP_SIZE, -0.01F).uv(1.0F, 1.0F).endVertex();
            builder.vertex(matrix4f, ClaimMapScreen.MAP_SIZE, 0.0F, -0.01F).uv(1.0F, 0.0F).endVertex();
            builder.vertex(matrix4f, 0.0F, 0.0F, -0.01F).uv(0.0F, 0.0F).endVertex();
            BufferUploader.drawWithShader(builder.end());
        }
    }

    private static void updateTexture(DynamicTexture texture, int[][] colors, int scale) {
        NativeImage nativeImage = texture.getPixels();
        if (nativeImage == null) return;
        for (int i = 0; i < scale; ++i) {
            for (int j = 0; j < scale; ++j) {
                nativeImage.setPixelRGBA(i, j, colors[i][j]);
            }
        }

        texture.upload();
    }
}
