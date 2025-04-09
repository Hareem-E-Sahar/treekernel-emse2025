import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @author Eric Bruneton
 */
public class Adapt extends ClassLoader {

    protected synchronized Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.")) {
            System.err.println("Adapt: loading class '" + name + "' without on the fly adaptation");
            return super.loadClass(name, resolve);
        } else {
            System.err.println("Adapt: loading class '" + name + "' with on the fly adaptation");
        }
        String resource = name.replace('.', '/') + ".class";
        InputStream is = getResourceAsStream(resource);
        byte[] b;
        try {
            ClassReader cr = new ClassReader(is);
            ClassWriter cw = new ClassWriter(0);
            ClassVisitor cv = new TraceFieldClassAdapter(cw);
            cr.accept(cv, 0);
            b = cw.toByteArray();
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }
        try {
            FileOutputStream fos = new FileOutputStream(resource + ".adapted");
            fos.write(b);
            fos.close();
        } catch (Exception e) {
        }
        return defineClass(name, b, 0, b.length);
    }

    public static void main(final String args[]) throws Exception {
        ClassLoader loader = new Adapt();
        Class c = loader.loadClass(args[0]);
        Method m = c.getMethod("main", new Class[] { String[].class });
        String[] applicationArgs = new String[args.length - 1];
        System.arraycopy(args, 1, applicationArgs, 0, applicationArgs.length);
        m.invoke(null, new Object[] { applicationArgs });
    }
}

class TraceFieldClassAdapter extends ClassAdapter implements Opcodes {

    private String owner;

    public TraceFieldClassAdapter(final ClassVisitor cv) {
        super(cv);
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        owner = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        if ((access & ACC_STATIC) == 0) {
            Type t = Type.getType(desc);
            int size = t.getSize();
            String gDesc = "()" + desc;
            MethodVisitor gv = cv.visitMethod(ACC_PRIVATE, "_get" + name, gDesc, null, null);
            gv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
            gv.visitLdcInsn("_get" + name + " called");
            gv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
            gv.visitVarInsn(ALOAD, 0);
            gv.visitFieldInsn(GETFIELD, owner, name, desc);
            gv.visitInsn(t.getOpcode(IRETURN));
            gv.visitMaxs(1 + size, 1);
            gv.visitEnd();
            String sDesc = "(" + desc + ")V";
            MethodVisitor sv = cv.visitMethod(ACC_PRIVATE, "_set" + name, sDesc, null, null);
            sv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
            sv.visitLdcInsn("_set" + name + " called");
            sv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
            sv.visitVarInsn(ALOAD, 0);
            sv.visitVarInsn(t.getOpcode(ILOAD), 1);
            sv.visitFieldInsn(PUTFIELD, owner, name, desc);
            sv.visitInsn(RETURN);
            sv.visitMaxs(1 + size, 1 + size);
            sv.visitEnd();
        }
        return fv;
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return mv == null ? null : new TraceFieldCodeAdapter(mv, owner);
    }
}

class TraceFieldCodeAdapter extends MethodAdapter implements Opcodes {

    private String owner;

    public TraceFieldCodeAdapter(final MethodVisitor mv, final String owner) {
        super(mv);
        this.owner = owner;
    }

    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        if (owner.equals(this.owner)) {
            if (opcode == GETFIELD) {
                String gDesc = "()" + desc;
                visitMethodInsn(INVOKESPECIAL, owner, "_get" + name, gDesc);
                return;
            } else if (opcode == PUTFIELD) {
                String sDesc = "(" + desc + ")V";
                visitMethodInsn(INVOKESPECIAL, owner, "_set" + name, sDesc);
                return;
            }
        }
        super.visitFieldInsn(opcode, owner, name, desc);
    }
}
