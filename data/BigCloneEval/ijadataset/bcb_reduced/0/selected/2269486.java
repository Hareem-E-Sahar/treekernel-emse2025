package faqparser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lionel FLAHAUT
 * 
 */
public class RegExpParser {

    private Pattern faqPattern;

    public RegExpParser() {
        faqPattern = Pattern.compile("(.*<span class=\"highlight\">.*</span>.*)");
    }

    public List parse(StringBuffer buffer) {
        Matcher results = faqPattern.matcher(buffer);
        while (results.find()) {
            System.err.println(results.groupCount() + " groups found");
            for (int i = 0; i < results.groupCount(); i++) System.err.println("group " + i + "\n" + results.group(i));
        }
        if (results.matches()) {
            System.err.println(results.group());
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        long max = 0, mean = 0, min = Long.MAX_VALUE;
        for (int i = 0; i < 1; i++) {
            File f = new File("exemple2.txt");
            FileReader fr = new FileReader(f);
            int nbread = -1;
            char bytes[] = new char[1024];
            StringBuffer buf = new StringBuffer();
            while ((nbread = fr.read(bytes)) >= 0) {
                buf.append(bytes, 0, nbread);
            }
            fr.close();
            long start = System.currentTimeMillis();
            new RegExpParser().parse(buf);
            long stop = System.currentTimeMillis();
            long value = stop - start;
            if (value > max) {
                max = value;
            }
            if (value < min) {
                min = value;
            }
            mean = (max + min) / 2;
        }
        System.out.println("Max : " + max + " ms");
        System.out.println("Mean : " + mean + " ms");
        System.out.println("Min : " + min + " ms");
    }
}
