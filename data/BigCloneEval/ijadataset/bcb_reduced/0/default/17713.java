import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author rblack
 */
public class WebstartUtil {

    private static File jarFile;

    private static boolean isWebstart;

    /**
     * @return true if this application was run via webstart.
     */
    public static boolean isWebstart() {
        return isWebstart;
    }

    /**
     * Looks for a file with the given name in the cache.  It first looks in the same directory as the jar that contains the WebstartInfo 
     * class, and then looks in its sibling directories.  Does not do any recursive directory traversal.
     *   
     * @param filename 
     * @return the file if it's found, null if it's not.
     * @throws IllegalStateException if the app wasn't loaded by webstart.  
     * You should verify that isWebstart returns true before calling this method.   
     */
    public static File findCacheFile(String filename) throws IllegalStateException {
        if (!isWebstart()) throw new IllegalStateException("This application has not been run from webstart");
        File thisDirectory = getJarFile().getParentFile();
        File f = new File(thisDirectory, filename);
        if (f.exists()) return f;
        File parent = thisDirectory.getParentFile();
        File[] siblings = parent.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (int i = 0; i < siblings.length; i++) {
            File siblingDir = siblings[i];
            if (!siblingDir.equals(thisDirectory)) {
                f = new File(siblingDir, filename);
                if (f.exists()) return f;
            }
        }
        return null;
    }

    static {
        getJarFile();
    }

    private static File getJarFile() {
        if (jarFile == null) {
            try {
                URL classURL = getClassURL();
                if (classURL == null) return null;
                String url = classURL.toString();
                if (url.startsWith("jar:")) {
                    int exclamationIndex = url.lastIndexOf('!');
                    if (exclamationIndex >= 0) {
                        if (url.charAt(9) != '/') {
                            url = url.substring(0, 9) + "/" + url.substring(9);
                            exclamationIndex++;
                        }
                        URL jarURL = new URL(url.substring(4, exclamationIndex));
                        jarFile = new File(jarURL.toURI());
                        if (!jarFile.exists()) jarFile = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isWebstart = jarFile != null && jarFile.exists() && jarFile.getName().startsWith("RM") && new File(jarFile.getParentFile(), "RT" + jarFile.getName().substring(2)).exists();
        return jarFile;
    }

    /**
     * @return
     */
    private static URL getClassURL() {
        ClassLoader cl = WebstartUtil.class.getClassLoader();
        Method m;
        try {
            m = cl.getClass().getMethod("findResource", new Class[] { String.class });
            if (!m.getReturnType().equals(URL.class)) return null;
            String name = WebstartUtil.class.getName();
            String path = name.replace('.', '/').concat(".class");
            return (URL) m.invoke(cl, new Object[] { path });
        } catch (Throwable t) {
            return null;
        }
    }

    private WebstartUtil() {
    }
}
