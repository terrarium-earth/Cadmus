package earth.terrarium.cadmus.client.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import earth.terrarium.cadmus.Cadmus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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
    private int scale;
    private boolean requiresUpload;
    ResourceLocation CLAIM_MAP_TEXTURE = new ResourceLocation(Cadmus.MOD_ID, "claimmaptextures");

    public ClaimMapRenderer() {
        this.textureManager = Minecraft.getInstance().getTextureManager();
    }

    public void update(ClaimMapData mapData) {
        ClaimMapScreen.calculatingMap = false;
        int scale = mapData.scale;
        if (this.scale != scale) {
            this.scale = scale;

            AbstractTexture texture = textureManager.getTexture(CLAIM_MAP_TEXTURE, MissingTextureAtlasSprite.getTexture());
            if (texture != MissingTextureAtlasSprite.getTexture()) {
                this.textureManager.release(CLAIM_MAP_TEXTURE);
            }
            this.texture = new DynamicTexture(scale, scale, true);
            textureManager.register(CLAIM_MAP_TEXTURE, this.texture);
        }

        this.data = mapData;
        this.requiresUpload = true;
    }

    public void render(PoseStack poseStack) {
        if (requiresUpload) {
            updateTexture();
            requiresUpload = false;
        }

        RenderSystem.setShaderTexture(0, CLAIM_MAP_TEXTURE);
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

    private void updateTexture() {
        if (texture == null || data == null) return;
        NativeImage nativeImage = this.texture.getPixels();
        if (nativeImage == null) return;
        for (int i = 0; i < scale; ++i) {
            for (int j = 0; j < scale; ++j) {
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
