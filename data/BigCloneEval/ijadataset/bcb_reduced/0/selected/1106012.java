package org.taddei.jemv.util;

import visad.*;
import java.awt.Color;

/**
 * Provides color tables and color functionality to GVO.
 */
public class Colors {

    private static float[][] eviTable;

    private static float[][] whiteRGBCMYblackTable;

    public Colors() {
    }

    static {
        eviTable = new float[][] { { 255, 199, 178, 166, 147, 126, 151, 119, 82, 61, 28, 0, 0, 0, 0, 0 }, { 255, 186, 149, 131, 114, 156, 182, 170, 148, 134, 115, 96, 71, 56, 35, 21 }, { 255, 167, 105, 77, 63, 44, 19, 3, 0, 0, 0, 0, 0, 0, 0, 0 } };
        normalize(eviTable);
        whiteRGBCMYblackTable = new float[][] { { 1, 1, 0, 0, 0, 1, 1, 0 }, { 1, 0, 1, 0, 1, 0, 1, 0 }, { 1, 0, 0, 1, 1, 1, 0, 0 } };
    }

    /**
  * The standard inverse rainbow (red to blue) table with definable length.
  */
    public static final float[][] getInvRainbowTable(int len) {
        if (len <= 0) {
            System.out.println("Exception: Table length must be greater than 0");
            len = 8;
        }
        float[][] table = new float[3][len];
        for (int i = 0; i < len; i++) {
            float a = ((float) i) / (float) (len - 1);
            table[2][i] = a;
            if ((float) i <= ((float) (len - 1) / 2.0f)) {
                table[1][i] = 2.0f * a;
            } else {
                table[1][i] = 2.0f - 2.0f * a;
            }
            table[0][i] = 1.0f - a;
        }
        return table;
    }

    /**
  * An appropriate color table for water related themes, varying from yellow to blue.
  */
    public static final float[][] getWaterTable(int len) {
        if (len <= 0) {
            System.out.println("Exception: Table length must be greater than 0");
            len = 8;
        }
        float[][] table = new float[3][len];
        for (int i = 0; i < len; i++) {
            float a = ((float) i) / (float) (len - 1);
            table[0][i] = 1.0f - 1.0f * a;
            table[1][i] = 1.0f - 1.0f * a;
            table[2][i] = 1.0f * a;
        }
        return table;
    }

    /**
  * A color table ranging from green to red.
  */
    public static final float[][] greenToRed(int len) {
        if (len <= 0) {
            System.out.println("Exception: Table length must be greater than 0");
            len = 8;
        }
        float[][] table = new float[3][len];
        for (int i = 0; i < len; i++) {
            float a = ((float) i) / (float) (len - 1);
            table[0][i] = a;
            table[1][i] = 1.0f - a;
            table[2][i] = 0.0f;
        }
        return table;
    }

    /**
  * An appropriate color table for water related themes, varying from brown to blue (middle is between yellow and green).
  * Length is fixed at 8.
  */
    public static final float[][] waterQual_8() {
        float[][] table = new float[3][8];
        table[0][0] = 0.60f;
        table[1][0] = 0.40f;
        table[2][0] = 0.0f;
        table[0][1] = 0.80f;
        table[1][1] = 0.60f;
        table[2][1] = 0.400f;
        table[0][2] = 0.90f;
        table[1][2] = 0.80f;
        table[2][2] = 0.40f;
        table[0][3] = 01.0f;
        table[1][3] = 1.0f;
        table[2][3] = 0.20f;
        table[0][4] = 0.0f;
        table[1][4] = 1.0f;
        table[2][4] = 0.0f;
        table[0][5] = 0.0f;
        table[1][5] = 0.80f;
        table[2][5] = 0.80f;
        table[0][6] = 0.0f;
        table[1][6] = 0.60f;
        table[2][6] = 1.0f;
        table[0][7] = 0.0f;
        table[1][7] = 0.0f;
        table[2][7] = 1.0f;
        return table;
    }

