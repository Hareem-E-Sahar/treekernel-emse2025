package wb;

import static wb.Wbdefs.*;
import java.io.*;
import java.lang.reflect.*;

public class Wbsys {

    public static RandomAccessFile openInputFile(String name) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(name, "r");
        } catch (FileNotFoundException e) {
        }
        return file;
    }

    public static RandomAccessFile openOutputFile(String name) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(name, "rw");
        } catch (FileNotFoundException e) {
        }
        return file;
    }

    public static RandomAccessFile openIoFile(String name) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(name, "rw");
        } catch (FileNotFoundException e) {
        }
        return file;
    }

    public static long filePosition(RandomAccessFile file) {
        long pointer = -1;
        try {
            pointer = file.getFilePointer();
        } catch (IOException e) {
        }
        return pointer;
    }

    public static boolean filePosition(RandomAccessFile file, long pos) {
        boolean retVal = false;
        try {
            file.seek(pos);
            retVal = true;
        } catch (IOException e) {
        }
        return retVal;
    }

    public static boolean closePort(RandomAccessFile file) {
        boolean retVal = false;
        try {
            file.close();
            retVal = true;
        } catch (IOException e) {
        }
        return retVal;
    }

    public static int subbytesWrite(byte[] buf, int start, int end, RandomAccessFile file) {
        int retVal = -1;
        try {
            file.write(buf, start, (end - start));
            retVal = end - start;
        } catch (IOException e) {
        }
        return retVal;
    }

    public static int subbytesRead(byte[] buf, int start, int end, RandomAccessFile file) {
        int retVal = -1;
        try {
            retVal = file.read(buf, start, (end - start));
        } catch (IOException e) {
        }
        return retVal;
    }

    public static boolean outputPort_P(RandomAccessFile file) {
        if (file == null) return false;
        try {
            file.getFD();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean inputPort_P(RandomAccessFile file) {
        if (file == null) return false;
        try {
            file.getFD();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static int min(int a, int b) {
        return (a < b ? a : b);
    }

    public static int max(int a, int b) {
        return (a < b ? b : a);
    }

    public static void dprintf(String s) {
        System.err.print(s);
    }

    public static void substringMove(byte[] src, int srcStart, int srcEnd, byte[] dest, int desStart) {
        System.arraycopy(src, srcStart, dest, desStart, (srcEnd - srcStart));
    }

    public static void substringMoveLeft(byte[] src, int srcStart, int srcEnd, byte[] dest, int desStart) {
        System.arraycopy(src, srcStart, dest, desStart, (srcEnd - srcStart));
    }

    public static void substringMoveRight(byte[] src, int srcStart, int srcEnd, byte[] dest, int desStart) {
        System.arraycopy(src, srcStart, dest, desStart, (srcEnd - srcStart));
    }

    public static byte[] subArray(byte[] byts, int idx) {
        byte[] smallArray = new byte[idx];
        substringMove(byts, 0, idx, smallArray, 0);
        return smallArray;
    }

    public static byte[] stringToBytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("stringToBytes: " + e);
            System.exit(-1);
        } catch (NullPointerException e) {
        }
        return (byte[]) null;
    }

    public static String bytesToString(byte[] byts) {
        try {
            return new String(byts, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("bytesToString: " + e);
            System.exit(-1);
        } catch (NullPointerException e) {
        }
        return (String) null;
    }

    public static int strlen(String str) {
        return str.length();
    }

    public static int strlen(byte[] str) {
        return str.length;
    }

    public static int time(byte[] b) {
        java.util.Date date = new java.util.Date();
        return (int) date.getTime() / 1000;
    }

    public static Object resizeArray(Object old, int newLength) {
        int oldLength = Array.getLength(old);
        Class elementType = old.getClass().getComponentType();
        Object newArray = Array.newInstance(elementType, newLength);
        int upto = (oldLength < newLength) ? oldLength : newLength;
        System.arraycopy(old, 0, newArray, 0, upto);
        return newArray;
    }

    public static java.lang.reflect.Method selectSplitFun(int type) {
        java.lang.Class blinkClass = null;
        java.lang.reflect.Method dummy = null;
        try {
            blinkClass = java.lang.Class.forName("Blink");
        } catch (ClassNotFoundException e) {
            System.out.println("Class Blink not found");
        }
        java.lang.Class[] argClass = new Class[] { byte[].class, byte[].class, int.class, byte[].class, int.class, int.class, byte[].class, int.class };
        try {
            switch(type) {
                case pastp:
                    return blinkClass.getMethod("pastpLeafSplit", argClass);
                case qpastp:
                    return blinkClass.getMethod("qpastpLeafSplit", argClass);
                case match:
                    return blinkClass.getMethod("valLeafSplit", argClass);
                default:
                    return blinkClass.getMethod("dummyLeafSplit", argClass);
            }
        } catch (NoSuchMethodException e) {
            System.out.println("No method of the given name found in the blink class");
        }
        return dummy;
    }

    /** Returns a Method object when given the method signature and the class it was
 * defined in.
 */
    public static Method getMethod(String className, String methodName, Class[] args) {
        Class cls = null;
        Method mtd = null;
        try {
            cls = Class.forName(className);
            mtd = cls.getMethod(methodName, args);
        } catch (Exception e) {
            dprintf("Exception " + e);
        }
        return mtd;
    }

    /** Wrapper for invoking methods that return integers. The wrapper is only for
 * handling exceptions.
 * @param func Method
 * @param obj  Object of the class the method was defined in. A null is sufficient for
 *              static methods.
 * @param args  Method arguments
 * @return  integer
 */
    public static int intFunInvoke(Method func, Object obj, Object[] args) {
        int result = -1;
        try {
            result = (Integer) func.invoke(obj, args);
        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }

    public static final byte[] longZero = new byte[16];

    public static final int jZero = 0;

    public static final int jOne = 1;

    public static final Object jNull = null;
}
