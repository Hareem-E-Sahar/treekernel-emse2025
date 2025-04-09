package jvs.vfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import jvs.vfs.sound.AudioSystem;
import jvs.vfs.util.Log;
import x.java.io.File;

/**
 * @author qiangli
 * 
 */
public class FileSystem {

    public static final String JVS_ROOTFS = "jvs.rootfs";

    private static final String version = "JVS VFS Version 0.1 by Li, Qiang";

    private static long uptime = java.lang.System.currentTimeMillis();

    private static FileSystem fs = new FileSystem();

    private static Properties fsmap = new Properties();

    private static Hashtable mountpoints = new Hashtable();

    private static Hashtable providers = new Hashtable();

    private static List bootClassPath = null;

    static {
        try {
            java.lang.System.getProperties().put("java.protocol.handler.pkgs", "x.java.net.protocol");
            fsmap.put("jfs", "jvs.vfs.dom.JfsFileImpl");
            fsmap.put("resource", "jvs.vfs.resource.ResourceFileImpl");
            fsmap.put("url", "jvs.vfs.resource.UrlFileImpl");
            fsmap.put("file", "jvs.vfs.local.LocalFileImpl");
            fsmap.put("ftp", "jvs.vfs.ftp.FtpFileImpl");
            String uri = java.lang.System.getProperty(JVS_ROOTFS);
            if (uri == null) {
                uri = "jfs:resource:/boot/root.xml!/";
                java.lang.System.setProperty(JVS_ROOTFS, uri);
                Log.log(Log.INFO, FileSystem.class.getName(), "Filesystem property jvs.rootfs not set (-Djvs.rootfs=<uri>), using default: " + uri);
            }
            if (uri.indexOf(":") == -1) {
                uri = new java.io.File(uri).toURI().toString();
            }
            fs.mount("/", new URI(uri), "r");
            Properties props = new Properties();
            File vfs_conf = new File("/etc/vfs.conf");
            props.load(vfs_conf.getInputStream());
            fsmap.putAll(props);
            File fstab = new File("/etc/fstab");
            fs.mountAll(fstab.getContent());
            File providerDir = new File("/etc/sound/providers/");
            if (providerDir.exists()) {
                AudioSystem.getAudioSystem().init(providerDir);
            }
        } catch (Exception e) {
            Log.log(Log.ERROR, FileSystem.class.getName(), e.getMessage());
            e.printStackTrace();
        }
    }

