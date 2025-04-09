package sample.evaluation.util;
import java.util.Arrays;
import java.io.File;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Collections;
import java.util.Random;
import java.util.Comparator;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.BufferedWriter;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;


public class Sampler {
	
	public static List<File> sampleForRQ3( Map<String, ArrayList<String>> refClones, ArrayList<File> sexprFiles, String complexityFile, String evaluationType, String funComplexity, String funSize, long seed) {
   		List<File> initialSample = new ArrayList<File>();
		List<File> finalSample = new ArrayList<>();
			
		
		Map<String, Integer> complexityMap = ComplexityUtils.populateComplexityMap(complexityFile);

    		int sampleSize = 1000;
    		
		if(evaluationType.equals("size_eval")) {
			// Filter files into two disjoint lists
			List<File> lessThan6LOC = sexprFiles.stream()
				.filter(file -> FileSampler2.getLOC(file) < 6)
				.collect(Collectors.toList());
				
			List<File> moreThan10LOC = sexprFiles.stream()
				.filter(file -> FileSampler2.getLOC(file) > 10)
				.collect(Collectors.toList());
			
			// Sample from each list and run experiment once
			List<File> sampleMoreThan10LOC = FileSampler2.sampleFiles(moreThan10LOC, seed, sampleSize);
			List<File> sampleLessThan6LOC = FileSampler2.sampleFiles(lessThan6LOC, seed, sampleSize);
			
			if (funSize.equals("More")) {
				initialSample = sampleMoreThan10LOC;
			}
			if (funSize.equals("Less")) {
				initialSample = sampleLessThan6LOC;
			}			
			
    		}
    		
    		if(evaluationType.equals("complexity_eval")) {
    			System.out.println("ComplexityMap:"+complexityMap.size());
						
			//Separate data into two disjoint lists
			List<File> lessThan10Complexity = sexprFiles.stream()
				.filter(file -> ComplexityUtils.getComplexity(file,complexityMap) <= 10)
				.collect(Collectors.toList());
			System.out.println("lessThan10Complexity:"+lessThan10Complexity.size());
				
			List<File> moreThan10Complexity = sexprFiles.stream()
				.filter(file -> ComplexityUtils.getComplexity(file,complexityMap) > 10)
				.collect(Collectors.toList());
			System.out.println("moreThan10Complexity:"+moreThan10Complexity.size());
		
			// Sample from each list and run experiment once
			List<File> sampleLessThan10Complexity = FileSampler2.sampleFiles(lessThan10Complexity, seed, sampleSize);
			List<File> sampleMoreThan10Complexity = FileSampler2.sampleFiles(moreThan10Complexity, seed, sampleSize);
			if (funComplexity.equals("High")) {
				initialSample = sampleMoreThan10Complexity;
				System.out.println("----High----");
			}	
			if (funComplexity.equals("Low")) {
				initialSample = sampleLessThan10Complexity;	
				System.out.println("----Low----");	
			}	
		}
		System.out.println("Initial sample size:"+initialSample.size());
		assertNotEquals(0,initialSample.size());
		FileSampler2.generateFinalSample(refClones, initialSample, finalSample);
		
		
	        //finalSample.add(new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/2109789_336_360.java")); 
	       //finalSample.add(new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/101041_17_53.java"));
		
		
	    return finalSample;
	}
}

