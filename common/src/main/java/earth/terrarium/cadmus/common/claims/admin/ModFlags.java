package earth.terrarium.cadmus.common.claims.admin;

import earth.terrarium.cadmus.api.claims.admin.FlagApi;
import earth.terrarium.cadmus.api.claims.admin.flags.BooleanFlag;
import earth.terrarium.cadmus.api.claims.admin.flags.ComponentFlag;
import earth.terrarium.cadmus.api.claims.admin.flags.FloatFlag;
import net.minecraft.network.chat.Component;

public class ModFlags {
    public static final String ALLOW_ENTRY = "allow-entry";
    public static final String ALLOW_EXIT = "allow-exit";
    public static final String BLOCK_BREAK = "block-break";
    public static final String BLOCK_EXPLOSIONS = "block-explosions";
    public static final String BLOCK_INTERACTIONS = "block-interactions";
    public static final String BLOCK_PLACE = "block-place";
    public static final String CREATURE_DAMAGE = "creature-damage";
    public static final String CREATURE_SPAWNING = "creature-spawning";
    public static final String ENTITY_DAMAGE = "entity-damage";
    public static final String ENTITY_INTERACTIONS = "entity-interactions";
    public static final String FIRE_SPREAD = "fire-spread";
    public static final String MONSTER_SPAWNING = "monster-spawning";
    public static final String ICE_FORM = "ice-form";
    public static final String ICE_MELT = "ice-melt";
    public static final String ITEM_PICKUP = "item-pickup";
    public static final String LEAF_DECAY = "leaf-decay";
    public static final String LIGHTNING = "lightning";
    public static final String MOB_GRIEFING = "mob-griefing";
    public static final String MOB_SPAWNING = "mob-spawning";
    public static final String MONSTER_DAMAGE = "monster-damage";
    public static final String PVP = "pvp";
    public static final String SNOW_FALL = "snow-fall";
    public static final String SNOW_MELT = "snow-melt";
    public static final String USE = "use";
    public static final String USE_CHESTS = "use-chests";
    public static final String USE_DOORS = "use-doors";
    public static final String USE_REDSTONE = "use-redstone";
    public static final String USE_VEHICLES = "use-vehicles";

    public static final String FEED_RATE = "feed-rate";
    public static final String HEAL_RATE = "heal-rate";

    public static final String DISPLAY_NAME = "display-name";
    public static final String ENTRY_DENY_MESSAGE = "entry-deny-message";
    public static final String EXIT_DENY_MESSAGE = "exit-deny-message";
    public static final String FAREWELL = "farewell";
    public static final String GREETING = "greeting";

    public static void init() {
        // Boolean Flags
        FlagApi api = FlagApi.API;
        api.register(ALLOW_ENTRY, new BooleanFlag(true));
        api.register(ALLOW_EXIT, new BooleanFlag(true));
        api.register(BLOCK_BREAK, new BooleanFlag(true));
        api.register(BLOCK_EXPLOSIONS, new BooleanFlag(true));
        api.register(BLOCK_INTERACTIONS, new BooleanFlag(true));
        api.register(BLOCK_PLACE, new BooleanFlag(true));
        api.register(CREATURE_DAMAGE, new BooleanFlag(true));
        api.register(CREATURE_SPAWNING, new BooleanFlag(true));
        api.register(ENTITY_DAMAGE, new BooleanFlag(true));
        api.register(ENTITY_INTERACTIONS, new BooleanFlag(true));
        api.register(FIRE_SPREAD, new BooleanFlag(true));
        api.register(MONSTER_SPAWNING, new BooleanFlag(true));
        api.register(ICE_FORM, new BooleanFlag(true));
        api.register(ICE_MELT, new BooleanFlag(true));
        api.register(ITEM_PICKUP, new BooleanFlag(true));
        api.register(LEAF_DECAY, new BooleanFlag(true));
        api.register(LIGHTNING, new BooleanFlag(true));
        api.register(MOB_GRIEFING, new BooleanFlag(true));
        api.register(MOB_SPAWNING, new BooleanFlag(true));
        api.register(MONSTER_DAMAGE, new BooleanFlag(true));
        api.register(PVP, new BooleanFlag(true));
        api.register(SNOW_FALL, new BooleanFlag(true));
        api.register(SNOW_MELT, new BooleanFlag(true));
        api.register(USE, new BooleanFlag(true));
        api.register(USE_CHESTS, new BooleanFlag(true));
        api.register(USE_DOORS, new BooleanFlag(true));
        api.register(USE_REDSTONE, new BooleanFlag(true));
        api.register(USE_VEHICLES, new BooleanFlag(true));

        // Float Flags
        api.register(FEED_RATE, new FloatFlag(0.0f));
        api.register(HEAL_RATE, new FloatFlag(0.0f));

        // Component Flags
        api.register(DISPLAY_NAME, new ComponentFlag(Component.empty()));
        api.register(ENTRY_DENY_MESSAGE, new ComponentFlag(Component.empty()));
        api.register(EXIT_DENY_MESSAGE, new ComponentFlag(Component.empty()));
        api.register(FAREWELL, new ComponentFlag(Component.empty()));
        api.register(GREETING, new ComponentFlag(Component.empty()));
    }
}
