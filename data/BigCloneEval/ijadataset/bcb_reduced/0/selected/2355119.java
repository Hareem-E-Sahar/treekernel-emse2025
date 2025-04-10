package com.ibm.wala.shrikeBT.analysis;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import com.ibm.wala.shrikeBT.DupInstruction;
import com.ibm.wala.shrikeBT.ExceptionHandler;
import com.ibm.wala.shrikeBT.IInstruction;
import com.ibm.wala.shrikeBT.ILoadInstruction;
import com.ibm.wala.shrikeBT.IStoreInstruction;
import com.ibm.wala.shrikeBT.LoadInstruction;
import com.ibm.wala.shrikeBT.MethodData;
import com.ibm.wala.shrikeBT.StoreInstruction;
import com.ibm.wala.shrikeBT.SwapInstruction;
import com.ibm.wala.shrikeBT.Util;

/**
 * @author roca
 */
public class Analyzer {

    protected final boolean isStatic;

    protected final String classType;

    protected final String signature;

    protected final IInstruction[] instructions;

    protected final ExceptionHandler[][] handlers;

    protected ClassHierarchyProvider hierarchy;

    protected int maxStack;

    protected int maxLocals;

    protected String[][] stacks;

    protected String[][] locals;

    protected int[] stackSizes;

    protected BitSet basicBlockStarts;

    protected int[][] backEdges;

    protected static final String[] noStrings = new String[0];

    protected static final int[] noEdges = new int[0];

    public Analyzer(boolean isStatic, String classType, String signature, IInstruction[] instructions, ExceptionHandler[][] handlers) {
        if (instructions == null) {
            throw new IllegalArgumentException("null instructions");
        }
        if (handlers == null) {
            throw new IllegalArgumentException("null handlers");
        }
        for (IInstruction i : instructions) {
            if (i == null) {
                throw new IllegalArgumentException("null instruction is illegal");
            }
        }
        this.classType = classType;
        this.isStatic = isStatic;
        this.signature = signature;
        this.instructions = instructions;
        this.handlers = handlers;
    }

    /**
   * Use class hierarchy information in 'h'. If this method is not called or h provides only partial hierarchy information, the
   * verifier behaves optimistically.
   */
    public final void setClassHierarchy(ClassHierarchyProvider h) {
        this.hierarchy = h;
    }

    private void addBackEdge(int from, int to) {
        int[] oldEdges = backEdges[from];
        if (oldEdges == null) {
            backEdges[from] = new int[] { to };
        } else if (oldEdges[oldEdges.length - 1] < 0) {
            int left = 1;
            int right = oldEdges.length - 1;
            while (true) {
                if (right - left < 2) {
                    if (oldEdges[left] < 0) {
                        break;
                    } else {
                        if (oldEdges[right] >= 0) throw new Error("Failed binary search");
                        left = right;
                        break;
                    }
                } else {
                    int mid = (left + right) / 2;
                    if (oldEdges[mid] < 0) {
                        right = mid;
                    } else {
                        left = mid + 1;
                    }
                }
            }
            oldEdges[left] = to;
        } else {
            int[] newEdges = new int[oldEdges.length * 2];
            System.arraycopy(oldEdges, 0, newEdges, 0, oldEdges.length);
            newEdges[oldEdges.length] = to;
            for (int i = oldEdges.length + 1; i < newEdges.length; i++) {
                newEdges[i] = -1;
            }
            backEdges[from] = newEdges;
        }
    }

