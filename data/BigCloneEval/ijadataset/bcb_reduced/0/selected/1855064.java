package com.tcurtil.jforker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringSerializer {

    private static String serializeRegex = "(\\\\|\n)";

    private static String unserializeRegex = "(\\\\\\\\|\\\\n)";

    public static String serialize(String s) {
        StringBuffer sb = new StringBuffer();
        Pattern compile = Pattern.compile(serializeRegex);
        Matcher matcher = compile.matcher(s);
        int previousCopy = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            sb.append(s.substring(previousCopy, start));
            String match = s.substring(start, end);
            sb.append(match.equals("\\") ? "\\\\" : "\\n");
            previousCopy = end;
        }
        sb.append(s.substring(previousCopy, s.length()));
        return sb.toString();
    }

    public static String unserialize(String s) {
        StringBuffer sb = new StringBuffer();
        Pattern compile = Pattern.compile(unserializeRegex);
        Matcher matcher = compile.matcher(s);
        int previousCopy = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            sb.append(s.substring(previousCopy, start));
            String match = s.substring(start, end);
            sb.append(match.equals("\\\\") ? "\\" : "\n");
            previousCopy = end;
        }
        sb.append(s.substring(previousCopy, s.length()));
        return sb.toString();
    }
}
