package org.retro.newtests.regex;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.retro.gis.BotLogger;

/**
 * Many of these tests are based on one of the regex javabooks from the
 * java.sun.com site.
 * 
 * (see <a href="http://java.sun.com/docs/books/tutorial/extra/regex/">RegEx</a>)
 * 
 * <pre>
 * . (any character)
 *  [ Pre-Defined ]
 *  \d
 *	A digit: [0-9]
 *	\D 
 *	A non-digit: [^0-9]
 *	\s 
 *	A whitespace character: [ \t\n\x0B\f\r] 
 *	\S 
 *	A non-whitespace character: [^\s] 
 *	\w 
 *	A word character: [a-zA-Z_0-9]
 *	\W 
 *	A non-word character: [^\w] 
 *  
 * </pre>
 * @author bigbinc
 */
public class RegexTest extends TestCase {

    public static void main(String[] args) {
        TestResult testRes = junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(RegexTest.class);
    }

    protected void setUp() {
        System.out.println("........ " + getName());
    }

    protected void tearDown() {
    }

    public void testHref() {
        String page = "Stuff you say, it is<a href=\"coolsite.htm\">Yea</a>I find it interesting";
        Pattern p = Pattern.compile("(<a.*?href=.*?>(.*?)</a>)");
        Matcher m = p.matcher(page);
        while (m.find()) {
            System.out.println(" ***** : " + m.groupCount());
            System.out.println(" ***** : " + m.group());
            System.out.println(" ***** + at " + m.start());
            System.out.println(" ***** - to " + m.end());
        }
    }

    public void testNonGreedyStrong() {
        String page = "Stuff you say, it is<strong>Yea</strong>I find it interesting";
        Pattern p = Pattern.compile("<strong>.*?</strong>");
        Matcher m = p.matcher(page);
        while (m.find()) {
            System.out.println(" ***** : " + m.group());
            System.out.println(" ***** + at " + m.start());
            System.out.println(" ***** - to " + m.end());
        }
    }

    public void testNonGreedyMatch() {
        String page = "Stuff you say, it is<a href=\"coolsite.htm\">Yea</a>I find it interesting";
        Pattern p = Pattern.compile("<a.*?href=.*?>.*?</a>");
        Matcher m = p.matcher(page);
        while (m.find()) {
            System.out.println(" ***** : " + m.group());
            System.out.println(" ***** + at " + m.start());
            System.out.println(" ***** - to " + m.end());
        }
    }

