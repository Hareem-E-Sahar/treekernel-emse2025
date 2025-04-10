package sample.evaluation.util;


import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.assertNotEquals;
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



    public static List<File> sampleEvaluationFiles( List<File> initialSample, ArrayList<File> sexprFiles, List<File> funStrFiles, Map<String, ArrayList<String>> refClones){
	    List<File> finalSample = new ArrayList<File>();	
	    try {
		    	
		    	Map<File, File> mapping = FileMapper.getMapping(initialSample,funStrFiles);
			for (File file : initialSample) {
				if (refClones.containsKey(file.getName())) {
					int numOfClones = refClones.get(file.getName()).size();
					if (numOfClones > 0) {
						File file1 = FileMapper.getFileByName(mapping,file.getName());
						finalSample.add(file1); //if a sampled file has reference clones, use it for evaluation
					}
					if (finalSample.size()==100)
						break;
				}	   
			}
				
			//finalSample.add(new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/functionStr/0/selected/2109789_336_360.java")); 
			//finalSample.add(new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/functionStr/0/default/101041_17_53.java"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalSample;
	}
	
	
	
    //for RQ2
    public static void generateFinalSampleTypewise(Map<String, ArrayList<String>> refClones, List<File> initialSample, List<File> finalSample, Set<String> typewiseClones, List<File> funStrFiles){
	
	Map<File, File> mapping = FileMapper.getMapping(initialSample,funStrFiles);
			
	for (File file : initialSample) {
		//What is the type of this sampled file.
		if (!typewiseClones.contains(file.getName())) {
			continue;
		}
		if (refClones.containsKey(file.getName())) {
		    int numOfClones = refClones.get(file.getName()).size();
		    if (numOfClones > 0) {
		    	File file1 = FileMapper.getFileByName(mapping,file.getName()); //super important. Without this the code will run but score is low. its using sexprs instead of raw code
			finalSample.add(file1); //if a sampled file has reference clones, use it for evaluation
		    }
		    if (finalSample.size()==100) {
		    	break;
		    }
		}	   
	}	
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


