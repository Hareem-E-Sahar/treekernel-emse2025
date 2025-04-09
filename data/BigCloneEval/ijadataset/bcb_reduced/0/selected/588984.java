package test.org.spark.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.spark.util.RegxUtils;
import test.OsgiTestCase;

public class RegxUtilsTest extends OsgiTestCase {

    public void testMatch2() {
        System.out.println(RegxUtils.match("transition1", "[1-6]"));
        System.out.println(RegxUtils.match("transition1", "[2-6]"));
        System.out.println(RegxUtils.match("transition1", ".*rans.*"));
        int[] pos = RegxUtils.find("transition1", "[1-6]");
        System.out.println(pos[0] + "," + pos[1]);
        pos = RegxUtils.find("transition1", "[2-6]");
        System.out.println(pos[0] + "," + pos[1]);
        pos = RegxUtils.find("transition1", "trans");
        System.out.println(pos[0] + "," + pos[1]);
        System.out.println(RegxUtils.startWith("transition1", "[1-6]"));
        System.out.println(RegxUtils.startWith("transition1", "[2-6]"));
        System.out.println(RegxUtils.startWith("transition1", "tr[\\S]*s"));
    }

    public void testSplit() {
        String[] temps = RegxUtils.split("aa#bb!cc^dd", "[#!^]", 2);
        if (temps == null) return;
        for (int i = 0; i < temps.length; i++) System.out.println(temps[i]);
    }

    public void testFind() {
        int[] pos = RegxUtils.find("{test1}{test2}", "\\{[\\s\\S]*?\\}");
        if (pos == null) return;
        System.out.println(pos[0] + "," + pos[1]);
    }

    public void testMatch() {
        System.out.println(RegxUtils.match("{test1}{test2}", "\\{([\\s\\S]*?)\\}"));
    }

    public void testMatch3() {
        System.out.println("Test.zip".endsWith(".zip"));
    }

    public void testMatch4() {
        System.out.println(RegxUtils.match("CM.Provisioning.Create", "CM.Provisioning.*"));
    }

    public void testMatchIpAddr() {
        Pattern ipaddrPattern = Pattern.compile("[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}");
        System.out.println(ipaddrPattern.matcher("10.10.5.12").matches());
        System.out.println(ipaddrPattern.matcher("10.q10.5.12").matches());
        System.out.println(ipaddrPattern.matcher("10,10.5.12").matches());
        System.out.println(ipaddrPattern.matcher("10.1110.5.12").matches());
        System.out.println(ipaddrPattern.matcher("10.111.5.12").matches());
        System.out.println(ipaddrPattern.matcher("10..5.12").matches());
        System.out.println(ipaddrPattern.matcher("10.5.12").matches());
    }

    public void testCapture() {
        String[] temps = RegxUtils.capture("{test1}{test2", "\\{([\\s\\S]*?)\\}[\\s\\S]*");
        if (temps == null) return;
        for (int i = 0; i < temps.length; i++) System.out.println(temps[i]);
    }

    public void testReplace() {
        String redirectUrl = "http://localhost:8080/xsmp?p2=value2&.x_ticket=abc";
        String ticketName = ".x_ticket";
        redirectUrl = redirectUrl.replaceAll("([?&])" + ticketName.replaceAll("\\.", "\\\\\\.") + "=[^&]*[&]?", "$1");
        System.out.println(redirectUrl);
    }

    public void testLucene() {
        Pattern splitQuotPattern = Pattern.compile("[\"][^\"]{0,}[\"]");
        Pattern replaceBlankPattern = Pattern.compile("(?<!(OR)|(AND)|(NOT)|(\\&\\&)|(\\|\\|)|[(\\[,\"]|(TO))[\\s]{1,}(?!(OR)|(AND)|(NOT)|(\\&\\&)|(\\|\\|)|[)\\]\"]|(TO))");
        String queryStr = "+publicAccess:\"1 2\"  NOT \"a TO b\" +(parent:/emcproot* (123, ableflag:3 ) )";
        StringBuffer buffer = new StringBuffer();
        Matcher splitQuotMatcher = splitQuotPattern.matcher(queryStr);
        int lastPos = 0;
        while (splitQuotMatcher.find()) {
            String temp = queryStr.substring(lastPos, splitQuotMatcher.start());
            temp = temp.replaceAll("\\s{1,}", " ");
            Matcher matcher = replaceBlankPattern.matcher(temp);
            buffer.append(matcher.replaceAll(" AND "));
            buffer.append(queryStr.substring(splitQuotMatcher.start(), splitQuotMatcher.end()));
            lastPos = splitQuotMatcher.end();
        }
        if (lastPos < queryStr.length() - 1) {
            String temp = queryStr.substring(lastPos);
            temp = temp.replaceAll("\\s{1,}", " ");
            Matcher matcher = replaceBlankPattern.matcher(temp);
            buffer.append(matcher.replaceAll(" AND "));
        }
        System.out.println(buffer);
    }
}
