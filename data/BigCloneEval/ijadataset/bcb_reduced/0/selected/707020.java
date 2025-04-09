package bsh.org.objectweb.asm.optimizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import bsh.org.objectweb.asm.ClassReader;
import bsh.org.objectweb.asm.FieldVisitor;
import bsh.org.objectweb.asm.MethodVisitor;
import bsh.org.objectweb.asm.commons.EmptyVisitor;

/**
 * A Jar file optimizer.
 * 
 * @author Eric Bruneton
 */
public class JarOptimizer {

    private static final Set API = new HashSet();

    private static final Map HIERARCHY = new HashMap();

    public static void main(final String[] args) throws IOException {
        File f = new File(args[0]);
        InputStream is = new GZIPInputStream(new FileInputStream(f));
        BufferedReader lnr = new LineNumberReader(new InputStreamReader(is));
        while (true) {
            String line = lnr.readLine();
            if (line != null) {
                if (line.startsWith("class")) {
                    String c = line.substring(6, line.lastIndexOf(' '));
                    String sc = line.substring(line.lastIndexOf(' ') + 1);
                    HIERARCHY.put(c, sc);
                } else {
                    API.add(line);
                }
            } else {
                break;
            }
        }
        optimize(new File(args[1]));
    }

    static void optimize(final File f) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; ++i) {
                optimize(files[i]);
            }
        } else if (f.getName().endsWith(".jar")) {
            File g = new File(f.getParentFile(), f.getName() + ".new");
            ZipFile zf = new ZipFile(f);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(g));
            Enumeration e = zf.entries();
            byte[] buf = new byte[10000];
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (ze.isDirectory()) {
                    continue;
                }
                out.putNextEntry(ze);
                if (ze.getName().endsWith(".class")) {
                    ClassReader cr = new ClassReader(zf.getInputStream(ze));
                    cr.accept(new ClassVerifier(), 0);
                }
                InputStream is = zf.getInputStream(ze);
                int n;
                do {
                    n = is.read(buf, 0, buf.length);
                    if (n != -1) {
                        out.write(buf, 0, n);
                    }
                } while (n != -1);
                out.closeEntry();
            }
            out.close();
            zf.close();
            f.delete();
            g.renameTo(f);
        }
    }

    static class ClassDump extends EmptyVisitor {

        String owner;

        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            owner = name;
            if (owner.startsWith("java/")) {
                System.out.println("class " + name + ' ' + superName);
            }
        }

        public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
            if (owner.startsWith("java/")) {
                System.out.println(owner + ' ' + name);
            }
            return null;
        }

        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            if (owner.startsWith("java/")) {
                System.out.println(owner + ' ' + name + desc);
            }
            return null;
        }
    }

    static class ClassVerifier extends EmptyVisitor {

        String owner;

        String method;

        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            owner = name;
        }

        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            method = name + desc;
            return this;
        }

        public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
            check(owner, name);
        }

        public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
            check(owner, name + desc);
        }

        private void check(String owner, String member) {
            if (owner.startsWith("java/")) {
                String o = owner;
                while (o != null) {
                    if (API.contains(o + ' ' + member)) {
                        return;
                    }
                    o = (String) HIERARCHY.get(o);
                }
                System.out.println("WARNING: " + owner + ' ' + member + " called in " + this.owner + ' ' + method + " is not defined in JDK 1.3 API");
            }
        }
    }
}
