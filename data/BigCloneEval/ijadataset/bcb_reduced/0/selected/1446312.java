package ge.telasi.tasks.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * @author dimitri
 */
public class FontManagement {

    static Font PLAIN_FONT;

    static Font BOLD_FONT;

    /**
     * The very first initialization of fonts.
     */
    static String FAMILY = "Dimitri-New";

    static String FAMILY_BOLD = "Dimitri-New Bold";

    private static void initFonts() {
        if (PLAIN_FONT == null || BOLD_FONT == null) {
            try {
                PLAIN_FONT = Font.createFont(Font.TRUETYPE_FONT, FontManagement.class.getResourceAsStream("font.ttf"));
                BOLD_FONT = Font.createFont(Font.TRUETYPE_FONT, FontManagement.class.getResourceAsStream("font_bold.ttf"));
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                env.registerFont(PLAIN_FONT);
                env.registerFont(BOLD_FONT);
                FAMILY = PLAIN_FONT.getFamily();
                FAMILY_BOLD = BOLD_FONT.getFamily();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void configureUIManager() {
        initFonts();
    }
}
