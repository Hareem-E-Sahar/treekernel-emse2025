package com.qaessentials.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will have the ability to parse text files and represent the data in a 
 * specified way.  This will allow for graphing or more easier manipulation of the data.
 * 
 * @author rwall
 *
 */
public class DataParser {

    public void outStream() {
    }

    public void inStream() {
    }

    public void findBlock() {
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) throws Exception {
        FileOutputStream output = new FileOutputStream("/tmp/output_iostat.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/tmp/rwall-desktop_iostat.txt")));
        String line = null;
        String buff = "";
        String start = "avg-cpu:";
        String end = "avg-cpu:";
        Pattern startPattern = Pattern.compile(start + ".*" + end);
        System.out.println("PATTERN : " + startPattern.pattern());
        Matcher startMatcher = null;
        while ((line = br.readLine()) != null) {
            buff = buff + line;
            startMatcher = startPattern.matcher(buff);
            while (startMatcher.find()) {
                String tmp = buff.substring(startMatcher.start(), startMatcher.end());
                Pattern tmpPattern = Pattern.compile(end);
                Matcher tmpMatcher = tmpMatcher = tmpPattern.matcher(tmp);
                ;
                return;
            }
        }
    }
}
