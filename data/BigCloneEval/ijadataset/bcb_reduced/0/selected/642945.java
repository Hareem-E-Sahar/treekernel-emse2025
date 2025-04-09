package org.jmol.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3f;
import org.jmol.viewer.JmolConstants;
import org.jmol.constant.EnumPalette;
import org.jmol.g3d.Graphics3D;

public class ColorEncoder {

    public ColorEncoder(ColorEncoder propertyColorEncoder) {
        if (propertyColorEncoder == null) {
            schemes = new Hashtable<String, int[]>();
            argbsCpk = EnumPalette.argbsCpk;
            argbsRoygb = JmolConstants.argbsRoygbScale;
            argbsRwb = JmolConstants.argbsRwbScale;
            argbsShapely = JmolConstants.argbsShapely;
            argbsAmino = JmolConstants.argbsAmino;
            ihalf = JmolConstants.argbsRoygbScale.length / 3;
            this.propertyColorEncoder = this;
        } else {
            this.propertyColorEncoder = propertyColorEncoder;
            schemes = propertyColorEncoder.schemes;
        }
    }

    private static final int GRAY = 0xFF808080;

    public static final String BYELEMENT_PREFIX = "byelement";

    public static final String BYRESIDUE_PREFIX = "byresidue";

    private static final String BYELEMENT_JMOL = BYELEMENT_PREFIX + "_jmol";

    private static final String BYELEMENT_RASMOL = BYELEMENT_PREFIX + "_rasmol";

    private static final String BYRESIDUE_SHAPELY = BYRESIDUE_PREFIX + "_shapely";

    private static final String BYRESIDUE_AMINO = BYRESIDUE_PREFIX + "_amino";

    public static final int CUSTOM = -1;

    public static final int ROYGB = 0;

    public static final int BGYOR = 1;

    public static final int JMOL = 2;

    public static final int RASMOL = 3;

    public static final int SHAPELY = 4;

    public static final int AMINO = 5;

    public static final int RWB = 6;

    public static final int BWR = 7;

    public static final int LOW = 8;

    public static final int HIGH = 9;

    public static final int BW = 10;

    public static final int WB = 11;

    public static final int USER = -12;

    public static final int RESU = -13;

    public static final int ALT = 14;

    private static final String[] colorSchemes = { "roygb", "bgyor", BYELEMENT_JMOL, BYELEMENT_RASMOL, BYRESIDUE_SHAPELY, BYRESIDUE_AMINO, "rwb", "bwr", "low", "high", "bw", "wb", "user", "resu", "rgb", "bgr", "jmol", "rasmol", BYRESIDUE_PREFIX };

    private static final int getSchemeIndex(String colorScheme) {
        for (int i = 0; i < colorSchemes.length; i++) if (colorSchemes[i].equalsIgnoreCase(colorScheme)) return (i >= ALT ? i - ALT : i < -USER ? i : -i);
        return CUSTOM;
    }

    private static final String fixName(String name) {
        if (name.equalsIgnoreCase(BYELEMENT_PREFIX)) return BYELEMENT_JMOL;
        int ipt = getSchemeIndex(name);
        return (ipt >= 0 ? colorSchemes[ipt] : name.toLowerCase());
    }

    private int[] paletteBW;

    private int[] paletteWB;

    private int[] argbsCpk;

    private int[] argbsRoygb;

    private int[] argbsRwb;

    private int[] argbsShapely;

    private int[] argbsAmino;

    private int ihalf;

    private static int[] rasmolScale;

    public Map<String, int[]> schemes;

    public int currentPalette = ROYGB;

    public int currentSegmentCount = 1;

    public boolean isTranslucent = false;

    public float lo;

    public float hi;

    public boolean isReversed;

    int[] userScale = new int[] { GRAY };

    int[] thisScale = new int[] { GRAY };

    String thisName = "scheme";

    boolean isColorIndex;

    ColorEncoder propertyColorEncoder;

