package org.wikiup.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wikiup.core.WikiupExpressionLanguage;
import org.wikiup.core.WikiupProperties;
import org.wikiup.core.imp.iterator.ArrayIterator;
import org.wikiup.core.inf.Gettable;

public class StringUtil {

    private static final byte[] ESCAPE_MASK = { 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F };

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    public static String join(Object[] array, String connector) {
        return join(new ArrayIterator<Object>(array), connector);
    }

    public static String join(Iterable<?> iterable, String connector) {
        return join(iterable.iterator(), connector);
    }

    public static String join(Iterator<?> iterator, String connector) {
        String result = null;
        if (iterator != null) {
            StringBuffer buffer = new StringBuffer();
            while (iterator.hasNext()) buffer.append(iterator.next()).append(connector);
            result = buffer.substring(0, buffer.length() - connector.length());
        }
        return result;
    }

    public static String connect(String str, String appendix, char connector) {
        return connect(new StringBuffer(str), appendix, connector).toString();
    }

    public static StringBuffer connect(StringBuffer buffer, String appendix, char connector) {
        if (buffer.length() == 0) buffer.append(connector);
        if (buffer.charAt(buffer.length() - 1) != connector) buffer.append(connector);
        if (appendix.length() > 0 && appendix.charAt(0) == connector) appendix = appendix.substring(1);
        return buffer.append(appendix);
    }

    public static StringBuffer connect(StringBuffer buffer, String appendix, String connector) {
        if (!buffer.toString().endsWith(connector)) buffer.append(connector);
        return buffer.append(appendix.startsWith(connector) ? appendix.substring(connector.length()) : appendix);
    }

    public static boolean compare(String str1, String str2) {
        return str1 != null && str2 != null ? str1.equals(str2) : false;
    }

    public static boolean compareIgnoreCase(String str1, String str2) {
        return str1 != null && str2 != null ? str1.equalsIgnoreCase(str2) : false;
    }

    public static String trim(String str) {
        return trimRight(trimLeft(str, WikiupProperties.TRIM_CHAR_SET), WikiupProperties.TRIM_CHAR_SET);
    }

    public static String trim(String str, String charSet) {
        return trimRight(trimLeft(str, charSet), charSet);
    }

    public static String trimLeft(String str, String charset) {
        int i, len = str.length();
        for (i = 0; i < len; i++) if (charset.indexOf(str.charAt(i)) == -1) return str.substring(i);
        return "";
    }

    public static String trimRight(String str, String charset) {
        int i;
        for (i = str.length() - 1; i >= 0; i--) if (charset.indexOf(str.charAt(i)) == -1) return str.substring(0, i + 1);
        return "";
    }

    public static String evaluateEL(String str, Gettable<?> getter) {
        return str != null ? ValueUtil.toString(WikiupExpressionLanguage.getInstance().evaluate(getter, str), "") : null;
    }

