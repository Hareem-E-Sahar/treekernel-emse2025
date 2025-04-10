package org.photovault.dcraw;

/**
 * Wrapper object for all settings related to raw conversion using dcraw.
 * This is an immutable object.
 * @author Harri Kaimio
 */
public class RawConversionSettings implements Cloneable {

    /**
     OJB identifier
     */
    int rawSettingId;

    /**
     The white pixel value in raw image (unadjusted)
     */
    int white;

    /**
     EV correction of the image
     */
    double evCorr;

    /**
     Highlight compression (in f-stops)
     */
    double hlightComp;

    /**
     black level
     */
    int black;

    /**
     Whether the ICC profile embedded to raw file should be used
     */
    boolean useEmbeddedICCProfile;

    /**
     Database ID for the color profile (used by OJB)
     */
    int colorProfileId;

    /**
     ICC color profile used for raw conversion.
     */
    ColorProfileDesc colorProfile;

    /**
     Methodfor setting the white balance
     */
    int whiteBalanceType;

    /**
     Invalid default value
     */
    public static final int WB_INVALID = 0;

    /**
     Use camera WB settings
     */
    public static final int WB_CAMERA = 1;

    /**
     User dcraw's automatic WB algorithm
     */
    public static final int WB_AUTOMATIC = 2;

    /**
     Set white balance manually
     */
    public static final int WB_MANUAL = 3;

    /**
     Ratio of red & green channel multipliers
     */
    double redGreenRatio;

    /**
     Ratio of blue & green channel multipliers
     */
    double blueGreenRatio;

    /**
     Ratio of red & green channel multipliers in daylight
     */
    double daylightRedGreenRatio;

    /**
     Ratio of blue & green channel multipliers in daylight
     */
    double daylightBlueGreenRatio;

    /** Creates a new instance of RawConversionSettings */
    public RawConversionSettings() {
    }

    /**
     * Get the OJB gey for this object
     * @return The key used in database
     */
    public int getRawSettingId() {
        return rawSettingId;
    }

    /**
     *     Get the white point
     * @return See {@link #white}
     */
    public int getWhite() {
        return white;
    }

    /**
     *     Get the black point
     * 
     * @return See {@link #black}
     */
    public int getBlack() {
        return black;
    }

    /**
     *     Get exposure correction
     * @return See {@link #evCorr}
     */
    public double getEvCorr() {
        return evCorr;
    }

    /**
     * Get the highlight compression used
     * @return Number of f-stops the highlights are compressed.
     */
    public double getHighlightCompression() {
        return hlightComp;
    }

    /**
     * Get info whether to use embedded color profile
     * @return See {@link #useEmbeddedICCProfile}
     */
    public boolean getUseEmbeddedICCProfile() {
        return useEmbeddedICCProfile;
    }

    /**
     Get the ICC color profile used for raw conversion
     @return The used profile or <code>null</code> if no non-embedded profile
     is assigned.
     */
    public ColorProfileDesc getColorProfile() {
        return colorProfile;
    }

    /**
     * Get the method for setting white balance
     * @return One of {@link #WB_AUTOMATIC}, {@link #WB_MANUAL} 
     * or {@link #WB_CAMERA}
     */
    public int getWhiteBalanceType() {
        return whiteBalanceType;
    }

    /**
     * Get the red/green channel multiplier ratio
     * @return see {@link #redGreenRatio}
     */
    public double getRedGreenRatio() {
        return redGreenRatio;
    }

    /**
     * Get blue/green channel multiplier ratio
     * @return See {@link #blueGreenRatio}
     */
    public double getBlueGreenRatio() {
        return blueGreenRatio;
    }

    /**
     * Get daylight channel multipliers
     * @return See {@link #daylightRedGreenRatio}
     */
    public double getDaylightRedGreenRatio() {
        return daylightRedGreenRatio;
    }

    /**
     * Get daylight channel multipliers
     * @return See {@link #daylightBlueGreenRatio}
     */
    public double getDaylightBlueGreenRatio() {
        return daylightBlueGreenRatio;
    }

    static final double XYZ_to_RGB[][] = { { 3.24071, -0.969258, 0.0556352 }, { -1.53726, 1.87599, -0.203996 }, { -0.498571, 0.0415557, 1.05707 } };

