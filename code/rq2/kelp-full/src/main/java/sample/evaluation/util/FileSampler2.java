package sample.evaluation.util;

import java.io.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;


public class FileSampler2 {
    public static List<File> sampleFiles(List<File> allFiles,long seed, int sampleSize) {
        Random random = new Random(seed);
        allFiles.sort(Comparator.comparing(File::getName));  // to ensure reproducable samples

        // Shuffle the list of files with the seeded random
        Collections.shuffle(allFiles, random);
        // Take a subset of the first 'sampleSize' elements
        List<File> sampledFiles = allFiles.subList(0, Math.min(sampleSize, allFiles.size()));
        return sampledFiles;
    }
    
    
    public static void generateFinalSampleTypewise(Map<String, ArrayList<String>> refClones, List<File> initialSample, List<File> finalSample, Set<String> typewiseClones){
	
	for (File file : initialSample) {
		//What is the type of this sampled file.
		if (!typewiseClones.contains(file.getName())) {
			continue;
		}
		if (refClones.containsKey(file.getName())) {
		    int numOfClones = refClones.get(file.getName()).size();
		    if (numOfClones > 0) {
			finalSample.add(file); //if a sampled file has reference clones, use it for evaluation
		    }
		    if (finalSample.size()==100) {
		    	break;
		    }
		}	   
	}	
    }
    
   public static void saveFinalSample(List<File> finalSample, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (File file : finalSample) {
                writer.write(file.getAbsolutePath() + "\n");
            } 
            //writer.write("\n-----------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
   }
   
  
   
}

