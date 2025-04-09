package sample.evaluation.util;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.ArrayList;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;


public class Sampler {

	public static List<File> sampleForRQ1(Map<String, ArrayList<String>> refClones, List<File> sexprFiles, long seed) {
	    	
	    	List<File> initialSample = new ArrayList<File>();
	    	List<File> finalSample   = new ArrayList<File>();
	    	try {
			int sampleSize = 400;
			initialSample = FileSampler2.sampleFiles(sexprFiles,seed,sampleSize);
			System.out.println("Initial sample size:"+initialSample.size());
			assertNotEquals(initialSample.size(), 0);
	    	
			for (File file : initialSample) {
				if (refClones.containsKey(file.getName())) {
					int numOfClones = refClones.get(file.getName()).size();
					if (numOfClones > 0) {
						finalSample.add(file); //if a sampled file has refclones, use it for
								       // evaluation
					}
					if (finalSample.size()==100) {
						break;
					}
				}	   
			}
			System.out.println("Final sample size:"+finalSample.size());
			
			finalSample.add(new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/2109789_336_360.java")); 
			//finalSample.add(new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/101041_17_53.java"));
			//assertEquals(finalSample.size(), 100);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalSample;
    	} 	
}

