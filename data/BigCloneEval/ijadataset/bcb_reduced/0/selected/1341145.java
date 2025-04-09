package listo.utils;

import listo.utils.types.DateTime;
import org.apache.commons.lang.StringUtils;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtils {

    /**
     * Performs an 'equal' between two nullable objects.
     * Two nulls are considered equal.
     *
     * @param a the a
     * @param b the b
     * @return true if equal or both null
     */
    public static boolean equals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    /**
     * Creates a new ArrayList and fills it with the contents of the given enumeration.
     *
     * @param enumeration the enumeration
     * @return the arraylist
     */
    public static <T> List<T> toArrayList(Enumeration<T> enumeration) {
        List<T> list = new ArrayList<T>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    /**
     * Serialized the given properties to a string.
     *
     * @param properties the properties
     * @param comments   an option comment
     * @return the string
     */
    public static String serialize(Properties properties, String comments) {
        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, comments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    /**
     * Deserialized properties from a string.
     *
     * @param text the string
     * @return the properties
     */
    public static Properties deserialize(String text) {
        return deserialize(text, null);
    }

    /**
     * Deserialized properties from a string.
     *
     * @param text     the string
     * @param defaults the default properties
     * @return the properties
     */
    public static Properties deserialize(String text, Properties defaults) {
        StringReader reader = new StringReader(text);
        Properties properties = new Properties(defaults);
        try {
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    /**
     * Provides s simple and fast string converter for basic types.
     *
     * @param string     the string to convert
     * @param targetType the target type
     * @return the deserialized object
     */
    @SuppressWarnings({ "unchecked" })
    public static Object convertFromString(String string, Class targetType) {
        if (StringUtils.isEmpty(string)) return null;
        if (targetType == String.class) return string;
        if (targetType == int.class || targetType == Integer.class) return Integer.valueOf(string);
        if (targetType == float.class || targetType == Float.class) return Float.valueOf(string);
        if (targetType == double.class || targetType == Double.class) return Double.valueOf(string);
        if (targetType == char.class || targetType == Character.class) return string.charAt(0);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.valueOf(string);
        if (targetType == long.class || targetType == Long.class) return Long.valueOf(string);
        if (targetType == DateTime.class) return DateTime.valueOf(string);
        if (targetType.isEnum()) return Enum.valueOf(targetType, string);
        throw new RuntimeException("Unknown type for string conversion: " + targetType);
    }

    /**
     * Split out the actually class name from a fully qualified name (including the package).
     *
     * @param object the object the get the class name from
     * @return the short class name
     */
    public static String getShortClassName(Object object) {
        return getShortClassName(object.getClass());
    }

    /**
     * Split out the actually class name from a fully qualified name (including the package).
     *
     * @param clazz the class
     * @return the short class name
     */
    public static String getShortClassName(Class clazz) {
        return getShortClassName(clazz.getName());
    }

    /**
     * Split out the actually class name from a fully qualified name (including the package).
     *
     * @param fullyQualifiedClassname the long name
     * @return the short class name
     */
    public static String getShortClassName(String fullyQualifiedClassname) {
        return fullyQualifiedClassname.substring(fullyQualifiedClassname.lastIndexOf('.') + 1);
    }

    /**
     * Returns the smallest of the given integers.
     *
     * @param integers the integers
     * @return the minimum
     */
    public static int min(int... integers) {
        if (integers.length == 0) throw new IllegalArgumentException();
        int min = integers[0];
        for (int i = 1; i < integers.length; i++) {
            min = Math.min(min, integers[i]);
        }
        return min;
    }

    /**
     * Returns the largest of the given integers.
     *
     * @param integers the integers
     * @return the maximum
     */
    public static int max(int... integers) {
        if (integers.length == 0) throw new IllegalArgumentException();
        int min = integers[0];
        for (int i = 1; i < integers.length; i++) {
            min = Math.max(min, integers[i]);
        }
        return min;
    }

    private static final Pattern URL_PATTERN = Pattern.compile("((?:(?:http|https|ftp|ftps)://\\S+?)|" + "(?:www(?:\\.[a-z0-9-]+)+\\.(?:[a-z]{2}|com|org|net|edu|gov|mil|biz|int|cat|pro|tel|name|info|aero|asia|coop|jobs|mobi|museum|travel)" + "(?:/\\S+?)?))[\\.,;:]*(?:\\s|$)", Pattern.CASE_INSENSITIVE);

    public static class UrlMatch {

        public String url;

        public int startIndex;

        public int endIndex;
    }

    /**
     * Returns all URLs found in the given text.
     *
     * @param text the text to search through
     * @return the found URLs
     */
    public static UrlMatch[] findUrls(String text) {
        List<UrlMatch> urls = new ArrayList<UrlMatch>();
        if (StringUtils.isNotEmpty(text)) {
            Matcher matcher = URL_PATTERN.matcher(text);
            while (matcher.find()) {
                UrlMatch match = new UrlMatch();
                match.url = matcher.group(1);
                match.startIndex = matcher.start(1);
                match.endIndex = matcher.end(1);
                urls.add(match);
            }
        }
        return urls.toArray(new UrlMatch[urls.size()]);
    }

    /**
     * Tries to open the default system browser for the given URL.
     *
     * @param url the url
     */
    public static void openBrowser(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException ex) {
            } catch (URISyntaxException ex1) {
            }
        }
    }

    /**
     * Inserts line breaks at word boundaries. Guaranties that the resulting string
     * will not have lines longer than maxLineLength.
     *
     * @param text          the string to wrap
     * @param maxLineLength the max number of chars in a line
     * @return the wrapped text
     */
    public static String wordWrap(String text, int maxLineLength) {
        if (StringUtils.isEmpty(text)) return text;
        if (maxLineLength <= 0) throw new IllegalArgumentException("maxLineLength must be greater than zero");
        StringBuilder sb = new StringBuilder(text);
        int lineLen = 0;
        int lastWhiteSpace = -1;
        int lineStart = 0;
        for (int i = 0; i < sb.length(); i++) {
            char cursor = sb.charAt(i);
            lineLen++;
            if (cursor != '\n' && Character.isWhitespace(cursor)) {
                if (lastWhiteSpace == i - 1 || lineLen == 1) {
                    sb.deleteCharAt(i--);
                    lineLen--;
                } else {
                    lastWhiteSpace = i;
                }
                if (lineLen < maxLineLength) continue;
            }
            if (cursor == '\n' || lineLen == maxLineLength) {
                if (lineLen == maxLineLength && i < sb.length() - 1) {
                    i = lastWhiteSpace > lineStart && !Character.isWhitespace(sb.charAt(i + 1)) ? lastWhiteSpace : i + 1;
                    if (Character.isWhitespace(sb.charAt(i))) {
                        sb.setCharAt(i, '\n');
                    } else {
                        sb.insert(i, '\n');
                    }
                }
                lineLen = 0;
                lineStart = i + 1;
            }
        }
        return sb.toString();
    }
}
