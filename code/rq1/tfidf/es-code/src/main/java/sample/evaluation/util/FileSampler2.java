package sample.evaluation.util;
import java.io.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Comparator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Set;
import java.util.HashSet;
import sample.evaluation.util.FileMapper;


public class FileSampler2 {
    public static List<File> sampleFiles(List<File> allFiles,long seed, int sampleSize) {
        Random random = new Random(seed);
        allFiles.sort(Comparator.comparing(File::getName));  // to ensure a consistent starting point before shuffling

        // Shuffle the list of files with the seeded random
        Collections.shuffle(allFiles, random);
        // Take a subset of the first 'sampleSize' elements
        List<File> sampledFiles = allFiles.subList(0, Math.min(sampleSize, allFiles.size()));
        return sampledFiles;
    }
    public static void saveFinalSample(List<File> finalSample, String outputFile) {
		System.out.println("Saving sampled files in..."+outputFile);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
		    for (File file : finalSample) {
		        writer.write(file.getAbsolutePath() + "\n");
		    } //writer.write("\n-----------------\n");
		} catch (IOException e) {
		    e.printStackTrace();
		}
    }
	
}

