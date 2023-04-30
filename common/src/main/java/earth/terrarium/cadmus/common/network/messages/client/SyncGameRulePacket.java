package earth.terrarium.cadmus.common.network.messages.client;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.registry.ModGameRules;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SyncGameRulePacket(byte id, boolean val) implements Packet<SyncGameRulePacket> {
    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "sync_game_rule");
    public static final Handler HANDLER = new Handler();

    public static final byte DO_CLAIMED_BLOCK_BREAKING = 0;
    public static final byte DO_CLAIMED_BLOCK_PLACING = 1;
    public static final byte DO_CLAIMED_BLOCK_EXPLOSIONS = 2;
    public static final byte DO_CLAIMED_BLOCK_INTERACTIONS = 3;
    public static final byte DO_CLAIMED_ENTITY_INTERACTIONS = 4;
    public static final byte CLAIMED_DAMAGE_ENTITIES = 5;
    public static final byte CLAIMED_MOB_GRIEFING = 6;
    public static final byte CAN_PICKUP_CLAIMED_ITEMS = 7;

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<SyncGameRulePacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<SyncGameRulePacket> {
        @Override
        public void encode(SyncGameRulePacket packet, FriendlyByteBuf buf) {
            buf.writeByte(packet.id);
            buf.writeBoolean(packet.val);
        }

        @Override
        public SyncGameRulePacket decode(FriendlyByteBuf buf) {
            return new SyncGameRulePacket(buf.readByte(), buf.readBoolean());
        }

        @Override
        public PacketContext handle(SyncGameRulePacket message) {
            return (player, level) -> {
                switch (message.id()) {
                    case DO_CLAIMED_BLOCK_BREAKING ->
                        ModUtils.CLIENT_GAME_RULES.put(ModGameRules.RULE_DO_CLAIMED_BLOCK_BREAKING, message.val());
                    case DO_CLAIMED_BLOCK_PLACING ->
                        ModUtils.CLIENT_GAME_RULES.put(ModGameRules.RULE_DO_CLAIMED_BLOCK_PLACING, message.val());
                    case DO_CLAIMED_BLOCK_EXPLOSIONS ->
                        ModUtils.CLIENT_GAME_RULES.put(ModGameRules.RULE_DO_CLAIMED_BLOCK_EXPLOSIONS, message.val());
                    case DO_CLAIMED_BLOCK_INTERACTIONS ->
                        ModUtils.CLIENT_GAME_RULES.put(ModGameRules.RULE_DO_CLAIMED_BLOCK_INTERACTIONS, message.val());
                    case DO_CLAIMED_ENTITY_INTERACTIONS ->
                        ModUtils.CLIENT_GAME_RULES.put(ModGameRules.RULE_DO_CLAIMED_ENTITY_INTERACTIONS, message.val());
                    case CLAIMED_DAMAGE_ENTITIES ->
                        ModUtils.CLIENT_GAME_RULES.put(ModGameRules.RULE_CLAIMED_DAMAGE_ENTITIES, message.val());
                    case CLAIMED_MOB_GRIEFING ->
                        ModUtils.CLIENT_GAME_RULES.put(ModGameRules.RULE_CLAIMED_MOB_GRIEFING, message.val());
                    case CAN_PICKUP_CLAIMED_ITEMS ->
                        ModUtils.CLIENT_GAME_RULES.put(ModGameRules.RULE_CAN_PICKUP_CLAIMED_ITEMS, message.val());
                }
            };
        }
    }
}
