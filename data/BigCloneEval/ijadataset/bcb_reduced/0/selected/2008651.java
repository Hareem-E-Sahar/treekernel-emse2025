package org.ibex.util;

import java.util.*;
import java.io.*;
import java.util.zip.*;
import org.apache.bcel.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.util.*;

public class NanoGoat {

    public static final boolean deleteMethods = false;

    public static SyntheticRepository repo = null;

    public static HashSet dest = new HashSet();

    public static HashSet constructed = new HashSet();

    public static String outdir = ".";

    public static Hashtable subclasses = new Hashtable();

    public static Hashtable uponconstruction = new Hashtable();

    public static Hashtable mark_if_constructed = new Hashtable();

    public static int level = 0;

    public NanoGoat() {
    }

    public void loadAllMethods(String classname) throws Exception {
        visitJavaClass(repo.loadClass(classname));
        Method[] meths = getMethods(repo.loadClass(classname));
        for (int i = 0; i < meths.length; i++) visitJavaMethod(repo.loadClass(classname), meths[i]);
    }

    public void loadAllStaticMethods(String classname) throws Exception {
        visitJavaClass(repo.loadClass(classname));
        Method[] meths = getMethods(repo.loadClass(classname));
        for (int i = 0; i < meths.length; i++) if (meths[i].isStatic()) visitJavaMethod(repo.loadClass(classname), meths[i]);
    }

    public void loadMethod(String classAndMethodName) throws Exception {
        String classname = classAndMethodName.substring(0, classAndMethodName.lastIndexOf('.'));
        String methodname = classAndMethodName.substring(classAndMethodName.lastIndexOf('.') + 1);
        if (classname.endsWith("." + methodname)) methodname = "<init>";
        visitJavaClass(repo.loadClass(classname));
        Method[] meths = getMethods(repo.loadClass(classname));
        for (int i = 0; i < meths.length; i++) if (meths[i].getName().equals(methodname)) visitJavaMethod(repo.loadClass(classname), meths[i]);
    }

