package color;

import java.awt.Color;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;
import css.PrimitiveAbstractValue;

/**
 * HSLColor store a color with 2 representation :
 * <ul>
 * <li>the RGB code,</li>
 * <li>the HSL value.</li>
 * </ul>
 * The color can be modified.
 * 
 * @author Desprez Jean-Marc
 * 
 */
public class HSLColor extends PrimitiveAbstractValue implements Comparable<HSLColor> {

    private static final StringBuilder BUILDER = new StringBuilder();

    private static final String REGEX_HEX = "#?[0-9a-fA-F]{6}";

    private static final String REGEX_RGB = "[rR][gG][bB]\\( *[0-9]{1,3} *, *[0-9]{1,3} *, *[0-9]{1,3} *\\)";

    private static final String REGEX_SHEX = "#[0-9a-fA-F]{3}";

    private static final int WEBSAFE_PAD = 51;

    private Color awt;

    private int color;

    private double hue;

    private boolean isWebSafe;

    private double lightness;

    private String name;

    private double saturation;

    private static double validateDegree(final double valeur) {
        double inRange = valeur;
        while (inRange < 0) {
            inRange++;
        }
        while (inRange >= 1) {
            inRange--;
        }
        return inRange;
    }

    private static double validateRange(final double valeur) {
        if (valeur < 0) {
            return 0;
        }
        if (valeur > 1) {
            return 1;
        }
        return valeur;
    }

    private static int validateRange(final int valeur) {
        if (valeur < 0) {
            return 0;
        }
        if (valeur > 255) {
            return 255;
        }
        return valeur;
    }

    /**
   * Utility function to add the hex value of a color to a StringBuilder. The
   * hex code will always contain 6 char.
   * 
   * @param currentColor
   *          the color to add.
   * @param sb
   *          StringBuilder where the hex code will be add.
   */
    public static void appendHexColor(final int currentColor, final StringBuilder sb) {
        String hexString = Integer.toHexString(currentColor);
        for (int i = hexString.length(); i < 6; i++) {
            sb.append('0');
        }
        sb.append(hexString);
    }

    /**
   * Take a css representation of a color and return a hex code in upper case.
   * Supported input format :
   * <ul>
   * <li>long hex format : #000000,</li>
   * <li>short hex format : #000,</li>
   * <li>rgb value : rgb(0,0,0),</li>
   * <li>named color : black.</li>
   * </ul>
   * 
   * @param cssText
   *          the string to convert.
   * @return cssText convert to hex value or null if the input String cannot be
   *         converted.
   */
    public static String convertToHex(final String cssText) {
        if (cssText == null) {
            return null;
        }
        if (cssText.matches(REGEX_HEX)) {
            if (cssText.startsWith("#")) {
                return cssText.substring(1).toUpperCase();
            }
            return cssText.toUpperCase();
        }
        if (cssText.matches(REGEX_SHEX)) {
            BUILDER.setLength(0);
            BUILDER.append(cssText.charAt(1)).append(cssText.charAt(1));
            BUILDER.append(cssText.charAt(2)).append(cssText.charAt(2));
            BUILDER.append(cssText.charAt(3)).append(cssText.charAt(3));
            return BUILDER.toString().toUpperCase();
        }
        if (cssText.matches(REGEX_RGB)) {
            String[] colors = cssText.substring(cssText.indexOf("(") + 1, cssText.indexOf(")")).split(",");
            BUILDER.setLength(0);
            appendHexColor((Integer.parseInt(colors[0].trim()) << 16) | (Integer.parseInt(colors[1].trim()) << 8) | Integer.parseInt(colors[2].trim()), BUILDER);
            return BUILDER.toString().toUpperCase();
        }
        String toLower = cssText.toLowerCase();
        if (toLower.equals("aqua")) {
            return "00FFFF";
        }
        if (toLower.equals("black")) {
            return "000000";
        }
        if (toLower.equals("blue")) {
            return "0000FF";
        }
        if (toLower.equals("fuchsia")) {
            return "FF00FF";
        }
        if (toLower.equals("gray")) {
            return "808080";
        }
        if (toLower.equals("green")) {
            return "008000";
        }
        if (toLower.equals("lime")) {
            return "00FF00";
        }
        if (toLower.equals("maroon")) {
            return "800000";
        }
        if (toLower.equals("navy")) {
            return "000080";
        }
        if (toLower.equals("olive")) {
            return "808000";
        }
        if (toLower.equals("purple")) {
            return "800080";
        }
        if (toLower.equals("red")) {
            return "FF0000";
        }
        if (toLower.equals("silver")) {
            return "C0C0C0";
        }
        if (toLower.equals("teal")) {
            return "008080";
        }
        if (toLower.equals("white")) {
            return "FFFFFF";
        }
        if (toLower.equals("yellow")) {
            return "FFFF00";
        }
        return null;
    }