    /**
   * 
   * @param name
   * @param scale  if null, then this is a reset.
   * @param isOverloaded  if TRUE, 
   * @return  >= 0 for a default color scheme
   */
    private synchronized int makeColorScheme(String name, int[] scale, boolean isOverloaded) {
        name = fixName(name);
        if (scale == null) {
            schemes.remove(name);
            int iScheme = getColorScheme(name, false, isOverloaded);
            if (isOverloaded) switch(iScheme) {
                case BW:
                    paletteBW = getPaletteBW();
                    break;
                case WB:
                    paletteWB = getPaletteWB();
                    break;
                case ROYGB:
                case BGYOR:
                    argbsRoygb = JmolConstants.argbsRoygbScale;
                    break;
                case RWB:
                case BWR:
                    argbsRwb = JmolConstants.argbsRwbScale;
                    break;
                case JMOL:
                    argbsCpk = EnumPalette.argbsCpk;
                    break;
                case RASMOL:
                    getRasmolScale();
                    break;
                case AMINO:
                    argbsAmino = JmolConstants.argbsAmino;
                    break;
                case SHAPELY:
                    argbsShapely = JmolConstants.argbsShapely;
                    break;
            }
            return (iScheme == Integer.MAX_VALUE ? ROYGB : iScheme);
        }
        schemes.put(name, scale);
        setThisScheme(name, scale);
        int iScheme = getColorScheme(name, false, isOverloaded);
        if (isOverloaded) switch(iScheme) {
            case BW:
                paletteBW = thisScale;
                break;
            case WB:
                paletteWB = thisScale;
                break;
            case ROYGB:
            case BGYOR:
                argbsRoygb = thisScale;
                ihalf = argbsRoygb.length / 3;
                break;
            case RWB:
            case BWR:
                argbsRwb = thisScale;
                break;
            case JMOL:
                argbsCpk = thisScale;
                break;
            case RASMOL:
                break;
            case AMINO:
                argbsAmino = thisScale;
                break;
            case SHAPELY:
                argbsShapely = thisScale;
                break;
        }
        return CUSTOM;
    }

    /**
   * 
   * @param colorScheme    name or name= or name=[x......] [x......] ....
   * @param defaultToRoygb
   * @param isOverloaded
   * @return paletteID
   */
    public int getColorScheme(String colorScheme, boolean defaultToRoygb, boolean isOverloaded) {
        colorScheme = colorScheme.toLowerCase();
        int pt = Math.max(colorScheme.indexOf("="), colorScheme.indexOf("["));
        if (pt >= 0) {
            String name = TextFormat.replaceAllCharacters(colorScheme.substring(0, pt), " =", "");
            if (name.length() > 0) isOverloaded = true;
            int n = 0;
            if (!colorScheme.contains("[")) {
                colorScheme = "[" + colorScheme.substring(pt + 1).trim() + "]";
                colorScheme = TextFormat.simpleReplace(colorScheme.replace('\n', ' '), "  ", " ");
                colorScheme = TextFormat.simpleReplace(colorScheme, ", ", ",").replace(' ', ',');
                colorScheme = TextFormat.simpleReplace(colorScheme, ",", "][");
            }
            pt = -1;
            while ((pt = colorScheme.indexOf("[", pt + 1)) >= 0) n++;
            if (n == 0) return makeColorScheme(name, null, isOverloaded);
            int[] scale = new int[n];
            n = 0;
            while ((pt = colorScheme.indexOf("[", pt + 1)) >= 0) {
                int pt2 = colorScheme.indexOf("]", pt);
                if (pt2 < 0) pt2 = colorScheme.length() - 1;
                int c = ColorUtil.getArgbFromString(colorScheme.substring(pt, pt2 + 1));
                if (c == 0) c = ColorUtil.getArgbFromString(colorScheme.substring(pt + 1, pt2).trim());
                if (c == 0) {
                    Logger.error("error in color value: " + colorScheme.substring(pt, pt2 + 1));
                    return ROYGB;
                }
                scale[n++] = c;
            }
            if (name.equals("user")) {
                setUserScale(scale);
                return USER;
            }
            return makeColorScheme(name, scale, isOverloaded);
        }
        colorScheme = fixName(colorScheme);
        int ipt = getSchemeIndex(colorScheme);
        if (schemes.containsKey(colorScheme)) {
            setThisScheme(colorScheme, schemes.get(colorScheme));
            return ipt;
        }
        return (ipt != CUSTOM ? ipt : defaultToRoygb ? ROYGB : Integer.MAX_VALUE);
    }

    public void setUserScale(int[] scale) {
        propertyColorEncoder.userScale = scale;
        makeColorScheme("user", scale, false);
    }

