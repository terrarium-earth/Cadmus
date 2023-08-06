package earth.terrarium.cadmus.common.claims;

import com.teamresourceful.resourcefullib.common.utils.TriState;
import net.minecraft.nbt.CompoundTag;

public record ClaimSettings(
    TriState canBreak,
    TriState canPlace,
    TriState canExplode,
    TriState canInteractWithBlocks,
    TriState canInteractWithEntities,
    TriState canDamageEntities
) {

    public static ClaimSettings ofFalse() {
        return new ClaimSettings(TriState.FALSE, TriState.FALSE, TriState.FALSE, TriState.FALSE, TriState.FALSE, TriState.FALSE);
    }

    public static ClaimSettings ofUndefined() {
        return new ClaimSettings(TriState.UNDEFINED, TriState.UNDEFINED, TriState.UNDEFINED, TriState.UNDEFINED, TriState.UNDEFINED, TriState.UNDEFINED);
    }
    public boolean canBreak(ClaimSettings defaultSettings) {
        if (canBreak.isDefined()) return canBreak.isTrue();
        return defaultSettings.canBreak().isTrue();
    }

    public boolean canPlace(ClaimSettings defaultSettings) {
        if (canPlace.isDefined()) return canPlace.isTrue();
        return defaultSettings.canPlace().isTrue();
    }

    public boolean canExplode(ClaimSettings defaultSettings) {
        if (canExplode.isDefined()) return canExplode.isTrue();
        return defaultSettings.canExplode().isTrue();
    }

    public boolean canInteractWithBlocks(ClaimSettings defaultSettings) {
        if (canInteractWithBlocks.isDefined()) return canInteractWithBlocks.isTrue();
        return defaultSettings.canInteractWithBlocks().isTrue();
    }

    public boolean canInteractWithEntities(ClaimSettings defaultSettings) {
        if (canInteractWithEntities.isDefined()) return canInteractWithEntities.isTrue();
        return defaultSettings.canInteractWithEntities().isTrue();
    }

    public boolean canDamageEntities(ClaimSettings defaultSettings) {
        if (canDamageEntities.isDefined()) return canDamageEntities.isTrue();
        return defaultSettings.canDamageEntities().isTrue();
    }

    public CompoundTag write(CompoundTag tag) {
        tag.putByte("canBreak", (byte) canBreak.ordinal());
        tag.putByte("canPlace", (byte) canPlace.ordinal());
        tag.putByte("canExplode", (byte) canExplode.ordinal());
        tag.putByte("canInteractWithBlocks", (byte) canInteractWithBlocks.ordinal());
        tag.putByte("canInteractWithEntities", (byte) canInteractWithEntities.ordinal());
        tag.putByte("canDamageEntities", (byte) canDamageEntities.ordinal());
        return tag;
    }

    public static ClaimSettings read(CompoundTag tag) {
        return new ClaimSettings(
            getTriState(tag, "canBreak"),
            getTriState(tag, "canPlace"),
            getTriState(tag, "canExplode"),
            getTriState(tag, "canInteractWithBlocks"),
            getTriState(tag, "canInteractWithEntities"),
            getTriState(tag, "canDamageEntities")
        );
    }

    private static TriState getTriState(CompoundTag tag, String key) {
        if (!tag.contains(key)) return TriState.UNDEFINED;
        return TriState.values()[tag.getByte(key)];
    }
}
