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
public class AlifeSpriteStream extends AlifeImageStreamAdapter {

    /**
	 * Nom de la section sur les informations de durée.
	 */
    public static final String PROPERTY_SECTION_DURATION = "DURATION";

    /**
	 * Clé contenant la propriété de mise à jour automatique.
	 */
    public static final String PROPERTY_AUTO_UPDATE = "autoUpdate";

    /**
	 * Clé contenant la propriété de boucle infini.
	 */
    public static final String PROPERTY_LOOPS = "loops";

    public AlifeSpriteStream() {
        this(null);
    }

    public AlifeSpriteStream(String filePath) {
        super(filePath);
    }

    protected void readAlifeImage(ZipFile spriteZipFile, PropertyFile spriteCfg) throws IOException {
        alifeImage = new AlifeSprite(spriteCfg.getProperty(PROPERTY_SECTION_SPRITE, PROPERTY_AUTO_UPDATE).getValue(false), spriteCfg.getProperty(PROPERTY_SECTION_SPRITE, PROPERTY_LOOPS).getValue(false), spriteZipFile.size() - 1);
        Enumeration<? extends ZipEntry> entries = spriteZipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(PROPERTY_FILENAME)) {
                continue;
            }
            BufferedInputStream imageInput = new BufferedInputStream(spriteZipFile.getInputStream(entry));
            BufferedImage image = ImageIO.read(imageInput);
            AlifeAnimatedImage animatedImage = new AlifeAnimatedImage(image);
            animatedImage.setName(entry.getName());
            animatedImage.setEndTime(spriteCfg.getProperty(PROPERTY_SECTION_DURATION, entry.getName()).getValue(0));
            ((AlifeSprite) alifeImage).addFrame(animatedImage);
        }
        if (!(((AlifeSprite) alifeImage).numberOfFrames() > 0)) {
            ErrorManager.getLogger().addWarning("No sprite image has been extracted.");
        }
    }

    protected void writeAlifeImage(ZipOutputStream output, PropertyFile spriteCfg) throws IOException {
        if (!(alifeImage instanceof AlifeSprite)) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        AlifeSprite sprite = (AlifeSprite) alifeImage;
        spriteCfg.setProperty(PROPERTY_SECTION_SPRITE, PROPERTY_AUTO_UPDATE).setValue(sprite.isAutoUpdate());
        spriteCfg.setProperty(PROPERTY_SECTION_SPRITE, PROPERTY_LOOPS).setValue(sprite.isLoops());
        int index = 0;
        for (AlifeAnimatedImage alifeAnimatedImage : sprite.getFrames()) {
            if (alifeAnimatedImage.getName() == null) {
                alifeAnimatedImage.setName(String.format("%03d", index++) + "." + IMAGE_FORMAT);
            }
            String frameName = alifeAnimatedImage.getName();
            output.putNextEntry(new ZipEntry(frameName));
            ImageIO.write((RenderedImage) alifeAnimatedImage.getImage(), IMAGE_FORMAT, output);
            spriteCfg.setProperty(PROPERTY_SECTION_DURATION, frameName).setValue(alifeAnimatedImage.getEndTime());
        }
    }
}
