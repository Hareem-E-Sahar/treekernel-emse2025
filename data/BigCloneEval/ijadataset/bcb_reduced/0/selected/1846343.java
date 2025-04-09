package jmax.ui.fonts;

import java.util.*;
import java.io.*;
import java.awt.*;
import jmax.commons.*;
import jmax.registry.*;

/**
 * The Font Manger Class.
 *
 * This class provide a set of global services related to fonts.
 *
 * - It provide the concept of current font, and default font, (i.e. the
 *   initial value of the current font).
 *
 * - It provide a font menu to use in the editor
 *
 * - It provides the implementation of the font menus, that thru the
 *   ui.Selection interface access and modify the edited objects font.
 *
 * - It should may be provide a font toolbar element: the menu paradigm is
 *   not appropriate to showing the current selected object font value.
 *
 * - It provide a font cache.
 */
public class FontManager {

    /**
   * Get a font from a registry path.
   * It expects that the path identify an
   * XML element  of the form:
   *
   * <font name=<font face name> size=<size>  [file=<jmaxURI> fontType=<fontType>]/>
   *
   *
   * Please note that the font name is actually the font face name, and can embody
   * style; for example, to specify an SansSerif bold font, the name should be "SansSerif Bold".
   * If the font name does not exists, it will default to the Java platform default.
   *
   * If the file attribute is defined, it should be the jmax URI of a file
   * defining the font, that is loaded; in this case the fontType attribute
   * must specify the format of the font file, and can be "trueType" or  "type1".
   *
   * Once accessed, fonts are locally cached using the path as key.
   *
   * If the font is not found, it return the default font (path "/jmax/preferences/defaultFont").
   * 
   * Should the size be an optional attribue and be handled in the applicative code ?
   */
    private static Map<String, Font> fontPathCache = new HashMap<String, Font>();

    public static Font getFont(String fontPath) {
        Font font;
        font = fontPathCache.get(fontPath);
        if (font == null) {
            try {
                RegistryElement fontElement = Registry.getElement(fontPath);
                if (fontElement == null) return getDefaultFont();
                String name = (String) fontElement.getProperty("name");
                int size = ((Integer) fontElement.getPropertyAs("size", Integer.class)).intValue();
                String style = (String) fontElement.getProperty("style");
                String fileName = (String) fontElement.getPropertyAs("file", String.class);
                String fontTypeName = (String) fontElement.getProperty("fontType");
                if ((fileName != null) && (fontTypeName != null)) loadAndRegisterFont(fileName, fontTypeName);
                int fontStyle;
                if (style != null) {
                    if (style.equals("bold")) fontStyle = Font.BOLD; else if (style.equals("italic")) fontStyle = Font.ITALIC; else if (style.equals("bold italic")) fontStyle = Font.BOLD | Font.ITALIC; else fontStyle = Font.PLAIN;
                } else fontStyle = Font.PLAIN;
                font = new Font(name, fontStyle, size);
                fontPathCache.put(fontPath, font);
                FontCache.addFont(font);
            } catch (MaxError e) {
                e.notifyError();
                return getDefaultFont();
            }
        }
        return font;
    }

    public static Font getDefaultFont() {
        return getFont("/jmax/preferences/defaultFont");
    }

    public static String getDefaultFontPath() {
        return "/jmax/preferences/defaultFont";
    }

    private static void loadAndRegisterFont(String fileName, String fontTypeName) throws MaxError {
        try {
            int fontType;
            Font newFont;
            if (fontTypeName.equals("trueType")) fontType = Font.TRUETYPE_FONT; else if (fontTypeName.equals("type1")) fontType = Font.TYPE1_FONT; else throw new FontManagerException("Font Error", "Unknown font type " + fontTypeName);
            newFont = Font.createFont(fontType, new File(fileName));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(newFont);
        } catch (FontFormatException e) {
            throw new FontManagerException("Font Error", "Error in font file format", e);
        } catch (IOException e) {
            throw new FontManagerException("Font Error", "Cannot read font file", e);
        }
    }
}
