package earth.terrarium.cadmus.client.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import com.teamresourceful.resourcefullib.client.utils.RenderUtils;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.claiming.ClaimTool;
import earth.terrarium.cadmus.common.claiming.ClaimType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ClaimMapScreen extends Screen {
    public static final int MAP_SIZE = 200;
    public static final int MAX_MAP_SIZE = 32;
    public static final int MAX_CLAIMED_CHUNKS = 1089;
    public static final int MAX_CHUNK_LOADED_CHUNKS = 64;

    public static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(Cadmus.MOD_ID, "textures/gui/map.png");
    public static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
    private static final ClaimMapRenderer MAP_RENDERER = new ClaimMapRenderer();

    // Temporary until server stuff is implemented
    public static final Object2ObjectOpenHashMap<Pair<ResourceKey<Level>, ChunkPos>, ClaimType> CLAIMED_CHUNKS = new Object2ObjectOpenHashMap<>();
    public static boolean calculatingMap;

    private ClaimTool tool = ClaimTool.NONE;

    public ClaimMapScreen() {
        super(Component.empty());
        update();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(poseStack);
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        fill(poseStack, (width - 200) / 2, (height - 200) / 2, (width + 200) / 2, (height + 200) / 2, 0xff000000);
        if (!calculatingMap) {
            MAP_RENDERER.render(poseStack);
            renderPlayerAvatar(player, poseStack);
        }
        renderBackgroundTexture(poseStack);
        renderChunkButtons(player, poseStack, mouseX, mouseY);

        renderText(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        if (calculatingMap) {
            GuiComponent.drawCenteredString(poseStack, font, Component.translatable("gui.cadmus.claim_map.loading"), (int) (width / 2f), (int) (height / 2f), 0xFFFFFF);
        }
    }

    @Override
    protected void init() {
        super.init();
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        this.addRenderableWidget(new ImageButton((int) (this.width / 1.5) + 3, 21, 12, 12, 218, 0, 12,
                CONTAINER_BACKGROUND,
                button -> clearDimension(level.dimension())
        )).setTooltip(Tooltip.create(Component.translatable("tooltip.cadmus.claim_map.clear_dimension")));

        this.addRenderableWidget(new ImageButton((int) (this.width / 1.5) + 18, 21, 12, 12, 230, 0, 12,
                CONTAINER_BACKGROUND,
                button -> clearAll()
        )).setTooltip(Tooltip.create(Component.translatable("tooltip.cadmus.claim_map.clear_all")));
    }

    private void renderBackgroundTexture(PoseStack poseStack) {
        int left = (this.width - 218) / 2;
        int top = (this.height - 227) / 2 - 4;
        RenderSystem.enableBlend();
        RenderUtils.bindTexture(CONTAINER_BACKGROUND);
        blit(poseStack, left, top, 0, 0, 218, 248);
    }

    private void renderPlayerAvatar(LocalPlayer player, PoseStack poseStack) {
        float left = (this.width) / 2f;
        float top = (this.height) / 2f;
        double x = (player.getX() % 16) - 8;
        double y = (player.getZ() % 16) - 8;
        float scale = ClaimMapUpdater.getScaledRenderDistance();
        scale = ClaimMapUpdater.getChunkScale((int) scale);
        scale = MAP_SIZE / scale;
        x *= scale;
        y *= scale;
        RenderUtils.bindTexture(MAP_ICONS_LOCATION);
        try (var ignored = new CloseablePoseStack(poseStack)) {
            poseStack.translate(left + x, top + y, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(player.getYRot()));
            poseStack.translate(-4, -4, 0);
            blit(poseStack, 0, 0, 40, 0, 8, 8, 128, 128);
        }
    }

    public void renderChunkButtons(LocalPlayer player, PoseStack poseStack, int mouseX, int mouseY) {
        float left = (this.width - MAP_SIZE) / 2f;
        float top = (this.height - MAP_SIZE) / 2f;
        float scale = ClaimMapUpdater.getChunkScale((ClaimMapUpdater.getScaledRenderDistance()));
        float chunkScale = scale / 16f;
        float pixelScale = MAP_SIZE / scale;
        ChunkPos playerChunk = player.chunkPosition();
        var dimension = player.level.dimension();

        for (int i = 0; i < chunkScale; i++) {
            for (int j = 0; j < chunkScale; j++) {
                float x = left + (i * 16 * pixelScale);
                float y = top + (j * 16 * pixelScale);

                float width = 16 * pixelScale;
                float height = 16 * pixelScale;

                int playerChunkX = Math.round(playerChunk.x - chunkScale / 2);
                int playerChunkZ = Math.round(playerChunk.z - chunkScale / 2);

                ChunkPos chunkPos = new ChunkPos(playerChunkX + i, playerChunkZ + j);
                var claim = CLAIMED_CHUNKS.getOrDefault(Pair.of(dimension, new ChunkPos(chunkPos.x, chunkPos.z)), null);

                boolean shouldRender = claim != null;
                boolean isHovering = false;

                int color = claim == ClaimType.CLAIMED ? 0xff00ff00 : 0xfff59a22;
                if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                    if (this.tool != ClaimTool.NONE) {
                        ClaimType claimType = switch (this.tool) {
                            case BRUSH -> ClaimType.CLAIMED;
                            case CHUNK_LOAD_BRUSH -> ClaimType.CHUNK_LOADED;
                            case CHUNK_LOAD_ERASER -> claim != null ? ClaimType.CLAIMED : null;
                            default -> null;
                        };

                        if (claimType != null) {
                            if (this.tool == ClaimTool.CHUNK_LOAD_ERASER || (this.tool == ClaimTool.BRUSH && CLAIMED_CHUNKS.size() < MAX_CLAIMED_CHUNKS) || (this.tool == ClaimTool.CHUNK_LOAD_BRUSH && getChunkLoaded() < MAX_CHUNK_LOADED_CHUNKS)) {
                                if (this.tool != ClaimTool.BRUSH || claim != ClaimType.CHUNK_LOADED) {
                                    CLAIMED_CHUNKS.put(Pair.of(dimension, chunkPos), claimType);
                                }
                            }
                        } else if (this.tool != ClaimTool.ERASER || claim != ClaimType.CHUNK_LOADED) {
                            CLAIMED_CHUNKS.remove(Pair.of(dimension, chunkPos));
                        }
                    }
                    isHovering = true;
                    color = Screen.hasShiftDown() ? 0xfff59a22 : tool == ClaimTool.ERASER ? 0xffff0000 : 0xff00ff00;
                }

                if (shouldRender || isHovering) {
                    boolean north = j == 0 || CLAIMED_CHUNKS.getOrDefault(Pair.of(dimension, new ChunkPos(playerChunkX + i, playerChunkZ + j - 1)), null) != claim;
                    boolean east = i == chunkScale - 1 || CLAIMED_CHUNKS.getOrDefault(Pair.of(dimension, new ChunkPos(playerChunkX + i + 1, playerChunkZ + j)), null) != claim;
                    boolean south = j == chunkScale - 1 || CLAIMED_CHUNKS.getOrDefault(Pair.of(dimension, new ChunkPos(playerChunkX + i, playerChunkZ + j + 1)), null) != claim;
                    boolean west = i == 0 || CLAIMED_CHUNKS.getOrDefault(Pair.of(dimension, new ChunkPos(playerChunkX + i - 1, playerChunkZ + j)), null) != claim;

                    // Do CTM on the claimed chunks
                    int roundedX = Math.round(x);
                    int roundedY = Math.round(y);
                    int roundedWidth = Math.round(x + width);
                    int roundedHeight = Math.round(y + height);
                    if (north || isHovering) {
                        fill(poseStack, roundedX, roundedY, roundedWidth, roundedY + 1, color);
                    }
                    if (east || isHovering) {
                        fill(poseStack, roundedWidth - 1, roundedY, roundedWidth, roundedHeight, color);
                    }
                    if (south || isHovering) {
                        fill(poseStack, roundedX, roundedHeight - 1, roundedWidth, roundedHeight, color);
                    }
                    if (west || isHovering) {
                        fill(poseStack, roundedX, roundedY, roundedX + 1, roundedHeight, color);
                    }

                    fill(poseStack, roundedX, roundedY, roundedWidth, roundedHeight, color & 0x33ffffff);
                }
            }
        }
    }

    private void renderText(PoseStack poseStack) {
        this.font.draw(poseStack, Component.translatable("gui.cadmus.claim_map.claimed_chunks", CLAIMED_CHUNKS.size(), MAX_CLAIMED_CHUNKS), 5, height - 24, 0xffffff);
        this.font.draw(poseStack, Component.translatable("gui.cadmus.claim_map.force_loaded_chunks", getChunkLoaded(), MAX_CHUNK_LOADED_CHUNKS), 5, height - 12, 0xffffff);
    }

    private int getChunkLoaded() {
        int chunksForceLoaded = 0;
        for (ClaimType value : CLAIMED_CHUNKS.values()) {
            if (value == ClaimType.CHUNK_LOADED) {
                chunksForceLoaded++;
            }
        }
        return chunksForceLoaded;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        setFocused(null);
        if (button == 0) {
            this.tool = Screen.hasShiftDown() ? ClaimTool.CHUNK_LOAD_BRUSH : ClaimTool.BRUSH;
            return true;
        } else if (button == 1) {
            this.tool = Screen.hasShiftDown() ? ClaimTool.CHUNK_LOAD_ERASER : ClaimTool.ERASER;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 || button == 1) {
            this.tool = ClaimTool.NONE;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void clearAll() {
        CLAIMED_CHUNKS.clear();
    }

    public void clearDimension(ResourceKey<Level> dimension) {
        CLAIMED_CHUNKS.entrySet().removeIf(entry -> entry.getKey().getFirst() == dimension);
    }

    public void update() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ClaimMapUpdater.update(false, player, player.clientLevel, MAP_RENDERER::update);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        // TODO send claimed chunks to server
        super.removed();
    }
}