    /**
  * A color table for elevation (green, over yellow to brown), with snow, if withSnow is true.
  */
    public static final float[][] getElevation_8(boolean withSnow) {
        int len = 8;
        float[][] elev_8 = new float[3][len];
        elev_8[0][0] = 0.0f;
        elev_8[1][0] = (float) 162 / 255.0f;
        elev_8[2][0] = (float) 119 / 255.0f;
        elev_8[0][1] = (float) 70 / 255.0f;
        elev_8[1][1] = (float) 170 / 255.0f;
        elev_8[2][1] = (float) 130 / 255.0f;
        elev_8[0][2] = (float) 145 / 255.0f;
        elev_8[1][2] = (float) 192 / 255.0f;
        elev_8[2][2] = (float) 146 / 255.0f;
        elev_8[0][3] = (float) 241 / 255.0f;
        elev_8[1][3] = (float) 226 / 255.0f;
        elev_8[2][3] = (float) 174 / 255.0f;
        elev_8[0][4] = (float) 228 / 255.0f;
        elev_8[1][4] = (float) 202 / 255.0f;
        elev_8[2][4] = (float) 143 / 255.0f;
        elev_8[0][5] = (float) 203 / 255.0f;
        elev_8[1][5] = (float) 172 / 255.0f;
        elev_8[2][5] = (float) 107 / 255.0f;
        elev_8[0][6] = (float) 179 / 255.0f;
        elev_8[1][6] = (float) 140 / 255.0f;
        elev_8[2][6] = (float) 66 / 255.0f;
        if (withSnow) {
            elev_8[0][7] = (float) 1.0f;
            elev_8[1][7] = (float) 1.0f;
            elev_8[2][7] = (float) 1.0f;
        } else {
            elev_8[0][7] = (float) 160 / 255.0f;
            elev_8[1][7] = (float) 130 / 255.0f;
            elev_8[2][7] = (float) 62 / 255.0f;
        }
        return elev_8;
    }

    /**
  * A color table for elevation (green, over yellow to brown), with snow, if withSnow is true, length is definable.
  */
    public static final float[][] getElevation_16(boolean withSnow) {
        int len = 16;
        float[][] table = new float[3][len];
        float[][] elev_8 = Colors.getElevation_8(withSnow);
        table[0][0] = elev_8[0][0];
        table[1][0] = elev_8[1][0];
        table[2][0] = elev_8[2][0];
        table[0][len - 1] = elev_8[0][7];
        table[1][len - 1] = elev_8[1][7];
        table[2][len - 1] = elev_8[2][7];
        for (int i = 1; i < (len - 1); i += 2) {
            table[0][i] = (table[0][i - 1] + table[0][i + 1]) / 2.0f;
            table[1][i] = (table[1][i - 1] + table[1][i + 1]) / 2.0f;
            table[2][i] = (table[2][i - 1] + table[2][i + 1]) / 2.0f;
        }
        return table;
    }

    public static final ConstantMap[] getColorMaps(java.awt.Color color) {
        ConstantMap[] cMap = null;
        try {
            float redComp = (float) color.getRed() / 255.0f;
            float greenComp = (float) color.getGreen() / 255.0f;
            float blueComp = (float) color.getBlue() / 255.0f;
            cMap = new ConstantMap[] { new ConstantMap(redComp, Display.Red), new ConstantMap(greenComp, Display.Green), new ConstantMap(blueComp, Display.Blue) };
        } catch (VisADException ve) {
            ve.printStackTrace();
        }
        return cMap;
    }

    public static final ConstantMap[] getPointMaps(java.awt.Color color, float pointSize) {
        ConstantMap[] cMap = null;
        try {
            float redComp = (float) color.getRed() / 255.0f;
            float greenComp = (float) color.getGreen() / 255.0f;
            float blueComp = (float) color.getBlue() / 255.0f;
            cMap = new ConstantMap[] { new ConstantMap(redComp, Display.Red), new ConstantMap(greenComp, Display.Green), new ConstantMap(blueComp, Display.Blue), new ConstantMap(pointSize, Display.PointSize) };
        } catch (VisADException ve) {
            ve.printStackTrace();
        }
        return cMap;
    }

    public static final ConstantMap[] getLineMaps(java.awt.Color color, float lineWidth) {
        ConstantMap[] cMap = null;
        try {
            float redComp = (float) color.getRed() / 255.0f;
            float greenComp = (float) color.getGreen() / 255.0f;
            float blueComp = (float) color.getBlue() / 255.0f;
            cMap = new ConstantMap[] { new ConstantMap(redComp, Display.Red), new ConstantMap(greenComp, Display.Green), new ConstantMap(blueComp, Display.Blue), new ConstantMap(lineWidth, Display.LineWidth) };
        } catch (VisADException ve) {
            ve.printStackTrace();
        }
        return cMap;
    }

    public static final float[] colorToFloatArray(Color c) {
        float[] rgb = new float[] { 0.5f, 0.5f, 0.5f };
        if (c != null) {
            rgb[0] = (float) c.getRed() / 255.0f;
            rgb[1] = (float) c.getGreen() / 255.0f;
            rgb[2] = (float) c.getBlue() / 255.0f;
        }
        return rgb;
    }

