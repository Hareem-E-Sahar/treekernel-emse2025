package com.yerihyo.program.periodicwebrequester;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.yerihyo.yeritools.net.HttpToolkit;

public class PeriodicWebRequester {

    public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
        test01();
    }

    protected static void test01() throws InterruptedException, MalformedURLException, IOException {
        CharSequence content = HttpToolkit.getHTMLString(new URL("http://java.sun.com/javase/6/docs/api/allclasses-frame.html"));
        Pattern pattern = Pattern.compile("A HREF=\"[\\p{Alpha}\\p{Punct}]+\"");
        Matcher matcher = pattern.matcher(content);
        List<CharSequence> urlStringList = new ArrayList<CharSequence>();
        while (matcher.find()) {
            int startIndex = matcher.start();
            int endIndex = matcher.end();
            urlStringList.add(content.subSequence(startIndex + 8, endIndex - 1));
        }
        test01_1(urlStringList);
    }

    private static Random r = new Random();

    private static int oneMinute = 60 * 1000;

    private static String header = "http://java.sun.com/javase/6/docs/api/";

    protected static void test01_1(List<CharSequence> list) throws InterruptedException, MalformedURLException, IOException {
        int next = 0;
        int size = list.size();
        while (true) {
            int nextPageIndex = r.nextInt(size);
            String urlString = header + list.get(nextPageIndex).toString();
            Reader reader = HttpToolkit.createHTMLReader(new URL(urlString));
            System.out.println(urlString);
            next = r.nextInt(oneMinute * 2);
            Thread.sleep(oneMinute / 2 + next);
            int stopInt = r.nextInt(10);
            if (stopInt == 9) {
                Thread.sleep(oneMinute * 5);
            }
            reader.close();
        }
    }
}
