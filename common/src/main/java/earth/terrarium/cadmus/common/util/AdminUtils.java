package earth.terrarium.cadmus.common.util;

import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.commands.claims.AdminClaimHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class AdminUtils {
    public static void preventAdminChunkEntry(ServerPlayer player, ChunkPos lastChunkPos) {
        if (lastChunkPos == null) return;
        if (player.isSpectator() || AdminClaimHandler.<Boolean>getFlag(player.serverLevel(), player.chunkPosition(), ModFlags.ALLOW_ENTRY)) {
            return;
        }

        Component message = AdminClaimHandler.getFlag((ServerLevel) player.level(), player.chunkPosition(), ModFlags.ENTRY_DENY_MESSAGE);
        if (!message.getString().isBlank()) {
            player.displayClientMessage(message.copy().withStyle(ChatFormatting.RED), false);
        }

        BlockPos currentPos = player.blockPosition();
        BlockPos lastPos = lastChunkPos.getMiddleBlockPosition(player.getBlockY());
        BlockPos betweenPos = new BlockPos(
            currentPos.getX() + (lastPos.getX() - currentPos.getX()) / 4,
            currentPos.getY(),
            currentPos.getZ() + (lastPos.getZ() - currentPos.getZ()) / 4
        );

        player.teleportTo(betweenPos.getX(), betweenPos.getY(), betweenPos.getZ());

    }

    public static void preventAdminChunkExit(ServerPlayer player, ChunkPos lastChunkPos) {
        if (lastChunkPos == null) return;
        if (player.isSpectator() || AdminClaimHandler.<Boolean>getFlag(player.serverLevel(), lastChunkPos, ModFlags.ALLOW_EXIT)) {
            return;
        }

        Component message = AdminClaimHandler.getFlag((ServerLevel) player.level(), lastChunkPos, ModFlags.EXIT_DENY_MESSAGE);
        if (!message.getString().isBlank() && AdminClaimHandler.<Component>getFlag((ServerLevel) player.level(), player.chunkPosition(), ModFlags.EXIT_DENY_MESSAGE).getString().isBlank()) {
            player.displayClientMessage(message.copy().withStyle(ChatFormatting.RED), false);
        }

        BlockPos currentPos = player.blockPosition();
        BlockPos lastPos = lastChunkPos.getMiddleBlockPosition(player.getBlockY());
        BlockPos betweenPos = new BlockPos(
            currentPos.getX() + (lastPos.getX() - currentPos.getX()) / 4,
            currentPos.getY(),
            currentPos.getZ() + (lastPos.getZ() - currentPos.getZ()) / 4
        );

        player.teleportTo(betweenPos.getX(), betweenPos.getY(), betweenPos.getZ());
    }

    public static void checkAccess(ServerPlayer player, ChunkPos lastChunkPos) {
        preventAdminChunkEntry(player, lastChunkPos);
        preventAdminChunkExit(player, lastChunkPos);
    }
}
