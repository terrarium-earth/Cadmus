package earth.terrarium.cadmus.client;

import com.mojang.math.Axis;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen;
import com.teamresourceful.resourcefullib.client.utils.ScreenUtils;
import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.client.events.CadmusClientEvents;
import earth.terrarium.cadmus.api.events.CadmusEvents;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.commands.claims.ClaimCommand;
import earth.terrarium.cadmus.common.commands.claims.ClaimCommandType;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.serverbound.RequestClaimSettingsPacket;
import earth.terrarium.cadmus.common.teams.TeamInfo;
import earth.terrarium.olympus.client.components.Widgets;
import earth.terrarium.olympus.client.components.buttons.Button;
import earth.terrarium.olympus.client.components.map.MapRenderer;
import earth.terrarium.olympus.client.components.map.MapWidget;
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers;
import earth.terrarium.olympus.client.constants.MinecraftColors;
import earth.terrarium.olympus.client.ui.UIConstants;
import earth.terrarium.olympus.client.ui.modals.DeleteConfirmModal;
import earth.terrarium.olympus.client.utils.State;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimMapScreen extends BaseCursorScreen {
    public static final ResourceLocation MAP_ICONS = ResourceLocation.withDefaultNamespace("textures/map/decorations/player.png");
    public static final int MAP_SIZE = 192;
    public static final int BANNER_HEIGHT = 15;
    public static final int BUTTON_HEIGHT = 24;
    public static final int PADDING = 4;
    public static final int WIDTH = MAP_SIZE + PADDING * 2 + 2;
    public static final int HEIGHT = MAP_SIZE + PADDING * 4 + 2 + BANNER_HEIGHT + BUTTON_HEIGHT;

    public boolean canModifyColor = false;
    public Color teamColor;

    private final Map<ChunkPos, ClaimTile> claims = new HashMap<>();
    private final Map<String, TriState> settings = new HashMap<>();

    private final LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
    private final ClientLevel level = player.clientLevel;

    private final State<MapRenderer> mapState = State.empty();

    private MapWidget mapWidget;
    private Button settingsButton;
    private final Map<TeamId, TeamData> teams = new HashMap<>();

    private float chunkScale;
    private float pixelScale;
    private int playerChunkX;
    private int playerChunkZ;

    private int selectionStartX;
    private int selectionStartZ;
    private int selectionEndX;
    private int selectionEndZ;

    public ClaimMapScreen() {
        super(CommonComponents.EMPTY);
    }

    public void refresh() {
        teams.clear();
        TeamApi.API.getTeamsList(this.player).forEach(teamId -> {
            TeamInfo info = CadmusClient.TEAM_INFO.get(teamId);
            teams.put(teamId, new TeamData(
                info.name(),
                info.color(),
                ClaimCommand.getClaimsCount(level, teamId, false),
                ClaimLimitApi.API.getMaxClaims(teamId),
                ClaimCommand.getClaimsCount(level, teamId, true),
                ClaimLimitApi.API.getMaxChunkLoadedClaims(teamId)
            ));
        });

        int renderDistanceScale = this.getScaledRenderDistance();
        this.chunkScale = renderDistanceScale / 16f;
        this.pixelScale = (float) MAP_SIZE / renderDistanceScale * 16;
        this.playerChunkX = Math.round(player.chunkPosition().x - chunkScale / 2);
        this.playerChunkZ = Math.round(player.chunkPosition().z - chunkScale / 2);

        this.calculateClaims();
        NetworkHandler.CHANNEL.sendToServer(new RequestClaimSettingsPacket());
    }

    public void refreshMap() {
        mapWidget.refreshMap();
    }

    @Override
    protected void init() {
        NetworkHandler.CHANNEL.sendToServer(new RequestClaimSettingsPacket());

        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        var frame = new FrameLayout(x, y, WIDTH, HEIGHT);
        frame.setMinDimensions(WIDTH, HEIGHT);

        frame.addChild(new ImageButton(0, 0, 11, 11, UIConstants.MODAL_CLOSE, button -> onClose()), (settings) -> {
                settings.padding(2);
                settings.alignHorizontallyRight();
                settings.alignVerticallyTop();
            })
            .setTooltip(Tooltip.create(ConstantComponents.CLOSE));

        frame.addChild(new ImageButton(0, 0, 11, 11, UIConstants.MODAL_REFRESH, button -> {
                refresh();
                refreshMap();
            }), (settings) -> {
                settings.padding(15, 2);
                settings.alignHorizontallyRight();
                settings.alignVerticallyTop();
            })
            .setTooltip(Tooltip.create(UIConstants.REFRESH));

        frame.addChild(new StringWidget(ConstantComponents.MAP_TITLE, font), (settings) -> {
            settings.padding(4);
            settings.alignHorizontallyLeft();
            settings.alignVerticallyTop();
        }).setColor(0xFFFFFF);

        this.refresh();
        this.mapWidget = frame.addChild(Widgets.map(mapState), (settings) -> {
            settings.padding(0, BANNER_HEIGHT + PADDING + 1);
            settings.alignHorizontallyCenter();
            settings.alignVerticallyTop();
        });

        mapWidget.withSize(MAP_SIZE);

        frame.addChild(
            Widgets.button()
                .withCallback(this::unclaimAll)
                .withTexture(UIConstants.DANGER_BUTTON)
                .withSize(MAP_SIZE / 2, BUTTON_HEIGHT)
                .withRenderer(WidgetRenderers.text(ConstantComponents.UNCLAIM_ALL).withColor(MinecraftColors.WHITE)),
            (settings) -> {
                settings.padding(PADDING);
                settings.alignHorizontallyLeft();
                settings.alignVerticallyBottom();
            }
        );

        settingsButton = frame.addChild(
            Widgets.button()
                .withCallback(() -> minecraft.setScreen(new ClaimConfigModal(this)))
                .withSize(MAP_SIZE / 2, BUTTON_HEIGHT)
                .withRenderer(WidgetRenderers.text(ConstantComponents.SETTINGS)),
            (settings) -> {
                settings.padding(PADDING);
                settings.alignHorizontallyRight();
                settings.alignVerticallyBottom();
            }
        );

        settingsButton.active = !settings.isEmpty();

        frame.arrangeElements();
        frame.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        drawClaimLabels(graphics);
        drawClaims(graphics, mouseX, mouseY);

        if (this.selectionStartX == 0 && this.selectionStartZ == 0) {
            drawHover(graphics, mouseX, mouseY);
        } else {
            drawSelection(graphics);
        }

        renderPlayerAvatar(graphics);
    }

    private void drawClaimLabels(GuiGraphics graphics) {
        int left = mapWidget.getX() + PADDING;
        int right = mapWidget.getX() + mapWidget.getWidth() - PADDING;
        int top = mapWidget.getY() + mapWidget.getHeight() - PADDING - font.lineHeight;

        String claimedCount = String.format("%d/%d", this.claimedCount, this.maxClaims);
        String chunkLoadedCount = String.format("%d/%d", this.chunkLoadedCount, this.maxChunkLoaded);

        graphics.drawString(font, claimedCount, left, top, 0xFFFFFF, true);
        graphics.drawString(font, chunkLoadedCount, right - font.width(chunkLoadedCount), top, 0xFFFFFF, true);

        graphics.drawString(font, ConstantComponents.MAX_CLAIMS, left, top - 10, 0xFFFFFF, true);
        graphics.drawString(font, ConstantComponents.MAX_CHUNK_LOADED_CLAIMS, right - font.width(ConstantComponents.MAX_CHUNK_LOADED_CLAIMS), top - 10, 0xFFFFFF, true);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;
        graphics.blitSprite(UIConstants.MODAL, left, top, WIDTH, HEIGHT);
        graphics.blitSprite(UIConstants.MODAL_HEADER, left, top, WIDTH, BANNER_HEIGHT);
        graphics.blitSprite(UIConstants.MODAL_FOOTER, left, top + HEIGHT - BUTTON_HEIGHT - PADDING * 2, WIDTH, BUTTON_HEIGHT + PADDING * 2);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < chunkScale; i++) {
            for (int j = 0; j < chunkScale; j++) {
                float x = mapWidget.getX() + (i * pixelScale);
                float y = mapWidget.getY() + (j * pixelScale);

                if (isHovering(mouseX, mouseY, x, y)) {
                    this.selectionStartX = playerChunkX + i;
                    this.selectionStartZ = playerChunkZ + j;
                    this.selectionEndX = playerChunkX + i;
                    this.selectionEndZ = playerChunkZ + j;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (int i = 0; i < chunkScale; i++) {
            for (int j = 0; j < chunkScale; j++) {
                float x = mapWidget.getX() + (i * pixelScale);
                float y = mapWidget.getY() + (j * pixelScale);

                if (isHovering(mouseX, mouseY, x, y)) {
                    this.selectionEndX = playerChunkX + i;
                    this.selectionEndZ = playerChunkZ + j;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        setFocused(null);
        doAction(this.selectionStartX, this.selectionEndX, this.selectionStartZ, this.selectionEndZ, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void drawClaims(GuiGraphics graphics, int mouseX, int mouseY) {
        this.claims.forEach((pos, claim) -> {
            float x = mapWidget.getX() + (claim.x * pixelScale);
            float y = mapWidget.getY() + (claim.y * pixelScale);

            drawClaimSquare(graphics, x, y, pixelScale, pixelScale, modifyAlpha(claim.color, 127),
                claim.north, claim.east,
                claim.south, claim.west,
                claim.northEast, claim.southEast,
                claim.southWest, claim.northWest);

            if (isHovering(mouseX, mouseY, x, y)) {
                ScreenUtils.setTooltip(claim.name);
            }
        });
    }

    private void drawHover(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = 0; i < chunkScale; i++) {
            for (int j = 0; j < chunkScale; j++) {
                float x = mapWidget.getX() + (i * pixelScale);
                float y = mapWidget.getY() + (j * pixelScale);

                if (isHovering(mouseX, mouseY, x, y)) {
                    drawClaimSquare(graphics, x, y, pixelScale, pixelScale, color(this.teamColor, 127),
                        true, true, true, true, true, true, true, true);
                    return;
                }
            }
        }
    }

    private void drawSelection(GuiGraphics graphics) {
        int selectionStartX = Math.min(this.selectionStartX, this.selectionEndX);
        int selectionStartZ = Math.min(this.selectionStartZ, this.selectionEndZ);
        int selectionEndX = Math.max(this.selectionStartX, this.selectionEndX);
        int selectionEndZ = Math.max(this.selectionStartZ, this.selectionEndZ);

        float x = mapWidget.getX() + ((selectionStartX - playerChunkX) * pixelScale);
        float y = mapWidget.getY() + ((selectionStartZ - playerChunkZ) * pixelScale);
        float width = Math.max(pixelScale, ((selectionEndX + 1 - selectionStartX)) * pixelScale);
        float height = Math.max(pixelScale, ((selectionEndZ + 1 - selectionStartZ)) * pixelScale);

        drawClaimSquare(graphics, x, y, width, height, color(this.teamColor, 127),
            true, true, true, true, true, true, true, true);
    }

    private void calculateClaims() {
        this.claims.clear();
        for (int i = 0; i < chunkScale; i++) {
            for (int j = 0; j < chunkScale; j++) {
                ChunkPos pos = new ChunkPos(playerChunkX + i, playerChunkZ + j);

                var claim = ClaimApi.API.getClaim(level, pos);
                if (claim.isEmpty()) continue;
                UUID id = claim.get().left();

                Component name = getName(id, claim.get().rightBoolean());
                int color = color(CadmusClient.TEAM_INFO.get(id).color(), 127);

                boolean north = checkSide(i, j, 0, -1);
                boolean east = checkSide(i, j, 1, 0);
                boolean south = checkSide(i, j, 0, 1);
                boolean west = checkSide(i, j, -1, 0);

                boolean northEast = checkSide(i, j, 1, -1);
                boolean southEast = checkSide(i, j, 1, 1);
                boolean southWest = checkSide(i, j, -1, 1);
                boolean northWest = checkSide(i, j, -1, -1);

                this.claims.put(pos, new ClaimTile(id, name, color, pos, i, j, north, east, south, west, northEast, southEast, southWest, northWest));
            }
        }
    }

    private boolean checkSide(int x, int z, int offsetX, int offsetZ) {
        ChunkPos pos = new ChunkPos(playerChunkX + x + offsetX, playerChunkZ + z + offsetZ);

        return ClaimApi.API.getClaim(level, pos).map(pair -> {
            ChunkPos currentPos = new ChunkPos(playerChunkX + x, playerChunkZ + z);
            return !pair.left().equals(ClaimApi.API.getClaim(level, currentPos).map(Pair::left).orElse(null));
        }).orElse(true);
    }

    private void drawClaimSquare(
        GuiGraphics graphics,
        float x, float y,
        float width, float height,
        int color,
        boolean north, boolean east,
        boolean south, boolean west,
        boolean northEast, boolean southEast,
        boolean southWest, boolean northWest
    ) {
        int roundedX = Math.round(x);
        int roundedY = Math.round(y);
        int roundedWidth = Math.round(x + width);
        int roundedHeight = Math.round(y + height);

        int borderColor = (color & 0x00FFFFFF) | 0xFF000000;

        if (north) graphics.fill(roundedX, roundedY, roundedWidth, roundedY + 1, 2, borderColor);
        else if (northEast) graphics.fill(roundedWidth - 1, roundedY, roundedWidth, roundedY + 1, 2, borderColor);

        if (east) graphics.fill(roundedWidth - 1, roundedY, roundedWidth, roundedHeight, 2, borderColor);
        else if (southEast) graphics.fill(roundedWidth - 1, roundedHeight - 1, roundedWidth, roundedHeight, 2, borderColor);

        if (south) graphics.fill(roundedX, roundedHeight - 1, roundedWidth, roundedHeight, 2, borderColor);
        else if (southWest) graphics.fill(roundedX, roundedHeight - 1, roundedX + 1, roundedHeight, 2, borderColor);

        if (west) graphics.fill(roundedX, roundedY, roundedX + 1, roundedHeight, 2, borderColor);
        else if (northWest) graphics.fill(roundedX, roundedY, roundedX + 1, roundedY + 1, 2, borderColor);

        graphics.fill(roundedX, roundedY, roundedWidth, roundedHeight, 2, color & 0x33ffffff);
    }

    private void renderPlayerAvatar(GuiGraphics graphics) {
        float left = (this.width) / 2f;
        float top = (this.height) / 2f;

        double playerX = player.getX();
        double playerZ = player.getZ();
        double x = (playerX % 16) + (playerX >= 0 ? -8 : 8);
        double y = (playerZ % 16) + (playerZ >= 0 ? -8 : 8);

        float scale = MAP_SIZE / (getMapScale() * 2f + 16);

        x *= scale;
        y *= scale;
        try (var pose = new CloseablePoseStack(graphics)) {
            pose.translate(left + x, top + y, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(player.getYRot()));
            pose.translate(-4, -4, 2);
            graphics.blit(MAP_ICONS, 0, 0, 40, 0, 8, 8, 128, 128);
        }
    }

    private void doAction(int startX, int endX, int startZ, int endZ, int button) {
        if (startX == 0 && startZ == 0) return;
        ChunkPos startPos = new ChunkPos(startX, startZ);
        ChunkPos endPos = new ChunkPos(endX, endZ);
        if (startPos.equals(endPos)) {
            if (button == 0 && !this.claims.containsKey(startPos)) {
                claim(startPos, hasShiftDown());
            } else if (button == 1 && this.claims.containsKey(startPos) && this.claims.get(startPos).id().equals(this.id)) {
                unclaim(startPos);
            }
        } else {
            if (button == 0) {
                claimArea(startPos, endPos, hasShiftDown());
            } else if (button == 1) {
                unclaimArea(startPos, endPos);
            }
        }
        this.selectionStartX = 0;
        this.selectionStartZ = 0;
        this.selectionEndX = 0;
        this.selectionEndZ = 0;
    }

    private boolean isHovering(double mouseX, double mouseY, float x, float y) {
        return mouseX >= x && mouseX < x + pixelScale && mouseY >= y && mouseY < y + pixelScale;
    }

    private int color(Color color, int alpha) {
        return modifyAlpha(color.getValue(), alpha);
    }

    private int modifyAlpha(int color, int alpha) {
        alpha = Math.min(255, Math.max(0, alpha));
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private Component getName(UUID id, boolean chunkLoad) {
        return Component.translatable("text.cadmus.claimed_by", TeamApi.API.getName(level, id)).withStyle(ChatFormatting.GRAY)
            .append(CommonComponents.SPACE)
            .append(chunkLoad ?
                Component.translatable("text.cadmus.chunk_loaded").withStyle(ChatFormatting.GOLD) :
                CommonComponents.EMPTY
            );
    }

    private int getScaledRenderDistance() {
        int scale = Minecraft.getInstance().options.renderDistance().get() * 16;
        return scale - (scale % 16) + 16;
    }

    public int getMapScale() {
        int scale = Minecraft.getInstance().options.renderDistance().get() * 8;
        return scale - scale % 16 + 16;
    }


    private void claim(ChunkPos pos, boolean chunkLoad) {
        CadmusClient.sendClaimCommand(ClaimCommandType.CLAIM, "%s %s %s".formatted(pos.getMaxBlockX(), pos.getMaxBlockZ(), chunkLoad));
    }

    private void unclaim(ChunkPos pos) {
        CadmusClient.sendClaimCommand(ClaimCommandType.UNCLAIM, "%s %s".formatted(pos.getMaxBlockX(), pos.getMaxBlockZ()));
    }

    private void claimArea(ChunkPos startPos, ChunkPos endPos, boolean chunkLoad) {
        CadmusClient.sendClaimCommand(ClaimCommandType.CLAIM_AREA, "%s %s %s %s %s".formatted(startPos.getMaxBlockX(), startPos.getMaxBlockZ(), endPos.getMaxBlockX(), endPos.getMaxBlockZ(), chunkLoad));
    }

    private void unclaimArea(ChunkPos startPos, ChunkPos endPos) {
        CadmusClient.sendClaimCommand(ClaimCommandType.UNCLAIM_AREA, "%s %s %s %s".formatted(startPos.getMaxBlockX(), startPos.getMaxBlockZ(), endPos.getMaxBlockX(), endPos.getMaxBlockZ()));
    }

    private void unclaimAll() {
        DeleteConfirmModal.open(ConstantComponents.UNCLAIM_MODAL_TITLE, ConstantComponents.UNCLAIM_MODAL_DESCRIPTION, ConstantComponents.UNCLAIM_MODAL_CONFIRM, () -> CadmusClient.sendClaimCommand(ClaimCommandType.UNCLAIM_ALL, ""));
    }

    private static void update() {
        if (!Minecraft.getInstance().isSameThread()) return;
        if (Minecraft.getInstance().screen instanceof ClaimMapScreen screen) {
            screen.refresh();
        }
    }

    public void updateSettings(Map<String, TriState> settings, boolean canModifyColor) {
        this.settings.putAll(settings);
        settingsButton.active = !settings.isEmpty();
        this.canModifyColor = canModifyColor;
    }

    public void updateColor(Color color) {
        this.teamColor = color;
        CadmusClient.TEAM_INFO.put(TeamApi.API.getTeamsList(player), new TeamInfo(CadmusClient.TEAM_INFO.get(id).name(), color));
    }

    public Map<String, TriState> getSettings() {
        return settings;
    }

    private record ClaimTile(
        UUID id,
        Component name,
        int color,
        ChunkPos pos,
        int x, int y,
        boolean north, boolean east,
        boolean south, boolean west,
        boolean northEast, boolean southEast,
        boolean southWest, boolean northWest
    ) {}

    private record TeamData(String name, Color color, int claimed, int maxClaims, int loaded, int maxLoaded) {}

    static {
        CadmusEvents.AddClaimsEvent.register((level, id, positions) -> update());
        CadmusEvents.RemoveClaimsEvent.register((level, id, positions) -> update());
        CadmusEvents.ClearClaimsEvent.register((level, id) -> update());
        CadmusClientEvents.UpdateTeamInfo.register((id, name, color, updateMaps) -> {
            if (updateMaps) update();
        });
    }
}
