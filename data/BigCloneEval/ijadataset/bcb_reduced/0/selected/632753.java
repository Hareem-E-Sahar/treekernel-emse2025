package bsh.org.objectweb.asm;

/**
 * A {@link CodeVisitor CodeVisitor} that generates Java bytecode instructions.
 * Each visit method of this class appends the bytecode corresponding to the
 * visited instruction to a byte vector, in the order these methods are called.
 */
public class CodeWriter implements CodeVisitor {

    /**
   * <tt>true</tt> if preconditions must be checked at runtime or not.
   */
    static final boolean CHECK = false;

    /**
   * Next code writer (see {@link ClassWriter#firstMethod firstMethod}).
   */
    CodeWriter next;

    /**
   * The class writer to which this method must be added.
   */
    private ClassWriter cw;

    /**
   * The constant pool item that contains the name of this method.
   */
    private Item name;

    /**
   * The constant pool item that contains the descriptor of this method.
   */
    private Item desc;

    /**
   * Access flags of this method.
   */
    private int access;

    /**
   * Maximum stack size of this method.
   */
    private int maxStack;

    /**
   * Maximum number of local variables for this method.
   */
    private int maxLocals;

    /**
   * The bytecode of this method.
   */
    private ByteVector code = new ByteVector();

    /**
   * Number of entries in the catch table of this method.
   */
    private int catchCount;

    /**
   * The catch table of this method.
   */
    private ByteVector catchTable;

    /**
   * Number of exceptions that can be thrown by this method.
   */
    private int exceptionCount;

    /**
   * The exceptions that can be thrown by this method. More
   * precisely, this array contains the indexes of the constant pool items
   * that contain the internal names of these exception classes.
   */
    private int[] exceptions;

    /**
   * Number of entries in the LocalVariableTable attribute.
   */
    private int localVarCount;

    /**
   * The LocalVariableTable attribute.
   */
    private ByteVector localVar;

    /**
   * Number of entries in the LineNumberTable attribute.
   */
    private int lineNumberCount;

    /**
   * The LineNumberTable attribute.
   */
    private ByteVector lineNumber;

    /**
   * Indicates if some jump instructions are too small and need to be resized.
   */
    private boolean resize;

    /**
   * <tt>true</tt> if the maximum stack size and number of local variables must
   * be automatically computed.
   */
    private final boolean computeMaxs;

    /**
   * The (relative) stack size after the last visited instruction. This size is
   * relative to the beginning of the current basic block, i.e., the true stack
   * size after the last visited instruction is equal to the {@link
   * Label#beginStackSize beginStackSize} of the current basic block plus
   * <tt>stackSize</tt>.
   */
    private int stackSize;

    /**
   * The (relative) maximum stack size after the last visited instruction. This
   * size is relative to the beginning of the current basic block, i.e., the
   * true maximum stack size after the last visited instruction is equal to the
   * {@link Label#beginStackSize beginStackSize} of the current basic block plus
   * <tt>stackSize</tt>.
   */
    private int maxStackSize;

    /**
   * The current basic block. This block is the basic block to which the next
   * instruction to be visited must be added.
   */
    private Label currentBlock;

    /**
   * The basic block stack used by the control flow analysis algorithm. This
   * stack is represented by a linked list of {@link Label Label} objects,
   * linked to each other by their {@link Label#next} field. This stack must
   * not be confused with the JVM stack used to execute the JVM instructions!
   */
    private Label blockStack;

    /**
   * The stack size variation corresponding to each JVM instruction. This stack
   * variation is equal to the size of the values produced by an instruction,
   * minus the size of the values consumed by this instruction.
   */
    private static final int[] SIZE;

    /**
   * The head of the list of {@link Edge Edge} objects used by this {@link
   * CodeWriter CodeWriter}. These objects, linked to each other by their
   * {@link Edge#poolNext} field, are added back to the shared pool at the
   * end of the control flow analysis algorithm.
   */
    private Edge head;

    /**
   * The tail of the list of {@link Edge Edge} objects used by this {@link
   * CodeWriter CodeWriter}. These objects, linked to each other by their
   * {@link Edge#poolNext} field, are added back to the shared pool at the
   * end of the control flow analysis algorithm.
   */
    private Edge tail;

    /**
   * The shared pool of {@link Edge Edge} objects. This pool is a linked list
   * of Edge objects, linked to each other by their {@link Edge#poolNext} field.
   */
    private static Edge pool;

    /**
   * Computes the stack size variation corresponding to each JVM instruction.
   */
    static {
        int i;
        int[] b = new int[202];
        String s = "EFFFFFFFFGGFFFGGFFFEEFGFGFEEEEEEEEEEEEEEEEEEEEDEDEDDDDDCDCDEEEEEEEEE" + "EEEEEEEEEEEBABABBBBDCFFFGGGEDCDCDCDCDCDCDCDCDCDCEEEEDDDDDDDCDCDCEFEF" + "DDEEFFDEDEEEBDDBBDDDDDDCCCCCCCCEFEDDDCDCDEEEEEEEEEEFEEEEEEDDEEDDEE";
        for (i = 0; i < b.length; ++i) {
            b[i] = s.charAt(i) - 'E';
        }
        SIZE = b;
    }

