package tester;

import org.objectweb.asm.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BytecodeRewrite {

    static class ClassVisitorImplementation extends ClassAdapter {

        public ClassVisitorImplementation(ClassVisitor cv) {
            super(cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv;
            mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if (mv != null) {
                mv = new MethodVisitorImplementation(mv);
            }
            return mv;
        }
    }

    static class MethodVisitorImplementation extends MethodAdapter {

        public MethodVisitorImplementation(MethodVisitor mv) {
            super(mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (owner.equals("com/google/gwt/core/ext/typeinfo/JClassType") || owner.equals("com/google/gwt/core/ext/typeinfo/JPackage") || owner.equals("com/google/gwt/core/ext/typeinfo/JMethod") || owner.equals("com/google/gwt/core/ext/typeinfo/JType") || owner.equals("com/google/gwt/core/ext/typeinfo/JField") || owner.equals("com/google/gwt/core/ext/typeinfo/JParameterizedType") || owner.equals("com/google/gwt/core/ext/typeinfo/JParameter")) {
                if (opcode != Opcodes.INVOKEINTERFACE) {
                    System.out.println("OOps");
                    super.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, name, desc);
                    return;
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    public static void main(String[] args) throws IOException {
        for (File jarFile : new File("d:\\Work\\Stuff\\serendepity-tutorial\\Serendipity 3.0\\target\\serendipity-3.0\\WEB-INF\\lib\\").listFiles()) {
            if (jarFile.isDirectory()) {
                continue;
            }
            ZipInputStream file = new ZipInputStream(new FileInputStream(jarFile));
            ZipEntry ze;
            String outFile = jarFile.getParent() + "/1/" + jarFile.getName();
            ZipOutputStream output = new ZipOutputStream(new FileOutputStream(outFile));
            System.out.println("Input:" + jarFile.getAbsolutePath());
            System.out.println("Otput:" + outFile);
            while ((ze = file.getNextEntry()) != null) {
                output.putNextEntry(new ZipEntry(ze.getName()));
                if (ze.getName().endsWith(".class")) {
                    ClassReader reader = new ClassReader(file);
                    ClassWriter cw = new ClassWriter(0);
                    reader.accept(new ClassVisitorImplementation(cw), ClassReader.SKIP_FRAMES);
                    output.write(cw.toByteArray());
                } else {
                    byte[] data = new byte[16 * 1024];
                    while (true) {
                        int r = file.read(data);
                        if (r == -1) {
                            break;
                        }
                        output.write(data, 0, r);
                    }
                }
            }
            output.flush();
            output.close();
        }
    }
}
