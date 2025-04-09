package de.grogra.vfs;

import java.io.*;
import java.nio.charset.Charset;
import java.net.*;
import java.util.*;
import java.util.Map;
import java.util.jar.*;
import java.util.zip.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import de.grogra.util.*;

public abstract class FileSystem extends TreeModelSupport implements TreeModel {

    public static final MimeType MIME_TYPE = new MimeType("application/x-grogra-fs", null);

    protected class Out extends OutputStream {

        private final Object file;

        private OutputStream out;

        public Out(OutputStream out, Object file) {
            this.out = out;
            this.file = file;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte b[]) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() throws IOException {
            OutputStream o;
            synchronized (this) {
                if ((o = out) == null) {
                    return;
                }
                out = null;
            }
            o.close();
            fireTreeModelEvent(NODES_CHANGED, getEventFor(file));
        }
    }

    protected FileNameMap fileNameMap;

    protected Manifest manifest = new Manifest();

    private final String protocol;

    private final String fsName;

    public FileSystem(String fsName, String protocol) {
        super(true);
        this.fsName = fsName;
        this.protocol = protocol;
    }

    public String getFSName() {
        return fsName;
    }

    public String getProtocol() {
        return protocol;
    }

    public abstract boolean isPersistent();

    public abstract void delete(Object file) throws IOException;

    public abstract String getName(Object file);

    public abstract URL toURL(Object file);

    public abstract Object toFile(URL url);

    public abstract Object getParent(Object file);

    public abstract boolean isReadOnly(Object file);

    public abstract long getTime(Object file);

    public abstract void setTime(Object file, long time);

    public abstract long getSize(Object file);

    public abstract Object getRoot();

    public abstract Object[] listFiles(Object parent);

    public abstract Object getFile(Object parent, String name);

    public boolean equals(Object a, Object b) {
        return (a == b) || a.equals(b);
    }

    public Object create(Object parent, String name, boolean createDirectory) throws IOException {
        return create(parent, name, createDirectory, false);
    }

    public Object create(Object parent, String name, boolean createDirectory, boolean ensureNew) throws IOException {
        Object createImplRet;
        synchronized (this) {
            if (ensureNew) {
                String base, ext;
                int i = name.lastIndexOf('.');
                base = (i < 0) ? name : name.substring(0, i);
                ext = (i < 0) ? "" : name.substring(i);
                i = 0;
                while (getFile(parent, name) != null) {
                    name = base + ++i + ext;
                }
            }
            createImplRet = createImpl(parent, name, createDirectory);
        }
        return fireInserted(createImplRet);
    }

    protected abstract Object createImpl(Object parent, String name, boolean createDirectory) throws IOException;

    protected Object fireInserted(Object createImplRet) {
        if (createImplRet instanceof TreeModelEvent) {
            fireTreeModelEvent(NODES_INSERTED, (TreeModelEvent) createImplRet);
            return getFile((TreeModelEvent) createImplRet);
        } else {
            return createImplRet;
        }
    }

    public abstract InputStream getInputStream(Object file) throws IOException;

    public abstract OutputStream getOutputStream(Object file, boolean append) throws IOException;

    public Reader getReader(Object file) throws IOException {
        return new InputStreamReader(getInputStream(file), getCharset(file));
    }

    public Writer getWriter(Object file, boolean append) throws IOException {
        return new OutputStreamWriter(getOutputStream(file, append), getCharset(file));
    }

    private static Charset DEFAULT_CHARSET;

    private static synchronized Charset getDefaultCharset() {
        if (DEFAULT_CHARSET == null) {
            DEFAULT_CHARSET = Charset.forName(new InputStreamReader(new ByteArrayInputStream(new byte[0])).getEncoding());
        }
        return DEFAULT_CHARSET;
    }

    public Charset getCharset(Object file) {
        MimeType t = getMimeType(file);
        Charset cs = null;
        if (t != null) {
            String enc = t.getParameter("charset");
            if ((enc != null) && Charset.isSupported(enc)) {
                cs = Charset.forName(enc);
            }
        }
        if (cs == null) {
            cs = getDefaultCharset();
            if (t != null) {
                StringMap m = t.getParameters();
                m.put("charset", cs.name());
                t = new MimeType(t.getMediaType(), m, t.getRepresentationClass());
                setMimeType(file, t);
            }
        }
        return cs;
    }

    public void setFileNameMap(FileNameMap map) {
        fileNameMap = map;
    }

