package earth.terrarium.cadmus.client.claims;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.ClientboundSendClaimedChunksPacket;
import earth.terrarium.cadmus.common.network.messages.ServerboundClearChunksPacket;
import earth.terrarium.cadmus.common.network.messages.ServerboundUpdateClaimedChunksPacket;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ClaimScreen extends BaseCursorScreen {

    public static final int MAP_SIZE = 200;
    public static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(Cadmus.MOD_ID, "textures/gui/map.png");
    private static final WidgetSprites TRASH_BUTTON_SPRITES = new WidgetSprites(
        new ResourceLocation(Cadmus.MOD_ID, "claimmap/trash_button"),
        new ResourceLocation(Cadmus.MOD_ID, "claimmap/trash_button_highlighted")
    );
    private static final WidgetSprites X_BUTTON_SPRITES = new WidgetSprites(
        new ResourceLocation(Cadmus.MOD_ID, "claimmap/x_button"),
        new ResourceLocation(Cadmus.MOD_ID, "claimmap/x_button_highlighted")
    );
    public static final ResourceLocation MAP_ICONS = new ResourceLocation("textures/map/map_icons.png");
    public static final int ORANGE = 0xfff59a22;
    public static final int AQUA = 0xff55ffff;
    public static final int DARK_RED = 0xffbd2025;

    private final Map<ChunkPos, Pair<String, ClaimType>> startClaims = new HashMap<>();
    private final Map<ChunkPos, ClaimType> teamClaims = new HashMap<>();
    private final Map<ChunkPos, Pair<String, ClaimType>> otherClaims = new HashMap<>();

    private final Map<String, Component> teamDisplayNames = new HashMap<>();

    private final Component displayName;
    private final int viewDistance;
    private final int color;
    private final int maxClaims;
    private final int maxChunkLoaded;

    @Nullable
    private ClaimMapRenderer mapRenderer;
    private ClaimTool tool = ClaimTool.NONE;
    private int claimedCount;
    private int chunkLoadedCount;
    private Button clearButton;

    public ClaimScreen(Map<ChunkPos, Pair<String, ClaimType>> claims, @Nullable String id, ChatFormatting color, Component displayName, Map<String, Component> teamDisplayNames, int claimedCount, int chunkLoadedCount, int maxClaims, int maxChunkLoaded, int viewDistance) {
        super(Component.empty());
        this.viewDistance = viewDistance;
        refreshMap();

        this.color = color.getColor() == null ? AQUA : color.getColor() | 0xff000000;
        claims.forEach((pos, pair) -> {
            if (pair.getFirst().equals(id)) {
                teamClaims.put(pos, pair.getSecond());
                startClaims.put(pos, pair);
            } else {
                otherClaims.put(pos, pair);
            }
        });

        this.claimedCount = claimedCount;
        this.chunkLoadedCount = chunkLoadedCount;

        this.displayName = displayName;
        this.teamDisplayNames.putAll(teamDisplayNames);
        this.maxClaims = maxClaims;
        this.maxChunkLoaded = maxChunkLoaded;
    }

    public static void createFromPacket(ClientboundSendClaimedChunksPacket message) {
        Minecraft.getInstance().setScreen(new ClaimScreen(
            message.claims(),
            message.id(),
            message.color(),
            message.displayName().map(Component::nullToEmpty)
                .orElse(Objects.requireNonNull(Minecraft.getInstance().player).getDisplayName()),
            message.teamDisplayNames(),
            message.claimedCount(),
            message.chunkLoadedCount(),
            message.maxClaims(),
            message.maxChunkLoaded(),
            message.viewDistance()));
    }

    public void clearDimension() {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundClearChunksPacket(false));
    }

    public void clearAll() {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundClearChunksPacket(true));
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

    public int getScaledRenderDistance() {
        int scale = this.viewDistance * 8;
        scale -= scale % 16;
        return scale;
    }

    @Override
    public void onClose() {
        super.onClose();

        Map<ChunkPos, ClaimType> addedChunks = teamClaims.entrySet()
            .stream()
            .filter(entry -> !startClaims.containsKey(entry.getKey()) || startClaims.get(entry.getKey()).getSecond() != entry.getValue())
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<ChunkPos, ClaimType> removedChunks = startClaims.entrySet()
            .stream()
            .filter(entry -> !teamClaims.containsKey(entry.getKey()))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> entry.getValue().getSecond()));

        // don't send if nothing has changed
        if (addedChunks.isEmpty() && removedChunks.isEmpty()) return;
        NetworkHandler.CHANNEL.sendToServer(new ServerboundUpdateClaimedChunksPacket(addedChunks, removedChunks));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        int x = (this.width - 216) / 2;
        int y = (this.height - 237) / 2;

        this.clearButton = this.addRenderableWidget(new ImageButton(x + 7, y + 6, 11, 11,
            TRASH_BUTTON_SPRITES,
            button -> {
                if (Screen.hasShiftDown()) {
                    clearAll();
                } else {
                    clearDimension();
                }
                CadmusClient.openClaimMap();
            }
        ));

        this.addRenderableWidget(new ImageButton(x + 216 - 11 - 7, y + 6, 11, 11,
            X_BUTTON_SPRITES,
            button -> this.onClose()
        )).setTooltip(Tooltip.create(ConstantComponents.CLOSE));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        renderBackgroundTexture(graphics);
        if (this.mapRenderer == null) {
            graphics.drawCenteredString(font, ConstantComponents.LOADING, (int) (width / 2f), (int) (height / 2f), 0xFFFFFF);
        } else {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            this.mapRenderer.render(graphics, this.width, this.height, MAP_SIZE);
            try (var pose = new CloseablePoseStack(graphics)) {
                pose.translate(0, 0, 2);
                this.renderText(graphics, mouseX, mouseY);
                this.renderClaims(player, graphics, mouseX, mouseY);
                this.renderPlayerAvatar(player, graphics);
            }
        }

        this.clearButton.setTooltip(Tooltip.create(hasShiftDown() ?
            ConstantComponents.CLEAR_ALL_CLAIMED_CHUNKS :
            ConstantComponents.CLEAR_CLAIMED_CHUNKS));
        this.clearButton.setTooltipDelay(-1);
    }

    private void renderBackgroundTexture(GuiGraphics graphics) {
        graphics.fill((width - MAP_SIZE) / 2, (height - MAP_SIZE) / 2, (width + MAP_SIZE) / 2, (height + MAP_SIZE) / 2, 0xff000000);
        int left = (this.width - 216) / 2;
        int top = (this.height - 237) / 2 + 1;
        graphics.blit(CONTAINER_BACKGROUND, left, top, 0, 0, 216, 237);
        graphics.drawString(font, ConstantComponents.TITLE, (int) ((this.width - font.width(ConstantComponents.TITLE)) / 2f), top + 7, 0x404040, false);
    }

    private void renderPlayerAvatar(LocalPlayer player, GuiGraphics graphics) {
        float left = (this.width) / 2f;
        float top = (this.height) / 2f;

        double playerX = player.getX();
        double playerZ = player.getZ();
        double x = (playerX % 16) + (playerX >= 0 ? -8 : 8);
        double y = (playerZ % 16) + (playerZ >= 0 ? -8 : 8);

        float scale = MAP_SIZE / (getScaledRenderDistance() * 2f + 16);

        x *= scale;
        y *= scale;
        try (var pose = new CloseablePoseStack(graphics)) {
            pose.translate(left + x, top + y, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(player.getYRot()));
            pose.translate(-4, -4, 0);
            graphics.blit(MAP_ICONS, 0, 0, 40, 0, 8, 8, 128, 128);
        }
    }

    private void renderText(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, claimedCount + "/" + this.maxClaims, (int) (((this.width + 218) / 2f) - 198), (int) (((this.height - 246) / 2f) + 228), 0x404040, false);
        graphics.drawString(font, chunkLoadedCount + "/" + this.maxChunkLoaded, (int) (((this.width + 218) / 2f) - 119), (int) (((this.height - 246) / 2f) + 228), 0x404040, false);

        // text tooltips
        float left = (this.width - MAP_SIZE) / 2f;
        float top = (this.height - MAP_SIZE) / 2f;

        if (mouseX + 2 > left && mouseX < left + MAP_SIZE / 3.5f + 15 && mouseY > top + MAP_SIZE && mouseY < top + MAP_SIZE + 13) {
            this.setTooltipForNextRenderPass(Component.translatable("gui.cadmus.claim_map.claimed_chunks", claimedCount, this.maxClaims));
        } else if (mouseX + 2 > left + 75 && mouseX < left + MAP_SIZE / 3.5f + 70 && mouseY > top + MAP_SIZE && mouseY < top + MAP_SIZE + 13) {
            this.setTooltipForNextRenderPass(Component.translatable("gui.cadmus.claim_map.chunk_loaded_chunks", chunkLoadedCount, this.maxChunkLoaded));
        }
    }

    private void renderClaims(LocalPlayer player, GuiGraphics graphics, int mouseX, int mouseY) {
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
                var otherInfo = otherClaims.get(chunkPos);
                ClaimType type = otherInfo != null ? otherInfo.getSecond() : teamType;

                int color;
                boolean isHovering = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
                if (isHovering) {
                    if (otherInfo == null) {
                        type = useTool(chunkPos, type);
                    }
                    drawTooltips(teamType, otherInfo);
                    color = Screen.hasShiftDown() ? ORANGE : tool == ClaimTool.ERASER || otherInfo != null ? DARK_RED : this.color;
                } else {
                    color = otherInfo != null ? ModUtils.isAdmin(otherInfo.getFirst()) ? 0xffff55ff : DARK_RED : teamType != null ? teamType == ClaimType.CHUNK_LOADED ? ORANGE : this.color : 0x00FFFFFF;
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

                drawCTMSquare(isHovering, graphics, x, y, width, height, color, north, east, south, west, northEast, southEast, southWest, northWest);
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
            if (this.claimedCount < this.maxClaims && tool == ClaimTool.BRUSH && !teamClaims.containsKey(pos)) {
                this.claimedCount++;
                teamClaims.put(pos, type);
            }

            if (this.chunkLoadedCount < this.maxChunkLoaded && tool == ClaimTool.CHUNK_LOAD_BRUSH && !(teamClaims.containsKey(pos) && teamClaims.get(pos) == ClaimType.CHUNK_LOADED)) {
                if (!teamClaims.containsKey(pos)) {
                    this.claimedCount++;
                }
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
            this.claimedCount--;
            if (teamType == ClaimType.CHUNK_LOADED) {
                this.chunkLoadedCount--;
            }
            teamClaims.remove(pos);
        }
        return type;
    }

    private void drawTooltips(ClaimType teamType, Pair<String, ClaimType> otherInfo) {
        if (otherInfo != null) {
            ImmutableList.Builder<FormattedCharSequence> tooltips = ImmutableList.builder();
            Component otherTeamDisplayName = teamDisplayNames.get(otherInfo.getFirst());
            if (otherTeamDisplayName == null || otherTeamDisplayName.getString().isEmpty()) return;
            tooltips.add(otherTeamDisplayName.copy().withStyle(ModUtils.isAdmin(otherInfo.getFirst()) ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_RED).getVisualOrderText());
            if (otherInfo.getSecond() == ClaimType.CHUNK_LOADED) {
                tooltips.add(ConstantComponents.CHUNK_LOADED.getVisualOrderText());
            }
            if (ModUtils.isAdmin(otherInfo.getFirst())) {
                tooltips.add(ConstantComponents.ADMIN_CLAIM.getVisualOrderText());
            }
            this.setTooltipForNextRenderPass(tooltips.build());
        } else if (teamType != null && tool == ClaimTool.NONE) {
            ImmutableList.Builder<FormattedCharSequence> tooltips = ImmutableList.builder();
            tooltips.add(this.displayName.copy().withStyle(this.displayName.getStyle().withColor(color)).getVisualOrderText());
            if (teamType == ClaimType.CHUNK_LOADED) {
                tooltips.add(ConstantComponents.CHUNK_LOADED.getVisualOrderText());
            }
            this.setTooltipForNextRenderPass(tooltips.build());
        }
    }

    private void drawCTMSquare(boolean isHovering, GuiGraphics graphics, float x, float y, float width, float height, int color, boolean north, boolean east, boolean south, boolean west, boolean northEast, boolean southEast, boolean southWest, boolean northWest) {
        int roundedX = Math.round(x);
        int roundedY = Math.round(y);
        int roundedWidth = Math.round(x + width);
        int roundedHeight = Math.round(y + height);
        if (north || isHovering) {
            graphics.fill(roundedX, roundedY, roundedWidth, roundedY + 1, color);
        } else if (northEast) {
            graphics.fill(roundedWidth - 1, roundedY, roundedWidth, roundedY + 1, color);
        }
        if (east || isHovering) {
            graphics.fill(roundedWidth - 1, roundedY, roundedWidth, roundedHeight, color);
        } else if (southEast) {
            graphics.fill(roundedWidth - 1, roundedHeight - 1, roundedWidth, roundedHeight, color);
        }
        if (south || isHovering) {
            graphics.fill(roundedX, roundedHeight - 1, roundedWidth, roundedHeight, color);
        } else if (southWest) {
            graphics.fill(roundedX, roundedHeight - 1, roundedX + 1, roundedHeight, color);
        }
        if (west || isHovering) {
            graphics.fill(roundedX, roundedY, roundedX + 1, roundedHeight, color);
        } else if (northWest) {
            graphics.fill(roundedX, roundedY, roundedX + 1, roundedY + 1, color);
        }
        graphics.fill(roundedX, roundedY, roundedWidth, roundedHeight, color & 0x33ffffff);
    }

    @Nullable
    private ClaimType getClaimType(ChunkPos chunkPos) {
        ClaimType teamType = teamClaims.get(chunkPos);
        if (teamType != null) {
            return teamType;
        }
        var otherInfo = otherClaims.get(chunkPos);
        if (otherInfo != null) {
            return otherInfo.getSecond();
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