    /**
   * Constructs a CodeWriter.
   *
   * @param cw the class writer in which the method must be added.
   * @param computeMaxs <tt>true</tt> if the maximum stack size and number of
   *      local variables must be automatically computed.
   */
    protected CodeWriter(final ClassWriter cw, final boolean computeMaxs) {
        if (cw.firstMethod == null) {
            cw.firstMethod = this;
            cw.lastMethod = this;
        } else {
            cw.lastMethod.next = this;
            cw.lastMethod = this;
        }
        this.cw = cw;
        this.computeMaxs = computeMaxs;
        if (computeMaxs) {
            currentBlock = new Label();
            currentBlock.pushed = true;
            blockStack = currentBlock;
        }
    }

    /**
   * Initializes this CodeWriter to define the bytecode of the specified method.
   *
   * @param access the method's access flags (see {@link Constants}).
   * @param name the method's name.
   * @param desc the method's descriptor (see {@link Type Type}).
   * @param exceptions the internal names of the method's exceptions. May be
   *      <tt>null</tt>.
   */
    protected void init(final int access, final String name, final String desc, final String[] exceptions) {
        this.access = access;
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);
        if (exceptions != null && exceptions.length > 0) {
            exceptionCount = exceptions.length;
            this.exceptions = new int[exceptionCount];
            for (int i = 0; i < exceptionCount; ++i) {
                this.exceptions[i] = cw.newClass(exceptions[i]).index;
            }
        }
        if (computeMaxs) {
            int size = getArgumentsAndReturnSizes(desc) >> 2;
            if ((access & Constants.ACC_STATIC) != 0) {
                --size;
            }
            if (size > maxLocals) {
                maxLocals = size;
            }
        }
    }

    public void visitInsn(final int opcode) {
        if (computeMaxs) {
            int size = stackSize + SIZE[opcode];
            if (size > maxStackSize) {
                maxStackSize = size;
            }
            stackSize = size;
            if ((opcode >= Constants.IRETURN && opcode <= Constants.RETURN) || opcode == Constants.ATHROW) {
                if (currentBlock != null) {
                    currentBlock.maxStackSize = maxStackSize;
                    currentBlock = null;
                }
            }
        }
        code.put1(opcode);
    }

    public void visitIntInsn(final int opcode, final int operand) {
        if (computeMaxs && opcode != Constants.NEWARRAY) {
            int size = stackSize + 1;
            if (size > maxStackSize) {
                maxStackSize = size;
            }
            stackSize = size;
        }
        if (opcode == Constants.SIPUSH) {
            code.put12(opcode, operand);
        } else {
            code.put11(opcode, operand);
        }
    }

    public void visitVarInsn(final int opcode, final int var) {
        if (computeMaxs) {
            if (opcode == Constants.RET) {
                if (currentBlock != null) {
                    currentBlock.maxStackSize = maxStackSize;
                    currentBlock = null;
                }
            } else {
                int size = stackSize + SIZE[opcode];
                if (size > maxStackSize) {
                    maxStackSize = size;
                }
                stackSize = size;
            }
            int n;
            if (opcode == Constants.LLOAD || opcode == Constants.DLOAD || opcode == Constants.LSTORE || opcode == Constants.DSTORE) {
                n = var + 2;
            } else {
                n = var + 1;
            }
            if (n > maxLocals) {
                maxLocals = n;
            }
        }
        if (var < 4 && opcode != Constants.RET) {
            int opt;
            if (opcode < Constants.ISTORE) {
                opt = 26 + ((opcode - Constants.ILOAD) << 2) + var;
            } else {
                opt = 59 + ((opcode - Constants.ISTORE) << 2) + var;
            }
            code.put1(opt);
        } else if (var >= 256) {
            code.put1(196).put12(opcode, var);
        } else {
            code.put11(opcode, var);
        }
    }

    public void visitTypeInsn(final int opcode, final String desc) {
        if (computeMaxs && opcode == Constants.NEW) {
            int size = stackSize + 1;
            if (size > maxStackSize) {
                maxStackSize = size;
            }
            stackSize = size;
        }
        code.put12(opcode, cw.newClass(desc).index);
    }

    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        if (computeMaxs) {
            int size;
            char c = desc.charAt(0);
            switch(opcode) {
                case Constants.GETSTATIC:
                    size = stackSize + (c == 'D' || c == 'J' ? 2 : 1);
                    break;
                case Constants.PUTSTATIC:
                    size = stackSize + (c == 'D' || c == 'J' ? -2 : -1);
                    break;
                case Constants.GETFIELD:
                    size = stackSize + (c == 'D' || c == 'J' ? 1 : 0);
                    break;
                default:
                    size = stackSize + (c == 'D' || c == 'J' ? -3 : -2);
                    break;
            }
            if (size > maxStackSize) {
                maxStackSize = size;
            }
            stackSize = size;
        }
        code.put12(opcode, cw.newField(owner, name, desc).index);
    }

    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        Item i;
        if (opcode == Constants.INVOKEINTERFACE) {
            i = cw.newItfMethod(owner, name, desc);
        } else {
            i = cw.newMethod(owner, name, desc);
        }
        int argSize = i.intVal;
        if (computeMaxs) {
            if (argSize == 0) {
                argSize = getArgumentsAndReturnSizes(desc);
                i.intVal = argSize;
            }
            int size;
            if (opcode == Constants.INVOKESTATIC) {
                size = stackSize - (argSize >> 2) + (argSize & 0x03) + 1;
            } else {
                size = stackSize - (argSize >> 2) + (argSize & 0x03);
            }
            if (size > maxStackSize) {
                maxStackSize = size;
            }
            stackSize = size;
        }
        if (opcode == Constants.INVOKEINTERFACE) {
            if (!computeMaxs) {
                if (argSize == 0) {
                    argSize = getArgumentsAndReturnSizes(desc);
                    i.intVal = argSize;
                }
            }
            code.put12(Constants.INVOKEINTERFACE, i.index).put11(argSize >> 2, 0);
        } else {
            code.put12(opcode, i.index);
        }
    }

    public void visitJumpInsn(final int opcode, final Label label) {
        if (CHECK) {
            if (label.owner == null) {
                label.owner = this;
            } else if (label.owner != this) {
                throw new IllegalArgumentException();
            }
        }
        if (computeMaxs) {
            if (opcode == Constants.GOTO) {
                if (currentBlock != null) {
                    currentBlock.maxStackSize = maxStackSize;
                    addSuccessor(stackSize, label);
                    currentBlock = null;
                }
            } else if (opcode == Constants.JSR) {
                if (currentBlock != null) {
                    addSuccessor(stackSize + 1, label);
                }
            } else {
                stackSize += SIZE[opcode];
                if (currentBlock != null) {
                    addSuccessor(stackSize, label);
                }
            }
        }
        if (label.resolved && label.position - code.length < Short.MIN_VALUE) {
            if (opcode == Constants.GOTO) {
                code.put1(200);
            } else if (opcode == Constants.JSR) {
                code.put1(201);
            } else {
                code.put1(opcode <= 166 ? ((opcode + 1) ^ 1) - 1 : opcode ^ 1);
                code.put2(8);
                code.put1(200);
            }
            label.put(this, code, code.length - 1, true);
        } else {
            code.put1(opcode);
            label.put(this, code, code.length - 1, false);
        }
    }

    public void visitLabel(final Label label) {
        if (CHECK) {
            if (label.owner == null) {
                label.owner = this;
            } else if (label.owner != this) {
                throw new IllegalArgumentException();
            }
        }
        if (computeMaxs) {
            if (currentBlock != null) {
                currentBlock.maxStackSize = maxStackSize;
                addSuccessor(stackSize, label);
            }
            currentBlock = label;
            stackSize = 0;
            maxStackSize = 0;
        }
        resize |= label.resolve(this, code.length, code.data);
    }

    public void visitLdcInsn(final Object cst) {
        Item i = cw.newCst(cst);
        if (computeMaxs) {
            int size;
            if (i.type == ClassWriter.LONG || i.type == ClassWriter.DOUBLE) {
                size = stackSize + 2;
            } else {
                size = stackSize + 1;
            }
            if (size > maxStackSize) {
                maxStackSize = size;
            }
            stackSize = size;
        }
        int index = i.index;
        if (i.type == ClassWriter.LONG || i.type == ClassWriter.DOUBLE) {
            code.put12(20, index);
        } else if (index >= 256) {
            code.put12(19, index);
        } else {
            code.put11(Constants.LDC, index);
        }
    }

    public void visitIincInsn(final int var, final int increment) {
        if (computeMaxs) {
            int n = var + 1;
            if (n > maxLocals) {
                maxLocals = n;
            }
        }
        if ((var > 255) || (increment > 127) || (increment < -128)) {
            code.put1(196).put12(Constants.IINC, var).put2(increment);
        } else {
            code.put1(Constants.IINC).put11(var, increment);
        }
    }

    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label labels[]) {
        if (computeMaxs) {
            --stackSize;
            if (currentBlock != null) {
                currentBlock.maxStackSize = maxStackSize;
                addSuccessor(stackSize, dflt);
                for (int i = 0; i < labels.length; ++i) {
                    addSuccessor(stackSize, labels[i]);
                }
                currentBlock = null;
            }
        }
        int source = code.length;
        code.put1(Constants.TABLESWITCH);
        while (code.length % 4 != 0) {
            code.put1(0);
        }
        dflt.put(this, code, source, true);
        code.put4(min).put4(max);
        for (int i = 0; i < labels.length; ++i) {
            labels[i].put(this, code, source, true);
        }
    }

    public void visitLookupSwitchInsn(final Label dflt, final int keys[], final Label labels[]) {
        if (computeMaxs) {
            --stackSize;
            if (currentBlock != null) {
                currentBlock.maxStackSize = maxStackSize;
                addSuccessor(stackSize, dflt);
                for (int i = 0; i < labels.length; ++i) {
                    addSuccessor(stackSize, labels[i]);
                }
                currentBlock = null;
            }
        }
        int source = code.length;
        code.put1(Constants.LOOKUPSWITCH);
        while (code.length % 4 != 0) {
            code.put1(0);
        }
        dflt.put(this, code, source, true);
        code.put4(labels.length);
        for (int i = 0; i < labels.length; ++i) {
            code.put4(keys[i]);
            labels[i].put(this, code, source, true);
        }
    }

    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        if (computeMaxs) {
            stackSize += 1 - dims;
        }
        Item classItem = cw.newClass(desc);
        code.put12(Constants.MULTIANEWARRAY, classItem.index).put1(dims);
    }

    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        if (CHECK) {
            if (start.owner != this || end.owner != this || handler.owner != this) {
                throw new IllegalArgumentException();
            }
            if (!start.resolved || !end.resolved || !handler.resolved) {
                throw new IllegalArgumentException();
            }
        }
        if (computeMaxs) {
            if (!handler.pushed) {
                handler.beginStackSize = 1;
                handler.pushed = true;
                handler.next = blockStack;
                blockStack = handler;
            }
        }
        ++catchCount;
        if (catchTable == null) {
            catchTable = new ByteVector();
        }
        catchTable.put2(start.position);
        catchTable.put2(end.position);
        catchTable.put2(handler.position);
        catchTable.put2(type != null ? cw.newClass(type).index : 0);
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
        if (computeMaxs) {
            int max = 0;
            Label stack = blockStack;
            while (stack != null) {
                Label l = stack;
                stack = stack.next;
                int start = l.beginStackSize;
                int blockMax = start + l.maxStackSize;
                if (blockMax > max) {
                    max = blockMax;
                }
                Edge b = l.successors;
                while (b != null) {
                    l = b.successor;
                    if (!l.pushed) {
                        l.beginStackSize = start + b.stackSize;
                        l.pushed = true;
                        l.next = stack;
                        stack = l;
                    }
                    b = b.next;
                }
            }
            this.maxStack = max;
            synchronized (SIZE) {
                if (tail != null) {
                    tail.poolNext = pool;
                    pool = head;
                }
            }
        } else {
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
        }
    }

    public void visitLocalVariable(final String name, final String desc, final Label start, final Label end, final int index) {
        if (CHECK) {
            if (start.owner != this || !start.resolved) {
                throw new IllegalArgumentException();
            }
            if (end.owner != this || !end.resolved) {
                throw new IllegalArgumentException();
            }
        }
        if (localVar == null) {
            cw.newUTF8("LocalVariableTable");
            localVar = new ByteVector();
        }
        ++localVarCount;
        localVar.put2(start.position);
        localVar.put2(end.position - start.position);
        localVar.put2(cw.newUTF8(name).index);
        localVar.put2(cw.newUTF8(desc).index);
        localVar.put2(index);
    }

    public void visitLineNumber(final int line, final Label start) {
        if (CHECK) {
            if (start.owner != this || !start.resolved) {
                throw new IllegalArgumentException();
            }
        }
        if (lineNumber == null) {
            cw.newUTF8("LineNumberTable");
            lineNumber = new ByteVector();
        }
        ++lineNumberCount;
        lineNumber.put2(start.position);
        lineNumber.put2(line);
    }

    /**
   * Computes the size of the arguments and of the return value of a method.
   *
   * @param desc the descriptor of a method.
   * @return the size of the arguments of the method (plus one for the implicit
   *      this argument), argSize, and the size of its return value, retSize,
   *      packed into a single int i = <tt>(argSize << 2) | retSize</tt>
   *      (argSize is therefore equal to <tt>i >> 2</tt>, and retSize to
   *      <tt>i & 0x03</tt>).
   */
    private static int getArgumentsAndReturnSizes(final String desc) {
        int n = 1;
        int c = 1;
        while (true) {
            char car = desc.charAt(c++);
            if (car == ')') {
                car = desc.charAt(c);
                return n << 2 | (car == 'V' ? 0 : (car == 'D' || car == 'J' ? 2 : 1));
            } else if (car == 'L') {
                while (desc.charAt(c++) != ';') {
                }
                n += 1;
            } else if (car == '[') {
                while ((car = desc.charAt(c)) == '[') {
                    ++c;
                }
                if (car == 'D' || car == 'J') {
                    n -= 1;
                }
            } else if (car == 'D' || car == 'J') {
                n += 2;
            } else {
                n += 1;
            }
        }
    }

    /**
   * Adds a successor to the {@link #currentBlock currentBlock} block.
   *
   * @param stackSize the current (relative) stack size in the current block.
   * @param successor the successor block to be added to the current block.
   */
    private void addSuccessor(final int stackSize, final Label successor) {
        Edge b;
        synchronized (SIZE) {
            if (pool == null) {
                b = new Edge();
            } else {
                b = pool;
                pool = pool.poolNext;
            }
        }
        if (tail == null) {
            tail = b;
        }
        b.poolNext = head;
        head = b;
        b.stackSize = stackSize;
        b.successor = successor;
        b.next = currentBlock.successors;
        currentBlock.successors = b;
    }

    /**
   * Returns the size of the bytecode of this method.
   *
   * @return the size of the bytecode of this method.
   */
    final int getSize() {
        if (resize) {
            resizeInstructions(new int[0], new int[0], 0);
        }
        int size = 8;
        if (code.length > 0) {
            cw.newUTF8("Code");
            size += 18 + code.length + 8 * catchCount;
            if (localVar != null) {
                size += 8 + localVar.length;
            }
            if (lineNumber != null) {
                size += 8 + lineNumber.length;
            }
        }
        if (exceptionCount > 0) {
            cw.newUTF8("Exceptions");
            size += 8 + 2 * exceptionCount;
        }
        if ((access & Constants.ACC_SYNTHETIC) != 0) {
            cw.newUTF8("Synthetic");
            size += 6;
        }
        if ((access & Constants.ACC_DEPRECATED) != 0) {
            cw.newUTF8("Deprecated");
            size += 6;
        }
        return size;
    }

    /**
   * Puts the bytecode of this method in the given byte vector.
   *
   * @param out the byte vector into which the bytecode of this method must be
   *      copied.
   */
    final void put(final ByteVector out) {
        out.put2(access).put2(name.index).put2(desc.index);
        int attributeCount = 0;
        if (code.length > 0) {
            ++attributeCount;
        }
        if (exceptionCount > 0) {
            ++attributeCount;
        }
        if ((access & Constants.ACC_SYNTHETIC) != 0) {
            ++attributeCount;
        }
        if ((access & Constants.ACC_DEPRECATED) != 0) {
            ++attributeCount;
        }
        out.put2(attributeCount);
        if (code.length > 0) {
            int size = 12 + code.length + 8 * catchCount;
            if (localVar != null) {
                size += 8 + localVar.length;
            }
            if (lineNumber != null) {
                size += 8 + lineNumber.length;
            }
            out.put2(cw.newUTF8("Code").index).put4(size);
            out.put2(maxStack).put2(maxLocals);
            out.put4(code.length).putByteArray(code.data, 0, code.length);
            out.put2(catchCount);
            if (catchCount > 0) {
                out.putByteArray(catchTable.data, 0, catchTable.length);
            }
            attributeCount = 0;
            if (localVar != null) {
                ++attributeCount;
            }
            if (lineNumber != null) {
                ++attributeCount;
            }
            out.put2(attributeCount);
            if (localVar != null) {
                out.put2(cw.newUTF8("LocalVariableTable").index);
                out.put4(localVar.length + 2).put2(localVarCount);
                out.putByteArray(localVar.data, 0, localVar.length);
            }
            if (lineNumber != null) {
                out.put2(cw.newUTF8("LineNumberTable").index);
                out.put4(lineNumber.length + 2).put2(lineNumberCount);
                out.putByteArray(lineNumber.data, 0, lineNumber.length);
            }
        }
        if (exceptionCount > 0) {
            out.put2(cw.newUTF8("Exceptions").index).put4(2 * exceptionCount + 2);
            out.put2(exceptionCount);
            for (int i = 0; i < exceptionCount; ++i) {
                out.put2(exceptions[i]);
            }
        }
        if ((access & Constants.ACC_SYNTHETIC) != 0) {
            out.put2(cw.newUTF8("Synthetic").index).put4(0);
        }
        if ((access & Constants.ACC_DEPRECATED) != 0) {
            out.put2(cw.newUTF8("Deprecated").index).put4(0);
        }
    }

    /**
   * Resizes the designated instructions, while keeping jump offsets and
   * instruction addresses consistent. This may require to resize other existing
   * instructions, or even to introduce new instructions: for example,
   * increasing the size of an instruction by 2 at the middle of a method can
   * increases the offset of an IFEQ instruction from 32766 to 32768, in which
   * case IFEQ 32766 must be replaced with IFNEQ 8 GOTO_W 32765. This, in turn,
   * may require to increase the size of another jump instruction, and so on...
   * All these operations are handled automatically by this method.
   * <p>
   * <i>This method must be called after all the method that is being built has
   * been visited</i>. In particular, the {@link Label Label} objects used to
   * construct the method are no longer valid after this method has been called.
   *
   * @param indexes current positions of the instructions to be resized. Each
   *      instruction must be designated by the index of its <i>last</i> byte,
   *      plus one (or, in other words, by the index of the <i>first</i> byte of
   *      the <i>next</i> instruction).
   * @param sizes the number of bytes to be <i>added</i> to the above
   *      instructions. More precisely, for each i &lt; <tt>len</tt>,
   *      <tt>sizes</tt>[i] bytes will be added at the end of the instruction
   *      designated by <tt>indexes</tt>[i] or, if <tt>sizes</tt>[i] is
   *      negative, the <i>last</i> |<tt>sizes[i]</tt>| bytes of the instruction
   *      will be removed (the instruction size <i>must not</i> become negative
   *      or null). The gaps introduced by this method must be filled in
   *      "manually" in the array returned by the {@link #getCode getCode}
   *      method.
   * @param len the number of instruction to be resized. Must be smaller than or
   *      equal to <tt>indexes</tt>.length and <tt>sizes</tt>.length.
   * @return the <tt>indexes</tt> array, which now contains the new positions of
   *      the resized instructions (designated as above).
   */
    protected int[] resizeInstructions(final int[] indexes, final int[] sizes, final int len) {
        byte[] b = code.data;
        int u, v, label;
        int i, j;
        int[] allIndexes = new int[len];
        int[] allSizes = new int[len];
        boolean[] resize;
        int newOffset;
        System.arraycopy(indexes, 0, allIndexes, 0, len);
        System.arraycopy(sizes, 0, allSizes, 0, len);
        resize = new boolean[code.length];
        int state = 3;
        do {
            if (state == 3) {
                state = 2;
            }
            u = 0;
            while (u < b.length) {
                int opcode = b[u] & 0xFF;
                int insert = 0;
                switch(ClassWriter.TYPE[opcode]) {
                    case ClassWriter.NOARG_INSN:
                    case ClassWriter.IMPLVAR_INSN:
                        u += 1;
                        break;
                    case ClassWriter.LABEL_INSN:
                        if (opcode > 201) {
                            opcode = opcode < 218 ? opcode - 49 : opcode - 20;
                            label = u + readUnsignedShort(b, u + 1);
                        } else {
                            label = u + readShort(b, u + 1);
                        }
                        newOffset = getNewOffset(allIndexes, allSizes, u, label);
                        if (newOffset < Short.MIN_VALUE || newOffset > Short.MAX_VALUE) {
                            if (!resize[u]) {
                                if (opcode == Constants.GOTO || opcode == Constants.JSR) {
                                    insert = 2;
                                } else {
                                    insert = 5;
                                }
                                resize[u] = true;
                            }
                        }
                        u += 3;
                        break;
                    case ClassWriter.LABELW_INSN:
                        u += 5;
                        break;
                    case ClassWriter.TABL_INSN:
                        if (state == 1) {
                            newOffset = getNewOffset(allIndexes, allSizes, 0, u);
                            insert = -(newOffset & 3);
                        } else if (!resize[u]) {
                            insert = u & 3;
                            resize[u] = true;
                        }
                        u = u + 4 - (u & 3);
                        u += 4 * (readInt(b, u + 8) - readInt(b, u + 4) + 1) + 12;
                        break;
                    case ClassWriter.LOOK_INSN:
                        if (state == 1) {
                            newOffset = getNewOffset(allIndexes, allSizes, 0, u);
                            insert = -(newOffset & 3);
                        } else if (!resize[u]) {
                            insert = u & 3;
                            resize[u] = true;
                        }
                        u = u + 4 - (u & 3);
                        u += 8 * readInt(b, u + 4) + 8;
                        break;
                    case ClassWriter.WIDE_INSN:
                        opcode = b[u + 1] & 0xFF;
                        if (opcode == Constants.IINC) {
                            u += 6;
                        } else {
                            u += 4;
                        }
                        break;
                    case ClassWriter.VAR_INSN:
                    case ClassWriter.SBYTE_INSN:
                    case ClassWriter.LDC_INSN:
                        u += 2;
                        break;
                    case ClassWriter.SHORT_INSN:
                    case ClassWriter.LDCW_INSN:
                    case ClassWriter.FIELDORMETH_INSN:
                    case ClassWriter.TYPE_INSN:
                    case ClassWriter.IINC_INSN:
                        u += 3;
                        break;
                    case ClassWriter.ITFMETH_INSN:
                        u += 5;
                        break;
                    default:
                        u += 4;
                        break;
                }
                if (insert != 0) {
                    int[] newIndexes = new int[allIndexes.length + 1];
                    int[] newSizes = new int[allSizes.length + 1];
                    System.arraycopy(allIndexes, 0, newIndexes, 0, allIndexes.length);
                    System.arraycopy(allSizes, 0, newSizes, 0, allSizes.length);
                    newIndexes[allIndexes.length] = u;
                    newSizes[allSizes.length] = insert;
                    allIndexes = newIndexes;
                    allSizes = newSizes;
                    if (insert > 0) {
                        state = 3;
                    }
                }
            }
            if (state < 3) {
                --state;
            }
        } while (state != 0);
        ByteVector newCode = new ByteVector(code.length);
        u = 0;
        while (u < code.length) {
            for (i = allIndexes.length - 1; i >= 0; --i) {
                if (allIndexes[i] == u) {
                    if (i < len) {
                        if (sizes[i] > 0) {
                            newCode.putByteArray(null, 0, sizes[i]);
                        } else {
                            newCode.length += sizes[i];
                        }
                        indexes[i] = newCode.length;
                    }
                }
            }
            int opcode = b[u] & 0xFF;
            switch(ClassWriter.TYPE[opcode]) {
                case ClassWriter.NOARG_INSN:
                case ClassWriter.IMPLVAR_INSN:
                    newCode.put1(opcode);
                    u += 1;
                    break;
                case ClassWriter.LABEL_INSN:
                    if (opcode > 201) {
                        opcode = opcode < 218 ? opcode - 49 : opcode - 20;
                        label = u + readUnsignedShort(b, u + 1);
                    } else {
                        label = u + readShort(b, u + 1);
                    }
                    newOffset = getNewOffset(allIndexes, allSizes, u, label);
                    if (newOffset < Short.MIN_VALUE || newOffset > Short.MAX_VALUE) {
                        if (opcode == Constants.GOTO) {
                            newCode.put1(200);
                        } else if (opcode == Constants.JSR) {
                            newCode.put1(201);
                        } else {
                            newCode.put1(opcode <= 166 ? ((opcode + 1) ^ 1) - 1 : opcode ^ 1);
                            newCode.put2(8);
                            newCode.put1(200);
                            newOffset -= 3;
                        }
                        newCode.put4(newOffset);
                    } else {
                        newCode.put1(opcode);
                        newCode.put2(newOffset);
                    }
                    u += 3;
                    break;
                case ClassWriter.LABELW_INSN:
                    label = u + readInt(b, u + 1);
                    newOffset = getNewOffset(allIndexes, allSizes, u, label);
                    newCode.put1(opcode);
                    newCode.put4(newOffset);
                    u += 5;
                    break;
                case ClassWriter.TABL_INSN:
                    v = u;
                    u = u + 4 - (v & 3);
                    int source = newCode.length;
                    newCode.put1(Constants.TABLESWITCH);
                    while (newCode.length % 4 != 0) {
                        newCode.put1(0);
                    }
                    label = v + readInt(b, u);
                    u += 4;
                    newOffset = getNewOffset(allIndexes, allSizes, v, label);
                    newCode.put4(newOffset);
                    j = readInt(b, u);
                    u += 4;
                    newCode.put4(j);
                    j = readInt(b, u) - j + 1;
                    u += 4;
                    newCode.put4(readInt(b, u - 4));
                    for (; j > 0; --j) {
                        label = v + readInt(b, u);
                        u += 4;
                        newOffset = getNewOffset(allIndexes, allSizes, v, label);
                        newCode.put4(newOffset);
                    }
                    break;
                case ClassWriter.LOOK_INSN:
                    v = u;
                    u = u + 4 - (v & 3);
                    source = newCode.length;
                    newCode.put1(Constants.LOOKUPSWITCH);
                    while (newCode.length % 4 != 0) {
                        newCode.put1(0);
                    }
                    label = v + readInt(b, u);
                    u += 4;
                    newOffset = getNewOffset(allIndexes, allSizes, v, label);
                    newCode.put4(newOffset);
                    j = readInt(b, u);
                    u += 4;
                    newCode.put4(j);
                    for (; j > 0; --j) {
                        newCode.put4(readInt(b, u));
                        u += 4;
                        label = v + readInt(b, u);
                        u += 4;
                        newOffset = getNewOffset(allIndexes, allSizes, v, label);
                        newCode.put4(newOffset);
                    }
                    break;
                case ClassWriter.WIDE_INSN:
                    opcode = b[u + 1] & 0xFF;
                    if (opcode == Constants.IINC) {
                        newCode.putByteArray(b, u, 6);
                        u += 6;
                    } else {
                        newCode.putByteArray(b, u, 4);
                        u += 4;
                    }
                    break;
                case ClassWriter.VAR_INSN:
                case ClassWriter.SBYTE_INSN:
                case ClassWriter.LDC_INSN:
                    newCode.putByteArray(b, u, 2);
                    u += 2;
                    break;
                case ClassWriter.SHORT_INSN:
                case ClassWriter.LDCW_INSN:
                case ClassWriter.FIELDORMETH_INSN:
                case ClassWriter.TYPE_INSN:
                case ClassWriter.IINC_INSN:
                    newCode.putByteArray(b, u, 3);
                    u += 3;
                    break;
                case ClassWriter.ITFMETH_INSN:
                    newCode.putByteArray(b, u, 5);
                    u += 5;
                    break;
                default:
                    newCode.putByteArray(b, u, 4);
                    u += 4;
                    break;
            }
        }
        if (catchTable != null) {
            b = catchTable.data;
            u = 0;
            while (u < catchTable.length) {
                writeShort(b, u, getNewOffset(allIndexes, allSizes, 0, readUnsignedShort(b, u)));
                writeShort(b, u + 2, getNewOffset(allIndexes, allSizes, 0, readUnsignedShort(b, u + 2)));
                writeShort(b, u + 4, getNewOffset(allIndexes, allSizes, 0, readUnsignedShort(b, u + 4)));
                u += 8;
            }
        }
        if (localVar != null) {
            b = localVar.data;
            u = 0;
            while (u < localVar.length) {
                label = readUnsignedShort(b, u);
                newOffset = getNewOffset(allIndexes, allSizes, 0, label);
                writeShort(b, u, newOffset);
                label += readUnsignedShort(b, u + 2);
                newOffset = getNewOffset(allIndexes, allSizes, 0, label) - newOffset;
                writeShort(b, u, newOffset);
                u += 10;
            }
        }
        if (lineNumber != null) {
            b = lineNumber.data;
            u = 0;
            while (u < lineNumber.length) {
                writeShort(b, u, getNewOffset(allIndexes, allSizes, 0, readUnsignedShort(b, u)));
                u += 4;
            }
        }
        code = newCode;
        return indexes;
    }

    /**
   * Reads an unsigned short value in the given byte array.
   *
   * @param b a byte array.
   * @param index the start index of the value to be read.
   * @return the read value.
   */
    static int readUnsignedShort(final byte[] b, final int index) {
        return ((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF);
    }

    /**
   * Reads a signed short value in the given byte array.
   *
   * @param b a byte array.
   * @param index the start index of the value to be read.
   * @return the read value.
   */
    static short readShort(final byte[] b, final int index) {
        return (short) (((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF));
    }

    /**
   * Reads a signed int value in the given byte array.
   *
   * @param b a byte array.
   * @param index the start index of the value to be read.
   * @return the read value.
   */
    static int readInt(final byte[] b, final int index) {
        return ((b[index] & 0xFF) << 24) | ((b[index + 1] & 0xFF) << 16) | ((b[index + 2] & 0xFF) << 8) | (b[index + 3] & 0xFF);
    }

    /**
   * Writes a short value in the given byte array.
   *
   * @param b a byte array.
   * @param index where the first byte of the short value must be written.
   * @param s the value to be written in the given byte array.
   */
    static void writeShort(final byte[] b, final int index, final int s) {
        b[index] = (byte) (s >>> 8);
        b[index + 1] = (byte) s;
    }

    /**
   * Computes the future value of a bytecode offset.
   * <p>
   * Note: it is possible to have several entries for the same instruction
   * in the <tt>indexes</tt> and <tt>sizes</tt>: two entries (index=a,size=b)
   * and (index=a,size=b') are equivalent to a single entry (index=a,size=b+b').
   *
   * @param indexes current positions of the instructions to be resized. Each
   *      instruction must be designated by the index of its <i>last</i> byte,
   *      plus one (or, in other words, by the index of the <i>first</i> byte of
   *      the <i>next</i> instruction).
   * @param sizes the number of bytes to be <i>added</i> to the above
   *      instructions. More precisely, for each i < <tt>len</tt>,
   *      <tt>sizes</tt>[i] bytes will be added at the end of the instruction
   *      designated by <tt>indexes</tt>[i] or, if <tt>sizes</tt>[i] is
   *      negative, the <i>last</i> |<tt>sizes[i]</tt>| bytes of the instruction
   *      will be removed (the instruction size <i>must not</i> become negative
   *      or null).
   * @param begin index of the first byte of the source instruction.
   * @param end index of the first byte of the target instruction.
   * @return the future value of the given bytecode offset.
   */
    static int getNewOffset(final int[] indexes, final int[] sizes, final int begin, final int end) {
        int offset = end - begin;
        for (int i = 0; i < indexes.length; ++i) {
            if (begin < indexes[i] && indexes[i] <= end) {
                offset += sizes[i];
            } else if (end < indexes[i] && indexes[i] <= begin) {
                offset -= sizes[i];
            }
        }
        return offset;
    }

    /**
   * Returns the current size of the bytecode of this method. This size just
   * includes the size of the bytecode instructions: it does not include the
   * size of the Exceptions, LocalVariableTable, LineNumberTable, Synthetic
   * and Deprecated attributes, if present.
   *
   * @return the current size of the bytecode of this method.
   */
    protected int getCodeSize() {
        return code.length;
    }

    /**
   * Returns the current bytecode of this method. This bytecode only contains
   * the instructions: it does not include the Exceptions, LocalVariableTable,
   * LineNumberTable, Synthetic and Deprecated attributes, if present.
   *
   * @return the current bytecode of this method. The bytecode is contained
   *      between the index 0 (inclusive) and the index {@link #getCodeSize
   *      getCodeSize} (exclusive).
   */
    protected byte[] getCode() {
        return code.data;
    }
}
