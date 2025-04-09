package leeon.mobile.BBSBrowser.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class HTMLUtil {

    /**
	 * 寻找字符
	 * @param source 原串
	 * @param startTag 开始标志
	 * @param strLength 目标的长度
	 * @return
	 */
    public static String findStr(String source, String startTag, int strLength) {
        return findStr(source, startTag, strLength, 0);
    }

    /**
	 * 寻找字符
	 * @param source 原串
	 * @param startTag 开始标志
	 * @param strLength 目标的长度
	 * @param startFrom 开始标志后第几位开始截取
	 * @return
	 */
    public static String findStr(String source, String startTag, int strLength, int startFrom) {
        int s = source.indexOf(startTag);
        if (s == -1) return null; else s = s + startTag.length() + startFrom;
        int e = s + strLength;
        if (e >= source.length()) return source.substring(s); else return source.substring(s, s + strLength);
    }

    /**
	 * 寻找字符
	 * @param source 原串
	 * @param startTag 开始标志
	 * @return
	 */
    public static String findStr(String source, String startTag) {
        return findStr(source, startTag, null, 0);
    }

    /**
	 * 寻找字符
	 * @param source 原串
	 * @param startTag 开始标志
	 * @param endTag 结束标志
	 * @return
	 */
    public static String findStr(String source, String startTag, String endTag) {
        return findStr(source, startTag, endTag, 0);
    }

    /**
	 * 寻找字符
	 * @param source 原串
	 * @param startTag 开始标志
	 * @param endTag 结束标志
	 * @param startFrom 开始标志后第几位开始截取
	 * @return
	 */
    public static String findStr(String source, String startTag, String endTag, int startFrom) {
        int s = source.indexOf(startTag);
        if (s == -1) return null; else s = s + startTag.length() + startFrom;
        int e = -1;
        if (endTag != null) e = source.indexOf(endTag, s);
        if (e == -1) return source.substring(s); else return source.substring(s, e);
    }

    /**
	 * 寻找字符regex版
	 * @param source 原串
	 * @param startTag 开始标志
	 * @param endTag 结束标志
	 * @return
	 */
    public static String findStrRegex(String source, String startTag, String endTag) {
        return findStrRegex(source, startTag, endTag, 0);
    }

    /**
	 * 寻找字符regex版
	 * @param source 原串
	 * @param startTag 开始标志
	 * @param endTag 结束标志
	 * @param startFrom 开始标志后第几位开始截取
	 * @return
	 */
    public static String findStrRegex(String source, String startTag, String endTag, int startFrom) {
        Pattern p1 = Pattern.compile(startTag, Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(source);
        Pattern p2 = Pattern.compile(endTag, Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(source);
        int s = -1;
        if (m1.find()) s = m1.start() + m1.group().length() + startFrom; else return null;
        int e = -1;
        if (m2.find(s)) e = m2.start();
        if (e == -1) return source.substring(s); else return source.substring(s, e);
    }

    /**
	 * 寻找多个字符
	 * @param source 原串
	 * @param startTag 开始标志
	 * @param endTag 结束标志
	 * @return
	 */
    public static String[] findStrs(String source, String startTag, String endTag) {
        return findStrs(source, startTag, endTag, 0);
    }

    /**
	 * 寻找多个字符
	 * @param source 原串
	 * @param startTag 开始标志
	 * @param endTag 结束标志
	 * @param startFrom 开始标志后第几位开始截取
	 * @return
	 */
    public static String[] findStrs(String source, String startTag, String endTag, int startFrom) {
        int s = 0, e = 0;
        ArrayList<String> list = new ArrayList<String>();
        while ((s = source.indexOf(startTag, e)) != -1) {
            s = s + startTag.length() + startFrom;
            e = source.indexOf(endTag, s);
            if (e == -1) list.add(source.substring(s)); else list.add(source.substring(s, e));
        }
        return list.toArray(new String[0]);
    }

    /**
	 * 寻找字符
	 * @param source 原串
	 * @param tag 结束标志，寻找该标志前的第一个startTag
	 * @param startTag 开始标志
	 * @param endTag 结束标志
	 * @return
	 */
    public static String findStrBeforeTag(String source, String tag, String startTag, String endTag) {
        return findStrBeforeTag(source, tag, startTag, endTag, 0);
    }

    /**
	 * 寻找字符
	 * @param source 原串
	 * @param tag 结束标志，寻找该标志前的第一个startTag
	 * @param startTag 开始标志
	 * @param endTag 结束标志
	 * @param startFrom 开始标志后第几位开始截取
	 * @return
	 */
    public static String findStrBeforeTag(String source, String tag, String startTag, String endTag, int startFrom) {
        if (source == null || source.length() == 0) return null;
        if (tag == null || tag.length() == 0) return null;
        int s = source.indexOf(tag);
        if (s == -1) return null;
        s = source.lastIndexOf(startTag, s);
        if (s == -1) return null; else s = s + startTag.length() + startFrom;
        int e = -1;
        if (endTag != null) e = source.indexOf(endTag, s);
        if (e == -1) return source.substring(s); else return source.substring(s, e);
    }

    /**
	 * 去掉文章中的font和a标记，还原成完整的html source
	 * @param source
	 * @return
	 */
    public static String removeHtmlTag(String source) {
        if (source != null) {
            source = source.replaceAll("<input[^>]*>", "");
            source = source.replaceAll("<font[^>]*>", "");
            source = source.replaceAll("</font>", "");
            source = source.replaceAll("<a[^>]*>", "");
            source = source.replaceAll("</a>", "");
            source = source.replaceAll("<b>", "");
            source = source.replaceAll("</b>", "");
            source = source.replaceAll("<span>", "");
            source = source.replaceAll("</span>", "");
            source = source.replaceAll("<br[^>]*>", "");
            return source;
        } else return null;
    }

    /**
	 * 处理用户的经验值
	 * 0 -
	 * 1 =
	 * 2 +
	 * 3 *
	 * 4 #
	 * 5 A
	 * @param source
	 * @return
	 */
    public static String dealUserLevel(String source) {
        if (source != null) {
            source = source.replaceAll("<img src=/images/level/0.gif border=0 align=absmiddle>", "-");
            source = source.replaceAll("<img src=/images/level/1.gif border=0 align=absmiddle>", "=");
            source = source.replaceAll("<img src=/images/level/2.gif border=0 align=absmiddle>", "+");
            source = source.replaceAll("<img src=/images/level/3.gif border=0 align=absmiddle>", "*");
            source = source.replaceAll("<img src=/images/level/4.gif border=0 align=absmiddle>", "#");
            source = source.replaceAll("<img src=/images/level/5.gif border=0 align=absmiddle>", "A");
            return source;
        } else return null;
    }

    /**
	 * 找到html中的第n个tables的source
	 * @param source html 源
	 * @return table的source，不包括table的标签，这里不能处理table嵌套的情况
	 */
    public static String dealTables(String source, int n) {
        if (source == null) return null;
        if (n <= 0) return null;
        int s = 0, e = 0;
        while ((s = source.indexOf("<table", s)) != -1) {
            if (--n == 0) {
                s = source.indexOf(">", s) + 1;
                e = source.indexOf("</table>", s);
                if (e == -1) break; else return source.substring(s, e);
            } else {
                s = source.indexOf(">", s) + 1;
                if (s == -1) break;
            }
        }
        return null;
    }

    /**
	 * 根据html语法，从table里抽取一行的信息
	 * @param source
	 * @return
	 */
    public static String[] dealTableRows(String source) {
        ArrayList<String> list = new ArrayList<String>();
        if (source != null) {
            int s = 0, e = 0;
            while ((s = source.indexOf("<tr", e)) != -1) {
                s = source.indexOf(">", s);
                if (s == -1) break; else s++;
                e = source.indexOf("<tr", s);
                if (e == -1) {
                    e = source.length();
                    if (source.charAt(e - 1) == '>' && source.charAt(e - 2) == 'r' && source.charAt(e - 3) == 't' && source.charAt(e - 4) == '/' && source.charAt(e - 5) == '<') {
                        e = e - 5;
                    }
                } else {
                    if (source.charAt(e - 1) == '>' && source.charAt(e - 2) == 'r' && source.charAt(e - 3) == 't' && source.charAt(e - 4) == '/' && source.charAt(e - 5) == '<') {
                        e = e - 5;
                    }
                }
                list.add(source.substring(s, e).trim());
            }
        }
        return list.toArray(new String[0]);
    }

    /**
	 * 根据html语法，从一行中抽取一个单元格的信息
	 * @param source
	 * @return
	 */
    public static String[] dealTableCells(String source) {
        ArrayList<String> list = new ArrayList<String>();
        if (source != null) {
            int s = 0, e = 0;
            while ((s = source.indexOf("<td", e)) != -1) {
                s = source.indexOf(">", s);
                if (s == -1) break; else s++;
                e = source.indexOf("<td", s);
                if (e == -1) {
                    e = source.length();
                    if (source.charAt(e - 1) == '>' && source.charAt(e - 2) == 'd' && source.charAt(e - 3) == 't' && source.charAt(e - 4) == '/' && source.charAt(e - 5) == '<') {
                        e = e - 5;
                    }
                } else {
                    if (source.charAt(e - 1) == '>' && source.charAt(e - 2) == 'd' && source.charAt(e - 3) == 't' && source.charAt(e - 4) == '/' && source.charAt(e - 5) == '<') {
                        e = e - 5;
                    }
                }
                list.add(source.substring(s, e).trim());
            }
        }
        return list.toArray(new String[0]);
    }

    /**
	 * 根据js的function入参格式进行分割，返回字符创数组
	 * @param source 源
	 * @return 分割后的字符串
	 */
    public static String[] splitArguments(String source) {
        if (source == null) return null;
        StringBuffer stack = new StringBuffer();
        List<String> ret = new ArrayList<String>();
        int in = 0;
        char l = 0;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == ',' && in == 0) {
                ret.add(stack.toString());
                stack.setLength(0);
            } else if (c == '\'' && in == 0) {
                in = 1;
            } else if (c == '\'' && in == 1 && l != '\\') {
                in = 0;
            } else if (c == '"' && in == 0) {
                in = 2;
            } else if (c == '"' && in == 2 && l != '\\') {
                in = 0;
            } else if (stack.length() == 0 && in == 0 && " \t\n\r".indexOf(c) != -1) {
            } else {
                stack.append(c);
            }
            l = c;
        }
        if (stack.length() != 0) ret.add(stack.toString());
        return ret.toArray(new String[0]);
    }

    /**
	 * 以下deal enclosing tag系列用于查找html源码中封闭的tag标签
	 * 比如<table></table>或者<div></div>
	 * 1.该系列方法可以提取递归的封闭tag
	 * 2.该系列方法不能处理tag attribute上含有>的tag，比如在tag的onload函数里有>标志
	 */
    public static String dealEnclosingTag(String source, String tagName, boolean includeTag) {
        return dealEnclosingTag(source, tagName, null, includeTag);
    }

    public static String dealEnclosingTag(String source, String tagName, String features, boolean includeTag) {
        String[] ret = dealEnclosingTags(source, tagName, features, includeTag);
        return ret != null && ret.length > 0 ? ret[0] : null;
    }

    public static String[] dealEnclosingTags(String source, String tagName, boolean includeTag) {
        return dealEnclosingTags(source, tagName, null, includeTag);
    }

    public static String[] dealEnclosingTags(String source, String tagName, String features, boolean includeTag) {
        if (source == null || tagName == null) return null;
        List<String> ret = new ArrayList<String>();
        int[] stack1 = new int[100];
        String[] stack2 = new String[100];
        int top = 0;
        Pattern p = Pattern.compile("<[/]?" + tagName.toLowerCase() + "[^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(source);
        while (m.find()) {
            String g = m.group();
            if (g.startsWith("</")) {
                if (top == 0) continue;
                int s = stack1[--top];
                String startTag = stack2[top];
                int e = includeTag ? m.end() : m.start();
                if (features == null || startTag.indexOf(features) != -1) ret.add(source.substring(s, e));
            } else {
                stack1[top] = includeTag ? m.start() : m.end();
                stack2[top++] = g;
            }
        }
        return ret.toArray(new String[0]);
    }

    /**
	 * 以下方法用于查找html中的a标记，并将a标记的tag部分和enclosing的部分分离成一个2维数组
	 * features用于过滤一些特殊的a标记
	 */
    public static String[] dealHref(String source) {
        return dealHref(source, null);
    }

    public static String[] dealHref(String source, String features) {
        List<String[]> r = dealHrefs(source, features);
        if (r != null && r.size() > 0) return r.get(0); else return new String[2];
    }

    public static List<String[]> dealHrefs(String source) {
        return dealHrefs(source, null);
    }

    public static List<String[]> dealHrefs(String source, String features) {
        if (source == null) return null;
        List<String[]> ret = new ArrayList<String[]>();
        Pattern p = Pattern.compile("<a[^>]*>[^<]*", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(source);
        while (m.find()) {
            String tag = m.group();
            if (features != null && tag.indexOf(features) == -1) continue;
            int e = tag.indexOf('>');
            String[] r = new String[2];
            r[0] = tag.substring(0, e + 1);
            r[1] = tag.substring(e + 1).replaceAll("\\s", "");
            ret.add(r);
        }
        return ret;
    }

    public static String replacePattern(String source, String pattern, PatternListener lsr) {
        if (pattern == null || pattern.length() == 0 || lsr == null) return source;
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(source);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String g = m.group();
            String n = lsr.onPatternMatch(g);
            m.appendReplacement(sb, (n != null) ? n : g);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public interface PatternListener {

        public String onPatternMatch(String source);
    }

    /**
	 * 以下是tag soup的相关方法，用来解析html代码
	 */
    public static class BaseContentHandler implements ContentHandler {

        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
        }

        public void setDocumentLocator(Locator locator) {
        }

        public void skippedEntity(String name) throws SAXException {
        }
    }

    private static class HtmlParser {

        private static final HTMLSchema schema = new HTMLSchema();
    }

    public static void fromHtml(String source, BaseContentHandler handler) {
        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
            parser.setContentHandler(handler);
            parser.parse(new InputSource(new StringReader(source)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