    /**
   * Return the closest webSafe value for a number between 0 and 255. If the
   * number is out of bound, 0 or 255 will be returned.
   * 
   * @param i
   *          the real value.
   * @return the closest webSafe value.
   */
    public static int getWebSafeValue(final int i) {
        if (i <= 0) {
            return 0;
        }
        if (i >= 255) {
            return 255;
        }
        return Math.round(i / (float) WEBSAFE_PAD) * WEBSAFE_PAD;
    }

    /**
   * Create a color with red = 0, green = 0 and blue = 0.
   * 
   */
    public HSLColor() {
        color = 0;
        hue = 0;
        saturation = 0;
        lightness = 0;
        awt = null;
    }

    /**
   * Create a clone on the given color.
   * 
   * @param c
   *          the color to clone.
   */
    public HSLColor(final Color c) {
        this(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
   * Create a color based on the HSL representation.
   * 
   * @param h
   *          the hue.
   * @param s
   *          the saturation.
   * @param l
   *          the ligthness.
   */
    public HSLColor(final double h, final double s, final double l) {
        this();
        setHue(h);
        setSaturation(s);
        setLightness(l);
    }

    /**
   * Create a color with a RGB code (Bits 16-23 are red, 8-15 are green, 0-7 are
   * blue).
   * 
   * @param i
   *          the RGB code.
   */
    public HSLColor(final int i) {
        this();
        setColor(i);
    }

    /**
   * Create a color the red, green and blue value.
   * 
   * @param red
   * @param green
   * @param blue
   */
    public HSLColor(final int red, final int green, final int blue) {
        this((validateRange(red) << 16) | (validateRange(green) << 8) | validateRange(blue));
    }

    /**
   * Try to convert the String to create a color.
   * 
   * @param cssText
   *          the String to convert.
   * @see #convertToHex(String) for the supported format.
   */
    public HSLColor(final String cssText) {
        setCssText(cssText);
    }

    private double css3LumDistance() {
        return ((getRed() * 299) + (getGreen() * 587) + (getBlue() * 114)) / 1000.0;
    }

    private double hueToRGB(final double v1, final double v2, final double vH) {
        double vHCorrected = vH;
        if (vHCorrected < 0) {
            vHCorrected += 1;
        }
        if (vHCorrected > 1) {
            vHCorrected -= 1;
        }
        if ((6 * vHCorrected) < 1) {
            return (v1 + (v2 - v1) * 6 * vHCorrected);
        }
        if ((2 * vHCorrected) < 1) {
            return (v2);
        }
        if ((3 * vHCorrected) < 2) {
            return (v1 + (v2 - v1) * (((double) 2 / 3) - vHCorrected) * 6);
        }
        return (v1);
    }

    private void stringToRGB(final String stringValue) {
        String converted = convertToHex(stringValue);
        if (converted == null) {
            throw new IllegalArgumentException(stringValue + " is not a valid color");
        }
        setColor(Integer.parseInt(converted, 16));
    }

    /**
   * Check if the given String can be convert to a color.
   * 
   * @param s
   *          String to test.
   * @return true only if the String can be converted.
   */
    public static boolean isValidString(final String s) {
        return convertToHex(s) != null;
    }

    /**
   * http://www.easyrgb.com/math.php?MATH=M18#text18
   * 
   */
    private void updateHSL() {
        awt = null;
        double var_R = ((double) ((color & 0xFF0000) >> 16) / 255);
        double var_G = ((double) ((color & 0x00FF00) >> 8) / 255);
        double var_B = ((double) ((color & 0x0000FF)) / 255);
        double var_Min = Math.min(var_R, Math.min(var_G, var_B));
        double var_Max = Math.max(var_R, Math.max(var_G, var_B));
        double del_Max = var_Max - var_Min;
        lightness = (var_Max + var_Min) / 2;
        hue = 0;
        saturation = 0;
        if (del_Max != 0) {
            if (lightness < 0.5) saturation = del_Max / (var_Max + var_Min); else saturation = del_Max / (2 - var_Max - var_Min);
            double del_R = (((var_Max - var_R) / 6) + (del_Max / 2)) / del_Max;
            double del_G = (((var_Max - var_G) / 6) + (del_Max / 2)) / del_Max;
            double del_B = (((var_Max - var_B) / 6) + (del_Max / 2)) / del_Max;
            if (var_R == var_Max) hue = del_B - del_G; else if (var_G == var_Max) hue = ((double) 1 / 3) + del_R - del_B; else if (var_B == var_Max) hue = ((double) 2 / 3) + del_G - del_R;
            if (hue < 0) hue += 1;
            if (hue > 1) hue -= 1;
        }
    }

    /**
   * http://www.easyrgb.com/math.php?MATH=M19#text19
   * 
   */
    private void updateRGB() {
        awt = null;
        double x, y;
        if (saturation == 0) {
            color = (int) Math.round(lightness * 255);
            color <<= 8;
            color |= (int) Math.round(lightness * 255);
            color <<= 8;
            color |= (int) Math.round(lightness * 255);
        } else {
            if (lightness < 0.5) {
                y = lightness * (1 + saturation);
            } else {
                y = (lightness + saturation) - (saturation * lightness);
            }
            x = 2 * lightness - y;
            color = ((int) Math.round(255 * hueToRGB(x, y, hue + ((double) 1 / 3)))) << 16;
            color |= ((int) Math.round(255 * hueToRGB(x, y, hue))) << 8;
            color |= (int) Math.round(255 * hueToRGB(x, y, hue - ((double) 1 / 3)));
        }
    }

    public final int compareTo(final HSLColor o) {
        if (o == null) {
            throw new NullPointerException("Can't compare with null");
        }
        return getColor() - o.getColor();
    }

    /**
   * Calculate the css3 distance between this color and the given Color.
   * 
   * @param c
   *          the color to compare
   * @return the css3 distance.
   */
    public final double[] css3Distance(final HSLColor c) {
        double[] distance = new double[2];
        distance[0] = Math.abs(css3LumDistance() - c.css3LumDistance());
        distance[1] = Math.abs(getRed() - c.getRed()) + Math.abs(getGreen() - c.getGreen()) + Math.abs(getBlue() - c.getBlue());
        return distance;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof HSLColor) {
            HSLColor c = (HSLColor) o;
            return getRed() == c.getRed() && getBlue() == c.getBlue() && getGreen() == c.getGreen();
        }
        return false;
    }

    /**
   * @return The java.awt.Color representation of this color.
   */
    public java.awt.Color getAwtColor() {
        if (awt == null) {
            awt = new Color(getColor());
        }
        return awt;
    }

    /**
   * @return the blue value of this color.
   */
    public int getBlue() {
        if (isWebSafe) {
            return getWebSafeValue(color & 0x0000FF);
        }
        return color & 0x0000FF;
    }

    /**
   * @return the RGB code of this color (Bits 16-23 are red, 8-15 are green, 0-7
   *         are blue).
   */
    public int getColor() {
        return color;
    }

    /**
   * @return the name of the color.
   */
    public String getColorName() {
        return name;
    }

    public String getCssText() {
        return toCSSLongHexString();
    }

    /**
   * @return the green value of this color.
   */
    public int getGreen() {
        if (isWebSafe) {
            return getWebSafeValue(((color & 0x00FF00) >> 8));
        }
        return ((color & 0x00FF00) >> 8);
    }

    /**
   * @return the hue of this color.
   */
    public double getHue() {
        return hue;
    }

    /**
   * @return the ligthness of this color.
   */
    public double getLightness() {
        return lightness;
    }

    public short getPrimitiveType() {
        return CSSPrimitiveValue.CSS_STRING;
    }

    /**
   * @return the red value of this color.
   */
    public int getRed() {
        if (isWebSafe) {
            return getWebSafeValue(((color & 0xFF0000) >> 16));
        }
        return ((color & 0xFF0000) >> 16);
    }

    public RGBColor getRGBColorValue() throws DOMException {
        return null;
    }

    /**
   * @return The saturation of this color.
   */
    public double getSaturation() {
        return saturation;
    }

    public String getStringValue() throws DOMException {
        return toCSSLongHexString();
    }

    @Override
    public int hashCode() {
        return getRed() ^ getGreen() ^ getBlue();
    }

    /**
   * Check the css3 distance between this color and the given color. The
   * brightness difference limit is 125 and the color difference limit is 500.
   * 
   * @param c
   *          the color to compare.
   * @return true if the two color have a compliant css3 distance, false
   *         otherwise.
   */
    public final boolean isDistanceCSS3Compliant(final HSLColor c) {
        double[] d = css3Distance(c);
        return d[0] >= 125 && d[1] >= 500;
    }

    /**
   * Check the HP distance between this color and the given color.<br />
   * This function use the same algorithm but the colour difference limit is set
   * to 400.
   * 
   * @param c
   *          the color to compare.
   * @return true if the two color have a compliant css3 distance, false
   *         otherwise.
   */
    public final boolean isDistanceHPCompliant(final HSLColor c) {
        double[] d = css3Distance(c);
        return d[0] >= 125 && d[1] >= 400;
    }

    /**
   * @return true if this color is in Web safe mode, false otherwise.
   */
    public boolean isWebSafeMode() {
        return isWebSafe;
    }

    /**
   * Convert this color into a Web safe color.
   */
    public void makeWebSafe() {
        color = getWebSafeValue(getRed()) << 16;
        color |= getWebSafeValue(getGreen()) << 8;
        color |= getWebSafeValue(getBlue());
        updateHSL();
    }

    /**
   * @param b
   *          the blue value.
   */
    public void setBlue(final int b) {
        color &= 0xFFFF00;
        color |= validateRange(b);
        updateHSL();
    }

    /**
   * @param code
   *          the RGB code value (Bits 16-23 are red, 8-15 are green, 0-7 are
   *          blue).
   */
    public void setColor(final int code) {
        color = code;
        updateHSL();
    }

    /**
   * @param name
   *          the name of the color.
   */
    public void setColorName(final String name) {
        if (!name.trim().equals("")) {
            this.name = name;
        } else {
            this.name = null;
        }
    }

    public void setCssText(final String cssText) throws DOMException {
        stringToRGB(cssText);
    }

    /**
   * @param g
   *          the green value.
   */
    public void setGreen(final int g) {
        color &= 0xFF00FF;
        color |= validateRange(g) << 8;
        updateHSL();
    }

    /**
   * @param h
   *          the hue.
   */
    public void setHue(final double h) {
        this.hue = validateDegree(h);
        updateRGB();
    }

    /**
   * @param l
   *          the ligthess.
   */
    public void setLightness(final double l) {
        this.lightness = validateRange(l);
        updateRGB();
    }

    /**
   * @param r
   *          the red value.
   */
    public void setRed(final int r) {
        color &= 0x00FFFF;
        color |= validateRange(r) << 16;
        updateHSL();
    }

    /**
   * @param s
   *          the saturation.
   */
    public void setSaturation(final double s) {
        this.saturation = validateRange(s);
        updateRGB();
    }

    public void setStringValue(final short stringType, final String stringValue) throws DOMException {
        if (stringType != CSSPrimitiveValue.CSS_STRING) {
            throw new IllegalArgumentException("setStringValue work only with string");
        }
        stringToRGB(stringValue);
    }

    /**
   * Set the Web safe mode.
   * 
   * @param isWebSafe
   */
    public void setWebSafeMode(final boolean isWebSafe) {
        this.isWebSafe = isWebSafe;
    }

    /**
   * @return the css long hex format in uppercase (#XXXXXX).
   */
    public String toCSSLongHexString() {
        BUILDER.setLength(0);
        BUILDER.append('#');
        appendHexColor(getColor(), BUILDER);
        return BUILDER.toString().toUpperCase();
    }

    /**
   * @return the RGB format : xxx.xxx.xxx
   */
    public String toRGBString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getRed()).append(".").append(getGreen()).append(".").append(getBlue());
        return sb.toString();
    }

    public String toString() {
        if (name != null) {
            return name;
        }
        return toCSSLongHexString();
    }
}
