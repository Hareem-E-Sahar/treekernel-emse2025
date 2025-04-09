package org.pubcurator.uima.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kai Schlamp (schlamp@gmx.de)
 *
 */
public class AnnotationUtil {

    public static int[][] fuzzyFind(String text, String term) {
        return fuzzyFind(text, term, true);
    }

    public static int[][] fuzzyFind(String text, String term, boolean fullcheck) {
        List<int[]> beginEnds = new ArrayList<int[]>();
        {
            beginEnds.addAll(Arrays.asList(find(text, term, false, false)));
        }
        if (fullcheck || beginEnds.isEmpty()) {
            beginEnds.addAll(Arrays.asList(find(text, term, true, false)));
        }
        if (fullcheck || beginEnds.isEmpty()) {
            beginEnds.addAll(Arrays.asList(find(text, term, false, true)));
        }
        if (fullcheck || beginEnds.isEmpty()) {
            beginEnds.addAll(Arrays.asList(find(text, term, true, true)));
        }
        return beginEnds.toArray(new int[0][0]);
    }

    public static int[][] find(String text, String term, boolean caseInsensitive, boolean fuzzy) {
        List<int[]> beginEnds = new ArrayList<int[]>();
        String regexp = "";
        if (fuzzy) {
            String[] termArray = term.split("\\s+");
            for (String termElement : termArray) {
                if (!regexp.isEmpty()) {
                    regexp += "[\\W|_]*";
                }
                regexp += Pattern.quote(termElement);
            }
        } else {
            regexp = Pattern.quote(term);
        }
        Pattern pattern;
        if (caseInsensitive) {
            pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        } else {
            pattern = Pattern.compile(regexp);
        }
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int[] beginEnd = new int[] { matcher.start(), matcher.end() };
            beginEnds.add(beginEnd);
        }
        return beginEnds.toArray(new int[0][0]);
    }

    public static boolean fuzzyMatch(String term1, String term2) {
        return fuzzyMatch(term1, term2, true);
    }

    public static boolean fuzzyMatch(String term1, String term2, boolean fullcheck) {
        boolean match = false;
        {
            match = match(term1, term2, false, false);
        }
        if (fullcheck || !match) {
            match = match(term1, term2, true, false);
        }
        if (fullcheck || !match) {
            match = match(term1, term2, false, true);
        }
        if (fullcheck || !match) {
            match = match(term1, term2, true, true);
        }
        return match;
    }

    public static boolean match(String term1, String term2, boolean caseInsensitive, boolean fuzzy) {
        boolean match = false;
        if (fuzzy) {
            String regexp = "[\\W|_]*";
            term1 = term1.replaceAll(regexp, "");
            term2 = term2.replaceAll(regexp, "");
        }
        if (caseInsensitive) {
            match = term1.toLowerCase().equals(term2.toLowerCase());
        } else {
            match = term1.equals(term2);
        }
        return match;
    }
}
