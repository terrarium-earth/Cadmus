package earth.terrarium.cadmus.client.claims;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import com.teamresourceful.resourcefullib.client.utils.RenderUtils;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.server.ClearChunksPacket;
import earth.terrarium.cadmus.common.network.messages.server.RequestClaimedChunksPacket;
import earth.terrarium.cadmus.common.network.messages.server.UpdateClaimedChunksPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ClaimMapScreen extends Screen {
    public static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(Cadmus.MOD_ID, "textures/gui/map.png");
    public static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");

    public static final int MAP_SIZE = 200;
    public static final int MAX_MAP_SIZE = 32;

    public static final ClaimMapRenderer MAP_RENDERER = new ClaimMapRenderer();

    @Nullable
    private static String teamName;
    private static final Map<ChunkPos, ClaimType> START_CHUNKS = new HashMap<>();
    private static final Map<ChunkPos, ClaimType> FRIENDLY_CHUNKS = new HashMap<>();
    private static final Map<ChunkPos, ClaimInfo> UNFRIENDLY_CHUNKS = new HashMap<>();

    public static boolean calculatingMap;
    public static boolean waitingForServerData;
    private static boolean isDirty;
    private static int maxClaimedChunks = 1089;
    private static int maxChunkLoadedChunks = 64;

    private ClaimTool tool = ClaimTool.NONE;

    public ClaimMapScreen() {
        super(Component.empty());
        ClaimMapScreen.waitingForServerData = true;
        NetworkHandler.CHANNEL.sendToServer(new RequestClaimedChunksPacket(Minecraft.getInstance().options.renderDistance().get()));
        START_CHUNKS.clear();
        FRIENDLY_CHUNKS.clear();
        UNFRIENDLY_CHUNKS.clear();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(poseStack);
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        fill(poseStack, (width - 200) / 2, (height - 200) / 2, (width + 200) / 2, (height + 200) / 2, 0xff000000);
        renderBackgroundTexture(poseStack);
        if (!calculatingMap && !waitingForServerData) {
            MAP_RENDERER.render(poseStack);
            try (var ignored = new CloseablePoseStack(poseStack)) {
                poseStack.translate(0, 0, 5);
                renderChunkButtons(player, poseStack, mouseX, mouseY);
                renderPlayerAvatar(player, poseStack);
                renderText(poseStack, mouseX, mouseY);
            }
        } else {
            GuiComponent.drawCenteredString(poseStack, font, ConstantComponents.LOADING, (int) (width / 2f), (int) (height / 2f), 0xFFFFFF);
        }
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new ImageButton(((this.width + 218) / 2) - 36, ((this.height - 248) / 2) + 10, 12, 12, 218, 0, 12,
            CONTAINER_BACKGROUND,
            button -> clearDimension()
        )).setTooltip(Tooltip.create(ConstantComponents.CLEAR_DIMENSION));

        this.addRenderableWidget(new ImageButton(((this.width + 218) / 2) - 20, ((this.height - 248) / 2) + 10, 12, 12, 230, 0, 12,
            CONTAINER_BACKGROUND,
            button -> clearAll()
        )).setTooltip(Tooltip.create(ConstantComponents.CLEAR_ALL));
    }

    private void renderBackgroundTexture(PoseStack poseStack) {
        int left = (this.width - 218) / 2;
        int top = (this.height - 227) / 2 - 4;
        RenderSystem.enableBlend();
        RenderUtils.bindTexture(CONTAINER_BACKGROUND);
        blit(poseStack, left, top, 0, 0, 218, 249);
    }

    private void renderPlayerAvatar(LocalPlayer player, PoseStack poseStack) {
        float left = (this.width) / 2f;
        float top = (this.height) / 2f;

        double playerX = player.getX();
        double playerZ = player.getZ();
        double x = (playerX % 16) + (playerX >= 0 ? -8 : 8);
        double y = (playerZ % 16) + (playerZ >= 0 ? -8 : 8);

        float renderDistance = ClaimMapUpdater.getScaledRenderDistance();
        float chunkScale = ClaimMapUpdater.getChunkScale((int) renderDistance);
        float scale = MAP_SIZE / chunkScale;

        x *= scale;
        y *= scale;
        RenderUtils.bindTexture(MAP_ICONS_LOCATION);
        try (var ignored = new CloseablePoseStack(poseStack)) {
            poseStack.translate(left + x, top + y, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(player.getYRot()));
            poseStack.translate(-8, -8, 0);
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

        for (int i = 0; i < chunkScale; i++) {
            for (int j = 0; j < chunkScale; j++) {
                float x = left + (i * 16 * pixelScale);
                float y = top + (j * 16 * pixelScale);

                float width = 16 * pixelScale;
                float height = 16 * pixelScale;

                int playerChunkX = Math.round(playerChunk.x - chunkScale / 2);
                int playerChunkZ = Math.round(playerChunk.z - chunkScale / 2);

                ChunkPos chunkPos = new ChunkPos(playerChunkX + i, playerChunkZ + j);
                var claim = FRIENDLY_CHUNKS.getOrDefault(new ChunkPos(chunkPos.x, chunkPos.z), null);
                boolean owned = true;
                var info = UNFRIENDLY_CHUNKS.get(new ChunkPos(chunkPos.x, chunkPos.z));
                if (claim == null) {
                    claim = info != null ? info.type() : null;
                    if (claim != null) {
                        owned = false;
                    }
                }

                boolean shouldRender = claim != null;
                int color = owned ? claim == ClaimType.CLAIMED ? 0xff55ffff : 0xfff59a22 : 0xffbd2025;
                boolean isHovering = false;

                if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                    if (this.tool != ClaimTool.NONE && owned) {
                        ClaimType claimType = switch (this.tool) {
                            case BRUSH, CHUNK_LOAD_ERASER -> ClaimType.CLAIMED;
                            case CHUNK_LOAD_BRUSH -> ClaimType.CHUNK_LOADED;
                            default -> null;
                        };

                        if (tool == ClaimTool.CHUNK_LOAD_ERASER && claim == null) {
                            claimType = null;
                        }

                        if (claimType != null) {
                            if (this.tool == ClaimTool.CHUNK_LOAD_ERASER || (this.tool == ClaimTool.BRUSH && FRIENDLY_CHUNKS.size() < maxClaimedChunks) || (this.tool == ClaimTool.CHUNK_LOAD_BRUSH && getChunkLoaded() < maxChunkLoadedChunks)) {
                                if (this.tool != ClaimTool.BRUSH || claim != ClaimType.CHUNK_LOADED) {
                                    FRIENDLY_CHUNKS.put(chunkPos, claimType);
                                    isDirty = true;
                                }
                            }
                        } else if (this.tool == ClaimTool.ERASER || claim != ClaimType.CHUNK_LOADED) {
                            FRIENDLY_CHUNKS.remove(chunkPos);
                            isDirty = true;
                        }
                    } else if (owned) {
                        isHovering = true;
                        color = Screen.hasShiftDown() ? 0xfff59a22 : tool == ClaimTool.ERASER ? 0xffff0000 : 0xff55ffff;
                    }

                    if (info != null) {
                        this.setTooltipForNextRenderPass(Component.literal(info.team().name()).withStyle(ChatFormatting.DARK_RED));
                    } else if (claim != null && teamName != null && tool == ClaimTool.NONE) {
                        this.setTooltipForNextRenderPass(Component.literal(teamName).withStyle(ChatFormatting.AQUA));
                    }
                }

                if (shouldRender || isHovering) {
                    boolean north = j == 0 || getClaimType(new ChunkPos(playerChunkX + i, playerChunkZ + j - 1)) != claim;
                    boolean east = i == chunkScale - 1 || getClaimType(new ChunkPos(playerChunkX + i + 1, playerChunkZ + j)) != claim;
                    boolean south = j == chunkScale - 1 || getClaimType(new ChunkPos(playerChunkX + i, playerChunkZ + j + 1)) != claim;
                    boolean west = i == 0 || getClaimType(new ChunkPos(playerChunkX + i - 1, playerChunkZ + j)) != claim;

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

    @Nullable
    private ClaimType getClaimType(ChunkPos chunkPos) {
        return FRIENDLY_CHUNKS.getOrDefault(chunkPos, UNFRIENDLY_CHUNKS.get(chunkPos) != null ? UNFRIENDLY_CHUNKS.get(chunkPos).type() : null);
    }

    private void renderText(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, Component.literal(FRIENDLY_CHUNKS.size() + " / " + maxClaimedChunks), ((this.width + 218) / 2f) - 198, ((this.height - 246) / 2f) + 228, 0x404040);
        this.font.draw(poseStack, Component.literal(getChunkLoaded() + " / " + maxChunkLoadedChunks), ((this.width + 218) / 2f) - 198, ((this.height - 246) / 2f) + 241, 0x404040);

        // text tooltips
        float left = (this.width - MAP_SIZE) / 2f;
        float top = (this.height - MAP_SIZE) / 2f;

        if (mouseX + 2 > left && mouseX < left + MAP_SIZE / 3.5f && mouseY > top + MAP_SIZE && mouseY < top + MAP_SIZE + 13) {
            this.setTooltipForNextRenderPass(Component.translatable("gui.cadmus.claim_map.claimed_chunks", FRIENDLY_CHUNKS.size(), maxClaimedChunks));
        } else if (mouseX + 2 > left && mouseX < left + MAP_SIZE / 3.5f && mouseY > top + MAP_SIZE + 13 && mouseY < top + MAP_SIZE + 26) {
            this.setTooltipForNextRenderPass(Component.translatable("gui.cadmus.claim_map.chunk_loaded_chunks", getChunkLoaded(), maxChunkLoadedChunks));
        }
    }

    private int getChunkLoaded() {
        int chunkLoadedCount = 0;
        for (ClaimType value : FRIENDLY_CHUNKS.values()) {
            if (value == ClaimType.CHUNK_LOADED) {
                chunkLoadedCount++;
            }
        }
        return chunkLoadedCount;
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
        NetworkHandler.CHANNEL.sendToServer(new ClearChunksPacket(true));
        FRIENDLY_CHUNKS.clear();
    }

    public void clearDimension() {
        NetworkHandler.CHANNEL.sendToServer(new ClearChunksPacket(false));
        FRIENDLY_CHUNKS.clear();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void update(Map<ChunkPos, ClaimInfo> claims, UUID playerTeam, String teamName, int maxClaimedChunks, int maxChunkLoadedChunks) {
        waitingForServerData = false;
        ClaimMapScreen.teamName = teamName;
        ClaimMapScreen.maxClaimedChunks = maxClaimedChunks;
        ClaimMapScreen.maxChunkLoadedChunks = maxChunkLoadedChunks;
        claims.forEach((pos, info) -> {
            if (info.team().teamId().equals(playerTeam)) {
                FRIENDLY_CHUNKS.put(pos, info.type());
                START_CHUNKS.put(pos, info.type());
            } else {
                UNFRIENDLY_CHUNKS.put(pos, info);
            }
        });
    }

    @Override
    public void removed() {
        super.removed();
        // don't send the packet if the player didn't change anything
        if (!isDirty) return;
        isDirty = false;

        Set<ChunkPos> removedChunks = START_CHUNKS.keySet()
            .stream()
            .filter(chunkPos -> !FRIENDLY_CHUNKS.containsKey(chunkPos))
            .collect(Collectors.toSet());

        Set<ChunkPos> addedChunkLoadedChunks = FRIENDLY_CHUNKS.keySet()
            .stream()
            .filter(chunkPos -> !START_CHUNKS.containsKey(chunkPos) && FRIENDLY_CHUNKS.get(chunkPos) == ClaimType.CHUNK_LOADED)
            .collect(Collectors.toSet());

        Set<ChunkPos> removedChunkLoadedChunks = START_CHUNKS.keySet()
            .stream()
            .filter(chunkPos -> FRIENDLY_CHUNKS.containsKey(chunkPos) && FRIENDLY_CHUNKS.get(chunkPos) != ClaimType.CHUNK_LOADED)
            .collect(Collectors.toSet());

        NetworkHandler.CHANNEL.sendToServer(new UpdateClaimedChunksPacket(FRIENDLY_CHUNKS, removedChunks, addedChunkLoadedChunks, removedChunkLoadedChunks));
    }
}
