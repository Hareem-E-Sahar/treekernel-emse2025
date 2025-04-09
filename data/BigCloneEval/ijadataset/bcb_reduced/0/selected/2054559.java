package org.forzaframework.util;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author cesarreyes
 *         Date: 08-sep-2008
 *         Time: 18:37:29
 */
public class RegexUtils {

    public static List<String> matchesToList(String pattern, String input) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(input);
        List<String> results = new ArrayList<String>();
        while (matcher.find()) {
            results.add(input.substring(matcher.start(), matcher.end()));
        }
        return results;
    }
}