    public final int[][] getBackEdges() {
        if (backEdges != null) {
            return backEdges;
        }
        backEdges = new int[instructions.length][];
        for (int i = 0; i < instructions.length; i++) {
            IInstruction instr = instructions[i];
            int[] targets = instr.getBranchTargets();
            for (int j = 0; j < targets.length; j++) {
                addBackEdge(targets[j], i);
            }
            ExceptionHandler[] hs = handlers[i];
            for (int j = 0; j < hs.length; j++) {
                addBackEdge(hs[j].getHandler(), i);
            }
        }
        for (int i = 0; i < backEdges.length; i++) {
            int[] back = backEdges[i];
            if (back == null) {
                backEdges[i] = noEdges;
            } else if (back[back.length - 1] < 0) {
                int j = back.length;
                while (back[j - 1] < 0) {
                    j--;
                }
                int[] newBack = new int[j];
                System.arraycopy(back, 0, newBack, 0, newBack.length);
                backEdges[i] = newBack;
            }
        }
        return backEdges;
    }

    public final boolean isSubtypeOf(String t1, String t2) {
        return ClassHierarchy.isSubtypeOf(hierarchy, t1, t2) != ClassHierarchy.NO;
    }

    public final String findCommonSupertype(String t1, String t2) {
        return ClassHierarchy.findCommonSupertype(hierarchy, t1, t2);
    }

    public final BitSet getBasicBlockStarts() {
        if (basicBlockStarts != null) {
            return basicBlockStarts;
        }
        BitSet r = new BitSet(instructions.length);
        r.set(0);
        for (int i = 0; i < instructions.length; i++) {
            int[] targets = instructions[i].getBranchTargets();
            for (int j = 0; j < targets.length; j++) {
                r.set(targets[j]);
            }
        }
        for (int i = 0; i < handlers.length; i++) {
            ExceptionHandler[] hs = handlers[i];
            if (hs != null) {
                for (int j = 0; j < hs.length; j++) {
                    r.set(hs[j].getHandler());
                }
            }
        }
        basicBlockStarts = r;
        return r;
    }

    public final IInstruction[] getInstructions() {
        return instructions;
    }

    private void getReachableRecursive(int from, BitSet reachable, boolean followHandlers, BitSet mask) throws IllegalArgumentException {
        if (from < 0) {
            throw new IllegalArgumentException("from < 0");
        }
        while (true) {
            if (reachable.get(from) || (mask != null && !mask.get(from))) {
                return;
            }
            reachable.set(from);
            IInstruction instr = instructions[from];
            int[] targets = instr.getBranchTargets();
            for (int i = 0; i < targets.length; i++) {
                getReachableRecursive(targets[i], reachable, followHandlers, mask);
            }
            if (followHandlers) {
                ExceptionHandler[] hs = handlers[from];
                for (int i = 0; i < hs.length; i++) {
                    getReachableRecursive(hs[i].getHandler(), reachable, followHandlers, mask);
                }
            }
            if (instr.isFallThrough()) {
                ++from;
                continue;
            }
            break;
        }
    }

    public final BitSet getReachableFrom(int from) {
        return getReachableFrom(from, true, null);
    }

    public final void getReachableFromUpdate(int from, BitSet reachable, boolean followHandlers, BitSet mask) {
        if (reachable == null) {
            throw new IllegalArgumentException("reachable is null");
        }
        reachable.clear();
        getReachableRecursive(from, reachable, followHandlers, mask);
    }

    public final BitSet getReachableFrom(int from, boolean followHandlers, BitSet mask) {
        BitSet reachable = new BitSet();
        getReachableRecursive(from, reachable, followHandlers, mask);
        return reachable;
    }

    private void getReachingRecursive(int to, BitSet reaching, BitSet mask) {
        while (true) {
            if (reaching.get(to) || (mask != null && !mask.get(to))) {
                return;
            }
            reaching.set(to);
            int[] targets = backEdges[to];
            for (int i = 0; i < targets.length; i++) {
                getReachingRecursive(targets[i], reaching, mask);
            }
            if (to > 0 && instructions[to - 1].isFallThrough()) {
                --to;
                continue;
            }
            break;
        }
    }

    private void getReachingBase(int to, BitSet reaching, BitSet mask) {
        int[] targets = backEdges[to];
        for (int i = 0; i < targets.length; i++) {
            getReachingRecursive(targets[i], reaching, mask);
        }
        if (to > 0 && instructions[to - 1].isFallThrough()) {
            getReachingRecursive(to - 1, reaching, mask);
        }
    }

