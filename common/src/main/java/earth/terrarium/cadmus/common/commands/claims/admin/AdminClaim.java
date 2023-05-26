package earth.terrarium.cadmus.common.commands.claims.admin;

import earth.terrarium.cadmus.api.claims.admin.flags.Flag;
import net.minecraft.network.chat.Component;

import java.util.Map;

public record AdminClaim(Component displayName, Map<String, Flag<?>> flags) {
}
