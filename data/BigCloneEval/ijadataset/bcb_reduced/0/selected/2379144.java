package arcadeflex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Random;

/**
 *
 * @author shadow
 */
public class libc {

    public static int argc;

    public static String[] argv;

    private static Random rand = new Random();

    public static final int UCLOCKS_PER_SEC = 1000000000;

    public static void ConvertArguments(String mainClass, String[] arguments) {
        argc = arguments.length + 1;
        argv = new String[argc];
        argv[0] = mainClass;
        for (int i = 1; i < argc; i++) {
            argv[i] = arguments[i - 1];
        }
    }

    public static char[] CreateArray(int size, char[] array) {
        char[] arrayChar = new char[size];
        for (int i = 0; i < array.length; i++) {
            arrayChar[i] = array[i];
        }
        return arrayChar;
    }

    public static int BOOL(int value) {
        return value != 0 ? 1 : 0;
    }

    public static int BOOL(boolean value) {
        return value ? 1 : 0;
    }

    public static int NOT(int value) {
        return value == 0 ? 1 : 0;
    }

    public static int NOT(boolean value) {
        return !value ? 1 : 0;
    }

    public static void printf(String str, Object... arguments) {
        System.out.printf(str, arguments);
    }

    public static String sprintf(String str, Object... arguments) {
        return String.format(str, arguments);
    }

    public static void sprintf(char[] array, String sstr, Object[] obj) {
        String str = String.format(sstr, obj) + '\0';
        char[] arrayOfChar = str.toCharArray();
        CopyArray(array, arrayOfChar);
    }

    public static int atoi(String str) {
        return Integer.parseInt(str);
    }

    public static int rand() {
        return rand.nextInt();
    }

    public static long uclock() {
        return System.nanoTime();
    }

    public static void getchar() {
        try {
            System.in.read();
        } catch (Exception e) {
        }
    }

    public static int sizeof(char[] array) {
        return array.length;
    }

    public static void CopyArray(Object[] dst, Object[] src) {
        if (src == null) {
            return;
        }
        int k;
        for (k = 0; k < src.length; k++) {
            dst[k] = src[k];
        }
    }

