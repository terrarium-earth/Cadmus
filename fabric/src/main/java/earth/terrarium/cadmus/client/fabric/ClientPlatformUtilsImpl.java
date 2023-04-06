package earth.terrarium.cadmus.client.fabric;

import earth.terrarium.cadmus.client.ClientPlatformUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ClientPlatformUtilsImpl {
    public static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreen(MenuType<? extends M> type, ClientPlatformUtils.ScreenConstructor<M, U> factory) {
        MenuScreens.register(type, factory::create);
    }
}