    private static String[] buildPaths(String s) {
        s = removeV(s);
        String[] sa = s.split("/");
        if (sa.length == 0) {
            return new String[] { "/" };
        }
        String[] pa = new String[sa.length];
        for (int i = 1; i <= sa.length; i++) {
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < i; j++) {
                sb.append(sa[j] + "/");
            }
            String f = sb.toString();
            int len = f.length();
            pa[pa.length - i] = (len == 1 ? "/" : f.substring(0, len - 1));
        }
        return pa;
    }

    private static String encodePath(String s) {
        try {
            return URIEncoder.encodeURI(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    private static Fstab.Entry findMountpoint(String path) {
        String[] ps = buildPaths(path);
        for (int i = 0; i < ps.length; i++) {
            Fstab.Entry fe = (Fstab.Entry) mountpoints.get(ps[i]);
            if (fe != null) {
                return fe;
            }
        }
        return null;
    }

    public static FileSystem getFileSystem() {
        return fs;
    }

    /**
	 * @throws
	 * NullPointerException if path is null
	 */
    private static boolean hasV(String path) {
        if (path.startsWith("v:") || path.startsWith("V:")) {
            return true;
        }
        return false;
    }

    private static String readURL(String url) {
        try {
            final int BUFSIZE = 1024;
            URL u = new URL(url);
            InputStream in = u.openStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte buf[] = new byte[BUFSIZE];
            int len;
            while ((len = in.read(buf, 0, BUFSIZE)) != -1) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void registerProvider(String scheme, String name) {
        fsmap.put(scheme, name);
    }

    /**
	 * 
	 * @param path
	 * @return 
	 * path with virtual drive letter v removed
	 */
    private static String removeV(String path) {
        if (path == null || path.length() < 2) {
            return path;
        }
        if (hasV(path)) {
            return path.substring(2);
        }
        return path;
    }

    private File root = null;

    private FileSystem() {
    }

    /**
	 * 
	 * @param file
	 * @return  true if filesystem is  readable; false otherwise
	 */
    public boolean canRead(File file) {
        String path = file.getPath();
        Fstab.Entry fe = findMountpoint(path);
        return fe != null && fe.canRead();
    }

    /**
	 * 
	 * @param file
	 * @return true if filesystem is writable; false otherwise
	 */
    public boolean canWrite(File file) {
        String path = file.getPath();
        Fstab.Entry fe = findMountpoint(path);
        return fe != null && fe.canWrite();
    }

    private void changeRoot(File root) {
        this.root = root;
    }

    public void changeRoot(String root) {
        changeRoot(new File(root));
    }

    private IFile createIFile(Class cls, URI uri) throws Exception {
        Class[] parameterTypes = new Class[] { URI.class };
        Constructor constructor = cls.getConstructor(parameterTypes);
        Object[] initargs = new Object[] { uri };
        Object obj = constructor.newInstance(initargs);
        return (IFile) obj;
    }

    public IFile createIFile(File file) throws Exception {
        URI uri = resolve(file);
        Class cls = findProvider(uri.getScheme());
        if (cls == null) {
            throw new IOException("No providers found for " + uri.getScheme());
        }
        return createIFile(cls, uri);
    }

    public boolean hasProvider(String scheme) {
        return providers.get(scheme) != null;
    }

    private Class findProvider(String scheme) {
        Class provider = (Class) providers.get(scheme);
        if (provider != null) {
            return provider;
        }
        String cname = fsmap.getProperty(scheme);
        try {
            ClassLoader cl = fs.getClass().getClassLoader();
            Class cls = cl.loadClass(cname);
            providers.put(scheme, cls);
            return cls;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            ClassLoader cl = getVfsClassLoader();
            Class cls = cl.loadClass(cname);
            providers.put(scheme, cls);
            return cls;
        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public List getBootClassPath() throws Exception {
        if (bootClassPath == null) {
            File blib = new File("/boot/lib");
            bootClassPath = new ArrayList();
            getClassPath(blib, (ArrayList) bootClassPath);
        }
        return bootClassPath;
    }

    public List getClassPath() {
        File home = (File) getHomeDirectory();
        return getClassPath(home);
    }

    public List getClassPath(File home) {
        ArrayList list = new ArrayList();
        try {
            list.addAll(getBootClassPath());
            getClassPath(new File(root, "lib"), list);
            getClassPath(new File(home, "lib"), list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.log(Log.DEBUG, this, "classpath: " + list);
        return list;
    }

    private void getClassPath(java.io.File file, ArrayList list) throws Exception {
        try {
            java.io.File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    getClassPath(files[i], list);
                } else if (files[i].isFile()) {
                    String path = files[i].getPath();
                    if (path.endsWith(".jar") || path.endsWith(".zip")) {
                        list.add(files[i].toURL());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getDefaultDirectory() {
        String dir = x.java.lang.System.getProperty("user.dir");
        if (dir == null) {
            return root;
        }
        return new File(dir);
    }

    public Properties getFsmap() {
        return fsmap;
    }

    public java.io.File getHomeDirectory() {
        String home = x.java.lang.System.getProperty("user.home");
        if (home == null) {
            return root;
        }
        return new File(home);
    }

    public Hashtable getMountpoints() {
        return mountpoints;
    }

    public File getRoot() {
        return root;
    }

    public long getUptime() {
        return uptime;
    }

    public String getVersion() {
        return version;
    }

    public ClassLoader getVfsClassLoader() {
        List list = getClassPath();
        Log.log(Log.TRACE, this, list);
        ClassLoader cl = new VfsClassLoader(list, getClass().getClassLoader());
        return cl;
    }

    public boolean isMountPoint(File file) {
        String p = file.getPath();
        p = removeV(p);
        return (mountpoints.get(p) != null);
    }

    public boolean isRoot(File f) {
        String p = f.getPath();
        return p.equals("v:/") || p.equals("/");
    }

    public java.io.File[] listRoots() {
        return new x.java.io.File[] { root };
    }

    private void mount(Fstab.Entry fe) {
        String mp = fe.getMountpoint();
        if (mp.startsWith("v:/")) {
            mp = mp.substring(2);
        }
        if (mp.length() > 1 && mp.endsWith("/")) {
            mp = mp.substring(0, mp.length() - 1);
        }
        fe.setMountpoint(mp);
        URI uri = fe.getUri();
        if (uri.getScheme().equals("env")) {
            String env = java.lang.System.getProperty(uri.toString().substring(4).trim());
            Log.log(Log.DEBUG, this, "uri: " + uri + " env:" + env);
            if (env == null) {
                Log.log(Log.INFO, this, "env not found for uri: " + uri);
                return;
            }
            int idx = env.indexOf(":");
            if (idx == -1 || !hasProvider(env.substring(0, idx))) {
                java.io.File file = new java.io.File(env);
                uri = file.toURI();
            } else {
                try {
                    uri = new URI(encodePath(env));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        uri = uri.normalize();
        try {
            fe.setUri(resolve(uri));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Fstab.Entry oldfe = (Fstab.Entry) mountpoints.put(mp, fe);
        if (test(mp)) {
            if (oldfe != null) {
                Log.log(Log.INFO, this, "unmounted: " + oldfe);
            }
            Log.log(Log.INFO, this, "mounted: " + fe);
            if (mp.equals("/")) {
                changeRoot(new File("/"));
            }
        } else {
            fe = (Fstab.Entry) mountpoints.remove(mp);
            Log.log(Log.INFO, this, "failed: " + mp + " " + fe.getUri());
            if (oldfe != null) {
                mountpoints.put(mp, oldfe);
            }
        }
    }

    public void mount(String base, String uri) {
        try {
            mount(base, new URI(uri), "r");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.log(Log.ERROR, this, base + ": " + e.getMessage());
        }
    }

    public void mount(String base, String uri, String options) {
        try {
            mount(base, new URI(uri), options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.log(Log.ERROR, this, base + ": " + e.getMessage());
        }
    }

    public void mount(String base, URI uri, String options) {
        Fstab.Entry fe = new Fstab.Entry();
        fe.setMountpoint(base);
        fe.setUri(uri);
        fe.setOptions(options);
        mount(fe);
    }

    public void mountAll(String c) {
        Log.log(Log.INFO, this, c);
        StringReader sr = new StringReader(c);
        Fstab fst = Fstab.parse(sr);
        for (Iterator it = fst.iterator(); it.hasNext(); ) {
            Fstab.Entry fe = (Fstab.Entry) it.next();
            mount(fe);
        }
    }

    /**
	 * Resolve virtual path to a real URI
	 */
    public URI resolve(File file) {
        return resolve(file.getPath());
    }

    private URI resolve(String path) {
        if (!hasV(path)) {
            throw new IllegalArgumentException("Not a valid virtual path: " + path);
        }
        Log.log(Log.TRACE, this, "path: " + path);
        String[] ps = buildPaths(path);
        for (int i = 0; i < ps.length; i++) {
            Fstab.Entry fe = (Fstab.Entry) mountpoints.get(ps[i]);
            if (fe != null) {
                String base = fe.getUri().toString();
                path = removeV(path);
                String relpath = path.substring(ps[i].length());
                URI uri = null;
                try {
                    if (relpath.length() == 0) {
                        uri = fe.getUri();
                    } else {
                        base = (base.endsWith("/") ? base.substring(0, base.length() - 1) : base);
                        relpath = (relpath.startsWith("/") ? relpath.substring(1) : relpath);
                        uri = new URI(base + "/" + encodePath(relpath)).normalize();
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Log.log(Log.TRACE, this, "base: " + base + " ps: " + ps[i] + " relpath: " + relpath + " uri: " + uri);
                return uri;
            }
        }
        throw new IllegalArgumentException("No mounted filesystem found for: " + path);
    }

    /**
	 * Find the real uri if virtual
	 */
    public URI resolve(URI uri) throws URISyntaxException {
        if (uri.getScheme().equals("v")) {
            return resolve(uri.getPath());
        } else {
            return uri;
        }
    }

    public void setDefaultDirectory(String dir) {
        File file = new File(dir);
        if (file.isDirectory()) {
            x.java.lang.System.setProperty("user.dir", file.getPath());
        } else if (file.exists()) {
            x.java.lang.System.setProperty("user.dir", file.getParent());
        }
    }

    public void setHomeDirectory(String dir) {
        File file = new File(dir);
        if (file.isDirectory()) {
            x.java.lang.System.setProperty("user.home", file.getPath());
        } else if (file.exists()) {
            x.java.lang.System.setProperty("user.home", file.getParent());
        }
    }

    private boolean test(String path) {
        try {
            File f = new File(path);
            return f.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unmount(String base) {
        try {
            if (base.startsWith("v:/")) {
                base = base.substring(2);
            }
            Fstab.Entry fe = (Fstab.Entry) mountpoints.remove(base);
            Log.log(Log.INFO, this, "unmounted: " + base + " " + fe.getUri());
        } catch (Exception e) {
            Log.log(Log.ERROR, this, base + ": " + e.getMessage());
        }
    }

    public void init() {
    }
}
