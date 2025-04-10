package javazoom.jlgui.player.amp.skin;

import java.awt.Image;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javazoom.jlgui.player.amp.util.BMPLoader;
import javazoom.jlgui.player.amp.util.Config;

/**
 * This class implements a Skin Loader.
 * WinAmp 2.x javazoom.jlgui.player.amp.skins compliant.
 */
public class SkinLoader {

    private Hashtable _images = null;

    private ZipInputStream _zis = null;

    /**
     * Contructs a SkinLoader from a skin file.
     */
    public SkinLoader(String filename) {
        _images = new Hashtable();
        try {
            if (Config.startWithProtocol(filename)) _zis = new ZipInputStream((new URL(filename)).openStream()); else _zis = new ZipInputStream(new FileInputStream(filename));
        } catch (Exception e) {
            ClassLoader cl = this.getClass().getClassLoader();
            InputStream sis = cl.getResourceAsStream("javazoom/jlgui/player/amp/nucleo.wsz");
            if (sis != null) _zis = new ZipInputStream(sis);
        }
    }

    /**
     * Contructs a SkinLoader from any input stream.
     */
    public SkinLoader(InputStream inputstream) {
        _images = new Hashtable();
        _zis = new ZipInputStream(inputstream);
    }

    /**
     * Loads data (images + info) from skin.
     */
    public void loadImages() throws Exception {
        ZipEntry entry = _zis.getNextEntry();
        String name;
        BMPLoader bmp = new BMPLoader();
        int pos;
        while (entry != null) {
            name = entry.getName().toLowerCase();
            pos = name.lastIndexOf("/");
            if (pos != -1) name = name.substring(pos + 1);
            if (name.endsWith("bmp")) {
                _images.put(name, bmp.getBMPImage(_zis));
            } else if (name.endsWith("txt")) {
                InputStreamReader reader = new InputStreamReader(_zis, "US-ASCII");
                StringWriter writer = new StringWriter();
                char buffer[] = new char[256];
                int charsRead;
                while ((charsRead = reader.read(buffer)) != -1) writer.write(buffer, 0, charsRead);
                _images.put(name, writer.toString());
            }
            entry = _zis.getNextEntry();
        }
        _zis.close();
    }

    /**
     * Return Image from name.
     */
    public Image getImage(String name) {
        return (Image) _images.get(name);
    }

    /**
     * Return skin content (Image or String) from name.
     */
    public Object getContent(String name) {
        return _images.get(name);
    }
}
