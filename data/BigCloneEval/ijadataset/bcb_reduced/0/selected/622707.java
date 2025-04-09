package org.pqt.mr2rib.ribtranslator;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Vector;
import org.pqt.mr2rib.GlobalOptions;
import org.pqt.mr2rib.PrivateOptions;
import org.pqt.mr2rib.RendererOptions;

/**Various utility functions
 *
 * @author Peter Quint  */
public class Util {

    /**Sum to equal length arrays of floats*/
    public static float[] sum(float[] a, float[] b) {
        if (a.length == b.length) {
            float[] res = new float[a.length];
            for (int i = 0; i < a.length; i++) res[i] = a[i] + b[i];
            return res;
        }
        return null;
    }

    /**Return a float[3] array from a given index position in a larger array,
     *the index is based on groups of three*/
    public static float[] getTriple(int i, float[] a) throws RIBException {
        if (i * 3 + 3 > a.length) throw new RIBException("Index into the group is out of bounds"); else return new float[] { a[i * 3], a[i * 3 + 1], a[i * 3 + 2] };
    }

    /**Return a float[4] array from a given index position in a larger array*
     * the index based on groups of 4*/
    public static float[] getQuad(int i, float[] a) throws RIBException {
        if (i * 3 + 4 > a.length) throw new RIBException("Index into the group is out of bounds"); else return new float[] { a[i * 4], a[i * 4 + 1], a[i * 4 + 2], a[i * 4 + 3] };
    }

    /** REturn a float[2] array from a given index position in a larger array
     **the index being based on triples*/
    public static float[] getDouble(int i, float[] a) throws RIBException {
        float[] temp = getTriple(i, a);
        return new float[] { temp[0], temp[1] };
    }

    /** Get the sum of a series of indices into an array of triples, return a
     * triple, only sum the first upTo indices, if upTo is larger than the number
     * of indices then the whole iarray is summed*/
    public static float[] getTotal(int[] iarray, float[] a, int upTo) throws RIBException {
        float[] res = { 0, 0, 0 }, tmp;
        int limit = upTo < iarray.length ? upTo : iarray.length;
        for (int i = 0; i < limit; i++) {
            tmp = getTriple(iarray[i], a);
            res = sum(res, tmp);
        }
        return res;
    }

    public static float[] getTotal(int[] iarray, float[] a) throws RIBException {
        return getTotal(iarray, a, iarray.length);
    }

    /** Add a series of numbers to a vector*/
    public static void add(Vector v, int[] iarray) {
        for (int i = 0; i < iarray.length; i++) v.add(new Integer(iarray[i]));
    }

    /**Add a series of numbers in reverse order to a vector*/
    public static void addReverse(Vector v, int[] iarray) {
        for (int i = iarray.length - 1; i >= 0; i--) v.add(new Integer(iarray[i]));
    }

    /** Add a series of numbers to a vector*/
    public static void add(Vector v, float[] farray) {
        for (int i = 0; i < farray.length; i++) v.add(new Float(farray[i]));
    }

    public static void add(Vector v, Object[] oarray) {
        if (oarray != null) for (int i = 0; i < oarray.length; i++) v.add(oarray[i]);
    }

    /**Extract an array of float from a vector*/
    public static float[] extractFloat(Vector v) {
        float[] res = new float[v.size()];
        for (int i = 0; i < v.size(); i++) res[i] = ((Float) v.get(i)).floatValue();
        return res;
    }

    /**Extract an array of int from a vector*/
    public static int[] extractInt(Vector v) {
        int[] res = new int[v.size()];
        for (int i = 0; i < v.size(); i++) res[i] = ((Integer) v.get(i)).intValue();
        return res;
    }

    /**Write out a progress message*/
    public static void progress(String message) {
        if (GlobalOptions.verbosity >= 2) GlobalOptions.messageStream.println(message);
    }

    /**Write out a warning message*/
    public static void warning(String message) {
        if (GlobalOptions.verbosity >= 1) GlobalOptions.messageStream.println(message);
    }

    public static void progressDetail(String message) {
        if (GlobalOptions.verbosity >= 3) GlobalOptions.messageStream.println(message);
    }

    /**Return the maximum value contained in a float array*/
    public static float max(float[] farray) {
        float max = -Float.MAX_VALUE;
        for (int i = 0; i < farray.length; i++) {
            if (farray[i] > max) max = farray[i];
        }
        return max;
    }

    public static String makeLegalShaderName(String name, int number) {
        String num;
        if (number > 0) num = '_' + Integer.toString(number); else num = "";
        if (name.length() + num.length() > RendererOptions.maxIdentifierLen) name = name.substring(0, RendererOptions.maxIdentifierLen - num.length());
        name = name + num;
        return makeLegalFileName(name);
    }

