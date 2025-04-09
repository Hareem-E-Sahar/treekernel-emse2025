package org.mazix.kernel.resource;

import static java.awt.Font.TRUETYPE_FONT;
import static java.util.logging.Level.SEVERE;
import static org.mazix.constants.FileConstants.FONT_PATH;
import static org.mazix.constants.log.ErrorConstants.CLOSE_FILE_ERROR;
import static org.mazix.constants.log.ErrorConstants.FONTS_FILE_FORMAT_ERROR;
import static org.mazix.constants.log.ErrorConstants.FONTS_FILE_NOT_FOUND_FROM_RESOURCES_ERROR;
import static org.mazix.constants.log.InfoConstants.FONTS_FOUND_FROM_RESOURCES_INFO;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;
import org.mazix.log.LogUtils;
import org.mazix.utils.file.DirectoryUtils;
import org.mazix.utils.file.FontFilenameFilter;

/**
 * The resource manager which manages {@link Font} resources.
 * 
 * @author Benjamin Croizet (graffity2199@yahoo.fr)
 * 
 * @since 0.7
 * @version 0.7
 */
class FontResourceManager extends AbstractFontResourceManager {

    /** The class logger. */
    private static final Logger LOGGER = Logger.getLogger("org.mazix.kernel.resource.FontResourceManager");

    /**
     * Private constructor to prevent from instantiation.
     * 
     * @since 0.6
     */
    FontResourceManager() {
        super();
        registerGameFonts();
    }

    /**
     * @see org.mazix.kernel.resource.AbstractResourceManager#lookup(java.util.SortedMap)
     * @since 0.7
     */
    @Override
    protected Object lookup(final SortedMap<String, Object> parameters) {
        final String fontName = (String) parameters.get(FONT_NAME_PARAMETER);
        final Integer fontStyle = (Integer) parameters.get(FONT_STYLE_PARAMETER);
        final Integer fontSize = (Integer) parameters.get(FONT_SIZE_PARAMETER);
        return new Font(fontName, fontStyle, fontSize);
    }

    /**
     * This method registers in the system all fonts which can be found in the resources directory
     * defined by the {@link org.mazix.constants.FileConstants.FONT_PATH} constant.
     * 
     * @since 0.7
     */
    private void registerGameFonts() {
        final List<String> fontFilenames = DirectoryUtils.listFilesOfDirectory(FONT_PATH, new FontFilenameFilter());
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (final String s : fontFilenames) {
            LOGGER.info(LogUtils.buildLogString(FONTS_FOUND_FROM_RESOURCES_INFO, s));
            InputStream ttf = null;
            try {
                ttf = new FileInputStream(FONT_PATH + s);
                try {
                    final Font font = Font.createFont(TRUETYPE_FONT, ttf);
                    ge.registerFont(font);
                } catch (final FontFormatException e) {
                    LOGGER.log(SEVERE, LogUtils.buildLogString(FONTS_FILE_FORMAT_ERROR, FONT_PATH + s), e);
                }
            } catch (final IOException e) {
                LOGGER.log(SEVERE, LogUtils.buildLogString(FONTS_FILE_NOT_FOUND_FROM_RESOURCES_ERROR, FONT_PATH + s), e);
            } finally {
                if (ttf != null) {
                    try {
                        ttf.close();
                    } catch (final IOException e) {
                        LOGGER.log(SEVERE, LogUtils.buildLogString(CLOSE_FILE_ERROR, FONT_PATH + fontFilenames), e);
                    }
                }
            }
        }
    }
}
