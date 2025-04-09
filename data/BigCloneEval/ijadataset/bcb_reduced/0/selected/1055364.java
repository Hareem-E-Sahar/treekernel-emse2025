package onepoint.resource;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import onepoint.log.XLog;
import onepoint.log.XLogFactory;

public class XLocalizer {

    private static final XLog logger = XLogFactory.getLogger(XLocalizer.class);

    private static final char LOCALIZED_RESOURCE_SYMBOL = '$';

    private XLanguageResourceMap resourceMap;

    public final void setResourceMap(XLanguageResourceMap resource_map) {
        resourceMap = resource_map;
    }

    public final XLanguageResourceMap getResourceMap() {
        return resourceMap;
    }

    public static XLocalizer getLocalizer(XLanguageResourceMap resourceMap) {
        XLocalizer result = new XLocalizer();
        result.setResourceMap(resourceMap);
        return result;
    }

    /**
    * Get the internationalized text for given key.
    *
    * @param s the message key could be <code>null</code>, empty or contain multiple i18n placeholders of format: <bold>${key}</bold>
    * @return an internationalized <code>String</code> message
    */
    public String localize(String s) {
        if (!isLocalizable(s)) {
            return s;
        }
        if (resourceMap == null) {
            return s;
        }
        char first, second, last = '}';
        if (s.indexOf("${") > -1) {
            first = LOCALIZED_RESOURCE_SYMBOL;
            second = '{';
        } else if (s.indexOf("{$") > -1) {
            first = '{';
            second = LOCALIZED_RESOURCE_SYMBOL;
        } else {
            return s;
        }
        String begining = String.valueOf(first) + String.valueOf(second);
        int start = s.indexOf(begining);
        if (start < 0) {
            return s;
        }
        StringBuffer localized_buffer = new StringBuffer();
        int end = -1;
        XLanguageResource language_resource;
        while (start < s.length()) {
            if ((s.length() <= start + 3) || (s.charAt(start + 1) != second)) {
                localized_buffer.append(s.substring(0));
                break;
            }
            localized_buffer.append(s.substring(end + 1, start));
            end = s.indexOf(last, start + 2);
            if (end < 0) {
                localized_buffer.append(s.substring(start));
                break;
            }
            language_resource = resourceMap.getResource(s.substring(start + 2, end));
            if (language_resource == null) {
                logger.error("Could not resolve language-resource reference " + s.substring(start + 2, end) + ", within " + resourceMap.getID());
                localized_buffer.append(s.substring(start));
                break;
            }
            localized_buffer.append(language_resource.getText());
            start = s.indexOf(first, end);
            if (start < 0) {
                localized_buffer.append(s.substring(end + 1));
                break;
            }
        }
        return localized_buffer.toString();
    }

    /**
    * Get the internationalized text for given key and format the message with the arguments.
    *
    * @param s    the message key could be <code>null</code>, empty or contain multiple i18n placeholders of format: <bold>${key}</bold>
    * @param args a <code>Map</code> of arguments to replace the named placeholders (e.g. {currency}, {name})
    * @return an internationalized <code>String</code> message
    */
    public String localize(String s, Map args) {
        if (!isLocalizable(s)) {
            return s;
        }
        String ls = localize(s);
        if (ls != null && ls.length() > 0 && args != null && args.size() > 0) {
            for (Iterator iterator = args.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = (String) entry.getKey();
                String value = escapeString((String) entry.getValue());
                ls = ls.replaceAll("\\{" + key + "\\}", value.toString());
            }
        }
        return ls;
    }

    /**
    * Returns a new <code>String</code> in which all the non alphanumeric characters from the <code>String</code>
    *    passes as parameter are escaped ("\\" is added in front of those characters).
    * @param s - the <code>String</code> which may contain unescaped special characters.
    * @return a <code>String</code> in which all the non alphanumeric characters from the <code>String</code>
    *    passes as parameter are escaped.
    */
    public String escapeString(String s) {
        Pattern pattern = Pattern.compile("\\W");
        Matcher matcher = pattern.matcher(s);
        StringBuffer escapedString = new StringBuffer();
        int previousEnd = 0;
        while (matcher.find()) {
            escapedString.append(s.substring(previousEnd, matcher.start()));
            escapedString.append("\\");
            escapedString.append(matcher.group());
            previousEnd = matcher.end();
        }
        if (previousEnd != s.length()) {
            escapedString.append(s.substring(previousEnd, s.length()));
        }
        return escapedString.toString();
    }

    /**
    * Get the internationalized text for given key and format the message with the arguments.
    *
    * @param s    the message key could be <code>null</code>, empty or contain multiple i18n placeholders of format: <bold>${key}</bold>
    * @param args a <code>List</code> of arguments to replace the index placeholders (e.g. {1}, {3}).
    *             Index start from zero and the text placeholders are compatible <code>java.util.Format</code> definition.
    * @return an internationalized <code>String</code> message
    */
    public String localize(String s, List args) {
        if (!isLocalizable(s)) {
            return s;
        }
        String ls = localize(s);
        if (ls != null && ls.length() > 0 && args != null && args.size() > 0) {
            ls = MessageFormat.format(ls, args.toArray());
        }
        return ls;
    }

    /**
    * Checks whether a string is localizable or not.
    * @param s a <code>String</code>
    * @return <code>true</code> if the string is localizable, false otherwise.
    */
    private boolean isLocalizable(String s) {
        if (s == null) {
            return false;
        }
        String prefix = String.valueOf(LOCALIZED_RESOURCE_SYMBOL) + "{";
        String oldPrefix = "{" + String.valueOf(LOCALIZED_RESOURCE_SYMBOL);
        return s.contains(prefix) || s.contains(oldPrefix);
    }
}
