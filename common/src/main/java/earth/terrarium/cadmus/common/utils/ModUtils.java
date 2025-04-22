package earth.terrarium.cadmus.common.utils;

import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.exceptions.NotImplementedException;
import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import dev.architectury.injectables.annotations.ExpectPlatform;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.protections.ProtectionApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.compat.prometheus.PrometheusCompat;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.clientbound.SyncClaimsPacket;
import earth.terrarium.olympus.client.constants.MinecraftColors;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Contract;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ModUtils {

    private static final int MAX_CHUNKS_PER_PACKET = 500;

    @Contract(pure = true)
    @ExpectPlatform
    public static boolean isMixinModLoaded(String modId) {
        throw new NotImplementedException();
    }

    public static UUID stringToUUID(String string) {
        return UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
    }

    public static Color uuidToColor(UUID id) {
        return MinecraftColors.COLORS[Math.abs(id.hashCode()) % MinecraftColors.COLORS.length];
    }

    /**
     * Sends all claims, packet splitting in batches of {@link #MAX_CHUNKS_PER_PACKET} to the player joining the server.
     */
    public static void sendJoinPackets(ServerPlayer player) {
        CadmusSaveData.addUniquePlayer(player);
        if (!NetworkHandler.CHANNEL.canSendToPlayer(player, SyncClaimsPacket.TYPE)) return;
        for (var level : player.server.getAllLevels()) {
            Object2ObjectMap<TeamId, Object2BooleanMap<ChunkPos>> allClaims = ClaimApi.API.getAllClaimsByOwner(player.serverLevel());
            if (allClaims.isEmpty()) continue;

            Object2ObjectMap<TeamId, Object2BooleanMap<ChunkPos>> batch = new Object2ObjectOpenHashMap<>();
            int count = 0;

            for (var entry : allClaims.entrySet()) {
                batch.put(entry.getKey(), entry.getValue());
                count++;

                if (count == MAX_CHUNKS_PER_PACKET || count == allClaims.size()) {
                    NetworkHandler.CHANNEL.sendToPlayer(new SyncClaimsPacket(level.dimension(), batch), player);
                    batch = new Object2ObjectOpenHashMap<>();
                    count = 0;
                }
            }
        }
    }

    public static Component translatableWithStyle(String key, Object... args) {
        for (int i = 0; i < args.length; ++i) {
            if (!(args[i] instanceof MutableComponent component)) continue;
            if (component.getStyle().getColor() == null) continue;

            ChatFormatting color = ChatFormatting.getByName(component.getStyle().getColor().toString());
            if (color != null) {
                args[i] = "§" + color.getChar() + component.getString();
            }
        }

        return Component.literal(CommonUtils.serverTranslatable(key, args).getString());
    }

    /**
     * Checks if the player has permission to modify the setting. if not, returns the component with the error message.
     *
     * @param player  the player to check
     * @param setting the setting to check
     * @return null if the player has permission, otherwise the component with the error message.
     */
    public static Component canUsePermission(Player player, TeamId id, String setting) {
        if (setting.equals("cadmus.color")) {
            return canModifyColor(player, id);
        }
        var protection = ProtectionApi.API.getProtection(setting);
        if (protection == null) {
            return ConstantComponents.NO_PERMISSION_ROLE;
        }
        if (!player.hasPermissions(2)) {
            if (!TeamApi.API.canModifySettings(player, id)) {
                return ConstantComponents.NO_PERMISSION_TEAM;
            } else if (player.getServer() == null || !PrometheusCompat.hasPermission(player.getServer(), player.getGameProfile(), protection.permission())) {
                return ConstantComponents.NO_PERMISSION_ROLE;
            }
        }
        return null;
    }

    public static Component canModifyColor(Player player, TeamId id) {
        if (!player.hasPermissions(2) && !TeamApi.API.canModifySettings(player, id)) {
            return ConstantComponents.NO_PERMISSION_TEAM;
        }
        return null;
    }
}
