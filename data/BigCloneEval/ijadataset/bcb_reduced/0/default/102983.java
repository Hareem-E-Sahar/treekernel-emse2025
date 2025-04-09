import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Main {

    public static void main(String[] args) {
        String className = "sce.swt.SceneryConfigEditor";
        File workingDirectory = getWorkingDirectory();
        String arch = System.getProperty("os.arch");
        int archCode = (arch.indexOf("64") >= 0) ? 64 : 32;
        ArrayList<URL> urls = new ArrayList<URL>();
        File archDir = new File(workingDirectory, "lib" + archCode);
        dirToURLs(archDir, urls);
        File libDir = new File(workingDirectory, "lib");
        dirToURLs(libDir, urls);
        if (urls.size() > 0) {
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), tcl);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        }
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> clazz = tcl.loadClass(className);
            Method main = clazz.getMethod("main", String[].class);
            main.getClass().getClassLoader();
            main.invoke(null, (Object) args);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getWorkingDirectory() {
        URL locationOfMain = Main.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            File mainFile = new File(locationOfMain.toURI());
            return mainFile.getParentFile();
        } catch (URISyntaxException e1) {
            return new File(".");
        }
    }

    private static void dirToURLs(File dir, ArrayList<URL> urls) {
        if (dir.exists()) {
            System.out.println("Found arch lib directory " + dir.getAbsolutePath());
            File[] dirList = dir.listFiles();
            Arrays.sort(dirList, new Comparator<File>() {

                @Override
                public int compare(File x, File y) {
                    return x.getPath().compareTo(y.getPath());
                }
            });
            for (File file : dir.listFiles()) {
                System.out.println("Found jar: " + file.getAbsolutePath());
                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
