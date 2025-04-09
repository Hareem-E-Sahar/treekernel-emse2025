package sample.chapter6.patterMatching;

import java.io.Console;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    public static void main(String[] args) {
        Console c = System.console();
        assert (c != null) : "console cannot be null";
        String matcherStr = c.readLine("%s", "Matcher: ");
        String patternStr = c.readLine("%s", "Pattern: ");
        Pattern p = Pattern.compile(patternStr);
        Matcher m = p.matcher(matcherStr);
        System.out.println("Pattern is " + m.pattern());
        while (m.find()) {
            System.out.println(m.start() + " " + m.group() + " " + m.end());
        }
    }
}
