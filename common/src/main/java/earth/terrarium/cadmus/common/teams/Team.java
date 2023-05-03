package earth.terrarium.cadmus.common.teams;

import net.minecraft.network.chat.Component;

import java.util.Set;
import java.util.UUID;

public record Team(UUID teamId, UUID creator, Set<UUID> members, String name, Component displayName) {
}
