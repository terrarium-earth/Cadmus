package earth.terrarium.cadmus.common.team;

import java.util.Set;
import java.util.UUID;

public record Team(UUID teamId, UUID creator, Set<UUID> members, String name) {
    public boolean hasMember(UUID member) {
        return members.contains(member) || creator.equals(member);
    }
}
