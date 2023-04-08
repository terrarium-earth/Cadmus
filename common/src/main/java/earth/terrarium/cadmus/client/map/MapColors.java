package earth.terrarium.cadmus.client.map;

import net.minecraft.world.level.material.MaterialColor;

public class MapColors {
    public static final int[][] COLORS = new int[][]{
            {0, 0, 0, 0}, // none
            {0x5d9144, 0x3a771b, 0x386024, 0x24440f} // grass
    };

    public static int getColor(int index, MaterialColor.Brightness brightness) {
        if (index >= COLORS.length) {
            return MaterialColor.getColorFromPackedId(MaterialColor.byId(index).getPackedId(brightness));
        }
        return COLORS[index][brightness.ordinal()];
    }

    // Add alpha to all the colors after since it's RGBA
    static {
        for (int i = 0; i < COLORS.length; i++) {
            for (int j = 0; j < COLORS[i].length; j++) {
                COLORS[i][j] |= 0xff000000;
            }
        }
    }
}
