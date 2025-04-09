package fanc.jcompiler;

import java.io.*;
import java.util.zip.*;
import fanc.*;
import fanc.ast.*;
import fanc.jemit.*;

/**
 * JCompiler is used to compile Fan to Java jars.
 *
 * @author  Brian Frank
 */
public class JCompiler extends fanc.Compiler {

    public JCompiler() {
        this.lib = Env.get().jlibDefault();
    }

    public void byline() {
        log("JCompile", srcDir);
    }

    protected LibFinder makeLibFinder() {
        return new JLibFinder(this);
    }

    public Class assembleToClass(ClassDef classDef) throws Exception {
        JAssembler asm = new JAssembler(this, classDef);
        asm.assemble();
        asmToLoad = asm;
        return asmClassLoader.loadClass(classDef.qname);
    }

    public void assemble() {
        File zipFile = new File(outDir, moduleName + ".jar");
        log("Assemble", zipFile);
        ZipOutputStream out = openZip(zipFile);
        log().indent();
        try {
            for (int i = 0; i < classDefs.length; ++i) {
                ClassDef def = classDefs[i];
                if (isVerbose()) verbose("Assemble", def.qname);
                JAssembler asm = new JAssembler(this, def);
                asm.assemble();
                writeClassFile(zipFile, out, asm);
            }
        } finally {
            log().unindent();
            closeZip(zipFile, out);
        }
    }

    private void writeClassFile(File zipFile, ZipOutputStream out, JAssembler asm) {
        ClassDef def = asm.def;
        Box box = asm.classFile;
        String path = def.ns.replace('.', '/') + '/' + def.name + ".class";
        try {
            out.putNextEntry(new ZipEntry(path));
            out.write(box.buf, 0, box.len);
            out.closeEntry();
        } catch (IOException e) {
            throw err("Cannot write zip entry " + path, zipFile);
        }
    }

    public static final int PUBLIC = 0x0001;

    public static final int PRIVATE = 0x0002;

    public static final int PROTECTED = 0x0004;

    public static final int STATIC = 0x0008;

    public static final int FINAL = 0x0010;

    public static final int SUPER = 0x0020;

    public static final int VOLATILE = 0x0040;

    public static final int INTERFACE = 0x0200;

    public static final int ABSTRACT = 0x0400;

    public static final int TRANSIENT = 0x0800;

    static AsmClassLoader asmClassLoader = new AsmClassLoader();

    static JAssembler asmToLoad;

    static void dumpToFile(JAssembler asm) {
        try {
            File f = new File(asm.def.name + ".class");
            System.out.println("Dump: " + f);
            FileOutputStream out = new FileOutputStream(f);
            out.write(asm.classFile.buf, 0, asm.classFile.len);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class AsmClassLoader extends ClassLoader {

        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (asmToLoad != null && asmToLoad.def.qname.equals(name)) {
                Box box = asmToLoad.classFile;
                Class cls = defineClass(name, box.buf, 0, box.len);
                asmToLoad = null;
                if (resolve) resolveClass(cls);
                return cls;
            }
            return findSystemClass(name);
        }
    }
}
