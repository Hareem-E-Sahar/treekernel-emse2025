package sample.evaluation.util;
import java.util.Arrays;
import java.io.File;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Comparator;
import java.util.Scanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.BufferedWriter;


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
    
    /*public static int getLOC1(File file, Map<String, Integer> functionLength) {
        String fileName = file.getName();
    	Integer funLength = functionLength.get(fileName);
    	
    	if (funLength!=null)
    		return funLength;
    	return 100000;
    }
    
    public static int getLOC(File file) {
        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
	// Split the remaining string by the underscore
	String[] parts = baseName.split("_");
	if (parts.length < 3 ) {
		System.out.println(Arrays.toString(parts));
	 	Scanner scanner = new Scanner(System.in);
		System.out.print("Enter your name: ");
		String name = scanner.nextLine();
	}
		
	// Extract startLine1 and endLine1
	int startLine1 = Integer.parseInt(parts[1]); // Convert to integer
	int endLine1   = Integer.parseInt(parts[2]); 
    	Integer funLength = endLine1-startLine1;
	
    	return funLength;
    }*/
    
    public static void generateFinalSample(Map<String, ArrayList<String>> refClones, List<File> initialSample, List<File> finalSample){
    	for (File file : initialSample) {
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
            } writer.write("\n-----------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int getLOC(File file) {
	    String fileName = file.getName();

	    // Remove the extension
	    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

	    // Split the string by underscores
	    String[] parts = baseName.split("_");

	    // Ensure the file has at least two parts for line numbers
	    if (parts.length < 3) {
		throw new IllegalArgumentException("Filename does not contain sufficient parts: " + fileName);
	    }

	    // Extract the last two parts
	    String startLineStr = parts[parts.length - 2];
	    String endLineStr = parts[parts.length - 1];

	    try {
		int startLine1 = Integer.parseInt(startLineStr);
		int endLine1 = Integer.parseInt(endLineStr);

		// Calculate and return the function length
		return endLine1 - startLine1;
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("Invalid line numbers in filename: " + fileName, e);
	    }
	}
	
	
}


