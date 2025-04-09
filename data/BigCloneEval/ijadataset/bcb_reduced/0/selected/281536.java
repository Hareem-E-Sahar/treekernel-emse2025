package org.jmlspecs.javacontract.bytecode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import org.jmlspecs.javacontract.JC;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.TraceClassVisitor;

public class BytecodeRewriter {

    abstract static class AnnotationInfo {
    }

    static class InvariantAnnotationInfo extends AnnotationInfo {

        static class MethodInfo {

            public final String className;

            public final String methodName;

            public final String methodDesc;

            public MethodInfo(final String className, final String methodName, final String methodDesc) {
                this.className = className;
                this.methodName = methodName;
                this.methodDesc = methodDesc;
            }
        }

        final List<MethodInfo> infos = new ArrayList<MethodInfo>();

        void addInfo(final String className, final String methodName, final String methodDesc) {
            this.infos.add(new MethodInfo(className, methodName, methodDesc));
        }
    }

    static final String JC_NAME = JC.class.getName().replace('.', '/');

    static final String JC_REQUIRES = "requires";

    static final String JC_ENSURES = "ensures";

    static final String JC_RESULT = "result";

    static final String ANN_INVARIANT = "org/jmlspecs/annotation/Invariant";

    static final String ANN_INVARIANT_KEY = 'L' + BytecodeRewriter.ANN_INVARIANT + ';';

    static HashMap<String, AnnotationInfo> collectAnnotationInfo(final String filePath) throws IOException {
        final HashMap<String, AnnotationInfo> result = new HashMap<String, AnnotationInfo>();
        final InvariantAnnotationInfo iai = new InvariantAnnotationInfo();
        result.put(BytecodeRewriter.ANN_INVARIANT, iai);
        final FileInputStream fis = new FileInputStream(filePath);
        final ClassReader cr = new ClassReader(fis);
        cr.accept(new ClassVisitor() {

            class MethodVisitor implements org.objectweb.asm.MethodVisitor {

                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                    if (BytecodeRewriter.ANN_INVARIANT_KEY.equals(desc)) {
                        iai.addInfo(className, methodName, methodDesc);
                    }
                    return null;
                }

                public AnnotationVisitor visitAnnotationDefault() {
                    return null;
                }

                public void visitAttribute(final Attribute attr) {
                }

                public void visitCode() {
                }

                public void visitEnd() {
                }

                public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
                }

                public void visitFrame(final int type, final int local, final Object[] local2, final int stack, final Object[] stack2) {
                }

                public void visitIincInsn(final int var, final int increment) {
                }

                public void visitInsn(final int opcode) {
                }

                public void visitIntInsn(final int opcode, final int operand) {
                }

                public void visitJumpInsn(final int opcode, final Label label) {
                }

                public void visitLabel(final Label label) {
                }

                public void visitLdcInsn(final Object cst) {
                }

                public void visitLineNumber(final int line, final Label start) {
                }

                public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
                }

                public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
                }

                public void visitMaxs(final int maxStack, final int maxLocals) {
                }

