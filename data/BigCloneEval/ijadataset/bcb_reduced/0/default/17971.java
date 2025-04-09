/**
 * @author John Barton
 */
public class ByteCodeContext {

    private VM_Method method;

    private byte[] bcodes;

    private int bindex;

    private int instruction_index;

    public static boolean traceByteCodes = false;

    ByteCodeContext(VM_Method method_in) {
        method = method_in;
        bcodes = method.getBytecodes();
    }

    boolean hasMoreElements() {
        return bindex < bcodes.length;
    }

    public int getCurrentIndex() {
        return instruction_index;
    }

    VM_Method getMethod() {
        return method;
    }

    ByteCode getByteCode(int bytecode_offset) {
        byte second = 0;
        if (bytecode_offset + 1 < bcodes.length) second = bcodes[bytecode_offset + 1];
        return new ByteCode(bcodes[bytecode_offset], second);
    }

    /** 
    *   Return the bytecode offset (0-based) for the catch clause
    *   that handles exceptions in the method of the type given as 
    *   the argument t
    *
    *    @param t the exception
    *    @return the index into the bytecodes were the handler starts 
    *    or -1 if there is no handler.
    */
    public int getHandlerOffsetForExceptionsMatching(Throwable t) {
        try {
            VM_Class cls = InterpreterBase.forName(t.getClass().getName());
            VM_Type exception_type = (VM_Type) cls;
            return method.findCatchBlockForBytecode(bindex, exception_type);
        } catch (VM_ResolutionException e) {
            return -1;
        }
    }

    public String toString() {
        return "ByteCodeContext at " + method + ":" + getCurrentIndex() + " " + getByteCode(getCurrentIndex()).toString();
    }

    final int fetch1ByteSigned() {
        return bcodes[bindex++];
    }

    final int getNextCode() {
        instruction_index = bindex;
        return fetch1ByteUnsigned();
    }

    final int fetch1ByteUnsigned() {
        return bcodes[bindex++] & 0xFF;
    }

    final int fetch2BytesSigned() {
        int i = bcodes[bindex++] << 8;
        i |= (bcodes[bindex++] & 0xFF);
        return i;
    }

    final int fetch2BytesUnsigned() {
        int i = (bcodes[bindex++] & 0xFF) << 8;
        i |= (bcodes[bindex++] & 0xFF);
        return i;
    }

    final int fetch4BytesSigned() {
        int i = bcodes[bindex++] << 24;
        i |= (bcodes[bindex++] & 0xFF) << 16;
        i |= (bcodes[bindex++] & 0xFF) << 8;
        i |= (bcodes[bindex++] & 0xFF);
        return i;
    }

    final int fetch4BytesSigned(int index) {
        int i = bcodes[index++] << 24;
        i |= (bcodes[index++] & 0xFF) << 16;
        i |= (bcodes[index++] & 0xFF) << 8;
        i |= (bcodes[index++] & 0xFF);
        return i;
    }

    final void takeBranch() {
        if (traceByteCodes) System.out.println("(branch taken)");
        int offset = fetch2BytesSigned();
        bindex += offset - 3;
    }

    final void skipBranch() {
        if (traceByteCodes) System.out.println("(branch not taken)");
        bindex += 2;
    }

    final void takeWideBranch() {
        if (traceByteCodes) System.out.println("(wide branch taken)");
        int offset = fetch4BytesSigned();
        bindex += offset - 3;
    }

    final void tableSwitch(int val) {
        int start = bindex - 1;
        int align = bindex & 3;
        if (align != 0) bindex += 4 - align;
        int defaultoff = this.fetch4BytesSigned();
        int low = this.fetch4BytesSigned();
        if (val < low) bindex = start + defaultoff; else {
            int high = this.fetch4BytesSigned();
            if (val > high) {
                bindex = start + defaultoff;
            } else {
                bindex += (val - low) * 4;
                int offset = this.fetch4BytesSigned();
                bindex = start + offset;
            }
        }
    }

    final void lookupswitch(int val) {
        if (traceByteCodes) System.out.println("lookupswitch");
        int start = bindex - 1;
        int align = bindex & 3;
        if (align != 0) bindex += 4 - align;
        int defaultoff = this.fetch4BytesSigned();
        int npairs = this.fetch4BytesSigned();
        int first = 0;
        int last = npairs - 1;
        for (; ; ) {
            if (first > last) {
                bindex = start + defaultoff;
                break;
            }
            int current = (last + first) / 2;
            int match = this.fetch4BytesSigned(bindex + current * 8);
            if (val < match) {
                last = current - 1;
            } else if (val > match) {
                first = current + 1;
            } else {
                int offset = this.fetch4BytesSigned(bindex + 4 + current * 8);
                bindex = start + offset;
                break;
            }
        }
    }

    final JumpSubroutineReturnOffset jumpSubroutine() {
        int jsr_index = bindex - 1;
        int offset = fetch2BytesSigned();
        if (traceByteCodes) System.out.println("jsr from " + jsr_index + " to " + (jsr_index + offset));
        JumpSubroutineReturnOffset return_offset = new JumpSubroutineReturnOffset(bindex);
        bindex = jsr_index + offset;
        return return_offset;
    }

    final JumpSubroutineReturnOffset jumpWideSubroutine() {
        int jsr_index = bindex - 1;
        int offset = fetch4BytesSigned();
        if (traceByteCodes) System.out.println("jsr from " + jsr_index + " to " + (jsr_index + offset));
        JumpSubroutineReturnOffset return_offset = new JumpSubroutineReturnOffset(bindex);
        bindex = jsr_index + offset;
        return return_offset;
    }

    final void returnSubroutine(Object jump_subroutine_return_offset) {
        JumpSubroutineReturnOffset offset = (JumpSubroutineReturnOffset) jump_subroutine_return_offset;
        bindex = offset.getBytecodeOffset();
    }

    final void jumpException(int offset) {
        bindex = offset;
    }
}
