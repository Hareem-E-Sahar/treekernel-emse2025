package com.cntinker.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.misc.BASE64Decoder;

/**
 * @author bin_liu
 */
public class StringHelper {

    private static String mobile_regx = "^[1]([3][0-9]{1}|59|58|88|89)[0-9]{8}$";

    public static StringHelper getInstancle() {
        return new StringHelper();
    }

    private StringHelper() {
    }

    /**
	 * �ڲ�ʹ������ƥ����
	 * 
	 * @param regx
	 * @param content
	 * @return boolean
	 */
    private static boolean isMatch(String regx, String content) {
        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(content);
        if (m.find()) return true;
        return false;
    }

    /**
	 * �õ�ϵͳ��ǰʱ��
	 * 
	 * @return String
	 */
    public static String getSystime() {
        DateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dft.format(new Date());
    }

    /**
	 * ����ʽ��ȡϵͳ��ǰʱ��
	 * 
	 * @param format
	 * @return String
	 */
    public static String getSystime(String format) {
        DateFormat dft = new SimpleDateFormat(format);
        return dft.format(new Date());
    }

    public int compare(Object o1, Object o2) {
        String a = (String) o1;
        String b = (String) o2;
        if (!isDigit(a) || !isDigit(b)) throw new IllegalArgumentException("the object must a digit");
        long aa = Long.valueOf(a).longValue();
        long bb = Long.valueOf(b).longValue();
        if (aa > bb) return 1; else if (aa < bb) return -1;
        return 0;
    }

