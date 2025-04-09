package aoetec.javalang._351regex;

import java.io.Console;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestFind {

    public static void main(String[] args) {
        Console c = System.console();
        if (c == null) {
            System.err.println("No console!");
            System.exit(-1);
        }
        while (true) {
            String regex = c.readLine("%n  find(), Enter your regex:");
            String input = c.readLine("  find(), enter input String to serach:");
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(input);
            boolean bFind = false;
            while (m.find()) {
                bFind = true;
                c.printf("find the text '%s' starting at %d ending at %d%n", m.group(), m.start(), m.end());
            }
            if (!bFind) {
                c.printf("No matcher found.%n");
            }
        }
    }
}
