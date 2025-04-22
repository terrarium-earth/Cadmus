package earth.terrarium.cadmus.common.protections;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public record SettingsData(Map<String, TriState> settings, boolean canModifyColor) {
    public static ByteCodec<SettingsData> CODEC = ObjectByteCodec.create(
        ByteCodec.mapOf(ByteCodec.STRING, TriState.BYTE_CODEC).fieldOf(SettingsData::settings),
        ByteCodec.BOOLEAN.fieldOf(SettingsData::canModifyColor),
        SettingsData::new
    );

    public static SettingsData of(Player player, TeamId team) {
        return new SettingsData(new HashMap<>(), ModUtils.canModifyColor(player, team) == null);
    }
}
