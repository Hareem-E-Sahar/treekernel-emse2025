package hailmary.util;

import java.util.ArrayList;
import java.util.regex.*;

public class PatternGroup {

    public static String getFirst(String regExp, String input, int flags) {
        Pattern pattern = Pattern.compile(regExp, flags);
        Matcher matcher = pattern.matcher(input);
        String groupString = null;
        if (matcher.find()) {
            groupString = matcher.group(1);
        }
        return groupString;
    }

    public static String getFirst(String[] regExps, String input, int flags) {
        String groupString = null;
        String regExp;
        int i = 0;
        boolean found = false;
        while ((i < regExps.length) && !found) {
            regExp = regExps[i];
            groupString = PatternGroup.getFirst(regExp, input, flags);
            if (groupString != null) {
                found = true;
            }
            i++;
        }
        return groupString;
    }

    public static String[] getAll(String regExp, String input, int flags) {
        Pattern pattern = Pattern.compile(regExp, flags);
        Matcher matcher = pattern.matcher(input);
        ArrayList groupStrings = new ArrayList();
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                groupStrings.add(matcher.group(i));
            }
        }
        return (String[]) groupStrings.toArray(new String[0]);
    }

    public static String replaceAll(String regExp, String input, String replacement, int flags) {
        Pattern pattern = Pattern.compile(regExp, flags);
        Matcher matcher = pattern.matcher(input);
        String returnString = "";
        int start = 0;
        int end = 0;
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                end = matcher.start(i);
                returnString = returnString + input.substring(start, end);
                returnString = returnString + replacement;
                start = matcher.end(i);
            }
        }
        returnString = returnString + input.substring(start);
        return returnString;
    }
}
