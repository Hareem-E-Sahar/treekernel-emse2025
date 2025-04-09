package jout.JBC;

/**
 *
 * @author shumi
 */
public class JByteCode {

    boolean debugmode = false;

    protected java.util.ArrayList<Opcode> __codes;

    public JByteCode() {
        __codes = new java.util.ArrayList<Opcode>();
    }

    public JByteCode(byte[] code) throws Exception {
        int i = 0, j, l = code.length, arglen;
        boolean wide = false;
        int temp_code;
        __codes = new java.util.ArrayList<Opcode>();
        while (i < l) {
            temp_code = code[i] & 0x000000FF;
            if (debugmode) System.out.println("\t\t[JByteCode()]\t" + i + "/" + (l - 1) + "\tcode = 0x" + Integer.toHexString(temp_code) + "(" + temp_code + "," + Opcode.getName(temp_code) + ")");
            if (wide) {
                if (!Opcode.canBeWide(temp_code)) throw new Exception("Wide command used before command that cannot be extended!");
                wide = false;
            }
            if (temp_code == BC.WIDE.ordinal()) wide = true;
            if (temp_code == BC.TABLESWITCH.ordinal()) {
                while (i % 4 != 0) ++i;
                int low = ((code[i + 4] & 0x000000FF) << 24) + ((code[i + 5] & 0x000000FF) << 16) + ((code[i + 6] & 0x000000FF) << 8) + (code[i + 7] & 0x000000FF);
                int high = ((code[i + 8] & 0x000000FF) << 24) + ((code[i + 9] & 0x000000FF) << 16) + ((code[i + 10] & 0x000000FF) << 8) + (code[i + 11] & 0x000000FF);
                arglen = ((high - low + 1) << 2) + 12;
                byte[] args_temp = new byte[arglen];
                for (j = 0; j < arglen; ++j) args_temp[j] = code[i + j];
                if (!add(new Opcode(BC.TABLESWITCH, args_temp, wide))) throw new Exception("Cannot add all codes to table!");
                i += arglen + 1;
            } else if (temp_code == BC.LOOKUPSWITCH.ordinal()) {
                while (i % 4 != 0) ++i;
                int n = ((code[i + 4] & 0x000000FF) << 24) + ((code[i + 5] & 0x000000FF) << 16) + ((code[i + 6] & 0x000000FF) << 8) + (code[i + 7] & 0x000000FF);
                arglen = (n << 3) + 8;
                byte[] args_temp = new byte[arglen];
                for (j = 0; j < arglen; ++j) {
                    args_temp[j] = code[i + j];
                }
                if (!add(new Opcode(BC.LOOKUPSWITCH, args_temp, wide))) throw new Exception("Cannot add all codes to table!");
                i += arglen;
            } else {
                arglen = Opcode.argSize(temp_code, wide);
                ++i;
                byte[] args_temp = new byte[arglen];
                for (j = 0; j < arglen; ++j) args_temp[j] = code[i + j];
                if (!add(new Opcode(temp_code, args_temp, wide))) throw new Exception("Cannot add all codes to table!");
                i += arglen;
            }
        }
    }

    public void setDebugMode(boolean state) {
        debugmode = state;
    }

    public int size() {
        return __codes.size();
    }

    public boolean add(Opcode opcode) {
        return __codes.add(opcode);
    }

    public void add(int index, Opcode opcode) {
        __codes.add(index, opcode);
    }

    /**
     * Reallocates an array with a new size, and copies the contents
     * of the old array to the new array.
     * @param oldArray  the old array, to be reallocated.
     * @param newSize   the new array size.
     * @return          A new array with the same contents.
     */
    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        return newArray;
    }

    public Opcode get(int index) {
        return __codes.get(index);
    }
}