    public final void getReachingToUpdate(int to, BitSet reaching, BitSet mask) {
        if (reaching == null) {
            throw new IllegalArgumentException("reaching is null");
        }
        getBackEdges();
        reaching.clear();
        getReachingBase(to, reaching, mask);
    }

    final BitSet getReachingTo(int to, BitSet mask) {
        getBackEdges();
        BitSet reaching = new BitSet();
        getReachingBase(to, reaching, mask);
        return reaching;
    }

    final BitSet getReachingTo(int to) {
        return getReachingTo(to, null);
    }

    private void computeStackSizesAt(int[] stackSizes, int i, int size) throws FailureException {
        while (true) {
            if (stackSizes[i] >= 0) {
                if (size != stackSizes[i]) {
                    throw new FailureException(i, "Stack size mismatch", null);
                }
                return;
            }
            stackSizes[i] = size;
            IInstruction instr = instructions[i];
            if (instr instanceof DupInstruction) {
                size += ((DupInstruction) instr).getSize();
            } else if (instr instanceof SwapInstruction) {
            } else {
                size -= instr.getPoppedCount();
                if (instr.getPushedWordSize() > 0) {
                    size++;
                }
            }
            int[] targets = instr.getBranchTargets();
            for (int j = 0; j < targets.length; j++) {
                computeStackSizesAt(stackSizes, targets[j], size);
            }
            ExceptionHandler[] hs = handlers[i];
            for (int j = 0; j < hs.length; j++) {
                computeStackSizesAt(stackSizes, hs[j].getHandler(), 1);
            }
            if (!instr.isFallThrough()) {
                return;
            }
            i++;
        }
    }

    /**
   * This exception is thrown by verify() when it fails.
   */
    public static final class FailureException extends Exception {

        private static final long serialVersionUID = -7663520961403117526L;

        private final int offset;

        private final String reason;

        private List<PathElement> path;

        FailureException(int offset, String reason, List<PathElement> path) {
            super(reason + " at offset " + offset);
            this.offset = offset;
            this.reason = reason;
            this.path = path;
        }

        /**
     * @return the index of the Instruction which failed to verify
     */
        public int getOffset() {
            return offset;
        }

        /**
     * @return a description of the reason why verification failed
     */
        public String getReason() {
            return reason;
        }

        /**
     * @return a list of PathElements describing how the type that caused the error was propagated from its origin to the point of
     *         the error
     */
        public List<PathElement> getPath() {
            return path;
        }

        void setPath(List<PathElement> path) {
            this.path = path;
        }

        /**
     * Print the path to the given stream, if there is one.
     */
        public void printPath(Writer w) throws IOException {
            if (path != null) {
                for (int i = 0; i < path.size(); i++) {
                    PathElement elem = path.get(i);
                    String[] stack = elem.stack;
                    String[] locals = elem.locals;
                    w.write("Offset " + elem.index + ": [");
                    for (int j = 0; j < stack.length; j++) {
                        if (j > 0) {
                            w.write(",");
                        }
                        w.write(stack[j]);
                    }
                    w.write("], [");
                    for (int j = 0; j < locals.length; j++) {
                        if (j > 0) {
                            w.write(",");
                        }
                        w.write(locals[j] == null ? "?" : locals[j]);
                    }
                    w.write("]\n");
                }
            }
        }
    }

    public static final class PathElement {

        final int index;

        final String[] stack;

        final String[] locals;

        PathElement(int index, String[] stack, String[] locals) {
            this.stack = stack.clone();
            this.locals = locals.clone();
            this.index = index;
        }

        /**
     * @return the bytecode offset of the instruction causing a value transfer.
     */
        public int getIndex() {
            return index;
        }