    public int[] getColorSchemeArray(int palette) {
        int[] b;
        switch(palette) {
            case CUSTOM:
                return thisScale;
            case ROYGB:
                return propertyColorEncoder.argbsRoygb;
            case BGYOR:
                return ArrayUtil.arrayCopy(propertyColorEncoder.argbsRoygb, 0, -1, true);
            case LOW:
                return ArrayUtil.arrayCopy(propertyColorEncoder.argbsRoygb, 0, propertyColorEncoder.ihalf, false);
            case HIGH:
                int[] a = ArrayUtil.arrayCopy(propertyColorEncoder.argbsRoygb, propertyColorEncoder.argbsRoygb.length - 2 * propertyColorEncoder.ihalf, -1, false);
                b = new int[propertyColorEncoder.ihalf];
                for (int i = b.length, j = a.length; --i >= 0 && --j >= 0; ) b[i] = a[j--];
                return b;
            case BW:
                return getPaletteBW();
            case WB:
                return getPaletteWB();
            case RWB:
                return propertyColorEncoder.argbsRwb;
            case BWR:
                return ArrayUtil.arrayCopy(propertyColorEncoder.argbsRwb, 0, -1, true);
            case JMOL:
                return propertyColorEncoder.argbsCpk;
            case RASMOL:
                return getRasmolScale();
            case SHAPELY:
                return propertyColorEncoder.argbsShapely;
            case AMINO:
                return propertyColorEncoder.argbsAmino;
            case USER:
                return propertyColorEncoder.userScale;
            case RESU:
                return ArrayUtil.arrayCopy(propertyColorEncoder.userScale, 0, -1, true);
            default:
                return null;
        }
    }

    public short getColorIndexFromPalette(float val, float lo, float hi, int palette, boolean isTranslucent) {
        short colix = Graphics3D.getColix(getArgbFromPalette(val, lo, hi, palette));
        if (isTranslucent) {
            float f = (hi - val) / (hi - lo);
            if (f > 1) f = 1; else if (f < 0.125f) f = 0.125f;
            colix = Graphics3D.getColixTranslucent(colix, true, f);
        }
        return colix;
    }

    public int getPaletteColorCount(int palette) {
        switch(palette) {
            case CUSTOM:
                return thisScale.length;
            case BW:
            case WB:
                getPaletteBW();
                return propertyColorEncoder.paletteBW.length;
            case ROYGB:
            case BGYOR:
                return propertyColorEncoder.argbsRoygb.length;
            case LOW:
            case HIGH:
                return propertyColorEncoder.ihalf;
            case RWB:
            case BWR:
                return propertyColorEncoder.argbsRwb.length;
            case USER:
            case RESU:
                return propertyColorEncoder.userScale.length;
            case JMOL:
                return argbsCpk.length;
            case RASMOL:
                return getRasmolScale().length;
            case SHAPELY:
                return propertyColorEncoder.argbsShapely.length;
            case AMINO:
                return propertyColorEncoder.argbsAmino.length;
            default:
                return 0;
        }
    }

    public int getArgbFromPalette(float val, float lo, float hi, int palette) {
        if (Float.isNaN(val)) return GRAY;
        int n = getPaletteColorCount(palette);
        switch(palette) {
            case CUSTOM:
                if (isColorIndex) {
                    lo = 0;
                    hi = thisScale.length;
                }
                return thisScale[quantize(val, lo, hi, n)];
            case BW:
                return getPaletteBW()[quantize(val, lo, hi, n)];
            case WB:
                return getPaletteWB()[quantize(val, lo, hi, n)];
            case ROYGB:
                return propertyColorEncoder.argbsRoygb[quantize(val, lo, hi, n)];
            case BGYOR:
                return propertyColorEncoder.argbsRoygb[quantize(-val, -hi, -lo, n)];
            case LOW:
                return propertyColorEncoder.argbsRoygb[quantize(val, lo, hi, n)];
            case HIGH:
                return propertyColorEncoder.argbsRoygb[propertyColorEncoder.ihalf + quantize(val, lo, hi, n) * 2];
            case RWB:
                return propertyColorEncoder.argbsRwb[quantize(val, lo, hi, n)];
            case BWR:
                return propertyColorEncoder.argbsRwb[quantize(-val, -hi, -lo, n)];
            case USER:
                return (propertyColorEncoder.userScale.length == 0 ? GRAY : propertyColorEncoder.userScale[quantize(val, lo, hi, n)]);
            case RESU:
                return (propertyColorEncoder.userScale.length == 0 ? GRAY : propertyColorEncoder.userScale[quantize(-val, -hi, -lo, n)]);
            case JMOL:
                return propertyColorEncoder.argbsCpk[colorIndex((int) val, n)];
            case RASMOL:
                return getRasmolScale()[colorIndex((int) val, n)];
            case SHAPELY:
                return propertyColorEncoder.argbsShapely[colorIndex((int) val, n)];
            case AMINO:
                return propertyColorEncoder.argbsAmino[colorIndex((int) val, n)];
            default:
                return GRAY;
        }
    }

