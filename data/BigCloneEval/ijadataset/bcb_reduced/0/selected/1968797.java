package com.ibm.wala.shrikeBT.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * This class provides functionality for performing offline instrumentation. It is subclassed with class-toolkit-specific
 * functionality.
 */
public abstract class OfflineInstrumenterBase {

    private int inputIndex;

    private final HashSet<String> entryNames = new HashSet<String>();

    private final ArrayList<Input> inputs = new ArrayList<Input>();

    private final BitSet ignoringInputs = new BitSet();

    private File outputFile;

    private boolean passUnmodifiedClasses = false;

    private JarOutputStream outputJar;

    private JarFile cachedJar;

    private File cachedJarFile;

    private ManifestBuilder manifestBuilder;

    /**
   * This installs a ManifestBuilder callback that this class will notify whenever an entry has been added to the output zip file.
   */
    public void setManifestBuilder(ManifestBuilder mb) {
        manifestBuilder = mb;
    }

    /**
   * Thiscallback is notified whenever an entry has been added to the output zip file.
   */
    public static interface ManifestBuilder {

        public void addEntry(ZipEntry ze);
    }

    /**
   * This class represents a resource which can be opened and read; either a file or a JAR entry.
   */
    abstract class Input {

        private String className;

        /**
     * Tell us what the classname is supposed to be, if it's a class file.
     */
        public final void setClassName(String c) {
            className = c.intern();
        }

        /**
     * Returns the classname if it has been set.
     */
        public final String getClassName() {
            return className;
        }

        /**
     * Open the resource for reading as a stream.
     */
        public abstract InputStream open() throws IOException;

        /**
     * @return true if this resource represents a class, false otherwise
     */
        public boolean isClass() {
            return true;
        }
    }

    /**
   * This class represents a JAR file entry. It might or might not be a class; we support non-class JAR resources so that we can
   * copy them to the output JAR if the client requests that.
   */
    final class JarInput extends Input {

        private final File file;

        private final String name;

        /**
     * Select a particular entry from a JAR file on disk.
     */
        public JarInput(File f, String je) {
            file = f;
            name = je;
        }

        @Override
        public InputStream open() throws IOException {
            JarFile cachedJar = openCachedJar(file);
            return cachedJar.getInputStream(cachedJar.getEntry(name));
        }

        @Override
        public String toString() {
            return file.getPath() + "#" + name;
        }

        @Override
        public boolean isClass() {
            return name.endsWith(".class");
        }

        /**
     * Get the underlying ZipEntry corresponding to this resource.
     */
        public ZipEntry getEntry() throws IOException {
            JarFile cachedJar = openCachedJar(file);
            return cachedJar.getEntry(name);
        }
    }

    /**
   * Open a JAR/ZIP file. This routine caches the last JAR file opened to save effort when the same file is accessed again and
   * again. DO NOT close the file returned by this routine until you've finished with this OfflineInstrumente completely. Also, this
   * JarFile will be closed the next time someone calls openCachedJar.
   */
    private JarFile openCachedJar(File file) throws IOException {
        if (cachedJarFile != null && cachedJarFile.equals(file)) {
            return cachedJar;
        } else {
            if (cachedJar != null) {
                cachedJar.close();
            }
            cachedJarFile = file;
            cachedJar = new JarFile(file);
            return cachedJar;
        }
    }

    /**
   * This class represents a plain old class file in the filesystem. Non-class file resources are not supported.
   */
    final class ClassInput extends Input {

        private final File file;

        public ClassInput(File f) {
            file = f;
        }

        @Override
        public InputStream open() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public String toString() {
            return file.getPath();
        }
    }

    protected OfflineInstrumenterBase() {
    }

    /**
   * Set the file in which instrumented classes will be deposited.
   */
    public final void setOutputJar(File f) {
        outputFile = f;
    }