    public MimeType getMimeType(Object file) {
        MimeType t = MimeType.valueOf(getAttribute(file, Attributes.Name.CONTENT_TYPE));
        return (t != null) ? t : (fileNameMap == null) ? MimeType.OCTET_STREAM : MimeType.valueOf(fileNameMap.getContentTypeFor(getName(file)));
    }

    public void setMimeType(Object file, MimeType t) {
        if (t.getParameter("charset") == null) {
            MimeType o = MimeType.valueOf(getAttribute(file, Attributes.Name.CONTENT_TYPE));
            if (o != null) {
                String enc = o.getParameter("charset");
                if (enc != null) {
                    StringMap m = t.getParameters();
                    m.put("charset", enc);
                    t = new MimeType(t.getMediaType(), m, t.getRepresentationClass());
                }
            }
        }
        getAttributes(file, true).put(Attributes.Name.CONTENT_TYPE, t.toString());
    }

    public void setManifest(Manifest manifest) {
        if (manifest != this.manifest) {
            this.manifest = manifest;
        }
    }

    public final Manifest getManifest() {
        return manifest;
    }

    public synchronized Object getFile(String path) {
        if ("/".equals(path)) {
            return getRoot();
        }
        int p = (path.charAt(0) == '/') ? 1 : 0;
        Object f = getRoot();
        while (true) {
            int i = path.indexOf('/', p);
            f = getFile(f, (i >= 0) ? path.substring(p, i) : path.substring(p));
            p = i + 1;
            if ((f == null) || (i < 0) || (p == path.length())) {
                return f;
            }
        }
    }

    public synchronized String getPath(Object file) {
        if (equals(file, getRoot())) {
            return "/";
        }
        StringBuffer b = new StringBuffer(getName(file));
        while (true) {
            file = getParent(file);
            if (equals(file, getRoot())) {
                return b.toString();
            }
            b.insert(0, '/');
            b.insert(0, getName(file));
        }
    }

    public final String getPathWithLeadingSlash(Object file) {
        String p = getPath(file);
        return (p.charAt(0) == '/') ? p : '/' + p;
    }

    public synchronized TreePath getTreePath(Object file) {
        if (equals(file, getRoot())) {
            return new TreePath(file);
        }
        Object p = getParent(file);
        if (p == null) {
            return null;
        }
        TreePath t = getTreePath(p);
        return (t != null) ? t.pathByAddingChild(file) : null;
    }

    public synchronized TreeModelEvent getEventFor(Object file) {
        Object p = getParent(file);
        return new TreeModelEvent(this, getTreePath(p), new int[] { getIndexOfChild(p, file) }, new Object[] { file });
    }

    public int getChildCount(Object file) {
        return listFiles(file).length;
    }

    public int getIndexOfChild(Object parent, Object file) {
        Object[] f = listFiles(parent);
        if (f == null) {
            return -1;
        }
        for (int i = 0; i < f.length; i++) {
            if (equals(file, f[i])) {
                return i;
            }
        }
        return -1;
    }

    public Object getChild(Object parent, int index) {
        return listFiles(parent)[index];
    }

    public static boolean isContainedInChildren(Object file, TreeModelEvent e) {
        Object[] c = e.getChildren();
        if (c == null) {
            return false;
        }
        FileSystem fs = (FileSystem) e.getSource();
        for (int i = 0; i < c.length; i++) {
            if (fs.equals(file, c[i])) {
                return true;
            }
        }
        return false;
    }

    public static Object getFile(TreeModelEvent e) {
        Object[] c = e.getChildren();
        return (c == null) ? e.getTreePath().getLastPathComponent() : c[0];
    }

    public Attributes getAttributes(Object file, boolean create) {
        String n = getManifestName(file);
        if (n.length() == 0) {
            return manifest.getMainAttributes();
        }
        Attributes a = manifest.getAttributes(n);
        if ((a == null) && create) {
            manifest.getEntries().put(n, a = new Attributes());
        }
        return a;
    }

    public String getAttribute(Object file, Attributes.Name name) {
        Attributes a = getAttributes(file, false);
        return (a == null) ? null : a.getValue(name);
    }

    public String getAttribute(Object file, String name) {
        Attributes a = getAttributes(file, false);
        return (a == null) ? null : a.getValue(name);
    }

    protected synchronized String getManifestName(Object file) {
        StringBuffer b = new StringBuffer();
        while (true) {
            Object p = getParent(file);
            if (p == null) {
                return b.toString();
            }
            if (!isLeaf(file)) {
                b.insert(0, '/');
            }
            b.insert(0, getName(file));
            file = p;
        }
    }