    private void setThisScheme(String name, int[] scale) {
        thisName = name;
        thisScale = scale;
        if (name.equals("user")) userScale = scale;
        isColorIndex = (name.indexOf(BYELEMENT_PREFIX) == 0 || name.indexOf(BYRESIDUE_PREFIX) == 0);
    }

    public int getArgb(float val) {
        return (isReversed ? getArgbFromPalette(-val, -hi, -lo, currentPalette) : getArgbFromPalette(val, lo, hi, currentPalette));
    }

    public short getColorIndex(float val) {
        return (isReversed ? getColorIndexFromPalette(-val, -hi, -lo, currentPalette, isTranslucent) : getColorIndexFromPalette(val, lo, hi, currentPalette, isTranslucent));
    }

    public Map<String, Object> getColorKey() {
        Map<String, Object> info = new Hashtable<String, Object>();
        int segmentCount = getPaletteColorCount(currentPalette);
        List<Point3f> colors = new ArrayList<Point3f>(segmentCount);
        float[] values = new float[segmentCount + 1];
        float quantum = (hi - lo) / segmentCount;
        float f = quantum * (isReversed ? -0.5f : 0.5f);
        for (int i = 0; i < segmentCount; i++) {
            values[i] = (isReversed ? hi - i * quantum : lo + i * quantum);
            colors.add(ColorUtil.colorPointFromInt2(getArgb(values[i] + f)));
        }
        values[segmentCount] = (isReversed ? lo : hi);
        info.put("values", values);
        info.put("colors", colors);
        info.put("min", Float.valueOf(lo));
        info.put("max", Float.valueOf(hi));
        info.put("reversed", Boolean.valueOf(isReversed));
        info.put("name", getColorSchemeName());
        return info;
    }

    /**
   * 
   * @param colorScheme
   * @param isTranslucent
   */
    public void setColorScheme(String colorScheme, boolean isTranslucent) {
        this.isTranslucent = isTranslucent;
        if (colorScheme != null) currentPalette = getColorScheme(colorScheme, true, false);
    }

    public void setRange(float lo, float hi, boolean isReversed) {
        if (hi == Float.MAX_VALUE) {
            lo = 1;
            hi = getPaletteColorCount(currentPalette) + 1;
        }
        this.lo = Math.min(lo, hi);
        this.hi = Math.max(lo, hi);
        this.isReversed = isReversed;
    }

    public String getColorSchemeName() {
        return getColorSchemeName(currentPalette);
    }

    public String getColorSchemeName(int i) {
        int absi = Math.abs(i);
        return (i == CUSTOM ? thisName : absi < colorSchemes.length && absi >= 0 ? colorSchemes[absi] : null);
    }

    public static final String getColorSchemeList(int[] scheme) {
        if (scheme == null) return "";
        String colors = "";
        for (int i = 0; i < scheme.length; i++) colors += (i == 0 ? "" : " ") + Escape.escapeColor(scheme[i]);
        return colors;
    }

    public static final synchronized int[] getRasmolScale() {
        if (rasmolScale != null) return rasmolScale;
        rasmolScale = new int[EnumPalette.argbsCpk.length];
        int argb = EnumPalette.argbsCpkRasmol[0] | 0xFF000000;
        for (int i = rasmolScale.length; --i >= 0; ) rasmolScale[i] = argb;
        for (int i = EnumPalette.argbsCpkRasmol.length; --i >= 0; ) {
            argb = EnumPalette.argbsCpkRasmol[i];
            rasmolScale[argb >> 24] = argb | 0xFF000000;
        }
        return rasmolScale;
    }

