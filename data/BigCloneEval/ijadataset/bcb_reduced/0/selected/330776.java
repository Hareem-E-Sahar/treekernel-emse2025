package common.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import common.utilities.fp.UnaryFunction;

public class StringUtilities {

    public static byte[] compress(String string) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(out);
        zout.putNextEntry(new ZipEntry("0"));
        zout.write(string.getBytes("UTF-8"));
        zout.closeEntry();
        byte[] compressed = out.toByteArray();
        zout.close();
        return compressed;
    }

    public static String decompress(byte[] compressed) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(compressed);
        ZipInputStream zin = new ZipInputStream(in);
        zin.getNextEntry();
        byte[] buffer = new byte[1024];
        int byteCount = -1;
        while ((byteCount = zin.read(buffer)) != -1) {
            out.write(buffer, 0, byteCount);
        }
        String decompressed = out.toString("UTF-8");
        out.close();
        zin.close();
        boolean compatibilityWithIncorrectEncoding = false;
        byte[] searchArray = out.toByteArray();
        for (int a = 0; a < searchArray.length - 3; a++) {
            if (searchArray[a] == -61 && searchArray[a + 1] == -125 && searchArray[a + 2] < 0) {
                compatibilityWithIncorrectEncoding = true;
                break;
            }
        }
        if (compatibilityWithIncorrectEncoding) {
            byte[] b2 = decompressed.getBytes("Cp1252");
            decompressed = new String(b2, "UTF-8");
        }
        return decompressed;
    }

    public static String replaceFirst(String string, String search, String substitute) {
        return string.replaceFirst(search, substitute.replace("$", "\\$"));
    }

    public static String replaceNextParam(String string, Object substitute) {
        return replaceFirst(string, "\\?", "'" + substitute + "'");
    }

    public static String replaceNextParam(String string, String substitute) {
        return replaceFirst(string, "\\?", "'" + substitute + "'");
    }

    public static String nn(String string) {
        return (string == null || "null".equals(string)) ? "" : string;
    }

    public static boolean nn(Boolean bool) {
        return bool == null ? false : bool.booleanValue();
    }

    public static int nn(Integer value) {
        return value == null ? 0 : value.intValue();
    }

    public static String notEmpty(String string, String _default) {
        return (string == null || "null".equals(string) || "".equals(string)) ? _default : string;
    }

    public static boolean isNotEmpty(String string) {
        return string != null && !"".equals(string);
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || "".equals(string);
    }

    public static String noSpaces(String string) {
        String result = "";
        for (char c : string.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue;
            }
            result += c;
        }
        return result;
    }

    public static String removeSpacesAndCapitalise(String string) {
        char[] result = new char[string.length()];
        char[] chars = string.toCharArray();
        int pos = -1;
        boolean nextCapitalized = false;
        for (int i = 0; i < result.length; i++) {
            if (Character.isWhitespace(chars[i])) {
                nextCapitalized = true;
                continue;
            }
            pos++;
            if (nextCapitalized) {
                nextCapitalized = false;
                result[pos] = Character.toUpperCase(chars[i]);
            } else {
                result[pos] = chars[i];
            }
        }
        result[0] = Character.toLowerCase(result[0]);
        return new String(result, 0, pos + 1);
    }

    public static String[] toStringArray(Object... array) {
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = "" + array[i];
        }
        return result;
    }

    public static String[] toStringArray(Collection<?> collection) {
        String[] result = new String[collection.size()];
        Iterator<?> it = collection.iterator();
        for (int i = 0; i < collection.size(); i++) {
            result[i] = "" + it.next();
        }
        return result;
    }

    public static <T> String[] toStringArray(UnaryFunction<String, T> formatter, Collection<T> collection) {
        String[] result = new String[collection.size()];
        Iterator<T> it = collection.iterator();
        for (int i = 0; i < collection.size(); i++) {
            result[i] = formatter.eval(it.next());
        }
        return result;
    }

    public static String multiply(String delimiter, String string, int count) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++) {
            sb.append(string);
            if (i + 1 < count) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String join(List<Character> list) {
        char[] array = new char[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return new String(array);
    }

    public static String join(String delimiter, String[] array) {
        StringBuffer sb = new StringBuffer();
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i]);
                if (i + 1 < array.length) {
                    sb.append(delimiter);
                }
            }
        }
        return sb.toString();
    }

    public static String join(String delimiter, Object[] array) {
        StringBuffer sb = new StringBuffer();
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i]);
                if (i + 1 < array.length) {
                    sb.append(delimiter);
                }
            }
        }
        return sb.toString();
    }

    public static String join(String delimiter, Collection<String> array) {
        return join(delimiter, array.toArray());
    }

    public static <T> String join(String delimiter, UnaryFunction<String, T> formatter, T[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (formatter != null) {
                sb.append(formatter.eval(array[i]));
            } else {
                sb.append(array[i]);
            }
            if (i + 1 < array.length) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static <T> String join(String delimiter, UnaryFunction<String, T> formatter, Collection<T> array) {
        if (array.isEmpty()) {
            return "<empty>";
        }
        int i = 0;
        StringBuffer res = new StringBuffer();
        for (T item : array) {
            if (formatter != null) {
                res.append(formatter.eval(item));
            } else {
                res.append(item);
            }
            i++;
            if (i < array.size()) {
                res.append(delimiter);
            }
        }
        return res.toString();
    }

    public static void add(List<String> strings, String toPrepend, String toAppend) {
        if (toPrepend == null) {
            toPrepend = "";
        }
        if (toAppend == null) {
            toAppend = "";
        }
        for (int i = 0; i < strings.size(); i++) {
            strings.set(i, toPrepend + strings.get(i) + toAppend);
        }
    }

    public static String format(String pattern, Object... args) {
        return MessageFormat.format(pattern, args);
    }

    public static void printf(PrintStream out, String pattern, Object... args) {
        out.println(format(pattern, args));
    }

    public static String expand(String string, String delimiter, int number) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < number; i++) {
            if (i + 1 < number) {
                result.append(string + delimiter);
            } else {
                result.append(string);
            }
        }
        return result.toString();
    }

    public static String firstToUpperCase(String property) {
        if ("".equals(property)) {
            return "";
        }
        if (Character.isUpperCase(property.charAt(0))) {
            return property;
        }
        char[] chars = property.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String firstToLowerCase(String property) {
        if ("".equals(property)) {
            return "";
        }
        if (Character.isLowerCase(property.charAt(0))) {
            return property;
        }
        char[] chars = property.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