    public static void CopyArray(char[] dst, char[] src) {
        if (src == null) {
            return;
        }
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
        }
    }

    public static int stricmp(String str1, String str2) {
        return str1.compareToIgnoreCase(str2);
    }

    public static int stricmp(char[] array, String str2) {
        String str = makeString(array);
        return str.compareToIgnoreCase(str2);
    }

    public static void strcpy(char[] dst, String src) {
        for (int i = 0; i < src.length(); i++) {
            dst[i] = src.charAt(i);
        }
    }

    public static String strcpy(String str) {
        return str;
    }

    public static String strcat(String str1, String str2) {
        return str1 + str2;
    }

    public static int strcmp(String str1, String str2) {
        return str1.compareTo(str2);
    }

    public static int strlen(String str) {
        return str.length();
    }

    public static int strlen(char[] ch) {
        int size = 0;
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] == 0) {
                break;
            }
            size++;
        }
        return size;
    }

    public static void memset(char[] buf, int value, int size) {
        for (int mem = 0; mem < size; mem++) {
            buf[mem] = (char) value;
        }
    }

    public static void memset(char[] buf, int ofs, int value, int size) {
        for (int mem = 0; mem < size; mem++) {
            buf[ofs + mem] = (char) value;
        }
    }

    public static int memcmp(char[] dst, int dstofs, char[] src, int size) {
        for (int mem = 0; mem < size; mem++) {
            if (dst[dstofs + mem] != src[mem]) {
                return -1;
            }
        }
        return 0;
    }

    public static int memcmp(char[] dst, char[] src, int size) {
        for (int i = 0; i < size; i++) {
            if (dst[i] != src[i]) {
                return -1;
            }
        }
        return 0;
    }

    public static int memcmp(char[] dst, int dstofs, char[] src, int srcofs, int size) {
        for (int mem = 0; mem < size; mem++) {
            if (dst[dstofs + mem] != src[srcofs + mem]) {
                return -1;
            }
        }
        return 0;
    }

    public static int memcmp(char[] dist, int dstoffs, String src, int size) {
        char[] srcc = src.toCharArray();
        for (int i = 0; i < size; i++) {
            if (dist[(dstoffs + i)] != srcc[i]) {
                return -1;
            }
        }
        return 0;
    }

    public static void memcpy(char[] dst, char[] src, int size) {
        for (int i = 0; i < Math.min(size, src.length); i++) {
            dst[i] = src[i];
        }
    }

    public static void memcpy(char[] dst, int dstofs, char[] src, int srcofs, int size) {
        for (int mem = 0; mem < size; mem++) {
            dst[dstofs + mem] = src[srcofs + mem];
        }
    }

    public static void memcpy(CharPtr dst, CharPtr src, int size) {
        for (int i = 0; i < size; i++) {
            dst.write(i, src.read(i));
        }
    }

    public static void memcpy(char[] dst, CharPtr src, int size) {
        for (int i = 0; i < size; i++) {
            dst[i] = src.read(i);
        }
    }

    public static String makeString(char[] array) {
        int i = 0;
        for (i = 0; i < array.length; i++) {
            if (array[i] == 0) {
                break;
            }
        }
        return new String(array, 0, i);
    }

    /*************************************
     *
     *
     *  FILE functions
     ************************************/
    public static class FILE {

        public FILE() {
        }

        public RandomAccessFile raf;

        public FileOutputStream fos;

        public FileWriter fw;

        public InputStream is;

        public String Name;
    }

    public static FILE fopen(char[] name, String format) {
        String nameS = new String(name);
        return fopen(nameS, format);
    }

    public static FILE fopen(String name, String format) {
        FILE file;
        mame.mame.dlprogress.setFileName("fetching file: " + name);
        file = new FILE();
        if (format.compareTo("rb") == 0) {
            try {
                if (mame.mame.onlinerom) {
                    File f = UrlDownload.fileUrl("http://www.arcadeflex.com/" + name);
                    file.raf = new RandomAccessFile(f, "r");
                    f.createNewFile();
                    f = null;
                } else {
                    file.raf = new RandomAccessFile(name, "r");
                }
                file.Name = name;
            } catch (Exception e) {
                file = null;
                return null;
            }
            return file;
        } else if (format.compareTo("wb") == 0) {
            try {
                if (mame.mame.onlinerom) {
                    File f = UrlDownload.fileUrl("http://www.arcadeflex.com/" + name);
                    file.fos = new FileOutputStream(f, false);
                    f.createNewFile();
                    f = null;
                } else {
                    file.fos = new FileOutputStream(name, false);
                }
            } catch (Exception e) {
                file = null;
                return null;
            }
            return file;
        } else if (format.compareTo("wa") == 0) {
            try {
                if (mame.mame.onlinerom) {
                    File f = UrlDownload.fileUrl("http://www.arcadeflex.com/" + name);
                    file.fw = new FileWriter(f, false);
                    f.createNewFile();
                    f = null;
                } else {
                    file.fw = new FileWriter(name, false);
                }
            } catch (Exception e) {
                file = null;
                return null;
            }
            return file;
        }
        file = null;
        return null;
    }

    public static int fread(char[] buf, int offset, int size, int count, FILE file) {
        byte bbuf[] = new byte[size * count];
        int readsize;
        try {
            readsize = file.raf.read(bbuf);
        } catch (Exception e) {
            bbuf = null;
            return -1;
        }
        for (int i = 0; i < readsize; i++) {
            buf[offset + i] = (char) ((bbuf[i] + 256) & 0xFF);
        }
        bbuf = null;
        return readsize;
    }

    public static int fread(char[] buf, int size, int count, FILE file) {
        return fread(buf, 0, size, count, file);
    }

    public static void fseek(FILE file, int pos) {
        if (file.raf != null) {
            try {
                file.raf.seek(pos);
            } catch (IOException ex) {
                file = null;
            }
        }
    }

    public static void fwrite(char[] buf, int offset, int size, int count, FILE file) {
        byte bbuf[] = new byte[size * count];
        for (int i = 0; i < size * count; i++) {
            bbuf[i] = (byte) (buf[offset + i] & 0xFF);
        }
        try {
            file.fos.write(bbuf);
        } catch (Exception e) {
            bbuf = null;
            return;
        }
        bbuf = null;
    }

    public static void fwrite(char[] buf, int size, int count, FILE file) {
        fwrite(buf, 0, size, count, file);
    }

    public static void fwrite(char buf, int size, int count, FILE file) {
        byte bbuf[] = new byte[size * count];
        bbuf[0] = (byte) (buf & 0xFF);
        try {
            file.fos.write(bbuf);
        } catch (Exception e) {
            bbuf = null;
            return;
        }
        bbuf = null;
    }

    public static void fprintf(FILE file, String str, Object... arguments) {
        String print = String.format(str, arguments);
        try {
            file.fw.write(print);
        } catch (Exception e) {
        }
    }

    public static void fclose(FILE file) {
        if (file.raf != null) {
            try {
                file.raf.close();
            } catch (Exception e) {
            }
        }
        if (file.is != null) {
            try {
                file.is.close();
            } catch (Exception e) {
            }
        }
        if (file.fos != null) {
            try {
                file.fos.close();
            } catch (Exception e) {
            }
        }
        if (file.fw != null) {
            try {
                file.fw.close();
            } catch (Exception e) {
            }
        }
    }

    /*************************************
     *
     *  Char Pointer Emulation
     *************************************/
    public static class CharPtr {

        public CharPtr() {
        }

        public CharPtr(char[] m) {
            set(m, 0);
        }

        public CharPtr(char[] m, int b) {
            set(m, b);
        }

        public CharPtr(CharPtr cp, int b) {
            set(cp.memory, cp.base + b);
        }

        public void set(char[] m, int b) {
            memory = m;
            base = b;
        }

        public char read(int offset) {
            return memory[base + offset];
        }

        public char read() {
            return memory[base];
        }

        public char readdec() {
            return this.memory[(this.base--)];
        }

        public char readinc() {
            return this.memory[(this.base++)];
        }

        public void write(int offset, int value) {
            memory[base + offset] = (char) value;
        }

        public void write(int value) {
            memory[base] = (char) value;
        }

        public void writeinc(int value) {
            this.memory[(this.base++)] = (char) value;
        }

        public void and(int value) {
            int tempbase = this.base;
            char[] tempmemory = this.memory;
            tempmemory[tempbase] = (char) (tempmemory[tempbase] & (char) value);
        }

        public void or(int value) {
            int tempbase = this.base;
            char[] tempmemory = this.memory;
            tempmemory[tempbase] = (char) (tempmemory[tempbase] | (char) value);
        }

        public void dec() {
            this.base -= 1;
        }

        public void inc() {
            this.base += 1;
        }

        public void inc(int count) {
            this.base += count;
        }

        public char[] getSubArray(int start, int end) {
            char[] b = new char[end - start + 1];
            for (int i = start; i <= end; i++) {
                b[(i - start)] = memory[i];
            }
            return b;
        }

        public char[] memory;

        public int base;
    }

    /*************************************
     *
     *  Int Pointer Emulation
     *************************************/
    public static class IntPtr {

        public IntPtr() {
        }

        public IntPtr(char[] m) {
            set(m, 0);
        }

        public IntPtr(IntPtr cp, int b) {
            set(cp.memory, cp.base + b);
        }

        public void set(char[] input, int b) {
            base = b;
            memory = input;
        }

        public void inc() {
            base += 4;
        }

        public void dec() {
            base -= 4;
        }

        public int read(int offset) {
            int myNumber = (((int) memory[base + offset]) << 0) | (((int) memory[base + offset + 1]) << 8) | (((int) memory[base + offset + 2]) << 16) | (((int) memory[base + offset + 3]) << 24);
            return myNumber;
        }

        public int read() {
            int myNumber = (((int) memory[base]) << 0) | (((int) memory[base + 1]) << 8) | (((int) memory[base + 2]) << 16) | (((int) memory[base + 3]) << 24);
            return myNumber;
        }

        public void or(int value) {
            int tempbase = this.base;
            char[] tempmemory = this.memory;
            tempmemory[tempbase] = (char) (tempmemory[tempbase] | (char) value);
        }

        public char[] readCA() {
            return memory;
        }

        public int getBase() {
            return base;
        }

        public int base;

        char[] memory;
    }

    /*************************************
     *
     *  Char Buffer Emulation
     *************************************/
    public static class CharBuf {

        public CharBuf() {
        }

        public CharBuf(String str) {
            set(str);
        }

        public void set(String str) {
            this.pos = 0;
            this.s = str;
            this.max = this.s.length();
            this.ch = (this.max == 0 ? '\0' : this.s.charAt(this.pos));
        }

        public void set(CharBuf cb, int ofs) {
            pos = cb.pos + ofs;
            this.s = cb.s;
            this.max = cb.max;
            if (this.pos < this.max) {
                this.ch = this.s.charAt(this.pos);
            } else {
                this.ch = '\0';
            }
        }

        public void inc() {
            this.pos += 1;
            if (this.pos < this.max) {
                this.ch = this.s.charAt(this.pos);
            } else {
                this.ch = '\0';
            }
        }

        int max;

        String s;

        public int pos;

        public char ch;
    }
}