    private int[] getPaletteWB() {
        if (propertyColorEncoder.paletteWB != null) return propertyColorEncoder.paletteWB;
        int[] b = new int[JmolConstants.argbsRoygbScale.length];
        for (int i = 0; i < b.length; i++) {
            float xff = (1f / b.length * (b.length - i));
            b[i] = ColorUtil.colorTriadToInt(xff, xff, xff);
        }
        return propertyColorEncoder.paletteWB = b;
    }

    public static int[] getPaletteAtoB(int color1, int color2, int n) {
        if (n < 2) n = JmolConstants.argbsRoygbScale.length;
        int[] b = new int[n];
        float red1 = (((color1 & 0xFF0000) >> 16) & 0xFF) / 255f;
        float green1 = (((color1 & 0xFF00) >> 8) & 0xFF) / 255f;
        float blue1 = (color1 & 0xFF) / 255f;
        float red2 = (((color2 & 0xFF0000) >> 16) & 0xFF) / 255f;
        float green2 = (((color2 & 0xFF00) >> 8) & 0xFF) / 255f;
        float blue2 = (color2 & 0xFF) / 255f;
        float dr = (red2 - red1) / (n - 1);
        float dg = (green2 - green1) / (n - 1);
        float db = (blue2 - blue1) / (n - 1);
        for (int i = 0; i < n; i++) b[i] = ColorUtil.colorTriadToInt(red1 + dr * i, green1 + dg * i, blue1 + db * i);
        return b;
    }

    private int[] getPaletteBW() {
        if (propertyColorEncoder.paletteBW != null) return propertyColorEncoder.paletteBW;
        int[] b = new int[JmolConstants.argbsRoygbScale.length];
        for (int i = 0; i < b.length; i++) {
            float xff = (1f / b.length * i);
            b[i] = ColorUtil.colorTriadToInt(xff, xff, xff);
        }
        return propertyColorEncoder.paletteBW = b;
    }

    /**
   * gets the value at the color boundary for this color range fraction 
   * @param x
   * @param isLowEnd
   * @return quantized value
   */
    public float quantize(float x, boolean isLowEnd) {
        int n = getPaletteColorCount(currentPalette);
        x = (((int) (x * n)) + (isLowEnd ? 0f : 1f)) / n;
        return (x <= 0 ? lo : x >= 1 ? hi : lo + (hi - lo) * x);
    }

    public static final int quantize(float val, float lo, float hi, int segmentCount) {
        float range = hi - lo;
        if (range <= 0 || Float.isNaN(val)) return segmentCount / 2;
        float t = val - lo;
        if (t <= 0) return 0;
        float quanta = range / segmentCount;
        int q = (int) (t / quanta + 0.0001f);
        if (q >= segmentCount) q = segmentCount - 1;
        return q;
    }

    private static final int colorIndex(int q, int segmentCount) {
        return (q <= 0 | q >= segmentCount ? 0 : q);
    }

    public int getState(StringBuffer s) {
        int n = 0;
        for (Map.Entry<String, int[]> entry : schemes.entrySet()) {
            String name = entry.getKey();
            if (name.length() > 0 & n++ >= 0) s.append("color \"" + name + "=" + getColorSchemeList(entry.getValue()) + "\";\n");
        }
        return n;
    }

    public String getColorScheme() {
        return (isTranslucent ? "translucent " : "") + (currentPalette < 0 ? getColorSchemeList(getColorSchemeArray(currentPalette)) : getColorSchemeName(currentPalette));
    }

    public static void RGBtoHSL(float r, float g, float b, float[] ret) {
        r /= 255;
        g /= 255;
        b /= 255;
        if (r > 1) r = 1;
        if (g > 1) g = 1;
        if (b > 1) b = 1;
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        float h = 0;
        if (max == min) h = 0; else if (max == r) h = ((60 * (g - b) / (max - min)) + 360) % 360; else if (max == g) h = (60 * (b - r) / (max - min)) + 120; else if (max == b) h = (60 * (r - g) / (max - min)) + 240;
        float l = (max + min) / 2;
        float s = 0;
        if (max == min) s = 0; else if (l <= .5f) s = (max - min) / (max + min); else s = (max - min) / (2 - max - min);
        ret[0] = h / 360;
        ret[1] = s;
        ret[2] = l;
    }
}
