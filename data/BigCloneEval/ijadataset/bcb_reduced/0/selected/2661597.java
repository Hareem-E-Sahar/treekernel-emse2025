package com.mlib.algorithm.massive.mapreduce;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * 
 * ClassName GenerateURL.java Version 1.0
 * 
 * @author fhd
 * @Date 2010-12-21
 */
public class GenerateURL {

    /**
	 * @param args
	 * @throws IOException
	 */
    public static void main(String[] args) throws IOException {
        File dir = new File("e:/algorithm/url");
        int FILE_COUNT = 100;
        int FILE_SIZE = 10000000;
        int MAX_URL_LENGTH = 7;
        String[] words = { "word1", "word2", "word3", "word4", "word5", "word6", "word7" };
        Random rnd = new Random();
        for (int fileIndex = 0; fileIndex < FILE_COUNT; fileIndex++) {
            File file = new File(dir, "url_" + fileIndex + ".data");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileOutputStream(file));
            for (int i = 0; i < FILE_SIZE; i++) {
                StringBuilder sb = new StringBuilder();
                int urlLen = rnd.nextInt(MAX_URL_LENGTH + 1);
                for (int len = 0; len < urlLen; len++) {
                    sb.append(words[rnd.nextInt(words.length)]);
                }
                writer.println(sb.toString());
            }
            writer.close();
            System.out.println(file.getName() + " has been written .");
        }
    }
}