    /**
     * Convert a color telmerature to RGB value of an white patch illuminated 
     * with light with that temperature
     * @param T The color temperature of illuminant (in Kelvin)
     * @return Patches RGB value as 3 doubles (RGB)
     */
    public static double[] colorTempToRGB(double T) {
        int c;
        double xD, yD, X, Y, Z, max;
        double RGB[] = new double[3];
        if (T <= 4000) {
            xD = 0.27475e9 / (T * T * T) - 0.98598e6 / (T * T) + 1.17444e3 / T + 0.145986;
        } else if (T <= 7000) {
            xD = -4.6070e9 / (T * T * T) + 2.9678e6 / (T * T) + 0.09911e3 / T + 0.244063;
        } else {
            xD = -2.0064e9 / (T * T * T) + 1.9018e6 / (T * T) + 0.24748e3 / T + 0.237040;
        }
        yD = -3 * xD * xD + 2.87 * xD - 0.275;
        X = xD / yD;
        Y = 1;
        Z = (1 - xD - yD) / yD;
        max = 0;
        for (c = 0; c < 3; c++) {
            RGB[c] = X * XYZ_to_RGB[0][c] + Y * XYZ_to_RGB[1][c] + Z * XYZ_to_RGB[2][c];
            if (RGB[c] > max) max = RGB[c];
        }
        for (c = 0; c < 3; c++) RGB[c] = RGB[c] / max;
        return RGB;
    }

    /**
     Converts RGB multipliers to color balance
     @param rgb The color triplet of a white patch in the raw image
     @return Array of 2 doubles that contains temperature & green gain for the 
             light source that has illuminated the patch.    
     */
    public static double[] rgbToColorTemp(double rgb[]) {
        double Tmax;
        double Tmin;
        double testRGB[] = null;
        Tmin = 2000;
        Tmax = 12000;
        double T;
        for (T = (Tmax + Tmin) / 2; Tmax - Tmin > 10; T = (Tmax + Tmin) / 2) {
            testRGB = colorTempToRGB(T);
            if (testRGB[2] / testRGB[0] > rgb[2] / rgb[0]) Tmax = T; else Tmin = T;
        }
        double green = (testRGB[1] / testRGB[0]) / (rgb[1] / rgb[0]);
        double result[] = { T, green };
        return result;
    }

    /**
     * Set the color temperature to use when converting the image
     * @param T Color temperature (in Kelvin)
     */
    public void setColorTemp(double T) {
        double rgb[] = colorTempToRGB(T);
        redGreenRatio = daylightRedGreenRatio / (rgb[0] / rgb[1]);
        blueGreenRatio = daylightBlueGreenRatio / (rgb[2] / rgb[1]);
    }

    /**
     * Get color temperature of the image
     * @return Color temperature (in Kelvin)
     */
    public double getColorTemp() {
        double rgb[] = { daylightRedGreenRatio / redGreenRatio, 1., daylightBlueGreenRatio / blueGreenRatio };
        double ct[] = rgbToColorTemp(rgb);
        return ct[0];
    }

    /**
     * Get the green channel gain used
     * @return Ratio of green channel multiplier to the multiplier caused by 
     * illuminant with current comlor temperature.
     */
    public double getGreenGain() {
        double rgb[] = { daylightRedGreenRatio / redGreenRatio, 1., daylightBlueGreenRatio / blueGreenRatio };
        double ct[] = rgbToColorTemp(rgb);
        return ct[1];
    }

    /**
     * Creates a new RawConversionSettings object
     * @param chanMult Channel multipliers (4 doubles)
     * @param daylightMult daylight channel multipliers (3 doubles)
     * @param white White pixel value
     * @param black Black pixel value
     * @param evCorr Exposure correction
     * @param hlightComp Highlight compression
     * @param wbType White balance setting method
     * @param embeddedProfile Should we use embedded ICC profile form raw file?
     * @return New RawConversionSettings object
     * @deprecated Use {@link RawSettingsFactory} instead.
     */
    public static RawConversionSettings create(double chanMult[], double daylightMult[], int white, int black, double evCorr, double hlightComp, int wbType, boolean embeddedProfile) {
        RawConversionSettings s = new RawConversionSettings();
        s.blueGreenRatio = chanMult[2] / chanMult[3];
        s.redGreenRatio = chanMult[0] / chanMult[1];
        s.daylightRedGreenRatio = daylightMult[0] / daylightMult[1];
        s.daylightBlueGreenRatio = daylightMult[2] / daylightMult[1];
        s.evCorr = evCorr;
        s.hlightComp = hlightComp;
        s.white = white;
        s.black = black;
        s.whiteBalanceType = wbType;
        s.useEmbeddedICCProfile = embeddedProfile;
        return s;
    }

