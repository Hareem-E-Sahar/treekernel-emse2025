package org.javanuke.tests.view;

import junit.framework.TestCase;
import org.javanuke.core.decorators.CodeDecorator;
import org.javanuke.core.util.StringUtils;
import studio.beansoft.syntax.ModeLoader;
import studio.beansoft.syntax.sample.HtmlOutputTokenHandler;

public class CodeDecoratorTest extends TestCase {

    /**
	 * Use lang='java' instead of lang="java"
	 *
	 */
    public void testDecorateWithSingleQuotes() {
        CodeDecorator decorator = new CodeDecorator();
        StringBuffer sb = new StringBuffer();
        sb.append("Any text before java content");
        sb.append(CodeDecorator.CODE_START_TAG + " lang='java'>");
        sb.append("package org.javanuke.core.radeox;");
        sb.append("public class JavaDecorator implements ContentDecorator {}");
        sb.append(CodeDecorator.CODE_END_TAG);
        String content = decorator.decorate(sb.toString());
        assertNotNull(content);
    }

    public void testDecorate() {
        CodeDecorator decorator = new CodeDecorator();
        StringBuffer sb = new StringBuffer();
        sb.append("Any text before java content");
        sb.append(CodeDecorator.CODE_START_TAG + " lang=\"java\">");
        sb.append("package org.javanuke.core.radeox;");
        sb.append("import java.util.regex.Matcher;");
        sb.append("import java.util.regex.Pattern;");
        sb.append("import de.java2html.Java2Html;");
        sb.append("public class JavaDecorator implements ContentDecorator {");
        sb.append("private static final String JAVA_START_TAG = \"<java>\";");
        sb.append("private static final String JAVA_END_TAG = \"</java>\";");
        sb.append("public String decorate(String content) {");
        sb.append("content = java2html(content);");
        sb.append("return content;");
        sb.append("}		 ");
        sb.append("public String java2html(String content){");
        sb.append("//		      is there work to do?");
        sb.append("if (content == null || content.length() == 0) {");
        sb.append("/* this pattern says \"take the shortest match you can find where there are\"");
        sb.append("one or more characters between java tags");
        sb.append("- the match is case insensitive and DOTALL means that newlines are");
        sb.append("- considered as a character match*/");
        sb.append("Pattern p = Pattern.compile(JAVA_START_TAG + \".+?\" + JAVA_END_TAG,");
        sb.append("Pattern.CASE_INSENSITIVE | Pattern.DOTALL);");
        sb.append("Matcher m = p.matcher(content);");
        sb.append("// while there are blocks to be escaped");
        sb.append("while (m.find()) {");
        sb.append("int start = m.start();");
        sb.append("int end = m.end();");
        sb.append("//grab the text, strip off the escape tags and transform it");
        sb.append("String textToWikify = content.substring(start, end);");
        sb.append("textToWikify = textToWikify.substring(JAVA_START_TAG.length(), textToWikify.length() - JAVA_END_TAG.length()");
        sb.append("textToWikify = Java2Html.convertToHtml(textToWikify);");
        sb.append("// now add it back into the original text");
        sb.append("content = content.substring(0, start) + textToWikify + content.substring(end, content.length());");
        sb.append("m = p.matcher(content);");
        sb.append("}		        ");
        sb.append("return content;");
        sb.append("}");
        sb.append("}");
        sb.append(CodeDecorator.CODE_END_TAG);
        sb.append("Any text after java content");
        String content = decorator.decorate(sb.toString());
        assertNotNull(content);
    }

    public void testModeLoader() {
        String path = StringUtils.getRealFilePath("/modes/catalog");
        ModeLoader.loadModeCatalog(path, false);
        HtmlOutputTokenHandler tokenHandler = new HtmlOutputTokenHandler();
        ModeLoader.parseTokens("public class Test() {}", "java", tokenHandler);
        System.out.println(tokenHandler.getOutputText());
    }
}
