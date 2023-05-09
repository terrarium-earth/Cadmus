package earth.terrarium.cadmus.common.teams;

import java.util.Set;
import java.util.UUID;

public record Team(UUID teamId, Set<UUID> members, String name) {
}