                public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
                }

                public void visitMultiANewArrayInsn(final String desc, final int dims) {
                }

                public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
                    return null;
                }

                public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label[] labels) {
                }

                public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
                }

                public void visitTypeInsn(final int opcode, final String type) {
                }

                public void visitVarInsn(final int opcode, final int var) {
                }
            }

            MethodVisitor mv = new MethodVisitor();

            String className;

            String methodName;

            String methodDesc;

            public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
                this.className = name;
            }

            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                return null;
            }

            public void visitAttribute(final Attribute attr) {
            }

            public void visitEnd() {
            }

            public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
                return null;
            }

            public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
            }

            public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
                this.methodName = name;
                this.methodDesc = desc;
                return this.mv;
            }

            public void visitOuterClass(final String owner, final String name, final String desc) {
            }

            public void visitSource(final String source, final String debug) {
            }
        }, ClassReader.SKIP_CODE);
        fis.close();
        return result;
    }

    static int getFirstIndex(final InsnList insns) {
        final int size = insns.size();
        for (int i = 0; i < size; i++) {
            if (insns.get(i) instanceof MethodInsnNode) {
                return i + 1;
            }
        }
        return -1;
    }

    static LabelNode getFirstLabelNode(final AbstractInsnNode[] insns) {
        final int size = insns.length;
        for (int i = 0; i < size; i++) {
            if (insns[i] instanceof LabelNode) {
                return (LabelNode) insns[i];
            }
        }
        assert false;
        return null;
    }

    static LabelNode getLastLabelNode(final AbstractInsnNode[] insns) {
        final int size = insns.length;
        for (int i = size - 1; i >= 0; i--) {
            if (insns[i] instanceof LabelNode) {
                return (LabelNode) insns[i];
            }
        }
        assert false;
        return null;
    }

    static int getMethodIndex(final InsnList insns, final int index, final String ciname, final String mname) {
        final int size = insns.size();
        for (int i = index; i < size; i++) {
            final AbstractInsnNode ain = insns.get(i);
            if (ain instanceof MethodInsnNode) {
                final MethodInsnNode min = (MethodInsnNode) ain;
                if (min.owner.equals(ciname) && min.name.equals(mname)) {
                    return i + 1;
                }
            }
        }
        return index;
    }

    static Label insertInvariant(final AbstractInsnNode insn, final AbstractInsnNode target, final MethodNode mn, final HashMap<String, AnnotationInfo> annInfo) {
        final InsnList insns = mn.instructions;
        final InvariantAnnotationInfo iai = (InvariantAnnotationInfo) annInfo.get(BytecodeRewriter.ANN_INVARIANT);
        int line = 0;
        {
            AbstractInsnNode temp = insn;
            while ((temp != null) && !(temp instanceof LineNumberNode)) {
                temp = temp.getNext();
            }
            if (temp != null) {
                final LineNumberNode lnn = (LineNumberNode) temp;
                line = lnn.line;
            }
        }
        final Label beforeInsnLabel = new Label();
        insns.insertBefore(insn, new LabelNode(beforeInsnLabel));
        insns.insertBefore(insn, new LineNumberNode(line, new LabelNode(beforeInsnLabel)));
        final Label beforeTargetLabel = new Label();
        insns.insertBefore(target, new LabelNode(beforeTargetLabel));
        final List<InvariantAnnotationInfo.MethodInfo> mis = iai.infos;
        final int size = mis.size();
        for (int i = 0; i < size; i++) {
            final InvariantAnnotationInfo.MethodInfo mi = mis.get(i);
            insns.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
            insns.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESPECIAL, mi.className, mi.methodName, mi.methodDesc));
            insns.insertBefore(insn, new InsnNode(Opcodes.DUP));
            insns.insertBefore(insn, new JumpInsnNode(Opcodes.IFEQ, new LabelNode(beforeTargetLabel)));
            insns.insertBefore(insn, new InsnNode(Opcodes.POP));
        }
        return beforeInsnLabel;
    }

    public static void main(final String[] args) throws IOException {
        BytecodeRewriter.rewrite(args[0], false, new PrintWriter(System.out));
    }

    static void rewrite(final MethodNode mn, final boolean scrub, final HashMap<String, AnnotationInfo> annInfo) {
        final InsnList insns = mn.instructions;
        final int first = mn.name.equals("<init>") ? BytecodeRewriter.getFirstIndex(insns) : 0;
        assert first >= 0;
        final int requiresIndex = BytecodeRewriter.getMethodIndex(insns, first, BytecodeRewriter.JC_NAME, BytecodeRewriter.JC_REQUIRES);
        final int ensuresIndex = BytecodeRewriter.getMethodIndex(insns, requiresIndex, BytecodeRewriter.JC_NAME, BytecodeRewriter.JC_ENSURES);
        if (scrub) {
            BytecodeRewriter.scrub(mn, first, requiresIndex, ensuresIndex);
        } else {
            BytecodeRewriter.rewrite(mn, first, requiresIndex, ensuresIndex, annInfo);
        }
    }

    @SuppressWarnings("unchecked")
    static void rewrite(final MethodNode mn, final int first, final int requiresIndex, final int ensuresIndex, final HashMap<String, AnnotationInfo> annInfo) {
        final InsnList insns = mn.instructions;
        final boolean hasRequires = first != requiresIndex;
        final boolean hasEnsures = requiresIndex != ensuresIndex;
        final AbstractInsnNode firstInsn = insns.get(first);
        AbstractInsnNode afterRequiresInsn = insns.get(requiresIndex);
        AbstractInsnNode afterEnsuresInsn = insns.get(ensuresIndex);
        final boolean isInstance = (mn.access & Opcodes.ACC_STATIC) == 0;
        final boolean hasInvariants = ((InvariantAnnotationInfo) annInfo.get(BytecodeRewriter.ANN_INVARIANT)).infos.size() > 0;
        final AbstractInsnNode[] insnsA = insns.toArray();
        final Label requiresLabel = BytecodeRewriter.getFirstLabelNode(insnsA).getLabel();
        Label requiresNewLabel = requiresLabel;
        if (hasRequires) {
            if (isInstance && hasInvariants) {
                requiresNewLabel = BytecodeRewriter.insertInvariant(firstInsn, afterRequiresInsn.getPrevious(), mn, annInfo);
            }
        }
        if (hasEnsures) {
            final Label beforeEnsuresLabel = new Label();
            insns.insertBefore(afterRequiresInsn, afterRequiresInsn = new LabelNode(beforeEnsuresLabel));
            if (isInstance && hasInvariants) {
                BytecodeRewriter.insertInvariant(afterRequiresInsn.getNext(), afterEnsuresInsn.getPrevious(), mn, annInfo);
            }
            final Label afterEnsuresLabel = new Label();
            insns.insertBefore(afterEnsuresInsn, afterEnsuresInsn = new LabelNode(afterEnsuresLabel));
            insns.insertBefore(afterRequiresInsn, afterRequiresInsn = new JumpInsnNode(Opcodes.GOTO, new LabelNode(afterEnsuresLabel)));
            AbstractInsnNode ain = null;
            int xsopcode = -1;
            int xlopcode = -1;
            for (AbstractInsnNode cin : insnsA) {
                if (cin instanceof InsnNode) {
                    final InsnNode in = (InsnNode) cin;
                    switch(in.getOpcode()) {
                        case Opcodes.IRETURN:
                            if (ain == null) {
                                ain = in;
                                in.getOpcode();
                            }
                            xsopcode = Opcodes.ISTORE;
                            xlopcode = Opcodes.ILOAD;
                            break;
                        case Opcodes.LRETURN:
                            if (ain == null) {
                                ain = in;
                                in.getOpcode();
                            }
                            xsopcode = Opcodes.LSTORE;
                            xlopcode = Opcodes.LLOAD;
                            break;
                        case Opcodes.FRETURN:
                            if (ain == null) {
                                ain = in;
                                in.getOpcode();
                            }
                            xsopcode = Opcodes.FSTORE;
                            xlopcode = Opcodes.FLOAD;
                            break;
                        case Opcodes.DRETURN:
                            if (ain == null) {
                                ain = in;
                                in.getOpcode();
                            }
                            xsopcode = Opcodes.DSTORE;
                            xlopcode = Opcodes.DLOAD;
                            break;
                        case Opcodes.ARETURN:
                            if (ain == null) {
                                ain = in;
                                in.getOpcode();
                            }
                            xsopcode = Opcodes.ASTORE;
                            xlopcode = Opcodes.ALOAD;
                            break;
                        case Opcodes.RETURN:
                            if (ain == null) {
                                ain = in;
                                in.getOpcode();
                            }
                            insns.set(in, new JumpInsnNode(Opcodes.GOTO, new LabelNode(beforeEnsuresLabel)));
                            break;
                    }
                    if (xsopcode != -1) {
                        insns.set(in, cin = new VarInsnNode(xsopcode, mn.maxLocals));
                        insns.insert(cin, new JumpInsnNode(Opcodes.GOTO, new LabelNode(beforeEnsuresLabel)));
                    }
                }
            }
            if (mn.localVariables != null) {
                if (requiresNewLabel != requiresLabel) {
                    final ArrayList<LocalVariableNode> newLVNs = new ArrayList<LocalVariableNode>();
                    for (final Object o : mn.localVariables) {
                        final LocalVariableNode lvn = (LocalVariableNode) o;
                        if (lvn.start.getLabel() == requiresLabel) {
                            newLVNs.add(new LocalVariableNode(lvn.name, lvn.desc, lvn.signature, new LabelNode(requiresNewLabel), lvn.end, lvn.index));
                        } else {
                            newLVNs.add(lvn);
                        }
                    }
                    mn.localVariables = newLVNs;
                }
            }
            assert ain != null;
            insns.insertBefore(afterEnsuresInsn, afterEnsuresInsn = ain);
            if (xsopcode != -1) {
                insns.insertBefore(afterEnsuresInsn, new VarInsnNode(xlopcode, mn.maxLocals));
                if (mn.localVariables == null) {
                    mn.localVariables = new ArrayList<LocalVariableNode>();
                }
                mn.localVariables.add(new LocalVariableNode("$result", Type.getReturnType(mn.desc).getDescriptor(), null, new LabelNode(requiresNewLabel), BytecodeRewriter.getLastLabelNode(insnsA), mn.maxLocals));
                mn.maxLocals++;
            }
            for (final ListIterator li = insns.iterator(); li.hasNext(); ) {
                final AbstractInsnNode cin = (AbstractInsnNode) li.next();
                if (cin instanceof MethodInsnNode) {
                    final MethodInsnNode min = (MethodInsnNode) cin;
                    if (min.owner.equals(BytecodeRewriter.JC_NAME) && min.name.equals(BytecodeRewriter.JC_RESULT)) {
                        li.set(new VarInsnNode(xlopcode, mn.maxLocals - 1));
                        if (xlopcode != Opcodes.ALOAD) {
                            li.next();
                            li.remove();
                            li.next();
                            li.remove();
                        }
                    }
                }
            }
        }
    }

    public static void rewrite(final String filePath, final boolean scrub, final PrintWriter pwOut) throws IOException {
        final HashMap<String, AnnotationInfo> annInfo = BytecodeRewriter.collectAnnotationInfo(filePath);
        final FileInputStream fis = new FileInputStream(filePath);
        final ClassReader cr = new ClassReader(fis);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final ClassAdapter ca = new ClassAdapter(cw) {

            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
                final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                final MethodNode mn = new MethodNode(access, name, desc, signature, exceptions) {

                    @Override
                    public void visitEnd() {
                        BytecodeRewriter.rewrite(this, scrub, annInfo);
                        accept(mv);
                    }
                };
                return mn;
            }
        };
        cr.accept(ca, 0);
        final byte[] newCode = cw.toByteArray();
        fis.close();
        if (pwOut != null) {
            final TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(pwOut));
            new ClassReader(newCode).accept(tcv, 0);
        }
        final FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(newCode);
        fos.close();
    }

    @SuppressWarnings("unchecked")
    static void scrub(final MethodNode mn, final int first, final int requiresIndex, final int ensuresIndex) {
        final InsnList insns = mn.instructions;
        final boolean hasRequires = first != requiresIndex;
        final boolean hasEnsures = requiresIndex != ensuresIndex;
        if (hasRequires || hasEnsures) {
            final ListIterator li = insns.iterator(first);
            for (int i = first; i < ensuresIndex; i++) {
                li.next();
                li.remove();
            }
        }
    }
}
