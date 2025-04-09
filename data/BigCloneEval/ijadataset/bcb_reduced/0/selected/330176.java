package trstudio.blueboximage;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import trstudio.classlibrary.drivers.ErrorManager;
import trstudio.classlibrary.io.PropertyFile;

/**
 * Information sur le flux du sprite.
 *
 * @author Sebastien Villemain
 */
public class AlifeBackgroundStream extends AlifeImageStreamAdapter {

    /**
	 * Clé contenant la propriété de la vitesse.
	 */
    public static final String PROPERTY_SPEED = "speed";

    /**
	 * Clé contenant la propriété de la position Y.
	 */
    public static final String PROPERTY_FOREGROUND = "foreground";

    public AlifeBackgroundStream() {
        this(null);
    }

    public AlifeBackgroundStream(String filePath) {
        super(filePath);
    }

    protected void readAlifeImage(ZipFile spriteZipFile, PropertyFile spriteCfg) throws IOException {
        Enumeration<? extends ZipEntry> entries = spriteZipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(PROPERTY_FILENAME)) {
                continue;
            }
            BufferedInputStream imageInput = new BufferedInputStream(spriteZipFile.getInputStream(entry));
            BufferedImage image = ImageIO.read(imageInput);
            alifeImage = new AlifeBackground(image, spriteCfg.getProperty(PROPERTY_SECTION_SPRITE, PROPERTY_SPEED).getValue(0F), spriteCfg.getProperty(PROPERTY_SECTION_SPRITE, PROPERTY_FOREGROUND).getValue(false));
            break;
        }
        if (alifeImage == null) {
            ErrorManager.getLogger().addWarning("No background image has been extracted.");
        }
    }

    protected void writeAlifeImage(ZipOutputStream output, PropertyFile spriteCfg) throws IOException {
        if (!(alifeImage instanceof AlifeBackground)) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        AlifeBackground background = (AlifeBackground) alifeImage;
        spriteCfg.setProperty(PROPERTY_SECTION_SPRITE, PROPERTY_SPEED).setValue(background.getSpeed());
        spriteCfg.setProperty(PROPERTY_SECTION_SPRITE, PROPERTY_FOREGROUND).setValue(background.isForeground());
        output.putNextEntry(new ZipEntry(background.getName() + "." + IMAGE_FORMAT));
        ImageIO.write((RenderedImage) background.getImage(), IMAGE_FORMAT, output);
    }
}
