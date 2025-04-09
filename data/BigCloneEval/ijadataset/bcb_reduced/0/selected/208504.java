package loci.formats;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Vector;

/**
 * FilePattern is a collection of methods for handling file patterns, a way of
 * succinctly representing a collection of files meant to be part of the same
 * data series.
 *
 * Examples:
 * <ul>
 *   <li>C:\data\BillM\sdub&lt;1-12&gt;.pic</li>
 *   <li>C:\data\Kevin\80&lt;01-59&gt;0&lt;2-3&gt;.pic</li>
 *   <li>/data/Josiah/cell-Z&lt;0-39&gt;.C&lt;0-1&gt;.tiff</li>
 * </ul>
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/formats/FilePattern.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/loci/formats/FilePattern.java">SVN</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class FilePattern {

    /** The file pattern string. */
    private String pattern;

    /** The validity of the file pattern. */
    private boolean valid;

    /** Error message generated during file pattern construction. */
    private String msg;

    /** Indices into the pattern indicating the start of a numerical block. */
    private int[] startIndex;

    /** Indices into the pattern indicating the end of a numerical block. */
    private int[] endIndex;

    /** First number of each numerical block. */
    private BigInteger[] begin;

    /** Last number of each numerical block. */
    private BigInteger[] end;

    /** Step size of each numerical block. */
    private BigInteger[] step;

    /** Total numbers withins each numerical block. */
    private int[] count;

    /** Whether each numerical block is fixed width. */
    private boolean[] fixed;

    /** The number of leading zeroes for each numerical block. */
    private int[] zeroes;

    /** File listing for this file pattern. */
    private String[] files;

    /** Creates a pattern object using the given file as a template. */
    public FilePattern(Location file) {
        this(FilePattern.findPattern(file));
    }

    /**
   * Creates a pattern object using the given
   * filename and directory path as a template.
   */
    public FilePattern(String name, String dir) {
        this(FilePattern.findPattern(name, dir));
    }

    /** Creates a pattern object for files with the given pattern string. */
    public FilePattern(String pattern) {
        this.pattern = pattern;
        valid = false;
        if (pattern == null) {
            msg = "Null pattern string.";
            return;
        }
        int len = pattern.length();
        Vector lt = new Vector(len);
        Vector gt = new Vector(len);
        int left = -1;
        while (true) {
            left = pattern.indexOf("<", left + 1);
            if (left < 0) break;
            lt.add(new Integer(left));
        }
        int right = -1;
        while (true) {
            right = pattern.indexOf(">", right + 1);
            if (right < 0) break;
            gt.add(new Integer(right));
        }
        int num = lt.size();
        if (num != gt.size()) {
            msg = "Mismatched numerical block markers.";
            return;
        }
        startIndex = new int[num];
        endIndex = new int[num];
        for (int i = 0; i < num; i++) {
            int val = ((Integer) lt.elementAt(i)).intValue();
            if (i > 0 && val < endIndex[i - 1]) {
                msg = "Bad numerical block marker order.";
                return;
            }
            startIndex[i] = val;
            val = ((Integer) gt.elementAt(i)).intValue();
            if (val <= startIndex[i]) {
                msg = "Bad numerical block marker order.";
                return;
            }
            endIndex[i] = val + 1;
        }
        begin = new BigInteger[num];
        end = new BigInteger[num];
        step = new BigInteger[num];
        count = new int[num];
        fixed = new boolean[num];
        zeroes = new int[num];
        for (int i = 0; i < num; i++) {
            String block = pattern.substring(startIndex[i], endIndex[i]);
            int dash = block.indexOf("-");
            String b, e, s;
            if (dash < 0) {
                b = e = block.substring(1, block.length() - 1);
                s = "1";
            } else {
                int colon = block.indexOf(":");
                b = block.substring(1, dash);
                if (colon < 0) {
                    e = block.substring(dash + 1, block.length() - 1);
                    s = "1";
                } else {
                    e = block.substring(dash + 1, colon);
                    s = block.substring(colon + 1, block.length() - 1);
                }
            }
            try {
                begin[i] = new BigInteger(b);
                end[i] = new BigInteger(e);
                if (begin[i].compareTo(end[i]) > 0) {
                    msg = "Begin value cannot be greater than ending value.";
                    return;
                }
                step[i] = new BigInteger(s);
                if (step[i].compareTo(BigInteger.ONE) < 0) {
                    msg = "Step value must be at least one.";
                    return;
                }
                count[i] = end[i].subtract(begin[i]).divide(step[i]).intValue() + 1;
                fixed[i] = b.length() == e.length();
                int z = 0;
                for (z = 0; z < e.length(); z++) {
                    if (e.charAt(z) != '0') break;
                }
                zeroes[i] = z;
            } catch (NumberFormatException exc) {
                msg = "Invalid numerical range values.";
                return;
            }
        }
        Vector v = new Vector();
        buildFiles("", num, v);
        files = new String[v.size()];
        v.copyInto(files);
        valid = true;
    }

    /** Gets the file pattern string. */
    public String getPattern() {
        return pattern;
    }

    /** Gets whether the file pattern string is valid. */
    public boolean isValid() {
        return valid;
    }

    /** Gets the file pattern error message, if any. */
    public String getErrorMessage() {
        return msg;
    }

    /** Gets the first number of each numerical block. */
    public BigInteger[] getFirst() {
        return begin;
    }

    /** Gets the last number of each numerical block. */
    public BigInteger[] getLast() {
        return end;
    }

    /** Gets the step increment of each numerical block. */
    public BigInteger[] getStep() {
        return step;
    }

    /** Gets the total count of each numerical block. */
    public int[] getCount() {
        return count;
    }

    /** Gets a listing of all files matching the given file pattern. */
    public String[] getFiles() {
        return files;
    }

    /** Gets the specified numerical block. */
    public String getBlock(int i) {
        if (i < 0 || i >= startIndex.length) return null;
        return pattern.substring(startIndex[i], endIndex[i]);
    }

    /** Gets each numerical block. */
    public String[] getBlocks() {
        String[] s = new String[startIndex.length];
        for (int i = 0; i < s.length; i++) s[i] = getBlock(i);
        return s;
    }

    /** Gets the pattern's text string before any numerical ranges. */
    public String getPrefix() {
        int s = pattern.lastIndexOf(File.separator) + 1;
        int e;
        if (startIndex.length > 0) e = startIndex[0]; else {
            int dot = pattern.lastIndexOf(".");
            e = dot < s ? pattern.length() : dot;
        }
        return s <= e ? pattern.substring(s, e) : "";
    }

    /** Gets the pattern's text string after all numerical ranges. */
    public String getSuffix() {
        return endIndex.length > 0 ? pattern.substring(endIndex[endIndex.length - 1]) : pattern;
    }

    /** Gets the pattern's text string before the given numerical block. */
    public String getPrefix(int i) {
        if (i < 0 || i >= startIndex.length) return null;
        int s = i > 0 ? endIndex[i - 1] : (pattern.lastIndexOf(File.separator) + 1);
        int e = startIndex[i];
        return s <= e ? pattern.substring(s, e) : null;
    }

    /** Gets the pattern's text string before each numerical block. */
    public String[] getPrefixes() {
        String[] s = new String[startIndex.length];
        for (int i = 0; i < s.length; i++) s[i] = getPrefix(i);
        return s;
    }

    /**
   * Identifies the group pattern from a given file within that group.
   * @param file The file to use as a template for the match.
   */
    public static String findPattern(Location file) {
        return findPattern(file.getName(), file.getAbsoluteFile().getParent());
    }

    /**
   * Identifies the group pattern from a given file within that group.
   * @param file The file to use as a template for the match.
   */
    public static String findPattern(File file) {
        return findPattern(file.getName(), file.getAbsoluteFile().getParent());
    }

    /**
   * Identifies the group pattern from a given file within that group.
   * @param name The filename to use as a template for the match.
   * @param dir The directory in which to search for matching files.
   */
    public static String findPattern(String name, String dir) {
        if (dir == null) dir = ""; else if (!dir.equals("") && !dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        Location dirFile = new Location(dir.equals("") ? "." : dir);
        Location[] f = dirFile.listFiles();
        if (f == null) return null;
        String[] nameList = new String[f.length];
        for (int i = 0; i < nameList.length; i++) nameList[i] = f[i].getName();
        return findPattern(name, dir, nameList);
    }

    /**
   * Identifies the group pattern from a given file within that group.
   * @param name The filename to use as a template for the match.
   * @param dir The directory prefix to use for matching files.
   * @param nameList The names through which to search for matching files.
   */
    public static String findPattern(String name, String dir, String[] nameList) {
        if (dir == null) dir = ""; else if (!dir.equals("") && !dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        int len = name.length();
        int bound = (len + 1) / 2;
        int[] indexList = new int[bound];
        int[] endList = new int[bound];
        int q = 0;
        boolean num = false;
        int ndx = -1, e = 0;
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (c >= '0' && c <= '9') {
                if (num) e++; else {
                    num = true;
                    ndx = i;
                    e = ndx + 1;
                }
            } else if (num) {
                num = false;
                indexList[q] = ndx;
                endList[q] = e;
                q++;
            }
        }
        if (num) {
            indexList[q] = ndx;
            endList[q] = e;
            q++;
        }
        StringBuffer sb = new StringBuffer(dir);
        for (int i = 0; i < q; i++) {
            int last = i > 0 ? endList[i - 1] : 0;
            sb.append(name.substring(last, indexList[i]));
            String pre = name.substring(0, indexList[i]);
            String post = name.substring(endList[i]);
            NumberFilter filter = new NumberFilter(pre, post);
            String[] list = matchFiles(nameList, filter);
            if (list == null || list.length == 0) return null;
            if (list.length == 1) {
                sb.append(name.substring(indexList[i], endList[i]));
                continue;
            }
            boolean fix = true;
            for (int j = 0; j < list.length; j++) {
                if (list[j].length() != len) {
                    fix = false;
                    break;
                }
            }
            if (fix) {
                int width = endList[i] - indexList[i];
                boolean[] same = new boolean[width];
                for (int j = 0; j < width; j++) {
                    same[j] = true;
                    int jx = indexList[i] + j;
                    char c = name.charAt(jx);
                    for (int k = 0; k < list.length; k++) {
                        if (list[k].charAt(jx) != c) {
                            same[j] = false;
                            break;
                        }
                    }
                }
                int j = 0;
                while (j < width) {
                    int jx = indexList[i] + j;
                    if (same[j]) {
                        sb.append(name.charAt(jx));
                        j++;
                    } else {
                        while (j < width && !same[j]) j++;
                        String p = findPattern(name, nameList, jx, indexList[i] + j, "");
                        if (p == null) {
                            return null;
                        }
                        sb.append(p);
                    }
                }
            } else {
                BigInteger[] numbers = new BigInteger[list.length];
                for (int j = 0; j < list.length; j++) {
                    numbers[j] = filter.getNumber(list[j]);
                }
                Arrays.sort(numbers);
                String bounds = getBounds(numbers, false);
                if (bounds == null) return null;
                sb.append(bounds);
            }
        }
        sb.append(q > 0 ? name.substring(endList[q - 1]) : name);
        return sb.toString();
    }

    /** Recursive method for parsing a fixed-width numerical block. */
    private static String findPattern(String name, String[] nameList, int ndx, int end, String p) {
        if (ndx == end) return p;
        for (int i = end - ndx; i >= 1; i--) {
            NumberFilter filter = new NumberFilter(name.substring(0, ndx), name.substring(ndx + i));
            String[] list = matchFiles(nameList, filter);
            BigInteger[] numbers = new BigInteger[list.length];
            for (int j = 0; j < list.length; j++) {
                numbers[j] = new BigInteger(list[j].substring(ndx, ndx + i));
            }
            Arrays.sort(numbers);
            String bounds = getBounds(numbers, true);
            if (bounds == null) continue;
            String pat = findPattern(name, nameList, ndx + i, end, p + bounds);
            if (pat != null) return pat;
        }
        return null;
    }

    /**
   * Gets a string containing start, end and step values
   * for a sorted list of numbers.
   */
    private static String getBounds(BigInteger[] numbers, boolean fixed) {
        if (numbers.length < 2) return null;
        BigInteger b = numbers[0];
        BigInteger e = numbers[numbers.length - 1];
        BigInteger s = numbers[1].subtract(b);
        if (s.equals(BigInteger.ZERO)) {
            return null;
        }
        for (int i = 2; i < numbers.length; i++) {
            if (!numbers[i].subtract(numbers[i - 1]).equals(s)) {
                return null;
            }
        }
        String sb = b.toString();
        String se = e.toString();
        StringBuffer bounds = new StringBuffer("<");
        if (fixed) {
            int zeroes = se.length() - sb.length();
            for (int i = 0; i < zeroes; i++) bounds.append("0");
        }
        bounds.append(sb);
        bounds.append("-");
        bounds.append(se);
        if (!s.equals(BigInteger.ONE)) {
            bounds.append(":");
            bounds.append(s);
        }
        bounds.append(">");
        return bounds.toString();
    }

    /** Filters the given list of filenames according to the specified filter. */
    private static String[] matchFiles(String[] inFiles, NumberFilter filter) {
        Vector v = new Vector();
        for (int i = 0; i < inFiles.length; i++) {
            if (filter.accept(inFiles[i])) v.add(inFiles[i]);
        }
        String[] s = new String[v.size()];
        v.copyInto(s);
        return s;
    }

    /** Recursive method for building filenames for the file listing. */
    private void buildFiles(String prefix, int ndx, Vector fileList) {
        int num = startIndex.length;
        int n1 = ndx == 0 ? 0 : endIndex[ndx - 1];
        int n2 = ndx == num ? pattern.length() : startIndex[ndx];
        String pre = pattern.substring(n1, n2);
        if (ndx == 0) fileList.add(pre + prefix); else {
            BigInteger bi = begin[--ndx];
            while (bi.compareTo(end[ndx]) <= 0) {
                String s = bi.toString();
                int z = zeroes[ndx];
                if (fixed[ndx]) z += end[ndx].toString().length() - s.length();
                for (int j = 0; j < z; j++) s = "0" + s;
                buildFiles(s + pre + prefix, ndx, fileList);
                bi = bi.add(step[ndx]);
            }
        }
    }

    /** Method for testing file pattern logic. */
    public static void main(String[] args) {
        String pat = null;
        if (args.length > 0) {
            Location file = new Location(args[0]);
            LogTools.println("File = " + file.getAbsoluteFile());
            pat = findPattern(file);
        } else {
            String[] nameList = new String[2 * 4 * 3 * 12 + 1];
            nameList[0] = "outlier.ext";
            int count = 1;
            for (int i = 1; i <= 2; i++) {
                for (int j = 1; j <= 4; j++) {
                    for (int k = 0; k <= 2; k++) {
                        for (int l = 1; l <= 12; l++) {
                            String sl = (l < 10 ? "0" : "") + l;
                            nameList[count++] = "hypothetical" + sl + k + j + "c" + i + ".ext";
                        }
                    }
                }
            }
            pat = findPattern(nameList[1], null, nameList);
        }
        if (pat == null) LogTools.println("No pattern found."); else {
            LogTools.println("Pattern = " + pat);
            FilePattern fp = new FilePattern(pat);
            if (fp.isValid()) {
                LogTools.println("Pattern is valid.");
                LogTools.println("Files:");
                String[] ids = fp.getFiles();
                for (int i = 0; i < ids.length; i++) {
                    LogTools.println("  #" + i + ": " + ids[i]);
                }
            } else LogTools.println("Pattern is invalid: " + fp.getErrorMessage());
        }
    }
}
