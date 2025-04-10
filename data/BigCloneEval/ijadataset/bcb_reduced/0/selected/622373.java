package cn.edu.wuse.musicxml.demo;

import java.awt.Button;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DespitorTest {

    public static void main(String[] args) {
        try {
            Pattern p;
            String ss = File.separator.equals("\\") ? "\\" : "/";
            String ps = "^(file://)?[\\\\w]+";
            System.out.println(ps);
            p = Pattern.compile(ps, Pattern.CASE_INSENSITIVE);
            String s = "file://\\gagad\\baba\\bag";
            Matcher m = p.matcher(s);
            System.out.println(m.matches());
            while (m.find()) {
                System.out.println(m.start() + "-" + m.end() + ":" + m.group());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
