import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.lang.reflect.Method;

public class Boot {

    public static String bootFile = null;

    public static String findInPath(String path, String fn) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String dirname = st.nextToken();
            try {
                File f = new File(dirname + File.separator + fn);
                if (f.isFile()) return f.getPath();
            } catch (Exception fex) {
            }
        }
        return null;
    }

    public static String findNativeLibrary(String basename, boolean internalFirst) {
        String libName = "lib" + basename;
        String ext = ".so";
        String os = System.getProperty("os.name");
        if (os.startsWith("Win")) {
            os = "Win";
            ext = ".dll";
            libName = basename;
        }
        if (os.startsWith("Mac")) {
            os = "Mac";
            ext = ".jnilib";
        }
        String fullName = libName + ext;
        if (!internalFirst) {
            try {
                String r = findInPath("." + File.pathSeparator + System.getProperty("java.library.path"), fullName);
                if (r != null) return r;
            } catch (Exception ex1) {
            }
        }
        String cp = System.getProperty("java.class.path");
        StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String dirname = st.nextToken();
            try {
                File f = new File(dirname);
                if (f.isFile()) {
                    ZipFile jf = new ZipFile(f);
                    ZipEntry ze = jf.getEntry(fullName);
                    if (ze != null) {
                        try {
                            bootFile = f.toString();
                            File tf = File.createTempFile(basename, ext);
                            System.out.println("Boot.findNativeLibrary: found in a JAR (" + jf + "), extracting into " + tf);
                            InputStream zis = jf.getInputStream(ze);
                            FileOutputStream fos = new FileOutputStream(tf);
                            byte b[] = new byte[65536];
                            while (zis.available() > 0) {
                                int n = zis.read(b);
                                if (n > 0) fos.write(b, 0, n);
                            }
                            zis.close();
                            fos.close();
                            tf.deleteOnExit();
                            return tf.getPath();
                        } catch (Exception foo) {
                        }
                    }
                } else if (f.isDirectory()) {
                    File ff = new File(dirname + File.separator + fullName);
                    if (ff.isFile()) return ff.getPath();
                }
            } catch (Exception ex2) {
            }
        }
        if (internalFirst) {
            try {
                String r = findInPath("." + File.pathSeparator + System.getProperty("java.library.path"), fullName);
                if (r != null) return r;
            } catch (Exception ex3) {
            }
        }
        return null;
    }

    public static void main(String[] args) {
        JRIClassLoader mcl = JRIClassLoader.getMainLoader();
        String nl = findNativeLibrary("boot", false);
        if (nl == null) {
            System.err.println("ERROR: Unable to locate native bootstrap library.");
            System.exit(1);
        }
        mcl.registerLibrary("boot", new File(nl));
        String cp = System.getProperty("java.class.path");
        StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String p = st.nextToken();
            mcl.addClassPath(p);
            if (bootFile == null && (new File(p)).isFile()) bootFile = p;
        }
        try {
            Class stage2class = mcl.findAndLinkClass("JRIBootstrap");
            Method m = stage2class.getMethod("bootstrap", new Class[] { String[].class });
            m.invoke(null, new Object[] { args });
        } catch (Exception rtx) {
            System.err.println("ERROR: Unable to invoke bootstrap method in JRIBootstrap! (" + rtx + ")");
            rtx.printStackTrace();
            System.exit(2);
        }
    }
}