    /**
     * Creates a new RawConversionSettings object using color temperature
     * @param colorTemp Color temperature
     * @param greenGain Green gain to use
     * @param daylightMult daylight channel multipliers (3 doubles)
     * @param white White pixel value
     * @param black Black pixel value
     * @param evCorr Exposure correction
     * @param hlightComp Highlight compression
     * @param wbType White balance setting method
     * @param embeddedProfile Should we use embedded ICC profile form raw file?
     * @return New RawConversionSettings object
     * @deprecated Use {@link RawSettingsFactory} instead.
     */
    public static RawConversionSettings create(double colorTemp, double greenGain, double daylightMult[], int white, int black, double evCorr, double hlightComp, int wbType, boolean embeddedProfile) {
        RawConversionSettings s = new RawConversionSettings();
        s.daylightRedGreenRatio = daylightMult[0] / daylightMult[1];
        s.daylightBlueGreenRatio = daylightMult[2] / daylightMult[1];
        double[] rgb = s.colorTempToRGB(colorTemp);
        s.redGreenRatio = (s.daylightRedGreenRatio / (rgb[0] / (rgb[1]))) / greenGain;
        s.blueGreenRatio = (s.daylightBlueGreenRatio / (rgb[2] / (rgb[1]))) / greenGain;
        s.evCorr = evCorr;
        s.hlightComp = hlightComp;
        s.white = white;
        s.black = black;
        s.whiteBalanceType = wbType;
        s.useEmbeddedICCProfile = embeddedProfile;
        return s;
    }

    /**
     * Create a copy of this object
     * @return Copy initialized with current field values
     */
    public RawConversionSettings clone() {
        RawConversionSettings s;
        try {
            s = (RawConversionSettings) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
        s.blueGreenRatio = blueGreenRatio;
        s.redGreenRatio = redGreenRatio;
        s.daylightBlueGreenRatio = daylightBlueGreenRatio;
        s.daylightRedGreenRatio = daylightRedGreenRatio;
        s.evCorr = evCorr;
        s.hlightComp = hlightComp;
        s.useEmbeddedICCProfile = useEmbeddedICCProfile;
        s.colorProfile = colorProfile;
        s.white = white;
        s.black = black;
        s.whiteBalanceType = whiteBalanceType;
        return s;
    }

    /**
     * Compares 2 doubles and check whether the are equal to a given accuracy
     * @param d1 The 1st number to compare
     * @param d2 The 2nd number to compare
     * @param errorLimit Maximum absolute error allowed
     * @return <code>true</code> if d1 & d2 are equal within the given 
     * accuracy, <code>false</code> otherwise.
     */
    private boolean equalsDouble(double d1, double d2, double errorLimit) {
        return Math.abs(d1 - d2) < errorLimit;
    }

    /**
     * Compare this objetc to another
     * @param o The object to compare with
     * @return Whether the 2 objects are equal.
     */
    public boolean equals(Object o) {
        if (o instanceof RawConversionSettings) {
            RawConversionSettings s = (RawConversionSettings) o;
            return (s.white == white && s.useEmbeddedICCProfile == this.useEmbeddedICCProfile && s.colorProfile == this.colorProfile && s.whiteBalanceType == this.whiteBalanceType && s.black == this.black && equalsDouble(s.blueGreenRatio, this.blueGreenRatio, 0.0001) && equalsDouble(s.redGreenRatio, this.redGreenRatio, 0.0001) && equalsDouble(s.daylightBlueGreenRatio, this.daylightBlueGreenRatio, 0.0001) && equalsDouble(s.daylightRedGreenRatio, this.daylightRedGreenRatio, 0.0001) && equalsDouble(s.hlightComp, this.hlightComp, 0.0001) && equalsDouble(s.evCorr, this.evCorr, 0.0001));
        } else {
            return false;
        }
    }

    public int hashCode() {
        return (white + black + (int) (blueGreenRatio * 1000000.0) + (int) (redGreenRatio * 1000000.0));
    }
}
