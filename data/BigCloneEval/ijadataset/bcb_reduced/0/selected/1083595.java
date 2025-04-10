package cn.hexen.software.cuelrcmaster;

import java.awt.Container;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

public class GraphicsUtils {

    public static final Toolkit toolKit = Toolkit.getDefaultToolkit();

    private static final MediaTracker mediaTracker = new MediaTracker(new Container());

    private static final Map<Object, Object> cacheImages = new WeakHashMap<Object, Object>(100);

    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public GraphicsUtils() {
    }

    /**
	 * 加载内部file转为Image
	 * 
	 * @param inputstream
	 * @return
	 */
    public static Image loadImage(String str) {
        if (str == null) {
            return null;
        }
        Image cacheImage = (Image) cacheImages.get(str.toLowerCase());
        ;
        if (cacheImage == null) {
            InputStream in = new BufferedInputStream(classLoader.getResourceAsStream(str));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byte[] bytes = new byte[16384];
                int read;
                while ((read = in.read(bytes)) >= 0) {
                    byteArrayOutputStream.write(bytes, 0, read);
                }
                bytes = byteArrayOutputStream.toByteArray();
                cacheImages.put(str.toLowerCase(), cacheImage = toolKit.createImage(bytes));
                mediaTracker.addImage(cacheImage, 0);
                mediaTracker.waitForID(0);
                waitImage(100, cacheImage);
            } catch (Exception e) {
                throw new RuntimeException(str + " not found!");
            } finally {
                try {
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                        byteArrayOutputStream = null;
                    }
                    if (in != null) {
                        in.close();
                        in = null;
                    }
                } catch (IOException e) {
                }
            }
        }
        if (cacheImage == null) {
            throw new RuntimeException(("File not found. ( " + str + " )").intern());
        }
        return cacheImage;
    }

    /**
	 * 延迟加载image,以使其同步。
	 * 
	 * @param delay
	 * @param image
	 */
    private static final void waitImage(int delay, Image image) {
        try {
            for (int i = 0; i < delay; i++) {
                if (toolKit.prepareImage(image, -1, -1, null)) {
                    return;
                }
                Thread.sleep(delay);
            }
        } catch (Exception e) {
        }
    }
}
