import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class is used as a wrapper for loading the
 * org.apache.commons.launcher.Launcher class and invoking its
 * <code>main(String[])</code> method. This particular
 * class is primary used by the Windows 95, 98, ME, and 2000 platforms to
 * overcome the difficulty of putting a jar file directly into the JVM's
 * classpath when using batch scripts on these platforms.
 * <p>
 * Specifically, the problem on thse platforms is when Windows uses the PATH
 * environment variable to find and run a batch script, %0 will resolve
 * incorrectly in that batch script.
 * <p>
 * The way to work around this Windows limitation is to do the following:
 * <ol>
 * <li>Put this class' class file - LauncherBootstrap.class - in the same
 * directory as the batch script. Do not put this class file in a jar file.
 * <li>Put the jar file containing the launcher's classes in the same
 * directory as the batch script and this class' class file. Be sure that
 * that the jar file is named "commons-launcher.jar".
 * <li>Make the Java command in the batch script invoke Java use the following
 * classpath arguments. Be sure to include the quotes to ensure that paths
 * containing spaces are handled properly:
 * <code>-classpath %0\..;"%PATH%"</code>
 * </ol>
 *
 * @author Patrick Luby
 */
public class LauncherBootstrap {

    /**
     * Ant classpath property name
     */
    public static final String ANT_CLASSPATH_PROP_NAME = "ant.class.path";

    /**
     * Jar file name
     */
    public static final String LAUNCHER_JAR_FILE_NAME = "commons-launcher.jar";

    /**
     * Properties file name
     */
    public static final String LAUNCHER_PROPS_FILE_NAME = "launcher.properties";

    /**
     * Class name to load
     */
    public static final String LAUNCHER_MAIN_CLASS_NAME = "org.apache.commons.launcher.Launcher";

    /**
     * Cached Laucher class.
     */
    private static Class launcherClass = null;

    /**
     * The main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            URL coreURL = LauncherBootstrap.class.getResource("/" + LauncherBootstrap.LAUNCHER_JAR_FILE_NAME);
            if (coreURL == null) throw new FileNotFoundException(LauncherBootstrap.LAUNCHER_JAR_FILE_NAME);
            File coreDir = new File(URLDecoder.decode(coreURL.getFile())).getCanonicalFile().getParentFile();
            File propsFile = new File(coreDir, LauncherBootstrap.LAUNCHER_PROPS_FILE_NAME);
            if (!propsFile.canRead()) throw new FileNotFoundException(propsFile.getPath());
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(propsFile);
            props.load(fis);
            fis.close();
            URL[] antURLs = LauncherBootstrap.fileListToURLs((String) props.get(LauncherBootstrap.ANT_CLASSPATH_PROP_NAME));
            URL[] urls = new URL[1 + antURLs.length];
            urls[0] = coreURL;
            for (int i = 0; i < antURLs.length; i++) urls[i + 1] = antURLs[i];
            ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader loader = null;
            if (parentLoader != null) loader = new URLClassLoader(urls, parentLoader); else loader = new URLClassLoader(urls);
            launcherClass = loader.loadClass(LAUNCHER_MAIN_CLASS_NAME);
            Method getLocalizedStringMethod = launcherClass.getDeclaredMethod("getLocalizedString", new Class[] { String.class });
            Method startMethod = launcherClass.getDeclaredMethod("start", new Class[] { String[].class });
            int returnValue = ((Integer) startMethod.invoke(null, new Object[] { args })).intValue();
            System.exit(returnValue);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Convert a ":" separated list of URL file fragments into an array of URL
     * objects. Note that any all URL file fragments must conform to the format
     * required by the "file" parameter in the
     * {@link URL(String, String, String)} constructor.
     *
     * @param fileList the ":" delimited list of URL file fragments to be
     *  converted
     * @return an array of URL objects
     * @throws MalformedURLException if the fileList parameter contains any
     *  malformed URLs
     */
    private static URL[] fileListToURLs(String fileList) throws MalformedURLException {
        if (fileList == null || "".equals(fileList)) return new URL[0];
        ArrayList list = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(fileList, ":");
        URL bootstrapURL = LauncherBootstrap.class.getResource("/" + LauncherBootstrap.class.getName() + ".class");
        while (tokenizer.hasMoreTokens()) list.add(new URL(bootstrapURL, tokenizer.nextToken()));
        return (URL[]) list.toArray(new URL[list.size()]);
    }
}
