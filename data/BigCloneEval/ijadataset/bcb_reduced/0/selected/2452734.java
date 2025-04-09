package net.sf.beezle.mork.classfile;

import net.sf.beezle.mork.classfile.attribute.Attribute;
import net.sf.beezle.sushi.util.IntArrayList;
import net.sf.beezle.sushi.util.IntBitSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class Code extends Attribute implements Constants {

    public static final Logger LOGGER = Logger.getLogger(Code.class.getName());

    public int locals;

    public List<Instruction> instructions;

    public List<ExceptionInfo> exceptions;

    /** List of Attributes */
    public List<Attribute> attributes;

    /** only valid while reading or writing. */
    private int codeSize;

    /**
     * Labels to have forware references:
     * Index i stores label -i; index 0 is not used
     *  -1: declared, but not defined
     *  otherwise: index
     */
    private IntArrayList labels;

    public Code() {
        super("Code");
        locals = 0;
        instructions = new ArrayList<Instruction>();
        exceptions = new ArrayList<ExceptionInfo>();
        attributes = new ArrayList<Attribute>();
        labels = new IntArrayList();
        labels.add(0);
    }

    public Code(Input src) throws IOException {
        this();
        Instruction instr;
        int i;
        int max;
        int loadedStack;
        int computedStack;
        src.readU4();
        loadedStack = src.readU2();
        locals = src.readU2();
        max = src.readU4();
        src.openCode(this);
        while (src.getOfs() < max) {
            instr = Instruction.read(src);
            instructions.add(instr);
        }
        codeSize = src.getOfs();
        max = instructions.size();
        for (i = 0; i < max; i++) {
            instr = instructions.get(i);
            instr.ofsToIdx(this);
        }
        max = src.readU2();
        for (i = 0; i < max; i++) {
            exceptions.add(new ExceptionInfo(src));
        }
        max = src.readU2();
        for (i = 0; i < max; i++) {
            attributes.add(Attribute.create(src));
        }
        src.closeCode();
        computedStack = calcStackSize();
        if (loadedStack < computedStack) {
            LOGGER.warning("loaded stack size differs from computed: " + "loaded: " + loadedStack + " < computed: " + computedStack);
        }
    }

    public void references(Collection<Reference> result) {
        for (Object i : instructions) {
            for (Object arg : ((Instruction) i).arguments) {
                if (arg instanceof Reference) {
                    result.add((Reference) arg);
                }
            }
        }
    }

    /**
     * @return number of instructions.
     */
    public int getSize() {
        return instructions.size();
    }

    @Override
    public void write(Output dest) throws IOException {
        int i, max;
        Instruction instr;
        ExceptionInfo info;
        Attribute attr;
        int start;
        layout(dest);
        if (codeSize > 0xffff) {
            throw new IllegalStateException("code segment exceeds 64k");
        }
        dest.writeUtf8(name);
        start = dest.writeSpace(4);
        dest.writeU2(calcStackSize());
        dest.writeU2(locals);
        dest.writeU4(codeSize);
        dest.openCode(this);
        max = instructions.size();
        for (i = 0; i < max; i++) {
            instr = (Instruction) instructions.get(i);
            instr.write(dest);
        }
        max = exceptions.size();
        dest.writeU2(max);
        for (i = 0; i < max; i++) {
            info = (ExceptionInfo) exceptions.get(i);
            info.write(dest);
        }
        max = attributes.size();
        dest.writeU2(max);
        for (i = 0; i < max; i++) {
            attr = (Attribute) attributes.get(i);
            attr.write(dest);
        }
        dest.closeCode();
        dest.writeFixup(start, dest.getGlobalOfs() - (start + 4));
    }

    /** compute ofs for all instructions. */
    private void layout(Output dest) {
        Instruction instr;
        int instrSize, varSize;
        IntArrayList vars;
        IntArrayList lens;
        int i, j, k, ofs;
        int shrink, len;
        boolean changes;
        instrSize = instructions.size();
        vars = new IntArrayList();
        lens = new IntArrayList();
        ofs = 0;
        for (i = 0; i < instrSize; i++) {
            instr = instructions.get(i);
            instr.ofs = ofs;
            len = instr.getMaxLength(dest);
            ofs += len;
            if (instr.type.isVariable()) {
                vars.add(i);
                lens.add(len);
            }
        }
        codeSize = ofs;
        varSize = vars.size();
        do {
            changes = false;
            shrink = 0;
            for (i = 0; i < varSize; i++) {
                j = vars.get(i);
                instr = instructions.get(j);
                len = instr.getVariableLength(this);
                if (len < lens.get(i)) {
                    changes = true;
                    shrink += (lens.get(i) - len);
                    lens.set(i, len);
                }
                if (shrink > 0) {
                    for (k = (i + 1 == varSize) ? instrSize - 1 : vars.get(i + 1); k > j; k--) {
                        instructions.get(k).ofs -= shrink;
                    }
                }
            }
            codeSize -= shrink;
        } while (changes);
    }

    public int findEndIdxOrLast(int startIdx, int len) {
        return findIdxOrLast(instructions.get(startIdx).ofs + len);
    }

    public int findIdxOrLast(int ofs) {
        if (ofs == codeSize) {
            return instructions.size();
        } else {
            return findIdx(ofs);
        }
    }

    public int findIdx(int ofs) {
        int low, high;
        int idx;
        int tmp;
        low = 0;
        high = instructions.size() - 1;
        while (low <= high) {
            idx = (low + high) / 2;
            tmp = instructions.get(idx).ofs;
            if (tmp < ofs) {
                low = idx + 1;
            } else if (tmp > ofs) {
                high = idx - 1;
            } else {
                return idx;
            }
        }
        throw new RuntimeException("no such ofs: " + ofs);
    }

    public int findEndOfsOrLast(int startIdx, int idx) {
        return findOfsOrLast(idx) - findOfs(startIdx);
    }

    public int findOfsOrLast(int idx) {
        if (idx == instructions.size()) {
            return codeSize;
        } else {
            return findOfs(idx);
        }
    }

    public int findOfs(int idx) {
        return instructions.get(resolveLabel(idx)).ofs;
    }

    public int resolveLabel(int idx) {
        int trueIdx;
        if (idx < 0) {
            trueIdx = labels.get(-idx);
            if (trueIdx < 0) {
                throw new RuntimeException("undefined label: " + idx);
            }
            return trueIdx;
        } else {
            return idx;
        }
    }

    public int declareLabel() {
        labels.add(-1);
        return -(labels.size() - 1);
    }

    public void defineLabel(int label) {
        if (labels.get(-label) != -1) {
            throw new RuntimeException("duplicate definition for label " + label);
        }
        labels.set(-label, instructions.size());
    }

    public int currentLabel() {
        return instructions.size();
    }

    @Override
    public String toString() {
        StringBuilder result;
        int i;
        Instruction instr;
        result = new StringBuilder();
        result.append("Code attribute: \n");
        result.append("    locals=" + locals);
        result.append("    codeSize=" + codeSize + "\n");
        for (i = 0; i < instructions.size(); i++) {
            instr = instructions.get(i);
            result.append(i + "\t" + "(" + instr.ofs + ")\t" + instr.toString() + "\n");
        }
        for (i = 0; i < exceptions.size(); i++) {
            result.append('\t');
            result.append(exceptions.get(i).toString());
            result.append('\n');
        }
        for (i = 0; i < attributes.size(); i++) {
            result.append('\t');
            result.append(attributes.get(i).toString());
            result.append('\n');
        }
        return result.toString();
    }

    /** pc is increased by 1. */
    public void emitGeneric(int opcode, Object[] args) {
        Instruction instr;
        InstructionType type;
        type = Set.TYPES[opcode];
        instr = new Instruction(-1, type, args);
        instructions.add(instr);
    }

    public void emit(int opcode) {
        emitGeneric(opcode, new Object[] {});
    }

    public void emit(int opcode, int arg) {
        emitGeneric(opcode, new Object[] { arg });
    }

    public void emit(int opcode, int arg0, int arg1) {
        emitGeneric(opcode, new Object[] { arg0, arg1 });
    }

    public void emit(int opcode, String arg) {
        emitGeneric(opcode, new Object[] { arg });
    }

    public void emit(int opcode, MethodRef arg) {
        emitGeneric(opcode, new Object[] { arg });
    }

    public void emit(int opcode, ClassRef arg) {
        emitGeneric(opcode, new Object[] { arg });
    }

    public void emit(int opcode, FieldRef arg) {
        emitGeneric(opcode, new Object[] { arg });
    }

    public void emit(int opcode, int a, int b, int c, IntArrayList d) {
        emitGeneric(opcode, new Object[] { a, b, c, d });
    }

    public void emit(int opcode, int a, IntArrayList b, IntArrayList c) {
        emitGeneric(opcode, new Object[] { a, b, c });
    }

    public int declareFixup() {
        int result;
        result = instructions.size();
        emit(Bytecodes.NOP);
        return result;
    }

    public void fixupGeneric(int fixup, int opcode, Object[] args) {
        Instruction instr;
        InstructionType type;
        type = Set.TYPES[opcode];
        instr = new Instruction(-1, type, args);
        instructions.set(fixup, instr);
    }

    public void fixup(int fixup, int opcode) {
        fixupGeneric(fixup, opcode, new Object[] {});
    }

    public void fixup(int fixup, int opcode, int arg) {
        fixupGeneric(fixup, opcode, new Object[] { arg });
    }

    public void fixup(int fixup, int opcode, int arg0, int arg1) {
        fixupGeneric(fixup, opcode, new Object[] { arg0, arg1 });
    }

    public void fixup(int fixup, int opcode, String arg) {
        fixupGeneric(fixup, opcode, new Object[] { arg });
    }

    public void fixup(int fixup, int opcode, MethodRef arg) {
        fixupGeneric(fixup, opcode, new Object[] { arg });
    }

    public void fixup(int fixup, int opcode, ClassRef arg) {
        fixupGeneric(fixup, opcode, new Object[] { arg });
    }

    public void fixup(int fixup, int opcode, FieldRef arg) {
        fixupGeneric(fixup, opcode, new Object[] { arg });
    }

    public void fixup(int fixup, int opcode, int a, int b, int c, IntArrayList d) {
        fixupGeneric(fixup, opcode, new Object[] { a, b, c, d });
    }

    public void fixup(int fixup, int opcode, int a, IntArrayList b, IntArrayList c) {
        fixupGeneric(fixup, opcode, new Object[] { a, b, c });
    }

    private int calcStackSize() {
        int i, max;
        int result;
        int[] startStack;
        ExceptionInfo e;
        int tmp;
        int unreachable;
        IntBitSet todo;
        List<Jsr> jsrs;
        jsrs = Jsr.findJsrs(this);
        startStack = new int[instructions.size()];
        for (i = 0; i < startStack.length; i++) {
            startStack[i] = -1;
        }
        startStack[0] = 0;
        todo = new IntBitSet();
        todo.add(0);
        max = exceptions.size();
        for (i = 0; i < max; i++) {
            e = exceptions.get(i);
            startStack[e.handler] = 1;
            todo.add(e.handler);
        }
        fillStack(jsrs, todo, startStack);
        result = 0;
        unreachable = -1;
        for (i = 0; i < startStack.length; i++) {
            tmp = startStack[i];
            if (tmp > result) {
                result = tmp;
            }
            if ((unreachable == -1) && (tmp == -1)) {
                unreachable = i;
            }
        }
        if (unreachable != -1) {
            LOGGER.warning("unreachable code, starting " + unreachable + "\n");
        }
        return result;
    }

    private void fillStack(List<Jsr> jsrs, IntBitSet todo, int[] startStack) {
        int succSize;
        Instruction instr;
        int idx;
        int i, max;
        int tmp, succIdx;
        IntArrayList succBuffer;
        while (true) {
            idx = todo.first();
            if (idx == -1) {
                return;
            }
            todo.remove(idx);
            instr = instructions.get(idx);
            succSize = startStack[idx] + instr.getStackDiff();
            if (succSize < 0) {
                throw new RuntimeException("stack size <0, idx: " + idx);
            }
            succBuffer = new IntArrayList();
            instr.getSuccessors(jsrs, idx, this, succBuffer);
            max = succBuffer.size();
            for (i = 0; i < max; i++) {
                succIdx = succBuffer.get(i);
                tmp = startStack[succIdx];
                if (tmp == succSize) {
                } else if (tmp == -1) {
                    startStack[succIdx] = succSize;
                    todo.add(succIdx);
                } else {
                    throw new RuntimeException("wrong stack idx = " + succIdx + ": " + tmp + "!=" + succSize + "\n" + toString());
                }
            }
        }
    }

    public int allocate(ClassRef cl) {
        int result;
        result = locals;
        locals += cl.operandSize();
        return result;
    }
}
