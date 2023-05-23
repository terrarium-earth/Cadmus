package earth.terrarium.cadmus.common.util;

import net.minecraft.network.chat.Component;

public interface LastMessageHolder {

    Component cadmus$getLastMessage();

    void cadmus$setLastMessage(Component message);
}
