package com.divosa.eformulieren.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 * Utility class for String manipulation.
 * 
 * @author Bart Ottenkamp
 */
public final class StringUtil {

    /**
     * The logger for this class.
     */
    private static Logger LOGGER = Logger.getLogger(StringUtil.class);

    /**
     * Replace all occurrences of the specified replacementSoource in the specified source with the specified
     * replacementTarget.
     * 
     * @param source the source to replace parts in
     * @param replacementSource the source to be replaced
     * @param replacementTarget the target to be inserted into the source
     * @return the adjusted source
     */
    public static String replaceInAttributesOfXMLTags(String source, final String replacementSource, final String replacementTarget) {
        Pattern attribute = Pattern.compile("= *\".*?\"");
        Matcher matcher = attribute.matcher(source);
        int addIndent = 0;
        int lengthDiv = replacementTarget.length() - replacementSource.length();
        while (matcher.find()) {
            String g = matcher.group();
            int localIndent = 0;
            while (g.indexOf(replacementSource) > -1) {
                g = g.replaceFirst(replacementSource, replacementTarget);
                source = source.substring(0, matcher.start() + addIndent) + g + source.substring(matcher.end() + addIndent + localIndent);
                localIndent += lengthDiv;
            }
            addIndent += localIndent;
        }
        return source;
    }

    /**
     * Replace all occurrences of the specified replacementString in the specified source with the replacementTarget.
     * 
     * @param source the source string before replacement
     * @param replacementSource the string to be replaced by the replacementTarget
     * @param replacementTarget the string to be inserted into the source
     * @return
     */
    public static String replaceInString(String source, String replacementSource, String replacementTarget) {
        String result = null;
        result = source.replaceAll(replacementSource, replacementTarget);
        return result;
    }

    public static String getStringBetween(String source, String character, String endCharacterPattern) {
        String result = "";
        int atPosition = source.indexOf(character) + 1;
        if (atPosition > 0) {
            String tmpResult = source.substring(atPosition);
            Pattern p = Pattern.compile(endCharacterPattern);
            Matcher matcher = p.matcher(tmpResult);
            if (matcher.find()) {
                result += matcher.group();
            }
        }
        return result;
    }

    public static int firstCharPositionNotInList(String source, List<String> strings) {
        int j = 0;
        for (int i = 0; i < source.length(); i++) {
            if (!strings.contains(Character.toString(source.charAt(i)))) {
                j = i;
                break;
            }
        }
        return j;
    }
}