    /**
	 * ȥ�ո񲢽����滻��ָ���ַ�
	 * 
	 * @param content
	 * @return String
	 */
    public static String alterSpace(String content, String character) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < content.length(); i++) {
            String c = new String(new char[] { content.charAt(i) });
            if (c.trim().length() == 0) {
                sb.append(character);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
	 * @param line
	 * @return boolean
	 */
    public static boolean isEmail(String line, int length) {
        return line.matches("\\w+[\\w.]*@[\\w.]+\\.\\w+$") && line.length() <= length;
    }

    /**
	 * �ж������Ƿ�ȫ������
	 * 
	 * @param value
	 * @param length
	 * @return boolean
	 */
    public static boolean isChineseName(String value, int length) {
        return value.matches("^[一-龥]+$") && value.length() <= length;
    }

    /**
	 * �ж��ַ��Ƿ���HTML��ǩ
	 * 
	 * @param value
	 * @return boolean
	 */
    public static boolean isHaveHtmlTag(String value) {
        return value.matches("<(\\S*?)[^>]*>.*?</\\1>|<.*? />");
    }

    /**
	 * ���URL�Ƿ�Ϸ�
	 * 
	 * @param value
	 * @return boolean
	 */
    public static boolean isURL(String value) {
        return value.matches("[a-zA-z]+://[^\\s]*");
    }

    /**
	 * ���IP�Ƿ�Ϸ�
	 * 
	 * @param value
	 * @return boolean
	 */
    public static boolean iskIP(String value) {
        return value.matches("\\d{1,3}+\\.\\d{1,3}+\\.\\d{1,3}+\\.\\d{1,3}");
    }

    /**
	 * ���QQ�Ƿ�Ϸ������������֣�����λ������Ļ
	 * 
	 * @param value
	 * @return boolean
	 */
    public static boolean isQQ(String value) {
        return value.matches("[1-9][0-9]{4,13}");
    }

    /**
	 * ����ʱ��Ƿ�Ϸ�
	 * 
	 * @param value
	 * @return boolean
	 */
    public static boolean isPostCode(String value) {
        return value.matches("[1-9]\\d{5}(?!\\d)");
    }

    /**
	 * ������֤�Ƿ��Ϊ15λ��18λ
	 * 
	 * @param value
	 * @return boolean
	 */
    public static boolean isIDCard(String value) {
        return value.matches("\\d{15}|\\d{18}");
    }

    /**
	 * ���������ַ��Ƿ�Ϊ�ֻ��
	 * 
	 * @param line
	 * @return List
	 */
    public static boolean isPhone(String line) {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile(mobile_regx);
        m = p.matcher(line);
        if (m.matches()) return true;
        return false;
    }

    /**
	 * ȥ���ֻ����ǰ���86��+86���
	 * 
	 * @param phoneno
	 * @return String
	 */
    public static String fixPhoneno(String phoneno) {
        if (phoneno.length() > 11 && phoneno.startsWith("86")) return phoneno.substring(2); else if (phoneno.length() > 11 && phoneno.startsWith("+86")) return phoneno.substring(3);
        return phoneno;
    }

    /**
	 * �Ƿ���ֻ��
	 * 
	 * @param line
	 * @return boolean
	 */
    public static boolean hasPhone(String line) {
        return isMatch("^[1]([3][0-9]{1}|59|58|88|89)[0-9]{8}$", line);
    }

    /**
	 * ��һ���ַ�����ȡ������������,11Ϊ���ַ���ֻ�Ź���;
	 * 
	 * @param line
	 * @return String
	 */
    public static String getPhone(String line) {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("1[3,5][4,5,6,7,8,9]\\d{8}|15[8,9]\\d{8}");
        for (int i = 0; i < line.length(); i++) {
            m = p.matcher(line);
            if (m.find()) {
                String str = line.substring(m.start(), m.end());
                return str;
            }
        }
        return "";
    }

    /**
	 * ���涨�����õ�һ���ı�����ַ�
	 * 
	 * @param text
	 * @param compile
	 * @return Set
	 */
    public static Set getTextBlock(String text, String compile) {
        Set set = new HashSet();
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile(compile);
        m = p.matcher(text);
        while (m.find()) {
            String str = text.substring(m.start(), m.end());
            set.add(str);
        }
        return set;
    }

    /**
	 * ���������ֻ����
	 * 
	 * @param strMail
	 * @return Set
	 */
    public static Set getCode(String strMail) {
        Set set = new HashSet();
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("1[3,5][4,5,6,7,8,9]\\d{8}|15[8,9]\\d{8}");
        m = p.matcher(strMail);
        while (m.find()) {
            String str = strMail.substring(m.start(), m.end());
            set.add(str);
        }
        return set;
    }

    /**
	 * get email
	 * 
	 * @param content
	 * @return Set
	 */
    public static Set getMail(String content) {
        Set set = new HashSet();
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("(?i)(?<=\\b)[a-z0-9][-a-z0-9_.]+[a-z0-9]@([a-z0-9][-a-z0-9]+\\.)+[a-z]{2,4}(?=\\b)");
        m = p.matcher(content);
        while (m.find()) {
            String str = content.substring(m.start(), m.end());
            set.add(str);
        }
        return set;
    }

    /**
	 * ���ָ��λ֮��������
	 * 
	 * @param min
	 * @param max
	 * @return int
	 */
    public static int getRandom(int min, int max) {
        return (int) ((double) min + (int) (max - min) * Math.random());
    }

    /**
	 * ���lengthλ����
	 * 
	 * @param length
	 * @return int
	 */
    public static int getRandom(int length) {
        return Integer.valueOf(getRand(length)).intValue();
    }

    /**
	 * ���lengthλ����
	 * 
	 * @param length
	 * @return long
	 */
    public static long getRandomL(int length) {
        return Long.valueOf(getRand(length)).longValue();
    }

    /**
	 * ���lengthλ����
	 * 
	 * @param length
	 * @return String
	 */
    public static String getRandomStr(int length) {
        return Long.toString(getRandom(length));
    }

    /**
	 * ���������ֵ��ַ�<br>
	 * ��λ���Ϊ0���滻Ϊ1<br>
	 * 
	 * @param length
	 * @return String
	 */
    private static String getRand(int length) {
        StringBuffer t = new StringBuffer();
        for (int j = 0; j < length; j++) {
            double d = Math.random() * 10;
            int c = (int) d;
            t.append(c);
        }
        String result = t.toString();
        if (result.substring(0, 1).equalsIgnoreCase("0")) {
            result.replaceAll("0", "1");
        }
        if (result.length() > length) {
            result = result.substring(0, length);
        } else if (result.length() < length) {
            result = result + StringHelper.getRand(length - result.length());
        }
        return result;
    }

    /**
	 * �ж������ڵ��ַ��Ƿ�Ϊ��
	 * 
	 * @param str
	 * @return boolean
	 */
    public static boolean isNull(String[] str) {
        for (int i = 0; i < str.length; i++) {
            if (isNull(str[i])) return true;
        }
        return false;
    }

    /**
	 * �ַ��Ƿ�Ϊ��
	 * 
	 * @param str
	 * @return boolean
	 */
    public static boolean isNull(String str) {
        return (str == null || str.trim().length() == 0);
    }

    /**
	 * �ж��ַ��е�ÿ���ַ��Ƿ�������
	 * 
	 * @param str
	 * @return boolean
	 */
    public static boolean isDigit(String[] str) {
        for (int i = 0; i < str.length; i++) {
            if (!isDigit(str[i])) return false;
        }
        return true;
    }

    /**
	 * �ж��ַ��Ƿ�������
	 * 
	 * @param str
	 * @return boolean
	 */
    public static boolean isDigit(String str) {
        if (isNull(str)) throw new NullPointerException();
        for (int i = 0, size = str.length(); i < size; i++) {
            if (!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }

    /**
	 * �õ�STACK����Ϣ
	 * 
	 * @param e
	 * @return String
	 */
    public static String getStackInfo(Throwable e) {
        StringBuffer info = new StringBuffer("Found Exception: ");
        info.append("\n");
        info.append(e.getClass().getName());
        info.append(" : ").append(e.getMessage() == null ? "" : e.getMessage());
        StackTraceElement[] st = e.getStackTrace();
        for (int i = 0; i < st.length; i++) {
            info.append("\t\n").append("at ");
            info.append(st[i].toString());
        }
        return info.toString();
    }

    /**
	 * ��������ַ�ָ��������ʽת��
	 * 
	 * @param str
	 * @param regEx
	 * @param code
	 * @return String
	 */
    private static String insteadCode(String str, String regEx, String code) {
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        String s = m.replaceAll(code);
        return s;
    }

    /**
	 * ��HTML�Ĺؼ����滻���
	 * 
	 * @param sourceStr
	 * @return String
	 */
    public static String toHtml(String sourceStr) {
        String targetStr;
        targetStr = insteadCode(sourceStr, ">", "&gt;");
        targetStr = insteadCode(targetStr, "<", "&lt;");
        targetStr = insteadCode(targetStr, "\n", "<br>");
        targetStr = insteadCode(targetStr, " ", "&nbsp;");
        return targetStr.trim();
    }

    /**
	 * ת�崫���е������ַ����� <li>+ URL ??�ű�ʾ��??%2B<br/> <li>�ո� URL�еĿո����??�Ż��߱�??%20<br/>
	 * <li>/ �ָ�Ŀ¼����Ŀ¼ %2F <br/> <li>? �ָ�ʵ��??URL �Ͳ�??%3F<br/> <li>% ָ�������ַ� %25
	 * <br/> <li># ��ʾ��ǩ %23 <br/> <li>& URL ��ָ���Ĳ����ķָ�??%26 <br/> <li>=URL
	 * ��ָ�������??%3D <br/>
	 * 
	 * @param parameter
	 * @return String
	 */
    public static String sendGetParameter(String parameter) {
        parameter = insteadCode(parameter, "&", "%26");
        parameter = insteadCode(parameter, " ", "%20");
        parameter = insteadCode(parameter, "%", "%25");
        parameter = insteadCode(parameter, "#", "%23");
        return parameter.trim();
    }

    /**
	 * ��ָ������ʼ����ֹ�ַ��и��ַ�
	 * 
	 * @param content
	 * @param start
	 * @param end
	 * @return String
	 */
    public static String spiltStr(String content, String start, String end) {
        if (!(content.indexOf(start) > -1) || !(content.indexOf(end) > -1)) throw new IndexOutOfBoundsException("[start Character or end Character,isn't exist in the specified content]");
        int s = content.indexOf(start);
        int e = start.equals(end) ? content.substring(s + 1).indexOf(end) : content.indexOf(end);
        if (s >= e) throw new IndexOutOfBoundsException("[the Character end is smallness Character start]"); else content = new String(content.substring(s + 1, e));
        return content.trim();
    }

    /**
	 * �õ��ı������а�ָ���ָ���кõ�Ԫ��
	 * 
	 * @param content
	 * @param split
	 * @return String[]
	 */
    public static String[] splitStr(String content, String split) {
        int s = 0;
        int e = content.indexOf(split);
        List list = new ArrayList();
        while (e <= content.length()) {
            if (content.indexOf(split) == -1 && list.size() != 0) {
                list.add(content);
                break;
            }
            list.add(content.substring(s, e));
            content = content.substring(e + 1, content.length());
            e = s + content.indexOf(split);
        }
        return (String[]) list.toArray(new String[0]);
    }

    /**
	 * ��ָ�����ַ�λ�и��ַ�(����ҳ����ʾ),ʣ��λ�ַ��ñ���end�е��ַ��ʾ �˷��������ת���ַ���
	 * 
	 * @param str
	 * @param num
	 * @param end
	 * @return String
	 * @throws Cm2Exception
	 */
    public static String splitStr(String str, int num, String end) {
        StringBuffer sb = new StringBuffer();
        if (str == null || end == null) throw new NullPointerException();
        if (str.length() > num) str = sb.append(str.substring(0, num)).append(end).toString();
        return toHtml(str);
    }

    /**
	 * �������Ҷ���Ĺ����ʽ��
	 * 
	 * @param content
	 * @param count
	 * @return String
	 */
    public static String completeText(String content, int count) {
        StringBuffer sb = new StringBuffer();
        if (count > content.length()) {
            for (int i = count - content.length(); content.length() < count && i != 0; i--) sb.append("0");
        }
        sb.append(content);
        return sb.toString();
    }

    /**
	 * �������Ҷ���Ĺ����ʽ��
	 * 
	 * @param content
	 * @param count
	 * @return String
	 */
    public static String completeText(int content, int count) {
        String c = Integer.toString(content);
        StringBuffer sb = new StringBuffer();
        if (count > c.length()) {
            for (int i = count - c.length(); c.length() < count && i != 0; i--) sb.append("0");
        }
        sb.append(content);
        return sb.toString();
    }

    /**
	 * ���Ҳ��ո������Ĺ����ʽ������
	 * 
	 * @param content
	 * @param count
	 * @return String
	 */
    public static String completeTextSpace(String content, int count) {
        StringBuffer sb = new StringBuffer();
        sb.append(content);
        if (count > content.length()) {
            for (int i = 0; i < count - content.length(); i++) sb.append(" ");
        }
        return sb.toString();
    }

    /**
	 * ���Ҳ��ո������Ĺ����ʽ������
	 * 
	 * @param content
	 * @param count
	 * @return String
	 */
    public static String completeTextSpace(int content, int count) {
        StringBuffer sb = new StringBuffer();
        String c = Integer.toString(content);
        sb.append(content);
        if (count > c.length()) {
            for (int i = 0; i < count - c.length(); i++) sb.append(" ");
        }
        return sb.toString();
    }

    /**
	 * ���ַ�ת����16����
	 * 
	 * @param content
	 * @return String
	 */
    public static String toHex(String content) {
        long i = new Long(content).longValue();
        String i_16 = Long.toHexString(i);
        return i_16;
    }

    /**
	 * �ж��ַ��Ƿ�������
	 * 
	 * @param c
	 * @return boolean
	 */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
	 * �õ�һ���ַ��BASE64����
	 * 
	 * @param b
	 * @return String
	 */
    public static String getBASE64(byte[] b) {
        String s = null;
        if (b != null) {
            s = new sun.misc.BASE64Encoder().encode(b);
        }
        return s;
    }

    /**
	 * @param s
	 * @return byte[]
	 */
    public static byte[] getFromBASE64(String s) {
        byte[] b = null;
        if (s != null) {
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                b = decoder.decodeBuffer(s);
                return b;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    /**
	 * �����ӵ�����ת�ɿ�ʶ����
	 * 
	 * @param content
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
    public static String getUrlEncode(String content) throws UnsupportedEncodingException {
        char[] a = content.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a.length; i++) {
            if (isChinese(a[i])) {
                sb.append(URLEncoder.encode(a[i] + "", "GBK"));
            } else {
                sb.append(a[i]);
            }
        }
        return sb.toString();
    }

    /**
	 * �������������ת�壬�ٰ�ָ�����ַ�ת�������ַ�
	 * 
	 * @param content
	 * @param encode
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
    public static String getUrlEncode(String content, String encode) throws UnsupportedEncodingException {
        char[] a = content.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a.length; i++) {
            if (isChinese(a[i])) {
                sb.append(URLEncoder.encode(a[i] + "", encode));
            } else {
                sb.append(a[i]);
            }
        }
        return sb.toString();
    }

    /**
	 * ����URL������GBK
	 * 
	 * @param content
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
    public static String getUrlDecode(String content) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        sb.append(URLDecoder.decode(content, "GBK"));
        return sb.toString();
    }

    /**
	 * ��ָ���������URL
	 * 
	 * @param content
	 * @param encode
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
    public static String getUrlDecode(String content, String encode) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        sb.append(URLDecoder.decode(content, encode));
        return sb.toString();
    }

    /**
	 * ת��ʮ����Ʊ���Ϊ�ַ�
	 * 
	 * @param bytes
	 * @return String
	 */
    public static String toStringHex(String bytes) {
        String hexString = "0123456789ABCDEF ";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        for (int i = 0; i < bytes.length(); i += 2) baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    /**
	 * ת��Ϊ16�����ַ�
	 * 
	 * @param str
	 * @return String
	 */
    public static String toHexString(String str) {
        String hexString = "0123456789ABCDEF ";
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(hasPhone("18911968624"));
        System.out.println(toHex("13811968624"));
        System.out.println(fixPhoneno("8613811968624"));
        System.out.println(fixPhoneno("13811968624"));
        System.out.println(sendGetParameter("&"));
        System.out.println();
        System.out.println(sendGetParameter("/send/100039.htm?cpid=1&adid=3&adverid=1"));
        System.out.println(getMail("<html><a href=\"testone@163.com\">163test</a>\n<a href='www.163.com@163-com.com'>163news</a>\n"));
        System.out.println(getSystime());
    }
}
