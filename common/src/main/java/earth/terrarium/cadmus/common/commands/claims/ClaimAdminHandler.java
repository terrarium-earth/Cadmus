package earth.terrarium.cadmus.common.commands.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class ClaimAdminHandler extends SavedData {
//    private final Map<ChunkPos, Pair<String, ClaimType>> claims = new HashMap<>();
//    private final Map<String, Map<ChunkPos, ClaimType>> claimsById = new HashMap<>();

    public ClaimAdminHandler() {
    }

    public ClaimAdminHandler(CompoundTag tag) {
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    public static ClaimAdminHandler read(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ClaimAdminHandler::new, ClaimAdminHandler::new, "cadmus_admin_claims");
    }
}
