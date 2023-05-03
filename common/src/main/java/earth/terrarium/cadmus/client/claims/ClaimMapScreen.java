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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ClaimMapScreen extends Screen {
    public static final int MAP_SIZE = 200;
    public static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(Cadmus.MOD_ID, "textures/gui/map.png");
    public static final ResourceLocation MAP_ICONS = new ResourceLocation("textures/map/map_icons.png");
    public static final int ORANGE = 0xfff59a22;
    public static final int AQUA = 0xff55ffff;
    public static final int DARK_RED = 0xffbd2025;

    private final Map<ChunkPos, ClaimInfo> startClaims = new HashMap<>();
    private final Map<ChunkPos, ClaimType> teamClaims = new HashMap<>();
    private final Map<ChunkPos, ClaimInfo> otherClaims = new HashMap<>();

    private final Map<UUID, String> teamDisplayNames = new HashMap<>();

    private final Component displayName;
    private final int maxClaims;
    private final int maxChunkLoaded;

    @Nullable
    private ClaimMapRenderer mapRenderer;
    private ClaimTool tool = ClaimTool.NONE;
    private int chunkLoadedCount;

    public ClaimMapScreen(Map<ChunkPos, ClaimInfo> claims, @Nullable UUID teamId, Component displayName, Map<UUID, String> teamDisplayNames, int maxClaims, int maxChunkLoaded) {
        super(Component.empty());
        refreshMap();

        claims.forEach((pos, info) -> {
            if (info.teamId().equals(teamId)) {
                teamClaims.put(pos, info.type());
                startClaims.put(pos, info);
            } else {
                otherClaims.put(pos, info);
            }
        });

        chunkLoadedCount = teamClaims.values()
            .stream()
            .mapToInt(info -> info == ClaimType.CHUNK_LOADED ? 1 : 0)
            .sum();

        this.displayName = displayName;
        this.teamDisplayNames.putAll(teamDisplayNames);
        this.maxClaims = maxClaims;
        this.maxChunkLoaded = maxChunkLoaded;
    }

    public void clearDimension() {
        NetworkHandler.CHANNEL.sendToServer(new ClearChunksPacket(false));
        teamClaims.clear();
    }

    public void clearAll() {
        NetworkHandler.CHANNEL.sendToServer(new ClearChunksPacket(true));
        teamClaims.clear();
    }

    public void refreshMap() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ChunkPos chunkPos = player.chunkPosition();
        int scale = getScaledRenderDistance();
        int minX = chunkPos.getMinBlockX() - scale;
        int minZ = chunkPos.getMinBlockZ() - scale;
        int maxX = chunkPos.getMaxBlockX() + scale + 1;
        int maxZ = chunkPos.getMaxBlockZ() + scale + 1;

        if (scale / 8 > 12) {
            // If the render distance is greater than 12 chunks, run asynchronously to avoid stuttering.
            CompletableFuture.supplyAsync(() -> ClaimMapTopologyAlgorithm.setColors(minX, minZ, maxX, maxZ, player.clientLevel, player)).thenAcceptAsync(colors ->
                this.mapRenderer = new ClaimMapRenderer(colors, scale * 2 + 16), Minecraft.getInstance());
        } else {
            int[][] colors = ClaimMapTopologyAlgorithm.setColors(minX, minZ, maxX, maxZ, player.clientLevel, player);
            this.mapRenderer = new ClaimMapRenderer(colors, scale * 2 + 16);
        }
    }

    public static int getScaledRenderDistance() {
        int scale = Math.min(Minecraft.getInstance().options.renderDistance().get(), 32) * 8;
        scale -= scale % 16;
        return scale;
    }

    @Override
    public void onClose() {
        super.onClose();

        Map<ChunkPos, ClaimType> addedClaims = teamClaims.entrySet()
            .stream()
            .filter(entry -> !startClaims.containsKey(entry.getKey()) || startClaims.get(entry.getKey()).type() != entry.getValue())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Set<ChunkPos> removedChunks = startClaims.keySet()
            .stream()
            .filter(chunkPos -> !teamClaims.containsKey(chunkPos))
            .collect(Collectors.toSet());

        NetworkHandler.CHANNEL.sendToServer(new UpdateClaimedChunksPacket(addedClaims, removedChunks));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);
        if (this.mapRenderer == null) {
            GuiComponent.drawCenteredString(poseStack, font, ConstantComponents.LOADING, (int) (width / 2f), (int) (height / 2f), 0xFFFFFF);
        } else {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            super.render(poseStack, mouseX, mouseY, partialTick);
            this.mapRenderer.render(poseStack, this.width, this.height, MAP_SIZE);
            this.renderText(poseStack, mouseX, mouseY);
            this.renderClaims(player, poseStack, mouseX, mouseY);
            this.renderPlayerAvatar(player, poseStack);
        }
    }

    private void renderBackgroundTexture(PoseStack poseStack) {
        fill(poseStack, (width - MAP_SIZE) / 2, (height - MAP_SIZE) / 2, (width + MAP_SIZE) / 2, (height + MAP_SIZE) / 2, 0xff000000);
        int left = (this.width - 218) / 2;
        int top = (this.height - 249) / 2 + 7;
        RenderSystem.enableBlend();
        RenderUtils.bindTexture(CONTAINER_BACKGROUND);
        blit(poseStack, left, top, 0, 0, 218, 249);
        this.font.draw(poseStack, ConstantComponents.TITLE, (this.width / 2f) - 101, ((this.height - 248) / 2f) + 12, 0x404040);
    }

    private void renderPlayerAvatar(LocalPlayer player, PoseStack poseStack) {
        float left = (this.width) / 2f;
        float top = (this.height) / 2f;

        double playerX = player.getX();
        double playerZ = player.getZ();
        double x = (playerX % 16) + (playerX >= 0 ? -8 : 8);
        double y = (playerZ % 16) + (playerZ >= 0 ? -8 : 8);

        float scale = MAP_SIZE / (getScaledRenderDistance() * 2f + 16);

        x *= scale;
        y *= scale;
        RenderUtils.bindTexture(MAP_ICONS);
        try (var ignored = new CloseablePoseStack(poseStack)) {
            poseStack.translate(left + x, top + y, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(player.getYRot()));
            poseStack.translate(-8, -8, 0);
            blit(poseStack, 0, 0, 40, 0, 8, 8, 128, 128);
        }
    }

    private void renderText(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, Component.literal(teamClaims.size() + " / " + this.maxClaims), ((this.width + 218) / 2f) - 198, ((this.height - 246) / 2f) + 228, 0x404040);
        this.font.draw(poseStack, Component.literal(chunkLoadedCount + " / " + this.maxChunkLoaded), ((this.width + 218) / 2f) - 198, ((this.height - 246) / 2f) + 241, 0x404040);

        // text tooltips
        float left = (this.width - MAP_SIZE) / 2f;
        float top = (this.height - MAP_SIZE) / 2f;

        if (mouseX + 2 > left && mouseX < left + MAP_SIZE / 3.5f && mouseY > top + MAP_SIZE && mouseY < top + MAP_SIZE + 13) {
            this.setTooltipForNextRenderPass(Component.translatable("gui.cadmus.claim_map.claimed_chunks", teamClaims.size(), this.maxClaims));
        } else if (mouseX + 2 > left && mouseX < left + MAP_SIZE / 3.5f && mouseY > top + MAP_SIZE + 13 && mouseY < top + MAP_SIZE + 26) {
            this.setTooltipForNextRenderPass(Component.translatable("gui.cadmus.claim_map.chunk_loaded_chunks", chunkLoadedCount, this.maxChunkLoaded));
        }
    }

    private void renderClaims(LocalPlayer player, PoseStack poseStack, int mouseX, int mouseY) {
        float left = (this.width - MAP_SIZE) / 2f;
        float top = (this.height - MAP_SIZE) / 2f;
        float scale = getScaledRenderDistance() * 2f + 16;
        float chunkScale = scale / 16f;
        float pixelScale = MAP_SIZE / scale;
        ChunkPos playerChunk = player.chunkPosition();

        for (int i = 0; i < chunkScale; i++) {
            for (int j = 0; j < chunkScale; j++) {
                float x = left + (i * 16 * pixelScale);
                float y = top + (j * 16 * pixelScale);
                float width = pixelScale * 16;
                float height = pixelScale * 16;

                int playerChunkX = Math.round(playerChunk.x - chunkScale / 2);
                int playerChunkZ = Math.round(playerChunk.z - chunkScale / 2);
                ChunkPos chunkPos = new ChunkPos(playerChunkX + i, playerChunkZ + j);
                ClaimType teamType = teamClaims.get(chunkPos);
                ClaimInfo otherInfo = otherClaims.get(chunkPos);
                ClaimType type = otherInfo != null ? otherInfo.type() : teamType;

                int color;
                boolean isHovering = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
                if (isHovering) {
                    if (otherInfo == null) {
                        type = useTool(chunkPos, type);
                    }
                    drawTooltips(teamType, otherInfo);
                    color = Screen.hasShiftDown() ? ORANGE : tool == ClaimTool.ERASER ? DARK_RED : AQUA;
                } else {
                    color = otherInfo != null ? DARK_RED : teamType != null ? teamType == ClaimType.CHUNK_LOADED ? ORANGE : AQUA : 0x00FFFFFF;
                }

                if (type == null && !isHovering) continue;
                boolean north = j == 0 || getClaimType(new ChunkPos(playerChunkX + i, playerChunkZ + j - 1)) != type;
                boolean east = i == chunkScale - 1 || getClaimType(new ChunkPos(playerChunkX + i + 1, playerChunkZ + j)) != type;
                boolean south = j == chunkScale - 1 || getClaimType(new ChunkPos(playerChunkX + i, playerChunkZ + j + 1)) != type;
                boolean west = i == 0 || getClaimType(new ChunkPos(playerChunkX + i - 1, playerChunkZ + j)) != type;

                boolean northEast = j == 0 || i == chunkScale - 1 || getClaimType(new ChunkPos(playerChunkX + i + 1, playerChunkZ + j - 1)) != type;
                boolean southEast = j == chunkScale - 1 || i == chunkScale - 1 || getClaimType(new ChunkPos(playerChunkX + i + 1, playerChunkZ + j + 1)) != type;
                boolean southWest = j == chunkScale - 1 || i == 0 || getClaimType(new ChunkPos(playerChunkX + i - 1, playerChunkZ + j + 1)) != type;
                boolean northWest = j == 0 || i == 0 || getClaimType(new ChunkPos(playerChunkX + i - 1, playerChunkZ + j - 1)) != type;

                drawCTMSquare(isHovering, poseStack, x, y, width, height, color, north, east, south, west, northEast, southEast, southWest, northWest);
            }
        }
    }

    private ClaimType useTool(ChunkPos pos, @Nullable ClaimType teamType) {
        ClaimType type = switch (this.tool) {
            case BRUSH, CHUNK_LOAD_ERASER -> ClaimType.CLAIMED;
            case CHUNK_LOAD_BRUSH -> ClaimType.CHUNK_LOADED;
            default -> null;
        };
        if (this.tool == ClaimTool.NONE) return null;

        if (type != null) {
            if (this.teamClaims.size() < this.maxClaims && tool == ClaimTool.BRUSH && !teamClaims.containsKey(pos)) {
                teamClaims.put(pos, type);
            }

            if (this.chunkLoadedCount < this.maxChunkLoaded && tool == ClaimTool.CHUNK_LOAD_BRUSH && !(teamClaims.containsKey(pos) && teamClaims.get(pos) == ClaimType.CHUNK_LOADED)) {
                this.chunkLoadedCount++;
                teamClaims.put(pos, type);
            }

            if (tool == ClaimTool.CHUNK_LOAD_ERASER && teamClaims.containsKey(pos)) {
                if (teamType == ClaimType.CHUNK_LOADED) {
                    this.chunkLoadedCount--;
                }
                teamClaims.put(pos, type);
            }

        } else if (this.tool == ClaimTool.ERASER && teamClaims.containsKey(pos)) {
            if (teamType == ClaimType.CHUNK_LOADED) {
                this.chunkLoadedCount--;
            }
            teamClaims.remove(pos);
        }
        return type;
    }

    private void drawTooltips(ClaimType teamType, ClaimInfo otherInfo) {
        if (otherInfo != null) {
            String otherTeamDisplayName = teamDisplayNames.get(otherInfo.teamId());
            if (otherTeamDisplayName == null) return;
            this.setTooltipForNextRenderPass(Component.literal(otherTeamDisplayName).withStyle(ChatFormatting.DARK_RED)); // TODO: use otherInfo.displayName
        } else if (teamType != null && tool == ClaimTool.NONE) {
            this.setTooltipForNextRenderPass(this.displayName.copy().withStyle(ChatFormatting.AQUA));
        }
    }

    private void drawCTMSquare(boolean isHovering, PoseStack poseStack, float x, float y, float width, float height, int color, boolean north, boolean east, boolean south, boolean west, boolean northEast, boolean southEast, boolean southWest, boolean northWest) {
        int roundedX = Math.round(x);
        int roundedY = Math.round(y);
        int roundedWidth = Math.round(x + width);
        int roundedHeight = Math.round(y + height);
        if (north || isHovering) {
            fill(poseStack, roundedX, roundedY, roundedWidth, roundedY + 1, color);
        } else if (northEast) {
            fill(poseStack, roundedWidth - 1, roundedY, roundedWidth, roundedY + 1, color);
        }
        if (east || isHovering) {
            fill(poseStack, roundedWidth - 1, roundedY, roundedWidth, roundedHeight, color);
        } else if (southEast) {
            fill(poseStack, roundedWidth - 1, roundedHeight - 1, roundedWidth, roundedHeight, color);
        }
        if (south || isHovering) {
            fill(poseStack, roundedX, roundedHeight - 1, roundedWidth, roundedHeight, color);
        } else if (southWest) {
            fill(poseStack, roundedX, roundedHeight - 1, roundedX + 1, roundedHeight, color);
        }
        if (west || isHovering) {
            fill(poseStack, roundedX, roundedY, roundedX + 1, roundedHeight, color);
        } else if (northWest) {
            fill(poseStack, roundedX, roundedY, roundedX + 1, roundedY + 1, color);
        }
        fill(poseStack, roundedX, roundedY, roundedWidth, roundedHeight, color & 0x33ffffff);
    }

    @Nullable
    private ClaimType getClaimType(ChunkPos chunkPos) {
        ClaimType teamType = teamClaims.get(chunkPos);
        if (teamType != null) {
            return teamType;
        }
        ClaimInfo otherInfo = otherClaims.get(chunkPos);
        if (otherInfo != null) {
            return otherInfo.type();
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        setFocused(null);
        this.tool = (button == 0) ? (Screen.hasShiftDown() ? ClaimTool.CHUNK_LOAD_BRUSH : ClaimTool.BRUSH)
            : (Screen.hasShiftDown() ? ClaimTool.CHUNK_LOAD_ERASER : ClaimTool.ERASER);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.tool = ClaimTool.NONE;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
