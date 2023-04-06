package earth.terrarium.cadmus.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.NotImplementedException;

@Environment(EnvType.CLIENT)
public class ClientPlatformUtils {
    @ExpectPlatform
    public static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreen(MenuType<? extends M> type, ScreenConstructor<M, U> factory) {
        throw new NotImplementedException();
    }

    public interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        U create(T abstractContainerMenu, Inventory inventory, Component component);
    }
}
