package earth.terrarium.cadmus.client.compat.rei;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.CadmusClient;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

@SuppressWarnings("UnstableApiUsage")
public class MapFavoriteEntry extends FavoriteEntry {
    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, Cadmus.MOD_ID);
    private static final ResourceLocation TEXTURE = new ResourceLocation(Cadmus.MOD_ID, "textures/gui/icons/map.png");

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public Renderer getRenderer(boolean showcase) {
        return new Renderer() {
            @Override
            public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
                graphics.pose().pushPose();
                graphics.pose().translate(bounds.getCenterX(), bounds.getCenterY(), 0);
                graphics.pose().scale(bounds.getWidth() / 16f, bounds.getHeight() / 16f, 1);
                graphics.blit(TEXTURE, -8, -8, 0, 0, 16, 16, 16, 16);
                graphics.pose().popPose();
            }

            @Override
            public Tooltip getTooltip(TooltipContext context) {
                return Tooltip.create(context.getPoint(), Component.translatable(ID.toLanguageKey("rei", "tooltip")));
            }
        };
    }

    @Override
    public boolean doAction(int button) {
        if (button == 0) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            CadmusClient.openClaimMap();
            return true;
        }
        return false;
    }

    @Override
    public long hashIgnoreAmount() {
        return 31290831290L;
    }

    @Override
    public FavoriteEntry copy() {
        return this;
    }

    @Override
    public ResourceLocation getType() {
        return ID;
    }

    @Override
    public boolean isSame(FavoriteEntry other) {
        return other instanceof MapFavoriteEntry;
    }

    public enum Type implements FavoriteEntryType<MapFavoriteEntry> {
        INSTANCE;

        @Override
        public DataResult<MapFavoriteEntry> read(CompoundTag object) {
            return DataResult.success(new MapFavoriteEntry(), Lifecycle.stable());
        }

        @Override
        public DataResult<MapFavoriteEntry> fromArgs(Object... args) {
            return DataResult.success(new MapFavoriteEntry(), Lifecycle.stable());
        }

        @Override
        public CompoundTag save(MapFavoriteEntry entry, CompoundTag tag) {
            return tag;
        }
    }
}