import java.io.File;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

public class FontLoader {

    /**
	 * Loads a custom font.
	 * http://java.sun.com/products/java-media/2D/reference/faqs/index.html#Q_How_can_I_make_my_custom_font
	 */
    public static void loadCustomFont(String fontFile) {
        try {
            Font newFont = Font.createFont(Font.TRUETYPE_FONT, new File(Config.FONTS_DIR + fontFile));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(newFont);
        } catch (Exception e) {
            System.out.println("ERROR: Cannot load " + fontFile + " font!");
            e.printStackTrace();
        }
    }
}
