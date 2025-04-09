package eulergui.util;

import java.io.File;

/**
 * @author Jean-Marc Vanel jeanmarc.vanel@gmail.com
 *
 */
public class FileHelper {

    /**
	 * @param cacheDir
	 */
    public static void recursiveDeleteDirectory(File cacheDir) {
        if (cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                recursiveDeleteDirectory(file);
            }
        } else {
            cacheDir.delete();
        }
    }
}
