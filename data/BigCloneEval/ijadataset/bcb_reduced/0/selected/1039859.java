package com.xenoage.util.font;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.xenoage.util.FileUtils;
import com.xenoage.util.SortedList;
import com.xenoage.util.font.awt.AWTTextMeasurerFactory;
import com.xenoage.util.io.IO;
import com.xenoage.util.logging.Log;

/**
 * Useful methods to work with fonts.
 * 
 * @author Andreas Wenger
 */
public class FontUtils {

    private static FontUtils instance = null;

    /** The default {@link TextMeasurerFactory}, which is the {@link AWTTextMeasurerFactory} */
    public static TextMeasurerFactory textMeasurerFactory = new AWTTextMeasurerFactory();

    private SortedList<String> supportedFontFamiliesSorted;

    private HashSet<String> supportedFontFamilies;

    public static FontUtils getInstance() {
        if (instance == null) instance = new FontUtils();
        return instance;
    }

    private FontUtils() {
        String[] systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(systemFonts);
        supportedFontFamiliesSorted = new SortedList<String>(systemFonts, false);
        supportedFontFamilies = new HashSet<String>(supportedFontFamiliesSorted.getSize() * 2);
        for (String s : systemFonts) {
            supportedFontFamilies.add(s);
        }
    }

    /**
	 * Loads the shipped fonts in "data/fonts".
	 * If an exception occurs during loading, it is thrown.
	 */
    public void loadShippedFonts() throws Exception {
        String fontPath = "data/fonts";
        Set<String> ttfFiles = IO.listDataFiles(fontPath, FileUtils.getTTFFilter());
        for (String file : ttfFiles) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath + "/" + file));
            String fontName = font.getFamily();
            if (!isFontFamilySupported(fontName)) {
                Log.message("Registering font: " + fontName);
                supportedFontFamiliesSorted.add(fontName);
                supportedFontFamilies.add(fontName);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            }
        }
    }

    /**
	 * Gets an alphabetically sorted list of all supported font families.
	 */
    public SortedList<String> getSupportedFontFamilies() {
        return supportedFontFamiliesSorted;
    }

    /**
	 * Returns, if the given font family is supported by this system.
	 */
    public boolean isFontFamilySupported(String fontFamily) {
        return supportedFontFamilies.contains(fontFamily);
    }

    public static TextMeasurer textMeasurer(FontInfo font, String text) {
        return textMeasurerFactory.textMeasurer(font, text);
    }
}
