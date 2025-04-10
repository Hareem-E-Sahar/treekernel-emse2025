package org.jpc.emulator.memory.codeblock.fastcompiler;

import java.io.*;
import org.javanile.wrapper.java.util.MissingResourceException;
import org.javanile.wrapper.java.util.logging.*;
import org.javanile.wrapper.java.util.zip.ZipEntry;
import org.javanile.wrapper.java.util.zip.ZipOutputStream;
import org.jpc.emulator.memory.codeblock.CodeBlock;
import org.jpc.classfile.ClassFile;

/**
 * Provides access to <code>ClassFile</code> instances constructed from the
 * various types of template classes used for building compiled code blocks.
 * <p>
 * Also provides the class-loading facilities used by the background compiler.
 * @author Chris Dennis
 */
public class ClassFileBuilder {

    private static final Logger LOGGING = Logger.getLogger(ClassFileBuilder.class.getName());

    private static final int CLASSES_PER_LOADER = 10;

    private static final InputStream realModeSkeleton, virtual8086ModeSkeleton, protectedModeSkeleton;

    private static CustomClassLoader currentClassLoader;

    private static ZipOutputStream zip;

    private static boolean saveClasses = false;

    static {
        try {
            realModeSkeleton = loadSkeletonClass(org.jpc.emulator.memory.codeblock.fastcompiler.real.RealModeSkeletonBlock.class);
            virtual8086ModeSkeleton = loadSkeletonClass(org.jpc.emulator.memory.codeblock.fastcompiler.virt.Virtual8086ModeSkeletonBlock.class);
            protectedModeSkeleton = loadSkeletonClass(org.jpc.emulator.memory.codeblock.fastcompiler.prot.ProtectedModeSkeletonBlock.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        newClassLoader();
    }

    public static void startSavingClasses(File f) {
        try {
            zip = new ZipOutputStream(new FileOutputStream((f)));
        } catch (IOException ex) {
            Logger.getLogger(ClassFileBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        saveClasses = true;
    }

    public static void finishSavingClasses() {
        try {
            zip.finish();
        } catch (IOException ex) {
            Logger.getLogger(ClassFileBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        saveClasses = false;
    }

    private static InputStream loadSkeletonClass(Class clz) throws IOException {
        InputStream in = clz.getResourceAsStream(clz.getSimpleName() + ".class");
        if (in == null) {
            throw new MissingResourceException("Skeleton class not found, mistake in packaging?", clz.getName(), clz.getSimpleName() + ".class");
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            while (true) {
                int ch = in.read();
                if (ch < 0) {
                    break;
                }
                bout.write((byte) ch);
            }
        } catch (IOException e) {
            LOGGING.log(Level.WARNING, "Exception reading skeleton class", e);
            throw e;
        }
        return new ByteArrayInputStream(bout.toByteArray());
    }

    private ClassFileBuilder() {
    }

    /**
     * Creates a new <code>ClassFile</code> instance from the real-mode
     * template class.
     * @return Real-mode codeblock template.
     */
    public static ClassFile createNewRealModeSkeletonClass() {
        ClassFile cf = new ClassFile();
        try {
            realModeSkeleton.reset();
            cf.read(realModeSkeleton);
        } catch (IOException e) {
            LOGGING.log(Level.WARNING, "class file did not fully parse", e);
        }
        return cf;
    }

    public static ClassFile createNewVirtual8086ModeSkeletonClass() {
        ClassFile cf = new ClassFile();
        try {
            virtual8086ModeSkeleton.reset();
            DataInputStream dis = new DataInputStream(virtual8086ModeSkeleton);
            cf.read(dis);
        } catch (IOException e) {
            LOGGING.log(Level.WARNING, "class file did not fully parse", e);
        }
        return cf;
    }

    /**
     * Creates a new <code>ClassFile</code> instance from the protected-mode
     * template class.
     * @return Protected-mode codeblock template.
     */
    public static ClassFile createNewProtectedModeSkeletonClass() {
        ClassFile cf = new ClassFile();
        try {
            protectedModeSkeleton.reset();
            cf.read(protectedModeSkeleton);
        } catch (IOException e) {
            LOGGING.log(Level.WARNING, "class file did not fully parse", e);
        }
        return cf;
    }

    /**
     * Returns a new instance of the class represented by the <code>ClassFile</code>
     * object.
     * @param cf Class to be loaded and instantiated.
     * @return Singleton instance of the given class.
     */
    public static CodeBlock instantiateClass(ClassFile cf) throws InstantiationException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            cf.write(bos);
        } catch (IOException e) {
            LOGGING.log(Level.WARNING, "class file couldn't be written out", e);
        }
        byte[] classBytes = bos.toByteArray();
        Class codeBlockClass = currentClassLoader.createClass(cf.getClassName(), classBytes);
        if (saveClasses) {
            try {
                ZipEntry entry = new ZipEntry(cf.getClassName().replace('.', '/') + ".class");
                zip.putNextEntry(entry);
                zip.write(classBytes);
                zip.flush();
                zip.closeEntry();
            } catch (IOException ex) {
                Logger.getLogger(ClassFileBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            return (CodeBlock) codeBlockClass.newInstance();
        } catch (IllegalAccessException e) {
            LOGGING.log(Level.WARNING, "insufficient access rights to instantiate class", e);
            throw (InstantiationException) new InstantiationException().initCause(e);
        }
    }

    public static ClassLoader getClassloader() {
        return currentClassLoader;
    }

    private static void newClassLoader() {
        LOGGING.fine("creating new CustomClassLoader instance");
        currentClassLoader = new CustomClassLoader();
    }

    private static class CustomClassLoader extends ClassLoader {

        private int classesCount;

        public CustomClassLoader() {
            super(CustomClassLoader.class.getClassLoader());
        }

        public Class createClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException(name);
        }
    }
}
