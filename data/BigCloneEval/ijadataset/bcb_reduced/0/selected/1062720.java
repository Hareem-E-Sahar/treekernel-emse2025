package web.sopo.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import web.sopo.page.Page;
import web.sopo.util.FileUtils;

public class PageTemplateTest extends TestCase {

    public void testParse() {
        Page page = new Page() {
        };
        new PageTemplate(page).parse(FileUtils.readPackageResource("web/sopo/template/Index.html"));
    }

    public void testPattern() {
        String input = FileUtils.readPackageResource("web/sopo/template/Index.html");
        input = input.replaceAll("\\n", "");
        Pattern pattern = Pattern.compile("<head>.*</head>");
        Matcher matcher = pattern.matcher(input);
        int i = 0;
        while (matcher.find()) {
            i++;
            int start = matcher.start();
            int end = matcher.end();
            String match = input.substring(start, end);
        }
        assertTrue(1 == i);
    }
}
