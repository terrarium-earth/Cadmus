package earth.terrarium.cadmus.client.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import earth.terrarium.cadmus.Cadmus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ClaimMapRenderer implements AutoCloseable {
    private final TextureManager textureManager;
    @Nullable
    private ClaimMapData data;
    @Nullable
    private DynamicTexture texture;
    @Nullable
    private RenderType renderType;
    private int scale;
    private boolean requiresUpload;

    public ClaimMapRenderer() {
        this.textureManager = Minecraft.getInstance().getTextureManager();
    }

    public void update(ClaimMapData mapData, int scale) {
        if (this.scale != scale) {
            this.scale = scale;

            ResourceLocation id = new ResourceLocation(Cadmus.MOD_ID, "claimmaptextures");
            AbstractTexture texture = textureManager.getTexture(id, MissingTextureAtlasSprite.getTexture());
            if (texture != MissingTextureAtlasSprite.getTexture()) {
                this.textureManager.release(id);
            }
            this.texture = new DynamicTexture(scale, scale, true);
            textureManager.register(id, this.texture);
            this.renderType = RenderType.text(id);
        }

        this.data = mapData;
        this.requiresUpload = true;
    }

    public void render(PoseStack poseStack) {
        if (requiresUpload) {
            updateTexture();
            requiresUpload = false;
        }

        if (renderType == null) return;
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.renderType);
        int imageScale = 200;
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        try (var ignored = new CloseablePoseStack(poseStack)) {
            poseStack.translate(screenWidth / 2.0f - imageScale / 2f, screenHeight / 2.0f - imageScale / 2f, 0.0);

            Matrix4f matrix4f = poseStack.last().pose();
            vertexConsumer.vertex(matrix4f, 0.0F, imageScale, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
            vertexConsumer.vertex(matrix4f, imageScale, imageScale, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
            vertexConsumer.vertex(matrix4f, imageScale, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
            vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        }
        bufferSource.endBatch();
    }

    private void updateTexture() {
        if (texture == null || data == null) return;
        for (int i = 0; i < scale; ++i) {
            for (int j = 0; j < scale; ++j) {
                NativeImage nativeImage = this.texture.getPixels();
                if (nativeImage == null || i > this.data.colors.length || j > this.data.colors[i].length) {
                    this.texture.upload();
                    return;
                }
                nativeImage.setPixelRGBA(i, j, this.data.colors[i][j]);
            }
        }

        this.texture.upload();
    }

    @Override
    public void close() {
        if (texture != null) texture.close();
    }
}
