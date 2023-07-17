package earth.terrarium.cadmus.client.compat.rei;

import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import net.minecraft.network.chat.Component;

@SuppressWarnings("UnstableApiUsage")
public class CadmusReiClientPlugin implements REIClientPlugin {

    @Override
    public void registerFavorites(FavoriteEntryType.Registry registry) {
        registry.register(MapFavoriteEntry.ID, MapFavoriteEntry.Type.INSTANCE);
        registry.getOrCrateSection(Component.translatable("rei.sections.odyssey"))
            .add(new MapFavoriteEntry());
    }
}