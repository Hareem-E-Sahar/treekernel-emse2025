package org.sysolar.sun.mvc.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Json {

    private static Pattern pKvPair = Pattern.compile("(\\w+):([@#]?[\\d\\.]+|'[^']*')");

    private static Pattern pJson = Pattern.compile("\\{(\\w+:([@#]?[\\d\\.]+|'[^']*'),?)*\\}");

    private static Pattern pJsonArrayElement = Pattern.compile("[@#]?[\\d\\.]+|'[^']*'");

    private static Pattern pJsonArray = Pattern.compile("\\[(([@#]?[\\d\\.]+|'[^']*'),?)*\\]");

    private static Pattern pMapReplacer = Pattern.compile("@\\d+");

    private static Pattern pListReplacer = Pattern.compile("#\\d+");

    /**
     * sysolar 框架专用方法，生成把 Java Map 对象转化为 JavaScript json 格式字串，并放至 DB 对象里
     * 的 JavaScript 可执行语句。
     * 
     * @param key 键
     * @param map Java Map 对象
     * @return JavaScript 可执行语句。
     */
    public static String toJs(String key, Map<Object, Object> map) {
        StringBuilder buffer = new StringBuilder(512);
        toJsPrifix(buffer, key);
        encode(buffer, map);
        toJsSuffix(buffer);
        return buffer.toString();
    }

    /**
     * sysolar 框架专用方法，生成把 Java Map 对象转化为 JavaScript json 格式字串，并放至 DB 对象里
     * 的 JavaScript 可执行语句。
     * 
     * @param key 键
     * @param values Java 对象数组
     * @return JavaScript 可执行语句。
     */
    public static String toJs(String key, Object[] values) {
        StringBuilder buffer = new StringBuilder(4096);
        toJsPrifix(buffer, key);
        encode(buffer, values);
        toJsSuffix(buffer);
        return buffer.toString();
    }

    /**
     * sysolar 框架专用方法，生成把 Java List 对象转化为 JavaScript json 格式字串，并放至 DB 对象里
     * 的 JavaScript 可执行语句。
     * 
     * @param key 键
     * @param list Java List
     * @return JavaScript 可执行语句。
     */
    public static String toJs(String key, List<Object> list) {
        return toJs(key, list.toArray());
    }

    /**
     * sysolar 框架专用方法，生成把 Java 对象转化为 JavaScript json 格式字串，并放至 DB 对象里
     * 的 JavaScript 可执行语句。
     * 
     * @param key 键
     * @param list Java List
     * @return JavaScript 可执行语句。
     */
    public static String toJs(String key, Object obj) {
        StringBuilder buffer = new StringBuilder(128);
        toJsPrifix(buffer, key);
        encode(buffer, obj);
        toJsSuffix(buffer);
        return buffer.toString();
    }

    private static void toJsPrifix(StringBuilder buffer, String key) {
        buffer.append("DB.put('");
        buffer.append(key);
        buffer.append("',");
    }

    private static void toJsSuffix(StringBuilder buffer) {
        buffer.append(");");
    }

    /**
     * 通过内嵌 iframe 和服务器进行交互时（多见上传文件的情况），交互完成后调用父页面里的 JavaScript 函数
     * 进行后续处理。
     * 
     * @param jsMethod JavaScript 方法名
     * @param params 需要传递的参数
     * @return 包含 script 标签和 JavaScript 可执行语句的串。
     */
    public static String toScript(String jsMethod, Object... params) {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append("<script>");
        buffer.append("parent.").append(jsMethod).append("(");
        for (Object param : params) {
            encode(buffer, param);
            buffer.append(",");
        }
        if (buffer.charAt(buffer.length() - 1) == ',') {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append(");");
        buffer.append("</script>");
        return buffer.toString();
    }

    /**
     * 把 Java Map 转化为 JavaScript 对象类型 json 串，即{}格式，考虑到 key 可能有多种数据形式
     * （如：0，'a b 1'），故 key 统一用String类型，即key统一用单引号引起来。当 value 是 String 时
     * 需要用单引号引起来，当 value 是日期类型时，转化为时间戳。
     * 
     * @param map
     * @return
     */
    public static String encode(Map<Object, Object> map) {
        StringBuilder buffer = new StringBuilder(512);
        encode(buffer, map);
        return buffer.toString();
    }

    public static void encode(StringBuilder buffer, Map<Object, Object> map) {
        buffer.append("{");
        Object value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            buffer.append("'").append(entry.getKey()).append("':");
            value = entry.getValue();
            encode(buffer, value);
            buffer.append(",");
        }
        if (buffer.charAt(buffer.length() - 1) == ',') {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append("}");
    }

    /**
     * 把 Java List 转化为 Javascript 数组格式 json 串。当 value 是 String 时需要用单引号引起来，
     * 当 value 是日期类型时，转化为时间戳。
     * 
     * @param list
     * @return
     */
    public static String encode(List<Object> list) {
        return encode(list.toArray());
    }

    public static void encode(StringBuilder buffer, List<Object> list) {
        encode(buffer, list.toArray());
    }

    /**
     * 把 Java 数组转化为 JavaScript 数组格式 json 串。当 value 是 String 时需要用单引号引起来，
     * 当 value 是日期类型时，转化为时间戳。
     * 
     * @param values
     * @return
     */
    public static String encode(Object[] values) {
        StringBuilder buffer = new StringBuilder(4096);
        encode(buffer, values);
        return buffer.toString();
    }

    public static void encode(StringBuilder buffer, Object[] values) {
        buffer.append("[");
        for (Object value : values) {
            encode(buffer, value);
            buffer.append(",");
        }
        if (buffer.charAt(buffer.length() - 1) == ',') {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append("]");
    }

    /**
     * 把 Java 对象转化为 JavaScript 格式 json 串。当 value 是 String 时需要用单引号引起来，
     * 当 value 是日期类型时，转化为时间戳。
     * 
     * @param value
     * @return
     */
    public static String encode(Object value) {
        StringBuilder buffer = new StringBuilder(128);
        encode(buffer, value);
        return buffer.toString();
    }

    public static void encode(StringBuilder buffer, Object value) {
        if (value instanceof String) {
            buffer.append("'").append(value).append("'");
        } else if (value instanceof Date) {
            buffer.append(((Date) value).getTime());
        } else {
            buffer.append(value);
        }
    }

    /**
     * 把 JSON 格式字串转化为 Java Map，以方便 Java 程序里使用。
     * 
     * @param json JSON格式数据字串 包含的{}对象不要超过500个
     * @return 包含数据的 Java Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toJavaMap(String json) {
        return (Map<String, Object>) toJavaObject(json);
    }

    /**
     * 把包含多个json对象的数组字串转化为Java List。
     * 
     * @param json 包含的{}对象不要超过500个
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Object> toJavaList(String json) {
        return (List<Object>) toJavaObject(json);
    }

    public static Object toJavaObject(String input) {
        input = input.trim();
        Matcher mKvPair = null;
        Matcher mJson = null;
        Matcher mJsonArrayElement = null;
        Matcher mJsonArray = null;
        StringBuilder buffer = new StringBuilder(input);
        int offset = 0;
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(100);
        List<List<Object>> listList = new ArrayList<List<Object>>(100);
        while (true) {
            if (pMapReplacer.matcher(input).matches()) {
                return mapList.get(mapList.size() - 1);
            } else if (pListReplacer.matcher(input).matches()) {
                return listList.get(listList.size() - 1);
            }
            boolean found = false;
            offset = 0;
            mJson = pJson.matcher(input);
            while (mJson.find()) {
                found = true;
                Map<String, Object> map = new HashMap<String, Object>(20);
                mKvPair = pKvPair.matcher(mJson.group());
                while (mKvPair.find()) {
                    String key = mKvPair.group(1);
                    String value = mKvPair.group(2);
                    if (pMapReplacer.matcher(value).matches()) {
                        map.put(key, mapList.get(Integer.parseInt(value.substring(1))));
                    } else if (pListReplacer.matcher(value).matches()) {
                        map.put(key, listList.get(Integer.parseInt(value.substring(1))));
                    } else if (value.charAt(0) == '\'') {
                        map.put(key, value.substring(1, value.length() - 1));
                    } else {
                        map.put(key, value);
                    }
                }
                String replacer = "@" + mapList.size();
                buffer.replace(mJson.start() - offset, mJson.end() - offset, replacer);
                offset += (mJson.end() - mJson.start() - replacer.length());
                mapList.add(map);
            }
            input = buffer.toString();
            offset = 0;
            mJsonArray = pJsonArray.matcher(input);
            while (mJsonArray.find()) {
                found = true;
                List<Object> list = new ArrayList<Object>(20);
                mJsonArrayElement = pJsonArrayElement.matcher(mJsonArray.group());
                while (mJsonArrayElement.find()) {
                    String element = mJsonArrayElement.group();
                    if (pMapReplacer.matcher(element).matches()) {
                        list.add(mapList.get(Integer.parseInt(element.substring(1))));
                    } else if (pListReplacer.matcher(element).matches()) {
                        list.add(listList.get(Integer.parseInt(element.substring(1))));
                    } else if (element.charAt(0) == '\'') {
                        list.add(element.substring(1, element.length() - 1));
                    } else {
                        list.add(element);
                    }
                }
                String replacer = "#" + listList.size();
                buffer.replace(mJsonArray.start() - offset, mJsonArray.end() - offset, replacer);
                offset += (mJsonArray.end() - mJsonArray.start() - replacer.length());
                listList.add(list);
            }
            input = buffer.toString();
            if (!found) {
                return null;
            }
        }
    }

    /**
     * 格式化从别的网站获取的json字串，以符合我们的规则。
     * 
     * @param json
     * @return
     */
    public static String format(String json) {
        json = json.replaceAll("\\\\\"", "").replaceAll("\\\\\'", "");
        int offset = 0;
        StringBuilder buffer = new StringBuilder(json);
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        while (m.find()) {
            buffer.replace(m.start(), m.end(), "'" + m.group(1).replace("'", " ") + "'");
        }
        json = buffer.toString();
        json = json.replaceAll("\\s+:", ":").replaceAll(":\\s+", ":");
        json = json.replaceAll("\\s+,", ",").replaceAll(",\\s+", ",");
        p = Pattern.compile("'(\\w+)':");
        m = p.matcher(json);
        buffer.delete(0, buffer.length()).append(json);
        while (m.find()) {
            buffer.deleteCharAt(m.start() - offset);
            offset++;
            buffer.deleteCharAt(m.end() - 2 - offset);
            offset++;
        }
        p = Pattern.compile(":([a-zA-Z_]+)([,\\]\\}])");
        m = p.matcher(buffer.toString());
        offset = 0;
        while (m.find()) {
            buffer.insert(m.start() + 1 + offset, '\'');
            offset++;
            buffer.insert(m.end() - 1 + offset, '\'');
            offset++;
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        Integer[] arr = new Integer[] { 1, 2, 3 };
        System.out.println(Json.toJs("abc", arr));
        System.out.println(Json.toJs("t", "hehh hhhh"));
    }
}
