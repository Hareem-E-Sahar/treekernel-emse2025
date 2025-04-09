package sample.evaluation.util;

import java.io.*;
import java.util.*;

public class ComplexityUtils {

    // Reads CSV and returns a map of (fileName -> complexity)
    public static Map<String, Integer> populateComplexityMap(String csvFile) {
        Map<String, Integer> complexityMap = new HashMap<>();
        String line;
        String delimiter = ","; // CSV delimiter

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
     	    // Skip the first line (header)
	    br.readLine(); 

            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);
                if (values.length == 3) { 
                    String fileName = values[0].trim();
                    String complexityStr = values[1].trim();
                    

                    try {
                        int complexity = Integer.parseInt(complexityStr);
                        complexityMap.put(fileName, complexity);
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid complexity value: " + complexityStr);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	     
        return complexityMap;
    }

    public static int getComplexity(File file , Map<String, Integer> complexityMap) {
    	int LOC = FileSampler2.getLOC(file);
    	String filename = file.getName();
        return complexityMap.getOrDefault(filename, LOC); 
    }

    public static void main(String[] args) {
        String csvFile =  "/home/hareem/UofA2023/Tree_Kernel2024/clone_detection/RQ3-Complexity/complexity/0/checkstyle_complexity_default.csv";

        Map<String, Integer> complexityMap = populateComplexityMap(csvFile);
        System.out.println("Complexity map size: " + complexityMap.size());
        
        File f1 = new File("/home/hareem/UofA2023/Tree_Kernel2024/clone_detection/RQ3-Complexity/complexity/0/default/65335_24_26.java");
        int val = getComplexity(f1, complexityMap);
        
        System.out.println("Complexity of 65335_24_26.java:"  + (val == -1 ? "Not Found" : val));
        
        File f2 = new File("/home/hareem/UofA2023/Tree_Kernel2024/clone_detection/RQ3-Complexity/complexity/0/default/36036_124_456.java");
        val = getComplexity(f2, complexityMap);
        
        System.out.println("Complexity of 36036_124_456.java:" + (val == -1 ? "Not Found" : val));
        
       
    }
}