    public static String[] separate(String str, String reg) {
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) list.add(str.substring(matcher.start(), matcher.end()));
        return list.toArray(new String[list.size()]);
    }

    public static String[] split(String s, char splitter) {
        List<String> list = new ArrayList<String>();
        int pos = 0;
        do {
            int next = s.indexOf(splitter, pos);
            String str = next != -1 ? s.substring(pos, next) : s.substring(pos);
            if (pos > 1 && s.charAt(pos - 2) == '\\') {
                String replace = list.get(list.size() - 1);
                replace = replace.length() > 0 ? replace.substring(0, replace.length() - 1) + splitter + str : splitter + str;
                list.set(list.size() - 1, replace);
            } else list.add(str);
            pos = next != -1 ? next + 1 : next;
        } while (pos != -1);
        return list.toArray(new String[list.size()]);
    }

    public static String shrinkLeft(String str, String shrink) {
        return shrink != null ? (str.startsWith(shrink) ? str.substring(shrink.length()) : str) : str;
    }

    public static String shrinkRight(String str, String shrink) {
        return shrink != null ? (str.endsWith(shrink) ? str.substring(0, str.length() - shrink.length()) : str) : str;
    }

    public static String unescape(String s) {
        return unescape(s, '%');
    }

    public static String unescape(String s, char escape) {
        StringBuffer sbuf = new StringBuffer();
        int i = 0;
        int len = s.length();
        while (i < len) {
            int ch = s.charAt(i);
            if (ch == escape) {
                int cint = 0;
                if ('u' != s.charAt(i + 1)) {
                    cint = (cint << 4) | ESCAPE_MASK[s.charAt(i + 1)];
                    cint = (cint << 4) | ESCAPE_MASK[s.charAt(i + 2)];
                    i += 2;
                } else {
                    cint = (cint << 4) | ESCAPE_MASK[s.charAt(i + 2)];
                    cint = (cint << 4) | ESCAPE_MASK[s.charAt(i + 3)];
                    cint = (cint << 4) | ESCAPE_MASK[s.charAt(i + 4)];
                    cint = (cint << 4) | ESCAPE_MASK[s.charAt(i + 5)];
                    i += 5;
                }
                sbuf.append((char) cint);
            } else sbuf.append(ch == '+' ? ' ' : (char) ch);
            i++;
        }
        return sbuf.toString();
    }

    public static String replaceAll(String s, char f, char t) {
        int i;
        char p[] = new char[s.length()];
        for (i = 0; i < p.length; i++) p[i] = s.charAt(i) == f ? t : s.charAt(i);
        return new String(p);
    }

    public static String[] splitNamespaces(String name) {
        int i, e;
        char splitter = 0;
        List<String> list = new ArrayList<String>();
        for (e = 0, i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((splitter == 0 && WikiupProperties.NAMESPACE_SPLITTER.indexOf(c) != -1) || splitter == c) {
                splitter = splitter == 0 ? name.charAt(i) : splitter;
                if (e < i) list.add(name.substring(e, i));
                e = i + 1;
            }
        }
        list.add(name.substring(e));
        return list.toArray(new String[list.size()]);
    }

    public static String getCamelName(String name, char splitter) {
        StringBuffer buf = new StringBuffer();
        int i;
        for (i = 0; i < name.length(); i++) buf.append(name.charAt(i) != splitter ? name.charAt(i) : Character.toUpperCase(name.charAt(++i)));
        return buf.toString();
    }

    public static String head(String str, char s, int offset) {
        int idx = str.indexOf(s, offset);
        return idx != -1 ? str.substring(offset, idx) : null;
    }

    public static String format(String str, Object... args) {
        StringBuffer buf = new StringBuffer();
        int i, len = str.length();
        for (i = 0; i < len; i++) {
            if (i > 0 && str.charAt(i - 1) == '\\') buf.setLength(buf.length() - 1); else if (str.charAt(i) == '{') {
                String idx = head(str, '}', i + 1);
                int o = ValueUtil.toInteger(idx, -1);
                Assert.notNull(idx);
                Assert.isTrue(o != -1);
                buf.append(args[o]);
                i += (idx.length() + 1);
                continue;
            }
            buf.append(str.charAt(i));
        }
        return buf.toString();
    }

    public static String[] scan(String format, String str) {
        ArrayList<String> vars = new ArrayList<String>();
        int i = 0, ilen = format.length(), j = 0, jlen = str.length();
        while (i < ilen && j < jlen) {
            if (format.charAt(i) == '{') {
                int l = format.indexOf('}', i + 1);
                int idx = ValueUtil.toInteger(l != -1 ? format.substring(i + 1, l) : null, -1);
                if (idx != -1) {
                    int m = l != ilen - 1 ? str.indexOf(format.charAt(l + 1), j) : jlen;
                    while (vars.size() < idx + 1) vars.add("");
                    vars.set(idx, str.substring(j, m));
                    j = m;
                    i = l + 1;
                } else break;
            } else if (format.charAt(i++) != str.charAt(j++)) break;
        }
        return i == ilen && j == jlen ? vars.toArray(new String[vars.size()]) : null;
    }

    public static String generateRandomString(int length) {
        return generateRandomString(length, -1);
    }

    public static String generateRandomString(int length, long seed) {
        int i;
        final String CHAR_SET = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = seed == -1 ? new Random() : new Random(seed);
        StringBuffer buffer = new StringBuffer();
        for (i = 0; i < length; i++) buffer.append(CHAR_SET.charAt(rnd.nextInt(CHAR_SET.length())));
        return buffer.toString();
    }
}