    /**
   * Indicate whether classes which are not modified will be put into the output jar anyway.
   */
    public final void setPassUnmodifiedClasses(boolean pass) {
        passUnmodifiedClasses = pass;
    }

    /**
   * Add a JAR file containing source classes to instrument.
   */
    public final void addInputJar(File f) throws IOException {
        JarFile jf = new JarFile(f);
        for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements(); ) {
            JarEntry entry = e.nextElement();
            if (!entry.isDirectory()) {
                String name = entry.getName();
                inputs.add(new JarInput(f, name));
            }
        }
        jf.close();
    }

    /**
   * Add a JAR entry containing a source class to instrument.
   */
    public final void addInputJarEntry(File f, String name) throws IOException {
        inputs.add(new JarInput(f, name));
    }

    /**
   * Add a class file containing a source class to instrument.
   */
    public final void addInputClass(File f) {
        inputs.add(new ClassInput(f));
    }

    /**
   * Add a directory containing class files to instrument. All subdirectories are also scanned.
   * 
   * @throws IllegalArgumentException if d is null
   */
    public final void addInputDirectory(File d) throws IOException, IllegalArgumentException {
        if (d == null) {
            throw new IllegalArgumentException("d is null");
        }
        File[] fs = d.listFiles(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".class");
            }
        });
        if (fs == null) {
            throw new IllegalArgumentException("bad directory " + d.getAbsolutePath());
        }
        for (int i = 0; i < fs.length; i++) {
            File f = fs[i];
            if (f.isDirectory()) {
                addInputDirectory(f);
            } else {
                addInputClass(f);
            }
        }
    }

    /**
   * Add something to instrument --- the name of a JAR file, a class file, a directory or an entry within a jar file (as
   * filename#entryname). If we can't identify it, nothing is added and we return false.
   * 
   * @throws IllegalArgumentException if a is null
   */
    public final boolean addInputElement(String a) throws IOException {
        if (a == null) {
            throw new IllegalArgumentException("a is null");
        }
        try {
            int poundIndex = a.indexOf('#');
            if (poundIndex > 0) {
                addInputJarEntry(new File(a.substring(0, poundIndex)), a.substring(poundIndex + 1));
                return true;
            }
            File f = new File(a);
            if (f.isDirectory()) {
                addInputDirectory(f);
                return true;
            } else if (f.exists()) {
                if (a.endsWith(".class")) {
                    addInputClass(f);
                    return true;
                } else if (a.endsWith(".jar") || a.endsWith(".zip")) {
                    addInputJar(new File(a));
                    return true;
                }
            }
        } catch (IOException ex) {
            throw new IOException("Error reading input element '" + a + "': " + ex.getMessage());
        }
        return false;
    }

    /**
   * Parse an argument list to find elements to instrument and the name of the output file. The "-o filename" option selects the
   * output JAR file name. Any other argument not starting with "-" is added to the list of elements to instrument, if it appears to
   * be the name of a class file, JAR file, or directory. If any argument starting with "--" is encountered, the rest of the
   * command-line is considered leftover
   * 
   * @return the arguments that were not understood
   * @throws IllegalArgumentException if args == null
   */
    public final String[] parseStandardArgs(String[] args) throws IllegalArgumentException, IOException {
        if (args == null) {
            throw new IllegalArgumentException("args == null");
        }
        ArrayList<String> leftover = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a == null) {
                throw new IllegalArgumentException("args[" + i + "] is null");
            }
            if (a.equals("-o") && i + 1 < args.length) {
                setOutputJar(new File(args[i + 1]));
                i++;
                continue;
            } else if (!a.startsWith("-")) {
                if (addInputElement(a)) {
                    continue;
                }
            } else if (a.startsWith("--")) {
                for (int j = i; j < args.length; j++) {
                    leftover.add(args[j]);
                }
                break;
            }
            leftover.add(a);
        }
        String[] r = new String[leftover.size()];
        leftover.toArray(r);
        return r;
    }

    /**
   * @return the number of source classes to be instrumented
   */
    public final int getNumInputClasses() {
        return inputs.size();
    }

    /**
   * Read a list of class file names from a stream and add them to the list of things to instrument.
   */
    public final void readInputClasses(InputStream s) throws IOException, IllegalArgumentException {
        if (s == null) {
            throw new IllegalArgumentException("illegal null inputStream");
        }
        String str;
        BufferedReader r = new BufferedReader(new InputStreamReader(s));
        while ((str = r.readLine()) != null) {
            addInputElement(str);
        }
    }

    /**
   * Start traversing the source class list from the beginning.
   */
    public final void beginTraversal() {
        inputIndex = 0;
    }

    protected abstract Object makeClassFromStream(BufferedInputStream s) throws IOException;

    protected abstract String getClassName(Object cl);

    protected abstract void writeClassTo(Object cl, Object mods, OutputStream s) throws IOException;

    protected final Object internalNextClass() throws IOException {
        while (true) {
            if (inputIndex >= inputs.size()) {
                return null;
            } else {
                Input in = inputs.get(inputIndex);
                inputIndex++;
                if (ignoringInputs.get(inputIndex - 1) || !in.isClass()) {
                    continue;
                }
                BufferedInputStream s = new BufferedInputStream(in.open());
                try {
                    Object r = makeClassFromStream(s);
                    String name = getClassName(r);
                    in.setClassName(name);
                    return r;
                } finally {
                    s.close();
                }
            }
        }
    }

    private static String toEntryName(String className) {
        return className.replace('.', '/') + ".class";
    }

    /**
   * Get the name of the resource containing the last class returned. This is either a file name (e.g., "com/ibm/Main.class"), or a
   * JAR entry name (e.g., "apps/app.jar#com/ibm/Main.class").
   * 
   * @return the resource name, or null if no class has been returned yet
   */
    public final String getLastClassResourceName() {
        if (inputIndex < 1) {
            return null;
        } else {
            Input in = inputs.get(inputIndex - 1);
            return in.toString();
        }
    }

    /**
   * Returns the File we are storing classes into.
   */
    public final File getOutputFile() {
        return outputFile;
    }

    protected final boolean internalOutputModifiedClass(Object cf, Object mods) throws IOException {
        makeOutputJar();
        String name = toEntryName(getClassName(cf));
        if (entryNames.contains(name)) {
            return false;
        } else {
            putNextEntry(new ZipEntry(name));
            BufferedOutputStream s = new BufferedOutputStream(outputJar);
            writeClassTo(cf, mods, s);
            s.flush();
            outputJar.closeEntry();
            return true;
        }
    }

    /**
   * Set the JAR Comment for the output JAR.
   */
    public final void setJARComment(String comment) throws IOException, IllegalStateException {
        makeOutputJar();
        outputJar.setComment(comment);
    }

    final void makeOutputJar() throws IOException, IllegalStateException {
        if (outputJar == null) {
            if (outputFile == null) {
                throw new IllegalStateException("Output file was not set");
            }
            outputJar = new JarOutputStream(new FileOutputStream(outputFile));
        }
    }

    /**
   * Skip the last class returned in every future traversal of the class list.
   */
    public final void setIgnore(boolean ignore) throws IllegalArgumentException {
        if (inputIndex == 0) {
            throw new IllegalArgumentException("Must get a class before ignoring it");
        }
        ignoringInputs.set(inputIndex - 1);
    }

    private static byte[] cachedBuf;

    private static synchronized byte[] makeBuf() {
        if (cachedBuf != null) {
            byte[] r = cachedBuf;
            cachedBuf = null;
            return r;
        } else {
            return new byte[60000];
        }
    }

    private static synchronized void releaseBuf(byte[] buf) {
        cachedBuf = buf;
    }

    public static void copyStream(InputStream in, OutputStream out) throws IllegalArgumentException, IOException {
        if (in == null) {
            throw new IllegalArgumentException("in == null");
        }
        byte[] buf = makeBuf();
        try {
            while (true) {
                int read = in.read(buf);
                if (read < 0) {
                    return;
                }
                out.write(buf, 0, read);
            }
        } finally {
            releaseBuf(buf);
        }
    }

    /**
   * Add a raw ZipEntry to the output JAR. Call endOutputJarEntry() when you're done.
   * 
   * @return the OutputStream to be used to write the entry contents
   */
    public final OutputStream addOutputJarEntry(ZipEntry ze) throws IOException, IllegalStateException {
        if (outputJar == null) {
            throw new IllegalStateException("output jar is null");
        }
        putNextEntry(ze);
        return outputJar;
    }

    /**
   * Complete and flush the entry initiated by addOutputJarEntry.
   */
    public final void endOutputJarEntry() throws IOException, IllegalStateException {
        if (outputJar == null) {
            throw new IllegalStateException("output jar is null");
        }
        outputJar.closeEntry();
    }

    /**
   * Call this to copy any unmodified classes to the output. This is called automatically by close(); you should only call this if
   * you want to write an entry to the JAR file *after* the unmodified classes. This will only ever be called once per output JAR.
   */
    public final void writeUnmodifiedClasses() throws IOException, IllegalStateException {
        passUnmodifiedClasses = false;
        makeOutputJar();
        for (int i = 0; i < inputs.size(); i++) {
            Input in = inputs.get(i);
            if (!in.isClass()) {
                if (in instanceof JarInput) {
                    JarInput jin = (JarInput) in;
                    ZipEntry entry = jin.getEntry();
                    InputStream s = jin.open();
                    try {
                        ZipEntry newEntry = new ZipEntry(entry.getName());
                        newEntry.setComment(entry.getComment());
                        newEntry.setExtra(entry.getExtra());
                        newEntry.setTime(entry.getTime());
                        putNextEntry(newEntry);
                        copyStream(s, outputJar);
                        outputJar.closeEntry();
                    } finally {
                        s.close();
                    }
                } else {
                    throw new Error("Unknown non-class input: " + in);
                }
            } else {
                String name = in.getClassName();
                if (name == null) {
                    BufferedInputStream s = new BufferedInputStream(in.open(), 65536);
                    try {
                        Object cl = makeClassFromStream(s);
                        String entryName = toEntryName(getClassName(cl));
                        if (!entryNames.contains(entryName)) {
                            putNextEntry(new ZipEntry(entryName));
                            BufferedOutputStream clOut = new BufferedOutputStream(outputJar);
                            writeClassTo(cl, null, clOut);
                            clOut.flush();
                            outputJar.closeEntry();
                        }
                    } finally {
                        s.close();
                    }
                } else {
                    String entryName = toEntryName(name);
                    if (!entryNames.contains(entryName)) {
                        BufferedInputStream s = new BufferedInputStream(in.open());
                        try {
                            putNextEntry(new ZipEntry(entryName));
                            BufferedOutputStream clOut = new BufferedOutputStream(outputJar);
                            copyStream(s, clOut);
                            clOut.flush();
                            outputJar.closeEntry();
                        } finally {
                            s.close();
                        }
                    }
                }
            }
        }
    }

    /**
   * Call this when you're done modifying classes.
   */
    public final void close() throws IOException, IllegalStateException {
        if (passUnmodifiedClasses) {
            writeUnmodifiedClasses();
        }
        if (outputJar != null) {
            outputJar.close();
        }
        if (cachedJar != null) {
            cachedJar.close();
        }
    }

    private void putNextEntry(ZipEntry newEntry) throws IOException, IllegalStateException {
        if (outputJar == null) {
            throw new IllegalStateException();
        }
        outputJar.putNextEntry(newEntry);
        entryNames.add(newEntry.getName());
        if (manifestBuilder != null) {
            manifestBuilder.addEntry(newEntry);
        }
    }
}
