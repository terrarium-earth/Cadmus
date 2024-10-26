package earth.terrarium.cadmus.common.flags;

import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.color.ConstantColors;
import earth.terrarium.cadmus.api.flags.Flag;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.flags.types.*;
import net.minecraft.ChatFormatting;

public class Flags {

    public static final StringFlag DISPLAY_NAME = FlagApi.API.register(new StringFlag("display-name", ""));
    public static final ColorFlag COLOR = FlagApi.API.register(new ColorFlag("color", Color.DEFAULT));

    public static final BooleanFlag BLOCK_BREAK = FlagApi.API.register(new BooleanFlag("block-break", true));
    public static final BooleanFlag BLOCK_PLACE = FlagApi.API.register(new BooleanFlag("block-place", true));
    public static final BooleanFlag BLOCK_INTERACTIONS = FlagApi.API.register(new BooleanFlag("block-interactions", true));
    public static final BooleanFlag BLOCK_EXPLOSIONS = FlagApi.API.register(new BooleanFlag("block-explosions", true));
    public static final BooleanFlag ENTITY_EXPLOSIONS = FlagApi.API.register(new BooleanFlag("entity-explosions", true));
    public static final BooleanFlag ENTITY_INTERACTIONS = FlagApi.API.register(new BooleanFlag("entity-interactions", true));
    public static final BooleanFlag ENTITY_DAMAGE = FlagApi.API.register(new BooleanFlag("entity-damage", true));
    public static final BooleanFlag MOB_GRIEFING = FlagApi.API.register(new BooleanFlag("mob-griefing", true));
    public static final BooleanFlag ITEM_PICKUP = FlagApi.API.register(new BooleanFlag("item-pickup", true));

    public static final BooleanFlag FIRE_SPREAD = FlagApi.API.register(new BooleanFlag("fire-spread", true));
    public static final BooleanFlag SNOW_FALL = FlagApi.API.register(new BooleanFlag("snow-fall", true));
    public static final BooleanFlag SNOW_MELT = FlagApi.API.register(new BooleanFlag("snow-melt", true));
    public static final BooleanFlag ICE_FORM = FlagApi.API.register(new BooleanFlag("ice-form", true));
    public static final BooleanFlag ICE_MELT = FlagApi.API.register(new BooleanFlag("ice-melt", true));
    public static final BooleanFlag LEAF_DECAY = FlagApi.API.register(new BooleanFlag("leaf-decay", true));
    public static final BooleanFlag LIGHTNING = FlagApi.API.register(new BooleanFlag("lightning", true));

    public static final BooleanFlag MONSTER_DAMAGE = FlagApi.API.register(new BooleanFlag("monster-damage", true));
    public static final BooleanFlag CREATURE_DAMAGE = FlagApi.API.register(new BooleanFlag("creature-damage", true));
    public static final BooleanFlag PVP = FlagApi.API.register(new BooleanFlag("pvp", true));

    public static final BooleanFlag MONSTER_SPAWNING = FlagApi.API.register(new BooleanFlag("monster-spawning", true));
    public static final BooleanFlag CREATURE_SPAWNING = FlagApi.API.register(new BooleanFlag("creature-spawning", true));

    public static final BooleanFlag KEEP_INVENTORY = FlagApi.API.register(new BooleanFlag("keep-inventory", false));

    public static final BooleanFlag ALLOW_ENTRY = FlagApi.API.register(new BooleanFlag("allow-entry", true));
    public static final BooleanFlag ALLOW_EXIT = FlagApi.API.register(new BooleanFlag("allow-exit", true));

    public static final BooleanFlag USE = FlagApi.API.register(new BooleanFlag("use", true));
    public static final BooleanFlag USE_CHESTS = FlagApi.API.register(new BooleanFlag("use-chests", true));
    public static final BooleanFlag USE_DOORS = FlagApi.API.register(new BooleanFlag("use-doors", true));
    public static final BooleanFlag USE_REDSTONE = FlagApi.API.register(new BooleanFlag("use-redstone", true));
    public static final BooleanFlag USE_VEHICLES = FlagApi.API.register(new BooleanFlag("use-vehicles", true));

    public static final FloatFlag FEED_RATE = FlagApi.API.register(new FloatFlag("feed-rate", 0.0f));
    public static final FloatFlag HEAL_RATE = FlagApi.API.register(new FloatFlag("heal-rate", 0.0f));

    public static final StringFlag ENTRY_DENY_MESSAGE = FlagApi.API.register(new StringFlag("entry-deny-message", ""));
    public static final StringFlag EXIT_DENY_MESSAGE = FlagApi.API.register(new StringFlag("exit-deny-message", ""));
    public static final StringFlag FAREWELL = FlagApi.API.register(new StringFlag("farewell", ""));
    public static final StringFlag GREETING = FlagApi.API.register(new StringFlag("greeting", ""));

    public static void init() {} // NO-OP
}