    public void readJar(InputStream in, boolean verify) throws IOException {
        JarInputStream j = new JarInputStream(in, verify);
        setManifest(j.getManifest());
        JarEntry e;
        byte[] buf = new byte[0x40000];
        while ((e = j.getNextJarEntry()) != null) {
            if (!e.isDirectory()) {
                String name = e.getName();
                Object dir = getRoot();
                int i, p = 0;
                while ((i = name.indexOf('/', p)) >= 0) {
                    String n = name.substring(p, i);
                    Object sub = getFile(dir, n);
                    if (sub == null) {
                        sub = create(dir, n, true);
                    }
                    dir = sub;
                    p = i + 1;
                }
                Object file = create(dir, name.substring(p), false);
                OutputStream out = getOutputStream(file, false);
                while ((i = j.read(buf, 0, buf.length)) >= 0) {
                    if (out != null) {
                        out.write(buf, 0, i);
                    }
                }
                out.flush();
                out.close();
                if (e.getTime() > 0) {
                    setTime(file, e.getTime());
                }
            }
        }
    }

    public void writeJar(OutputStream out) throws IOException {
        JarOutputStream j = new JarOutputStream(out, getManifest());
        writeJar(j, new byte[0x40000], getRoot());
        j.flush();
        j.close();
    }

    private void writeJar(JarOutputStream out, byte[] buf, Object file) throws IOException {
        if (!isLeaf(file)) {
            Object[] files = listFiles(file);
            for (int i = 0; i < files.length; i++) {
                writeJar(out, buf, files[i]);
            }
        } else {
            ZipEntry z = new ZipEntry(getPath(file));
            z.setSize(getSize(file));
            z.setTime(getTime(file));
            out.putNextEntry(z);
            InputStream in = getInputStream(file);
            int i;
            while ((i = in.read(buf, 0, buf.length)) >= 0) {
                out.write(buf, 0, i);
            }
            in.close();
            out.closeEntry();
        }
    }

    public void copyFilesTo(Collection list, FileSystem fs) throws IOException {
        byte[] buf = new byte[0x40000];
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            copyFileTo(i.next(), fs, buf);
        }
    }

    public Object copyFileTo(Object file, FileSystem fs) throws IOException {
        return copyFileTo(file, fs, new byte[0x40000]);
    }

    public void copyFileTo(Object file, FileSystem fs, Object destFile) throws IOException {
        copyFileTo(file, fs, destFile, true, new byte[0x40000]);
    }

    public Object copyFileToDirectory(Object file, FileSystem fs, Object destDir) throws IOException {
        copyFileTo(file, fs, destDir = fs.create(destDir, getName(file), false), true, new byte[0x40000]);
        return destDir;
    }

    public Object copyFileToDirectory(Object file, FileSystem fs, Object destDir, String name) throws IOException {
        copyFileTo(file, fs, destDir = fs.create(destDir, name, false), true, new byte[0x40000]);
        return destDir;
    }

    private Object copyFileTo(Object file, FileSystem fs, byte[] buf) throws IOException {
        if (equals(file, getRoot())) {
            return fs.getRoot();
        }
        copyFileTo(file, fs, file = fs.create(getTargetDirectory(getParent(file), fs), getName(file), false), true, buf);
        return file;
    }

    private void copyFileTo(Object file, FileSystem fs, Object destFile, boolean copyAttributes, byte[] buf) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getInputStream(file);
            out = fs.getOutputStream(destFile, false);
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
            out.flush();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        fs.setTime(destFile, getTime(file));
        if (copyAttributes) {
            Attributes a = getAttributes(file, false);
            if ((a != null) && !a.isEmpty()) {
                fs.getAttributes(destFile, true).putAll(a);
            }
        }
    }

    public Object addLocalFile(File source, Object targetDir, String name) throws IOException {
        ((FileSystem) LocalFileSystem.FILE_ADAPTER).copyFileTo(source, this, targetDir = create(targetDir, name, false), false, new byte[0x40000]);
        return targetDir;
    }

    private Object getTargetDirectory(Object directory, FileSystem fs) throws IOException {
        return equals(directory, getRoot()) ? fs.getRoot() : fs.create(getTargetDirectory(getParent(directory), fs), getName(directory), true);
    }

    public void removeNonlistedAttributes(Collection list) {
        StringMap m = new StringMap(list.size());
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            m.put(getManifestName(i.next()), this);
        }
        Map e = getManifest().getEntries();
        Object[] keys = e.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            if (!m.containsKey(keys[i])) {
                e.remove(keys[i]);
            }
        }
    }

    public void valueForPathChanged(TreePath path, Object value) {
    }
}
