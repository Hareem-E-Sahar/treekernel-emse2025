package br.net.woodstock.rockframework.core.test.text;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

public class TextFinder extends TestCase {

    private static final String PATTERN_1 = "(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?([Uu][Mm][Ll])(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?(\\w*[\\s\\n\\.,])?[\\.\\s\\n]?.*?";

    private static final String PATTERN_2 = "\\b([Uu][Mm][Ll])\\b";

    private File[] getFiles() {
        File file = new File("C:/Temp/Test_documento");
        return file.listFiles();
    }

    private String readFile(final File file) throws IOException {
        System.out.println(file);
        Scanner scanner = new Scanner(file);
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNextLine()) {
            builder.append(scanner.nextLine());
            builder.append("\n");
        }
        return builder.toString();
    }

    public void xtest1() throws Exception {
        Pattern pattern1 = Pattern.compile(TextFinder.PATTERN_1);
        Pattern pattern2 = Pattern.compile(TextFinder.PATTERN_2);
        File[] files = this.getFiles();
        for (File file : files) {
            String text = this.readFile(file);
            System.out.println(text);
            Matcher matcher1 = pattern1.matcher(text);
            int index = 0;
            while (matcher1.find()) {
                String group = matcher1.group().trim();
                System.out.println((index++) + " - " + group);
                Matcher matcher2 = pattern2.matcher(group);
                while (matcher2.find()) {
                    System.out.println(matcher2.start() + " - " + matcher2.end());
                }
            }
        }
    }

    public void test2() throws Exception {
        String text = "Ola, Lourival, bem vindo";
        System.out.println(text.replaceAll("([Ll][Oo][Uu][Rr][Ii][Vv][Aa][Ll])", "<b>LOURIVAL</b>"));
    }
}
