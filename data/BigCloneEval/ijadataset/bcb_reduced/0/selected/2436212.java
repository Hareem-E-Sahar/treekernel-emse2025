package net.sf.clairv.search.util;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qiuyin
 * 
 */
public class StringUtils {

    public static String[] extractByPattern(String str, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        ArrayList list = new ArrayList(20);
        while (m.find()) {
            list.add(str.substring(m.start(), m.end()));
        }
        if (!list.isEmpty()) {
            return (String[]) list.toArray(new String[0]);
        } else return null;
    }

    public static String replaceAll(String str, String pattern, String replacement) {
        return str.replaceAll(pattern, replacement);
    }

    public static String concatPath(String path1, String path2) {
        String separator = System.getProperty("file.separator");
        int length = path1.length();
        if (path1.length() < separator.length() || !path1.substring(length - separator.length(), length).equals(separator)) {
            path1 += separator;
        }
        return path1 + path2;
    }

    public static String arrayToString(Object[] arr) {
        StringBuilder sb = new StringBuilder();
        int len = arr.length;
        for (int i = 0; i < len; i++) {
            sb.append(arr[i]);
            if (i != len - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static String[] splitStrings(String str) {
        ArrayList list = new ArrayList();
        StringTokenizer st = new StringTokenizer(str, ", \r\n\t");
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return (String[]) list.toArray(new String[0]);
    }
}