    public static void main(String[] args) throws Exception {
        int start = 1;
        repo = SyntheticRepository.getInstance(new ClassPath(args[0]));
        NanoGoat bcp = new NanoGoat();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        for (String s = br.readLine(); s != null; s = br.readLine()) {
            s = s.trim();
            if (s.length() == 0) continue;
            try {
                if (s.endsWith("$")) s = s.substring(0, s.length() - 1);
                if (s.endsWith(".class")) {
                    bcp.visitJavaClass(repo.loadClass(s.substring(0, s.length() - 6)));
                } else {
                    JavaClass cl = repo.loadClass(s.substring(0, s.lastIndexOf('.')));
                    ;
                    bcp.visitJavaClass(cl);
                    bcp.loadMethod(s);
                    Field[] fields = cl.getFields();
                    for (int j = 0; j < fields.length; j++) {
                        if (fields[j].getName().equals(s.substring(s.lastIndexOf('.') + 1))) bcp.visitJavaField(fields[j], cl);
                    }
                }
            } catch (Exception e) {
                System.out.println("WARNING: couldn't load class for " + s);
                e.printStackTrace();
            }
        }
        System.out.println("\n\n======================================================================\n");
        bcp.loadMethod("java.lang.Thread.run");
        bcp.loadAllMethods("java.lang.SecurityContext");
        bcp.loadAllMethods("java.lang.ThreadDeath");
        bcp.loadMethod("java.lang.Thread.run");
        bcp.loadMethod("java.lang.ref.Reference.enqueue");
        bcp.loadAllMethods("gnu.gcj.runtime.StringBuffer");
        bcp.loadAllMethods("gnu.gcj.convert.Input_UTF8");
        bcp.loadAllMethods("gnu.gcj.convert.Output_UTF8");
        bcp.loadMethod("gnu.gcj.convert.BytesToUnicode.done");
        bcp.loadAllStaticMethods("java.lang.reflect.Modifier");
        bcp.loadAllMethods("org.ibex.js.Interpreter$TryMarker");
        bcp.loadAllMethods("org.ibex.js.Interpreter$CatchMarker");
        bcp.loadAllMethods("org.ibex.js.Interpreter$LoopMarker");
        bcp.loadAllMethods("org.ibex.js.Interpreter$FinallyData");
        bcp.loadAllMethods("org.ibex.js.Interpreter$CallMarker");
        bcp.loadAllMethods("org.ibex.js.Interpreter");
        bcp.loadAllMethods("org.ibex.js.Interpreter$1");
        bcp.loadAllMethods("org.ibex.js.Interpreter$Stub");
        bcp.loadAllMethods("org.ibex.js.Trap$TrapScope");
        bcp.loadMethod("org.ibex.js.JSScope.top");
        bcp.loadAllMethods("org.ibex.graphics.Picture$1");
        bcp.loadAllMethods("org.ibex.core.Vexi$Blessing");
        bcp.loadAllMethods("org.ibex.net.HTTP$HTTPInputStream");
        bcp.visitJavaClass(repo.loadClass("org.ibex.net.SSL"));
        bcp.loadAllMethods("java.util.Hashtable$HashIterator");
        bcp.loadMethod("java.util.SimpleTimeZone.useDaylightTime");
        bcp.visitJavaClass(repo.loadClass("gnu.gcj.runtime.FinalizerThread"));
        bcp.visitJavaClass(repo.loadClass("gnu.gcj.runtime.FirstThread"));
        bcp.loadAllMethods("org.ibex.plat.Linux");
        bcp.loadAllMethods("org.ibex.plat.X11");
        bcp.loadAllMethods("org.ibex.plat.GCJ");
        bcp.loadAllMethods("org.ibex.plat.POSIX");
        bcp.loadAllMethods("org.ibex.plat.X11$X11Surface");
        bcp.loadAllMethods("org.ibex.plat.X11$X11PixelBuffer");
        bcp.loadAllMethods("org.ibex.plat.X11$X11Picture");
        bcp.loadAllMethods("org.ibex.graphics.Surface");
        bcp.loadAllMethods("org.ibex.graphics.Picture");
        bcp.loadAllMethods("org.ibex.graphics.PixelBuffer");
        bcp.loadMethod("org.ibex.plat.Linux.main");
        System.out.println();
        System.out.println("Dumping...");
        ZipFile zf = new ZipFile(args[0]);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(args[0] + ".tmp"));
        Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = ((ZipEntry) e.nextElement());
            String ss = ze.getName();
            if (!ss.endsWith(".class")) continue;
            ss = ss.substring(0, ss.length() - 6);
            ss = ss.replace('/', '.');
            dump(repo.loadClass(ss), zos);
        }
        zos.close();
        zf.close();
        new File(args[0] + ".tmp").renameTo(new File(args[0] + ".pruned"));
    }

    public static void dump(JavaClass clazz, ZipOutputStream zos) throws Exception {
        if (!dest.contains(clazz)) return;
        ConstantPoolGen newcpg = new ConstantPoolGen(clazz.getConstantPool());
        ClassGen cg = new ClassGen(clazz);
        InstructionFactory factory = new InstructionFactory(cg, newcpg);
        cg.setMajor(46);
        cg.setMinor(0);
        cg.setConstantPool(newcpg);
        boolean isconstructed = false;
        Method[] methods = getMethods(clazz);
        for (int i = 0; i < methods.length; i++) if (dest.contains(methods[i]) && methods[i].getName().equals("<init>")) isconstructed = true;
        Field[] fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            if ((!dest.contains(fields[i]) && fields[i].isStatic()) || ((!(constructed.contains(clazz))) && !fields[i].isStatic())) {
                System.out.println("  pruning field " + clazz.getClassName() + "." + fields[i].getName());
            }
        }
        int numMethods = 0;
        boolean good = false;
        for (int i = 0; i < methods.length; i++) {
            if (dest.contains(methods[i]) && (isconstructed || methods[i].isStatic())) {
                good = true;
            } else {
                if (methods[i].getCode() == null) {
                    System.out.println("  empty codeblock: " + clazz.getClassName() + "." + methods[i].getName());
                } else {
                    System.out.println("  pruning " + (isconstructed ? "" : "unconstructed") + " method " + clazz.getClassName() + "." + methods[i].getName());
                    if (deleteMethods) {
                        cg.removeMethod(methods[i]);
                        continue;
                    }
                    MethodGen mg = new MethodGen(methods[i], clazz.getClassName(), newcpg);
                    mg.removeExceptions();
                    InstructionList il = new InstructionList();
                    mg.setInstructionList(il);
                    InstructionHandle ih_0 = il.append(factory.createNew("java.lang.UnsatisfiedLinkError"));
                    il.append(InstructionConstants.DUP);
                    il.append(factory.createInvoke("java.lang.UnsatisfiedLinkError", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
                    il.append(InstructionConstants.ATHROW);
                    mg.setMaxStack();
                    mg.setMaxLocals();
                    mg.removeExceptions();
                    mg.removeLocalVariables();
                    mg.removeExceptionHandlers();
                    mg.removeLineNumbers();
                    cg.replaceMethod(methods[i], mg.getMethod());
                    il.dispose();
                }
            }
        }
        good = true;
        if (!good && !clazz.isAbstract() && !clazz.isInterface()) {
            System.out.println("DROPPING " + clazz.getClassName());
            JavaClass[] ifaces = clazz.getInterfaces();
            String[] ifacestrings = new String[ifaces.length];
            for (int i = 0; i < ifaces.length; i++) ifacestrings[i] = ifaces[i].getClassName();
            cg = new ClassGen(clazz.getClassName(), clazz.getSuperClass().getClassName(), clazz.getFileName(), clazz.getAccessFlags(), ifacestrings, newcpg);
        } else {
            System.out.println("dumping " + clazz.getClassName());
        }
        FilterOutputStream noclose = new FilterOutputStream(zos) {

            public void close() throws IOException {
                flush();
            }
        };
        zos.putNextEntry(new ZipEntry(clazz.getClassName().replace('.', '/') + ".class"));
        cg.getJavaClass().dump(noclose);
        noclose.flush();
    }

    public JavaClass sig2class(String sig) throws Exception {
        if (sig == null) return null;
        while (sig.length() > 0 && (sig.charAt(0) == 'L' || sig.charAt(0) == '[')) {
            if (sig.charAt(0) == 'L') sig = sig.substring(1, sig.length() - 1); else if (sig.charAt(0) == '[') sig = sig.substring(1, sig.length());
        }
        if (sig.length() <= 1) return null;
        if (sig.equals("<null object>")) return null;
        if (sig.startsWith("<return address")) return null;
        return repo.loadClass(sig);
    }

    public void load(String sig) throws Exception {
        if (sig == null) return;
        while (sig.length() > 0 && (sig.charAt(0) == 'L' || sig.charAt(0) == '[')) {
            if (sig.charAt(0) == 'L') sig = sig.substring(1, sig.length() - 1); else if (sig.charAt(0) == '[') sig = sig.substring(1, sig.length());
        }
        if (sig.length() <= 1) return;
        if (sig.equals("<null object>")) return;
        if (sig.startsWith("<return address")) return;
        visitJavaClass(repo.loadClass(sig));
    }

    public void load(Type t) throws Exception {
        if (t == null) return;
        if (t instanceof ArrayType) load(((ArrayType) t).getElementType());
        if (!(t instanceof ObjectType)) return;
        load(((ObjectType) t).getClassName());
    }

    public String getMethodSignature(Method m, ConstantPoolGen cpg) throws Exception {
        return m.getName() + m.getSignature();
    }

    public String getMethodSignature(InvokeInstruction ii, ConstantPoolGen cpg) throws Exception {
        String sig = "";
        Type[] argtypes = ii.getArgumentTypes(cpg);
        for (int j = 0; j < argtypes.length; j++) sig += argtypes[j].getSignature();
        return ii.getMethodName(cpg) + "(" + sig + ")" + ii.getReturnType(cpg).getSignature();
    }

    public void visitJavaMethod(JavaClass jc, Method method) throws Exception {
        visitJavaClass(jc);
        if (jc.getClassName().indexOf("SharedLib") != -1) return;
        if (jc.getClassName().indexOf("Datagram") != -1) return;
        if (jc.getClassName().startsWith("java.io.Object")) return;
        if (jc.getClassName().startsWith("java.util.jar.")) return;
        if (jc.getClassName().startsWith("java.net.Inet6")) return;
        if (jc.getClassName().equals("java.lang.System") && method.getName().equals("runFinalizersOnExit")) return;
        if (method.getName().equals("<init>") && jc.getClassName().startsWith("java.lang.reflect.")) return;
        if (dest.contains(method)) return;
        dest.add(method);
        if (method.getName().equals("<clinit>") && jc.getSuperClass() != null) loadMethod(jc.getSuperClass().getClassName() + ".<clinit>");
        if (method.isStatic() || method.getName().equals("<init>")) loadMethod(jc.getClassName() + ".<clinit>");
        if (method.getName().equals("<init>")) {
            constructed.add(jc);
            HashSet hs = (HashSet) uponconstruction.get(jc);
            if (hs != null) {
                Iterator it = hs.iterator();
                while (it.hasNext()) visitJavaMethod(jc, (Method) it.next());
            }
            loadMethod(jc.getClassName() + ".equals");
            loadMethod(jc.getClassName() + ".hashCode");
            loadMethod(jc.getClassName() + ".toString");
            loadMethod(jc.getClassName() + ".finalize");
            loadMethod(jc.getClassName() + ".clone");
        }
        ConstantPoolGen cpg = new ConstantPoolGen(method.getConstantPool());
        if (!method.isStatic() && !constructed.contains(jc)) {
            HashSet hs = (HashSet) uponconstruction.get(jc);
            if (hs == null) uponconstruction.put(jc, hs = new HashSet());
            hs.add(method);
            markMethodInSubclasses(jc, method, cpg);
            dest.remove(method);
            return;
        }
        level += 2;
        for (int i = 0; i < level; i++) System.out.print(" ");
        System.out.print(jc.getClassName() + "." + getMethodSignature(method, cpg));
        markMethodInSubclasses(jc, method, cpg);
        if (method.getCode() == null) {
            System.out.println();
            level -= 2;
            return;
        }
        byte[] code = method.getCode().getCode();
        InstructionList il = new InstructionList(code);
        InstructionHandle[] instructions = il.getInstructionHandles();
        System.out.println(" [" + instructions.length + " instructions]");
        for (int i = 0; i < instructions.length; i++) {
            Instruction instr = instructions[i].getInstruction();
            ;
            if (instr instanceof Select) {
                InstructionHandle[] ih2 = ((Select) instr).getTargets();
                InstructionHandle[] ih3 = new InstructionHandle[instructions.length + ih2.length];
                System.arraycopy(instructions, 0, ih3, 0, instructions.length);
                System.arraycopy(ih2, 0, ih3, instructions.length, ih2.length);
                instructions = ih3;
            }
            if (instr instanceof LoadClass) {
                ObjectType ot = (ObjectType) ((LoadClass) instr).getLoadClassType(cpg);
                if (ot != null) loadMethod(ot.getClassName() + ".<clinit>");
            }
            if (instr instanceof CPInstruction) load(((CPInstruction) instr).getType(cpg));
            if (instr instanceof TypedInstruction) load(((TypedInstruction) instr).getType(cpg));
            if (instr instanceof NEW) loadMethod(((NEW) instr).getLoadClassType(cpg).getClassName() + ".<init>");
            if (instr instanceof org.apache.bcel.generic.FieldOrMethod) load(((org.apache.bcel.generic.FieldOrMethod) instr).getClassType(cpg));
            if (instr instanceof org.apache.bcel.generic.FieldInstruction) {
                load(((org.apache.bcel.generic.FieldInstruction) instr).getFieldType(cpg));
                load(((org.apache.bcel.generic.FieldInstruction) instr).getType(cpg));
                String fieldName = ((org.apache.bcel.generic.FieldInstruction) instr).getFieldName(cpg);
                JavaClass jc2 = repo.loadClass(((ObjectType) ((org.apache.bcel.generic.FieldInstruction) instr).getLoadClassType(cpg)).getClassName());
                Field[] fields = jc2.getFields();
                for (int j = 0; j < fields.length; j++) if (fields[j].getName().equals(fieldName)) visitJavaField(fields[j], jc2);
            }
            if (instr instanceof InvokeInstruction) {
                InvokeInstruction ii = (InvokeInstruction) instr;
                String ii_sig = getMethodSignature(ii, cpg);
                JavaClass c = sig2class(ii.getLoadClassType(cpg).getSignature());
                load(ii.getType(cpg));
                Method[] meths = getMethods(c);
                boolean good = false;
                for (int i2 = 0; i2 < meths.length; i2++) {
                    if (getMethodSignature(meths[i2], cpg).equals(ii_sig)) {
                        visitJavaMethod(c, meths[i2]);
                        good = true;
                        break;
                    }
                }
                if (!good) throw new Exception("couldn't find method " + getMethodSignature(ii, cpg) + " in " + c.getClassName());
            }
        }
        Type[] argtypes = method.getArgumentTypes();
        for (int i = 0; i < argtypes.length; i++) load(argtypes[i]);
        if (method.getExceptionTable() != null) {
            String[] exntypes = method.getExceptionTable().getExceptionNames();
            for (int i = 0; i < exntypes.length; i++) load(exntypes[i]);
        }
        level -= 2;
    }

    public void visitJavaField(Field field, JavaClass clazz) throws Exception {
        if (dest.contains(field)) return;
        dest.add(field);
        if (field.isStatic()) loadMethod(clazz.getClassName() + ".<clinit>");
    }

    public void visitJavaClass(JavaClass clazz) throws Exception {
        if (dest.contains(clazz)) return;
        dest.add(clazz);
        ConstantPoolGen cpg = new ConstantPoolGen(clazz.getConstantPool());
        level += 2;
        for (int i = 0; i < level; i++) System.out.print(" ");
        System.out.println(clazz.getClassName() + ".class");
        JavaClass superclass = clazz.getSuperClass();
        for (JavaClass sup = superclass; sup != null; sup = sup.getSuperClass()) {
            if (subclasses.get(sup) == null) subclasses.put(sup, new HashSet());
            ((HashSet) subclasses.get(sup)).add(clazz);
        }
        JavaClass[] interfaces = clazz.getAllInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (subclasses.get(interfaces[i]) == null) subclasses.put(interfaces[i], new HashSet());
            ((HashSet) subclasses.get(interfaces[i])).add(clazz);
        }
        for (JavaClass sup = superclass; sup != null; sup = sup.getSuperClass()) {
            visitJavaClass(sup);
            remarkMethods(sup, clazz, cpg);
        }
        for (int i = 0; i < interfaces.length; i++) {
            visitJavaClass(interfaces[i]);
            remarkMethods(interfaces[i], clazz, cpg);
        }
        Field[] fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (!fields[i].isStatic()) visitJavaField(fields[i], clazz); else {
                Type t = fields[i].getType();
                if (t instanceof ObjectType) load(t);
            }
        }
        level -= 2;
    }

    public void markMethodInSubclasses(JavaClass c, Method m, JavaClass subclass, ConstantPoolGen cpg) throws Exception {
        if (m.isStatic()) return;
        if (m.getName().equals("<init>")) return;
        if (m.getName().equals("equals")) return;
        if (m.getName().equals("hashCode")) return;
        if (m.getName().equals("clone")) return;
        if (m.getName().equals("finalize")) return;
        if (m.getName().equals("toString")) return;
        String sig = getMethodSignature(m, cpg);
        Method[] submethods = getMethods(subclass);
        for (int j = 0; j < submethods.length; j++) if (getMethodSignature(submethods[j], cpg).equals(sig)) visitJavaMethod(subclass, submethods[j]);
    }

    public void markMethodInSubclasses(JavaClass c, Method m, ConstantPoolGen cpg) throws Exception {
        if (m.isStatic()) return;
        if (m.getName().equals("<init>")) return;
        HashSet s = (HashSet) subclasses.get(c);
        if (s == null) return;
        Object[] subclasses = s.toArray();
        for (int i = 0; i < subclasses.length; i++) {
            JavaClass subclass = (JavaClass) subclasses[i];
            if (subclass == c) continue;
            markMethodInSubclasses(c, m, subclass, cpg);
        }
    }

    public void remarkMethods(JavaClass c, ConstantPoolGen cpg) throws Exception {
        Method[] meths = getMethods(c);
        for (int j = 0; j < meths.length; j++) if (dest.contains(meths[j]) || (uponconstruction.get(c) != null && ((HashSet) uponconstruction.get(c)).contains(meths[j]))) markMethodInSubclasses(c, meths[j], cpg);
    }

    public void remarkMethods(JavaClass c, JavaClass target, ConstantPoolGen cpg) throws Exception {
        Method[] meths = getMethods(c);
        for (int j = 0; j < meths.length; j++) if (dest.contains(meths[j]) || (uponconstruction.get(c) != null && ((HashSet) uponconstruction.get(c)).contains(meths[j]))) markMethodInSubclasses(c, meths[j], target, cpg);
    }

    public static Hashtable methodsHashtable = new Hashtable();

    public static Method[] getMethods(JavaClass c) {
        Method[] ret = (Method[]) methodsHashtable.get(c);
        if (ret == null) methodsHashtable.put(c, ret = c.getMethods());
        return ret;
    }
}