    public static String makeLegalFileName(String name) {
        if (name.length() == 0) return name;
        StringBuffer s = new StringBuffer();
        char c;
        for (int i = 0; i < name.length(); i++) {
            c = name.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c) || (c == '_') || (c == '.')) s.append(c); else s.append('_');
        }
        if (Character.isDigit(s.charAt(0))) s.insert(0, '_');
        return s.toString();
    }

    public static String makeLegalIdentifier(String name) {
        return makeLegalFileName(name).replace('.', '_');
    }

    public static String convertPath(String path) {
        if (RendererOptions.useUnixPathSep) return path.replace('\\', '/'); else return path;
    }

    /**Remove the extension of the form .xxx from a name*/
    public static String stripExtension(String name) {
        if (name == null) return null;
        int p = name.lastIndexOf('.');
        if (p >= 0) return name.substring(0, p); else return name;
    }

    /**Return any extension .xxx from a name, without removing it
     *@param name the name to work on
     *@return the extension, without the '.' or the empty string if
     *no extension is found
     */
    public static String getExtension(String name) {
        int p = name.lastIndexOf('.');
        if (p >= 0) return name.substring(p + 1); else return "";
    }

    /**Check through a search path, expressed as an array of files, for
     * a file that exists with the given name, return the file if found
     * else return null*/
    public static File searchPath(File[] path, String name) {
        File f = new File(name);
        if (f.exists()) return f;
        if (path != null) {
            for (int i = 0; i < path.length; i++) {
                f = new File(path[i], name);
                if (f.exists()) return f;
            }
        }
        return null;
    }

    /**Create a unique name for a file based on its path - this method is not
     *100% guaranteed to be unique, but its pretty good*/
    public static String makeUniqueName(File path) {
        int hash = path.getPath().hashCode();
        String suffix = Integer.toHexString(hash);
        String name = path.getName();
        int p = name.lastIndexOf('.');
        String ending = "";
        if (p > 0) {
            ending = name.substring(p);
            name = name.substring(0, p);
        }
        return name + suffix + ending;
    }

    /**See whether we can translate a file */
    public static String convertToTiff(File inFile, File outFile) {
        if (inFile.getName().toLowerCase().endsWith(".tif") || inFile.getName().toLowerCase().endsWith(".tiff")) return null;
        String convertor;
        if (inFile.getName().toLowerCase().endsWith(".jpg")) convertor = GlobalOptions.convertjpgtotif; else if (inFile.getName().toLowerCase().endsWith(".gif")) convertor = GlobalOptions.convertgiftotif; else if (inFile.getName().toLowerCase().endsWith(".bmp")) convertor = GlobalOptions.convertbmptotif; else convertor = GlobalOptions.convertgeneral;
        convertor = GlobalOptions.substitute(convertor, "$infile", inFile.getPath());
        convertor = GlobalOptions.substitute(convertor, "$outfile", outFile.getPath());
        return convertor;
    }

    public static String stripLeadingUnderscores(String string) {
        int i = 0;
        while ((i < string.length()) && (string.charAt(i) == '_')) i++;
        return string.substring(i);
    }

    public static Object concatArrays(Object[] a, Object[] b) {
        if (a == null) return b;
        if (b == null) return a;
        Object result = Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static String toString(Object value) {
        String valString;
        if (value instanceof Float) valString = value.toString(); else if (value instanceof String) valString = '"' + value.toString() + '"'; else if (value instanceof float[]) {
            StringBuffer sb = new StringBuffer();
            float[] fa = (float[]) value;
            sb.append("[ ");
            for (int i = 0; i < fa.length; i++) {
                sb.append(fa[i]);
                sb.append(' ');
            }
            sb.append(']');
            valString = sb.toString();
        } else if (value instanceof String[]) {
            StringBuffer sb = new StringBuffer();
            String[] sa = (String[]) value;
            sb.append("[ ");
            for (int i = 0; i < sa.length; i++) {
                sb.append('"');
                sb.append(sa[i]);
                sb.append("\" ");
            }
            sb.append(']');
            valString = sb.toString();
        } else throw new Error("Value of unknown type");
        return valString;
    }

    /**Convert a vector (more precisely the values vector found in a ParamValueHolder
     *into a string suitable for use as a RIB shader argument value*/
    public static String toString(Vector v) {
        if (v.size() > 0) {
            if (v.size() == 1) return toString(v.get(0)); else {
                if (v.get(0) instanceof Float) return toString(Util.extractFloat(v));
                if (v.get(0) instanceof String) {
                    String temp[] = new String[v.size()];
                    v.toArray(temp);
                    return toString(temp);
                } else return "";
            }
        } else return "";
    }

    public static String[] getParts(String name) {
        StringBuffer sb = new StringBuffer(10);
        int startNum = -1, endNum = -1;
        char c;
        for (int i = name.length() - 1; i >= 0; i--) {
            c = name.charAt(i);
            if (Character.isDigit(c)) {
                if (endNum < 0) endNum = i + 1;
            } else if ((endNum >= 0) && (startNum < 0)) {
                startNum = i + 1;
                break;
            }
        }
        if ((endNum >= 0) && (startNum < 0)) startNum = 0;
        String result[] = new String[] { "", "", "" };
        if (startNum >= 0) {
            result[1] = name.substring(startNum, endNum);
            if (startNum > 0) result[0] = name.substring(0, startNum);
            if (endNum < name.length()) result[2] = name.substring(endNum);
        } else {
            result[0] = Util.stripExtension(name);
            result[2] = '.' + Util.getExtension(name);
        }
        return result;
    }

    /**Delete all files (but not directories) in a given directory.
     * This routine is not recursive
     * @param directory the directory
     */
    public static void deleteAll(File directory) {
        File files[] = directory.listFiles();
        for (int i = 0; i < files.length; i++) if (files[i].isFile()) files[i].delete();
    }

    /**Delete all files with the given extension
     * 
     * @param directory the directory to delete files in
     * @param extension the extension
     */
    public static void deleteAll(File directory, String extension) {
        if (!extension.startsWith(".")) extension = "." + extension;
        File files[] = directory.listFiles();
        for (int i = 0; i < files.length; i++) if ((files[i].isFile()) && (files[i].getName().endsWith(extension))) files[i].delete();
    }

    /**Create a number string padded with zeros
     * @param length the total length of string required
     * @param frameNo the number to conver to a string
     * @return a string padded with leading zeros
     */
    public static String pad(int length, int frameNo) {
        String num = Integer.toString(frameNo);
        StringBuffer zeros = new StringBuffer(length);
        while (num.length() + zeros.length() < length) zeros.append('0');
        return zeros.append(num).toString();
    }
}