        /**
     * @return the types of the local variabls at the instruction.
     */
        public String[] getLocals() {
            return locals;
        }

        /**
     * @return the types of the working stack at the instruction.
     */
        public String[] getStack() {
            return stack;
        }
    }

    private String[] cutArray(String[] a, int len) {
        if (len == 0) {
            return noStrings;
        } else {
            String[] r = new String[len];
            System.arraycopy(a, 0, r, 0, len);
            return r;
        }
    }

    private boolean mergeTypes(int i, String[] curStack, int curStackSize, String[] curLocals, int curLocalsSize, List<PathElement> path) throws FailureException {
        boolean changed = false;
        if (stacks[i] == null) {
            stacks[i] = cutArray(curStack, curStackSize);
            changed = true;
        } else {
            String[] st = stacks[i];
            if (st.length != curStackSize) {
                throw new FailureException(i, "Stack size mismatch: " + st.length + ", " + curStackSize, path);
            }
            for (int j = 0; j < curStackSize; j++) {
                String t = findCommonSupertype(st[j], curStack[j]);
                if (t != st[j]) {
                    if (t == null) {
                        throw new FailureException(i, "Stack type mismatch at " + j + " (" + st[j] + " vs " + curStack[j] + ")", path);
                    }
                    st[j] = t;
                    changed = true;
                }
            }
        }
        if (locals[i] == null) {
            locals[i] = cutArray(curLocals, curLocalsSize);
            changed = true;
        } else {
            String[] ls = locals[i];
            for (int j = 0; j < ls.length; j++) {
                String t = findCommonSupertype(ls[j], curLocals[j]);
                if (t != ls[j]) {
                    ls[j] = t;
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
   * A PathElement describes a point where a value is moved from one location to another.
   */
    private void computeTypes(int i, TypeVisitor visitor, BitSet makeTypesAt, List<PathElement> path) throws FailureException {
        final String[] curStack = new String[maxStack];
        final String[] curLocals = new String[maxLocals];
        while (true) {
            if (path != null) {
                path.add(new PathElement(i, stacks[i], locals[i]));
            }
            int curStackSize = stacks[i].length;
            System.arraycopy(stacks[i], 0, curStack, 0, curStackSize);
            final int[] curLocalsSize = { locals[i].length };
            System.arraycopy(locals[i], 0, curLocals, 0, curLocalsSize[0]);
            IInstruction.Visitor localsUpdate = new IInstruction.Visitor() {

                @Override
                public void visitLocalLoad(ILoadInstruction instruction) {
                    String t = curLocals[instruction.getVarIndex()];
                    curStack[0] = t;
                }

                @Override
                public void visitLocalStore(IStoreInstruction instruction) {
                    int index = instruction.getVarIndex();
                    curLocals[index] = curStack[0];
                    if (index >= curLocalsSize[0]) {
                        curLocalsSize[0] = index + 1;
                    }
                }
            };
            boolean restart = false;
            while (true) {
                IInstruction instr = instructions[i];
                int popped = instr.getPoppedCount();
                if (curStackSize < popped) {
                    throw new FailureException(i, "Stack underflow", path);
                }
                if (visitor != null) {
                    visitor.setState(i, path, curStack, curLocals);
                    instr.visit(visitor);
                    if (!visitor.shouldContinue()) {
                        return;
                    }
                }
                if (instr instanceof DupInstruction) {
                    DupInstruction d = (DupInstruction) instr;
                    int size = d.getSize();
                    System.arraycopy(curStack, popped, curStack, popped + size, curStackSize - popped);
                    System.arraycopy(curStack, 0, curStack, popped, size);
                    curStackSize += size;
                } else if (instr instanceof SwapInstruction) {
                    String s = curStack[0];
                    curStack[0] = curStack[1];
                    curStack[1] = s;
                } else {
                    String pushed = instr.getPushedType(curStack);
                    if (pushed != null) {
                        System.arraycopy(curStack, popped, curStack, 1, curStackSize - popped);
                        curStack[0] = Util.getStackType(pushed);
                        instr.visit(localsUpdate);
                        curStackSize -= popped - 1;
                    } else {
                        instr.visit(localsUpdate);
                        System.arraycopy(curStack, popped, curStack, 0, curStackSize - popped);
                        curStackSize -= popped;
                    }
                }
                int[] targets = instr.getBranchTargets();
                for (int j = 0; j < targets.length; j++) {
                    if (mergeTypes(targets[j], curStack, curStackSize, curLocals, curLocalsSize[0], path)) {
                        computeTypes(targets[j], visitor, makeTypesAt, path);
                    }
                }
                if (!instr.isFallThrough()) {
                    break;
                } else {
                    i++;
                    if (makeTypesAt.get(i)) {
                        if (mergeTypes(i, curStack, curStackSize, curLocals, curLocalsSize[0], path)) {
                            restart = true;
                            break;
                        }
                        if (path != null) {
                            path.remove(path.size() - 1);
                        }
                        return;
                    }
                }
            }
            if (!restart) {
                break;
            }
        }
        if (path != null) {
            path.remove(path.size() - 1);
        }
    }

    public int[] getStackSizes() throws FailureException {
        if (stackSizes != null) {
            return stackSizes;
        }
        stackSizes = new int[instructions.length];
        for (int i = 0; i < stackSizes.length; i++) {
            stackSizes[i] = -1;
        }
        computeStackSizesAt(stackSizes, 0, 0);
        return stackSizes;
    }

    private void computeMaxLocals() {
        maxLocals = locals[0].length;
        for (int i = 0; i < instructions.length; i++) {
            IInstruction instr = instructions[i];
            if (instr instanceof LoadInstruction) {
                maxLocals = Math.max(maxLocals, ((LoadInstruction) instr).getVarIndex() + 1);
            } else if (instr instanceof StoreInstruction) {
                maxLocals = Math.max(maxLocals, ((StoreInstruction) instr).getVarIndex() + 1);
            }
        }
    }

    protected final void initTypeInfo() throws FailureException {
        stacks = new String[instructions.length][];
        locals = new String[instructions.length][];
        stacks[0] = noStrings;
        locals[0] = Util.getParamsTypesInLocals(isStatic ? null : classType, signature);
        int[] stackSizes = getStackSizes();
        maxStack = 0;
        for (int i = 0; i < stackSizes.length; i++) {
            maxStack = Math.max(maxStack, stackSizes[i]);
        }
        computeMaxLocals();
    }

    /**
   * Verify the method and compute types at every program point.
   * 
   * @throws FailureException the method contains invalid bytecode
   */
    public final void computeTypes(TypeVisitor v, BitSet makeTypesAt, boolean wantPath) throws FailureException {
        initTypeInfo();
        computeTypes(0, v, makeTypesAt, wantPath ? new ArrayList<PathElement>() : null);
    }

    public abstract class TypeVisitor extends IInstruction.Visitor {

        public abstract void setState(int index, List<PathElement> path, String[] curStack, String[] curLocals);

        public abstract boolean shouldContinue();
    }

    /**
   * @return an array indexed by instruction index; each entry is an array of Strings giving the types of the locals at that
   *         instruction.
   */
    public final String[][] getLocalTypes() {
        return locals;
    }

    /**
   * @return an array indexed by instruction index; each entry is an array of Strings giving the types of the stack elements at that
   *         instruction. The top of the stack is the last element of the array.
   */
    public final String[][] getStackTypes() {
        return stacks;
    }

    protected Analyzer(MethodData info) {
        this(info.getIsStatic(), info.getClassType(), info.getSignature(), info.getInstructions(), info.getHandlers());
    }

    public static Analyzer createAnalyzer(MethodData info) {
        if (info == null) {
            throw new IllegalArgumentException("info is null");
        }
        return new Analyzer(info);
    }
}
