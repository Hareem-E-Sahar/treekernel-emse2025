package org.mss.db.hibernateapp.wizard;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex Beispiele
 * abz    exakt die buchstabenfolge
 * [abc]  a b oder c
 * [^abc] alles ausser abc
 * [a-z]* kleinbuchstaben hintereinander, 0 - n mal
 * [a-z]+ kleinbuchstaben hintereinander, 1 - n mal
 * [a-zA-Z0-9] Alle Zahlen und Buchstaben
 * [a-zA-Z]+[0-9] Alle Buchstabenkombinationen, gefolgt von Zahlen
 * abz|xzw entweder erste oder zweite kombination
 * 
 * Vordefinierte Klassen
 * \d = [0-9]
 * \D = [^0-9]
 * \s = whitespace, 
 * \S = non whitespace character
 * \w = [a-zA-Z_0-9]
 * \W = non word character [^\w]
 * 
 * Arten von Parametern
 * greedy
 * reluctant
 * possessive
 * 
 * Capturing Gruppen
 * ([a-zA-Z]*)([0-9]*) 
 * \1 enth�lt die gefundenen Buchstaben
 * \2 enth�lt die gefundenen Zahlen
 * \0 enth�lt \1 und \2
 * 
 * @author Administrator
 *
 */
public class RegexUtil {

    public Pattern createPattern(String strPattern) {
        Pattern pattern = Pattern.compile(strPattern);
        return pattern;
    }

    public Matcher match(Pattern pattern, String strText) {
        return pattern.matcher(strText);
    }

    public boolean find(String strText, String strPattern) {
        Pattern findPattern = Pattern.compile("[a-z]*");
        Matcher matcher = findPattern.matcher(strText);
        int finder = 0;
        while (matcher.find()) {
            finder++;
            System.out.println("Found " + strText.substring(matcher.start(), matcher.end()));
        }
        System.out.println("Found " + finder + " matches in " + strText);
        if (finder > 0) return true;
        return false;
    }

    public boolean replaceAll(String strText, String strReplace, String strPattern) {
        Pattern findPattern = Pattern.compile("[a-z]*");
        Matcher matcher = findPattern.matcher(strText);
        int finder = 0;
        strText = matcher.replaceAll(strReplace);
        System.out.println("Replace " + finder + " matches in " + strText);
        if (finder > 0) return true;
        return false;
    }

    /**
	 * 
	 * @param strString
	 * @return
	 * 
	 * ein Space kann ein normaler space oder ein linebreak sein
	 * Darum sucht das regex nach allem was keine Zahl ist und ersetzt die Vorkomnisse mit ""
	 */
    public String removeSpace(String strString) {
        String strPattern1 = "[^0-9.,]";
        Pattern findPattern = Pattern.compile(strPattern1);
        Matcher matcher = findPattern.matcher(strString);
        String returnValue = matcher.replaceAll("");
        return returnValue;
    }

    public void testgroup(String strRegex, String strInput) {
        strRegex = "([^aeiou]*)(.*)";
        Pattern findPattern = Pattern.compile(strRegex);
        Matcher matcher = findPattern.matcher(strInput);
        if (matcher.matches()) {
            System.out.println(matcher.group(2) + matcher.group(1));
        }
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        RegexUtil util = new RegexUtil();
        String strPattern1 = "(.*)([\\d+])(.*)";
        String strText1 = "TR[54]";
        Pattern pattern = Pattern.compile(strPattern1);
        Matcher matcher = pattern.matcher(strText1);
        System.out.println("Matches entire String " + matcher.matches());
        System.out.println("Matches at beginning " + matcher.lookingAt());
        System.out.println(matcher.group(1));
        while (matcher.find()) {
            System.out.println("Found a match: " + matcher.group());
            System.out.println("Start position: " + matcher.start());
            System.out.println("End position: " + matcher.end());
        }
        String strText2 = "abasdfABSDSAFASDF";
        System.out.println(!strText2.matches("[a-zA-Z]*"));
    }
}
