import java.net.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.lang.reflect.*;

public class Muffin {

    private final String TMP_PREFIX = "Muffin-";

    private final String TMP_SUFFIX = ".jar";

    private ClassLoader classLoader;

    Muffin(String[] args) throws Exception {
        init();
        doit(args);
    }

    private void init() throws Exception {
        StringTokenizer st;
        String path = null;
        cleanup();
        st = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
        while (st.hasMoreTokens()) {
            String entry = st.nextToken();
            if (entry.toLowerCase().endsWith("muffin.jar")) {
                path = entry;
                break;
            }
        }
        if (path != null) {
            JarFile jar;
            ArrayList urls;
            URL context;
            context = new URL("jar:file:" + path + "!/");
            urls = new ArrayList();
            urls.add(new URL(context, "WEB-INF/classes/"));
            jar = new JarFile(path);
            for (Enumeration e = jar.entries(); e.hasMoreElements(); ) {
                JarEntry jentry = (JarEntry) e.nextElement();
                String name = jentry.getName();
                if (name.startsWith("WEB-INF/lib/") && name.toLowerCase().endsWith(".jar")) {
                    File file = File.createTempFile(TMP_PREFIX, TMP_SUFFIX);
                    extractJar(jar.getInputStream(jentry), file);
                    file.deleteOnExit();
                    urls.add(file.toURL());
                }
            }
            classLoader = new URLClassLoader((URL[]) urls.toArray(new URL[0]));
        } else {
            classLoader = this.getClass().getClassLoader();
        }
    }

    private void cleanup() throws IOException {
        File tmpdir;
        File[] files;
        tmpdir = new File(System.getProperty("java.io.tmpdir", "/tmp"));
        files = tmpdir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith(TMP_PREFIX) && files[i].getName().endsWith(TMP_SUFFIX)) {
                files[i].delete();
            }
        }
    }

    private void extractJar(InputStream in, File to) throws IOException {
        byte[] buf;
        int n;
        OutputStream out = null;
        try {
            out = new FileOutputStream(to);
            buf = new byte[8192];
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            in.close();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void doit(String args[]) throws Exception {
        Class clazz = classLoader.loadClass("org.doit.muffin.Main");
        Method method = clazz.getMethod("main", new Class[] { args.getClass() });
        method.invoke(null, new Object[] { args });
    }

    public static void main(String[] args) throws Exception {
        new Muffin(args);
    }
}
