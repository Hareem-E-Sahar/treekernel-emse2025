package sample.evaluation.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Util {

    public static void countBrackets(StringBuilder data, int countOpen, int countClose) {
	for (int i = 0; i < data.length(); i++) {
	    char ch = data.charAt(i);
	    if (ch == '(') {
		countOpen++;
	    } else if (ch == ')') {
		countClose++;
	    }
	}
	
    }
    
    public static String read_sexpr_as_string(String fname)throws Exception {
        File currentFile = new File(fname);
        if (!currentFile.exists()) {
            throw new FileNotFoundException("File does not exist!");
        }

        StringBuilder data = new StringBuilder();
        try (Scanner scanner = new Scanner(currentFile)) {
            scanner.useDelimiter("\\A");  // Read the entire file as a single string
            if (scanner.hasNext()) {
                data.append(scanner.next());
            }
        }
        
        // Check for balanced parentheses
        int countOpen  = StringUtils.countMatches(data,"(");
        int countClose = StringUtils.countMatches(data,")");
        countBrackets( data,  countOpen, countClose);
        if (countOpen != countClose) {
        	System.out.println(fname+" has unequal brackets");
            throw new IllegalArgumentException("Unbalanced parentheses in the file.");
        }
        
        // Replace all '#' with '@'
        String modifiedData = data.toString().replace('#', '@');
        modifiedData = modifiedData.replace("StringLiteral:<STR>", "");
        modifiedData = modifiedData.replaceAll("\\s+", " "); // Replaces one or more whitespace characters with a single space
        return modifiedData;
    }

    public static void printSimilarityScores(Map<String, Map<String, Float>> similarityScores) {
        for (Map.Entry<String, Map<String, Float>> entry : similarityScores.entrySet()) {
            String file1 = entry.getKey();
            Map<String, Float> scores = entry.getValue();
            System.out.println("Similarity scores for " + file1 + ":");
            for (Map.Entry<String, Float> scoreEntry : scores.entrySet()) {
                System.out.println("   " + file1 + " to " + scoreEntry.getKey() + " = " + scoreEntry.getValue());
            }
        }
    }
    
    public static void appendResultToFile(float prec5, float prec10, float mrr, float map, int func, double time, String filename,long seed, String technique) {
        try (FileWriter fw = new FileWriter(filename, true);
            PrintWriter pw = new PrintWriter(fw)) {
       	    pw.print(prec5);
       	    pw.print(",");
       	    pw.print(prec10);
       	    pw.print(",");
	       	pw.print(mrr);
	       	pw.print(",");
	       	pw.print(map);
	       	pw.print(",");
	       	pw.print(technique);
	    	pw.print(",");
	       	pw.print(func);
	       	pw.print(",");
	       	pw.print(time);
	       	pw.print(",");
	       	pw.print(seed);
       	    pw.println();

       	} catch (IOException e) {
       	    e.printStackTrace();
       	}
    }

    public static void printSortedSimilarityScores(Map<String, Map<String, Float>> similarityScores) {	     
    	Set<String> uniqueKeys = new HashSet<>();
    	for (Map.Entry<String, Map<String, Float>> entry : similarityScores.entrySet()) {
            String file1 = entry.getKey();
            uniqueKeys.add(file1);
            Map<String, Float> scores = entry.getValue();
            
            // Sorting the inner map by values (similarity scores) in descending order
            List<Map.Entry<String, Float>> sortedScores = new ArrayList<>(scores.entrySet());
            sortedScores.sort(Map.Entry.<String, Float>comparingByValue().reversed());
          
            //System.out.println("Sorted similarity scores for " + file1 + ":");
            /*for (Map.Entry<String, Float> scoreEntry : sortedScores) {
            	System.out.println("    " + file1 + " to " + scoreEntry.getKey() + " = " + scoreEntry.getValue());   
            }*/ 
        }
    	System.out.println("Unique Keys:"+uniqueKeys.size());
    }
    
    public static void generate_seed() {
    	Random random = new Random(12345);
	    System.out.println("Randomly generated numbers:");
	    
	    // Generate and print 10 random numbers
	    for (int i = 0; i < 10; i++) {
	        int randomNumber = random.nextInt(10000); // Generates a number between 0 and 10000
	        System.out.println(randomNumber);
	    }
    }
    public static void saveSimilarityScores(Map<String, Map<String, Float>> similarityScores, String filename) {
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {  
	        for (Map.Entry<String, Map<String, Float>> entry : similarityScores.entrySet()) {
	            String file1 = entry.getKey();
	            Map<String, Float> scores = entry.getValue();
	            // Sorting the inner map by values (similarity scores) in descending order
	            List<Map.Entry<String, Float>> sortedScores = new ArrayList<>(scores.entrySet());
	            sortedScores.sort(Map.Entry.<String, Float>comparingByValue().reversed());
	            for (Map.Entry<String, Float> scoreEntry : sortedScores) {
	                String line = file1 + "," + scoreEntry.getKey() + "," + scoreEntry.getValue() + "\n";
	                writer.write(line);
	            }
	        }
    	} catch (IOException e) {
			e.printStackTrace();
	}
    }

}
