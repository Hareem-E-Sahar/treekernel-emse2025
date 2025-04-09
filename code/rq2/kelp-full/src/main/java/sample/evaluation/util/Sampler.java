package sample.evaluation.util;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;


public class Sampler {

	public static List<File> sampleForRQ2(Map<String, ArrayList<String>> refClones, Set<String> typewiseClones, List<File> sexprFiles, long seed) {
	    	
	    	List<File> initialSample = new ArrayList<File>();
	    	List<File> finalSample   = new ArrayList<File>();
	    	try {
			int sampleSize = 10000;
			initialSample = FileSampler2.sampleFiles(sexprFiles,seed,sampleSize);
			System.out.println("Initial sample size:"+ initialSample.size());
			assertNotEquals(initialSample.size(), 0);
	    	
			FileSampler2.generateFinalSampleTypewise(refClones, initialSample, finalSample, typewiseClones);
			System.out.println("Final sample size:"+finalSample.size());
			System.out.println(finalSample);
			
			
			//finalSample.add(new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/2109789_336_360.java")); 
			//finalSample.add(new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/101041_17_53.java"));
			//assertEquals(100,finalSample.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalSample;
    	}
    	
}