    /**
	 * Test a Boundary Matcher, check for beginning of the line.
	 * Note, the quantifier '*' was added to signify once, or not at all.	 
	 */
    public void testBoundaryMatches() {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("^Hello\\w*", Pattern.CASE_INSENSITIVE);
        m = p.matcher("Hello how are you doing");
        boolean _found = false;
        while (m.find()) {
            System.out.println(" : Boundary-Search \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
        m = p.matcher("Helloksjkld how are you doing");
        _found = false;
        while (m.find()) {
            System.out.println(" : Boundary-Search \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
        p = Pattern.compile("\\bHello\\B", Pattern.CASE_INSENSITIVE);
        m = p.matcher("This is not fun Hello ksjkldhow are you doing");
        _found = false;
        while (m.find()) {
            System.out.println(" : New-Search \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertFalse(_found);
        p = Pattern.compile("\\bHello\\b", Pattern.CASE_INSENSITIVE);
        m = p.matcher("This is not fun Hello ksjkldhow are you doing");
        _found = false;
        while (m.find()) {
            System.out.println(" : New-Search \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
    }

    public void testVowelsReplaceAE() {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("[ae]", Pattern.CASE_INSENSITIVE);
        m = p.matcher("aaaa this will be fun a a is going to be hello yes");
        String res = m.replaceAll("[a-z&&[aeiou]]");
        BotLogger.log(res);
    }

    public void testVowelsInner() {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("H[a-z&&[aeiou]]llo", Pattern.CASE_INSENSITIVE);
        m = p.matcher("My name is berlin hallo what is up");
        boolean _found = false;
        while (m.find()) {
            System.out.println(" : Vowel-Search \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
    }

    public void testVowels() {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("[a-z&&[aeiou]]", Pattern.CASE_INSENSITIVE);
        m = p.matcher("BerlinBrown");
        boolean _found = false;
        while (m.find()) {
            System.out.println(" : Vowel-Search \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
    }

    public void testEnd() {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("spiritbot\\W", Pattern.CASE_INSENSITIVE);
        m = p.matcher("spiritbot: how are you doing");
        boolean _found = false;
        while (m.find()) {
            System.out.println(" : Text \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
    }

    public void testReplaceIt() {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("spiritbot", Pattern.CASE_INSENSITIVE);
        m = p.matcher("spiritbot how are you doing");
        String res = m.replaceAll("");
        System.out.println("---------+" + res.trim());
        m = p.matcher("how are you spiritbot doing");
        res = m.replaceAll("");
        System.out.println("(middle)---------+" + res.trim());
    }

    public void testExtractFullSent() {
        String cmd = "hello how are";
        String msgFull = "Hello How Are You";
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile(cmd, Pattern.CASE_INSENSITIVE);
        m = p.matcher(msgFull);
        BotLogger.log(" ;; Check Full Compare ;");
        boolean _found = false;
        while (m.find()) {
            System.out.println(" : Text \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
    }

    public void testExtractFront() {
        String cmd = "sendmsg hello this is fun";
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("\\s", Pattern.CASE_INSENSITIVE);
        m = p.matcher(cmd);
        String results[] = p.split(cmd);
        boolean _found = false;
        int str = -1;
        int end = -1;
        while (m.find()) {
            str = m.start();
            end = m.end();
            _found = true;
            break;
        }
        BotLogger.log("--+" + results[0] + " :" + cmd.substring(end, cmd.length()).trim());
    }

    public void testSentFront() {
        String botRecord = "hello";
        String msg = "hellohow are you";
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile(".*?hel", Pattern.CASE_INSENSITIVE);
        m = p.matcher(msg);
        boolean _found = false;
        while (m.find()) {
            System.out.println(" : Text \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
    }

    public void testSentEquals() {
        String botRecord = "hello";
        String msg = "This is fun, hello how are you";
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile(".*?hel", Pattern.CASE_INSENSITIVE);
        m = p.matcher(msg);
        boolean _found = false;
        while (m.find()) {
            System.out.println(" : Text \"" + m.group() + "\" start :  " + m.start() + " end : " + m.end() + ".");
            _found = true;
        }
        assertTrue(_found);
    }

    public void testSentCompare() {
        String botRecord = "how are you doing";
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("\\s");
        String[] results = p.split(botRecord);
        BotLogger.log(results);
    }

    public void testExpression() {
        Pattern p = Pattern.compile("a*b");
        Matcher m = p.matcher("aaaaab");
        boolean b = m.matches();
        assertTrue(b);
    }

    public void testWildcard() {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("^Hello$", Pattern.CASE_INSENSITIVE);
        m = p.matcher("HeLLo");
        boolean b = m.matches();
        assertTrue(b);
    }

    public void testMultiple() {
        Pattern pattern;
        Matcher matcher;
        boolean _found = false;
        String _regex = "hell.";
        String _in = "hello mom";
        pattern = Pattern.compile(_regex);
        matcher = pattern.matcher(_in);
        while (matcher.find()) {
            System.out.println(" : Text \"" + matcher.group() + "\" start :  " + matcher.start() + " end : " + matcher.end() + ".");
            _found = true;
        }
        if (!_found) {
            System.out.println("No match found.");
        }
        assertTrue(_found);
    }

    public void testBoundMatch() {
        Pattern p = null;
        Matcher m = null;
        p = Pattern.compile("^Hello$", Pattern.CASE_INSENSITIVE);
        m = p.matcher("HeLLo");
        boolean b = m.matches();
        assertTrue(b);
    }

    public void testHello() {
        Pattern p = Pattern.compile("Hello", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("HeLLo");
        boolean b = m.matches();
        assertTrue(b);
    }
}
