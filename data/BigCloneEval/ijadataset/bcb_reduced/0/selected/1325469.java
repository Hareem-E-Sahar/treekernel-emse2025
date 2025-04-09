package regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * http://www.regular-expressions.info/java.html http://www.sitepoint.com/article/java-regex-api-explained http://java.sun.com/developer/technicalArticles/releases/1.4regex/ Note: a group is given
 * with paranthesis ( ) identified by index 1,2, 3 ... 0 is the pattern itself Better go for a ready made library like regexLib http://www.cacas.org/java/gnu/regexp/#latest
 * @author Sandeep.Maloth
 */
public class RegexUtility {

    static final String FIND_MARK_CHAR = "$";

    private static Log logger = LogFactory.getLog(RegexUtility.class);

    public static void main(String[] args) {
        String pattern = "(pmc|cc|example)([a-zA-Z]*)/logon(/?)(/?)(\\?)*(.*)$";
        String input = "http://local.physmathcentral.com/pmcphysb/logon?url=%2Fjournal%2FfullText.html%26coreJournalIssn%3D1754-0429%26article_id%3D1754-0429-1-13";
        group(pattern, input);
    }

    public static void group(String pattern, String input) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(input);
        if (matcher.find()) {
            logger.debug("Group :" + matcher.group());
            for (int i = 0; i < matcher.groupCount(); i++) {
                logger.debug("group " + i + " : " + matcher.group(i + 1));
            }
        }
    }

    /**
	 * Marks the found pattern with a $ sign
	 * 
	 * @param source
	 * @param findPattern
	 */
    public static String find(String source, Object findPattern, boolean mark) {
        StringBuffer buf = new StringBuffer(source);
        Pattern p = Pattern.compile(findPattern.toString());
        Matcher m = p.matcher(buf);
        int i = 1;
        while (m.find()) {
            logger.debug("\t Found " + i++ + ". " + m.group());
            for (int j = 1; j < m.groupCount(); j++) {
                logger.debug("\t\t Group " + j + ". " + m.group(j));
            }
            if (mark) {
                buf.insert(m.start(), FIND_MARK_CHAR);
                buf.insert(m.end() + 1, FIND_MARK_CHAR);
            }
        }
        return buf.toString();
    }

    public static int locate(String source, Object findPattern) {
        StringBuffer buf = new StringBuffer(source);
        Pattern p = Pattern.compile(findPattern.toString());
        Matcher m = p.matcher(buf);
        int i = 1;
        if (m.find()) {
            logger.debug("\t Found " + i++ + ". " + m.group());
            for (int j = 1; j < m.groupCount(); j++) {
                logger.debug("\t\t Group " + j + ". " + m.group(j));
            }
            return m.start();
        }
        return -1;
    }

    public StringBuffer matchAndInsert(StringBuffer buffer, Object strPattern, String strInsert) {
        Pattern p = Pattern.compile(strPattern.toString());
        Matcher m = p.matcher(buffer);
        if (m.find()) {
            logger.debug("Inserting for pattern ::" + strPattern + " ::contents:::" + strInsert);
            buffer.insert(m.end(), strInsert);
        }
        return buffer;
    }

    public static String findAndReplace(String source, Object findPattern, String replacePattern) {
        StringBuffer sourceBuffer = new StringBuffer(source);
        Pattern p = Pattern.compile(findPattern.toString());
        Matcher m = p.matcher(sourceBuffer);
        logger.debug("Replacing for pattern ::" + findPattern + " :: with contents:::" + replacePattern);
        if (m.find()) {
            return m.replaceAll(replacePattern);
        }
        return source;
    }
}