    /**
  * The standard rainbow (blue to green to red) table with definable length.
  * Conveniece method for use with method setColorTable(int layer, float[][] colorTable).
  * @param int len length of color table
  */
    public static final float[][] getRainbowTable(int len) {
        if (len <= 0) {
            System.out.println("Exception: Table length must be greater than 0");
            len = 8;
        }
        float[][] table = new float[3][len];
        for (int i = 0; i < len; i++) {
            float a = ((float) i) / (float) (len - 1);
            table[0][i] = a;
            if ((float) i <= ((float) (len - 1) / 2.0f)) {
                table[1][i] = 2.0f * a;
            } else {
                table[1][i] = 2.0f - 2.0f * a;
            }
            table[2][i] = 1.0f - a;
        }
        return table;
    }

    public static final float[][] getSpectrumTable() {
        Color[] c = new Color[] { new Color(238, 130, 238), Color.blue, Color.cyan, Color.green, Color.yellow, Color.orange, Color.red };
        float[][] table = new float[3][c.length];
        for (int i = 0; i < table[0].length; i++) {
            table[0][i] = colorToFloatArray(c[i])[0];
            table[1][i] = colorToFloatArray(c[i])[1];
            table[2][i] = colorToFloatArray(c[i])[2];
        }
        return table;
    }

    public static final float[][] getSkyTable() {
        float[][] skyTable = new float[][] { { 227f, 201f, 175f, 155f, 135f, 104f, 117f, 117f }, { 243f, 232f, 221f, 213f, 206f, 203f, 198f, 198f }, { 251f, 246f, 241f, 238f, 235f, 233f, 232f, 232f } };
        return normalize(skyTable);
    }

    public static final float[][] getTwilightTable() {
        float[][] skyTable = new float[][] { { 255, 255, 255, 235, 153, 82, 38, 0 }, { 164, 102, 51, 31, 0, 1, 1, 0 }, { 1, 0, 0, 89, 153, 127, 97, 0 } };
        return normalize(skyTable);
    }

    public static final float[][] normalize(float[][] tableIn_0_255) {
        for (int i = 0; i < tableIn_0_255.length; i++) {
            for (int j = 0; j < tableIn_0_255[0].length; j++) {
                tableIn_0_255[i][j] = tableIn_0_255[i][j] / 255f;
            }
        }
        return tableIn_0_255;
    }

    /**
  * A color table with dimension float[3][8] for elevation (green, over yellow to brown), with snow, if withSnow is true.
  * Conveniece method for use with setColorTable(int layer, float[][] colorTable) method.
  * @param boolean withSnow makes table top white (as snow)

  Menat to be used with values from 0 to 1
  */
    public static final float[][] getAltiTable(boolean withSnow) {
        int len = 8;
        float[][] elev_8 = new float[3][len];
        elev_8[0][0] = 0.0f;
        elev_8[1][0] = (float) 162 / 255.0f;
        elev_8[2][0] = (float) 119 / 255.0f;
        elev_8[0][1] = (float) 70 / 255.0f;
        elev_8[1][1] = (float) 170 / 255.0f;
        elev_8[2][1] = (float) 130 / 255.0f;
        elev_8[0][2] = (float) 145 / 255.0f;
        elev_8[1][2] = (float) 192 / 255.0f;
        elev_8[2][2] = (float) 146 / 255.0f;
        elev_8[0][3] = (float) 241 / 255.0f;
        elev_8[1][3] = (float) 226 / 255.0f;
        elev_8[2][3] = (float) 174 / 255.0f;
        elev_8[0][4] = (float) 228 / 255.0f;
        elev_8[1][4] = (float) 202 / 255.0f;
        elev_8[2][4] = (float) 143 / 255.0f;
        elev_8[0][5] = (float) 203 / 255.0f;
        elev_8[1][5] = (float) 172 / 255.0f;
        elev_8[2][5] = (float) 107 / 255.0f;
        elev_8[0][6] = (float) 179 / 255.0f;
        elev_8[1][6] = (float) 140 / 255.0f;
        elev_8[2][6] = (float) 66 / 255.0f;
        if (withSnow) {
            elev_8[0][7] = (float) 1.0f;
            elev_8[1][7] = (float) 1.0f;
            elev_8[2][7] = (float) 1.0f;
        } else {
            elev_8[0][7] = (float) 160 / 255.0f;
            elev_8[1][7] = (float) 130 / 255.0f;
            elev_8[2][7] = (float) 62 / 255.0f;
        }
        return elev_8;
    }

    public static final float[][] getEVI_16() {
        float[][] eviTable_2 = new float[][] { { 255, 199, 178, 166, 147, 126, 151, 119, 82, 61, 28, 0, 0, 0, 0, 0 }, { 255, 186, 149, 131, 114, 156, 182, 170, 148, 134, 115, 96, 71, 56, 35, 21 }, { 255, 167, 105, 77, 63, 44, 19, 3, 0, 0, 0, 0, 0, 0, 0, 0 } };
        return eviTable;
    }

    public static final float[][] getWhiteRGBCMYblack() {
        return whiteRGBCMYblackTable;
    }
}
