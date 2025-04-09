package sample.evaluation.kelp;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import sample.evaluation.util.FileSampler2;
import sample.evaluation.util.MAPCalculator;
import sample.evaluation.util.MRRCalculator;
import sample.evaluation.util.PrecisionCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.Util;
import sample.evaluation.util.Sampler;

public class TKBigCloneEval {
	 public Map<String, Map<String, Float>> evaluate(List<File> sexprFiles, List<File> sampledFiles) {
		TKSimilarity tkSimilarity = new TKSimilarity();
		tkSimilarity.findClones(sexprFiles,sampledFiles);
		Map<String, Map<String, Float>> similarityScores = tkSimilarity.getSimMap().getSimilarityScores();    	    
	   	return similarityScores; 
	 }
	 
	 
	 //https://github.com/SAG-KeLP/kelp-full/issues/11
	 public static void evaluateOnSeed(long seed) {
	 	System.out.println("Seed:"+seed);
		int functionality = 0;  
 		float threshold   = 0.3f;	
 		String rqNum  = "RQ1";
 		String kernel = "PTK";
 		String technique = "kelp_evaluation_random_sample";

	    	Path mainDirPath = Paths.get(System.getProperty("user.home"), "treekernel-emse2025");
	    	Path dataDirPath = Paths.get(System.getProperty("user.home"), "treekernel-emse2025", "data", "BigCloneEval","ijadataset");
	    	Path BCDirPath   = Paths.get(System.getProperty("user.home"),"treekernel-emse2025", "data","TestH2Database");
	    	String refFile = BCDirPath.resolve("bigclonedb_clones_alldir_8584153.txt").toString();
	    	Path resultDirPath = mainDirPath.resolve("results").resolve(rqNum);
	    	String codesDir = dataDirPath.resolve("bcb_reduced")
	    				     .resolve(String.valueOf(functionality))
	    				     .toString();
	    				 
	    	String sexprDir = dataDirPath.resolve("sexpr_from_gumtree")
	    				     .resolve(String.valueOf(functionality))
	    				     .toString();

		String mainDir   = mainDirPath.toString();
	    	String dataDir   = dataDirPath.toString();
	      	
	  	String similarityFile = resultDirPath.resolve("kelp_similarity_random_sample.csv").toString();
    		String metricsFile    = resultDirPath.resolve("metrics.csv").toString(); 
    	
    		System.out.println(codesDir);
	    	System.out.println(sexprDir);
	    	System.out.println(mainDir);
	    	System.out.println(dataDir);
	    	System.out.println(refFile); 	
	    	System.out.println(metricsFile); 
	    	System.out.println(similarityFile); 
	    		
	    	
    		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
	    	try {
		 	refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
			
	    	ArrayList<File> sexprFiles = new ArrayList<>(Arrays.asList(new File(sexprDir).listFiles()));
	    	assertNotEquals(0,sexprFiles.size());
	    	List<File> finalSample  = Sampler.sampleForRQ1(refClones, sexprFiles, seed);
	    	Map<String, ArrayList<String>> sampledClones  = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:"+sampledClones.size());
		   
	    	long startTime = System.currentTimeMillis();
		TKBigCloneEval tkeval = new TKBigCloneEval();
		Map<String, Map<String, Float>> similarityScores = tkeval.evaluate(sexprFiles,finalSample);
		
		//Util.saveSimilarityScores(similarityScores, similarityFile);
	    	//Util.printSimilarityScores(similarityScores);
	    	long endTime = System.currentTimeMillis();
	    	double totalTime = (endTime - startTime)/1000; // millisecs to second
		
	    	System.out.println("Old Sim scores size :"+similarityScores.size()); 
	    	System.out.println("Total time:"+totalTime); 
	    	System.out.println("=".repeat(90));
	    	System.out.println("Calculating metrics now!");
		PrecisionCalculator calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold);
		System.out.println("\nPrecision 5");
		float prec5  = calculator2.calculateMeanPrecision(5);
	    	System.out.println("=".repeat(90));
		System.out.println("\n\nPrecision 10");
		float prec10 = calculator2.calculateMeanPrecision(10);
		System.out.println("=".repeat(90));
		System.out.println("\n\nMRR");
		MRRCalculator calculator =  new MRRCalculator(similarityScores,sampledClones,threshold);
		float MRR = calculator.calculateOverallMRR();	
		System.out.println("=".repeat(90));
		System.out.println("\n\nMAP");
		MAPCalculator calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold);
		float MAP = calculator3.calculateOverallMAP();
		
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
	  
	}
	
	public static void main(String[] args) {
		long [] all_seeds= {6251};//,9080,8241,8828,55,2084,1375,2802,3501,3389}; //from Util.generate_seed();
		for (int i=0; i<all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]); 
 		
	}
}
