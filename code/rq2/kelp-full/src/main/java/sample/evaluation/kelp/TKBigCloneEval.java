//On 18th March I changed this code to use the typewise file for the ground truth instead of using all those 8.5M clones.
package sample.evaluation.kelp;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import sample.evaluation.util.FileSampler2;
import sample.evaluation.util.MAPCalculator;
import sample.evaluation.util.MRRCalculator;
import sample.evaluation.util.PrecisionCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.Util;
import sample.evaluation.util.TypeClones;
import sample.evaluation.util.Sampler;

public class TKBigCloneEval {
	 public Map<String, Map<String, Float>> evaluate(List<File> sexprFiles, List<File> sampledFiles, String kernel) {
		TKSimilarity tkSimilarity = new TKSimilarity(kernel);
		tkSimilarity.findClones(sexprFiles,sampledFiles);
		Map<String, Map<String, Float>> similarityScores = tkSimilarity.getSimMap().getSimilarityScores();  
	        return similarityScores; 
	 }
	 
	 
	 //https://github.com/SAG-KeLP/kelp-full/issues/11
	 public static void evaluateOnSeed(long seed) {
	 	System.out.println("Seed:"+seed);
		int functionality = 0;  
 		float threshold   = 0.3f;	
	  	String cloneType  = "T1";
	  	String kernel     = "PTK";
	  	String rqNum      = "RQ2";
	  	String technique  = "kelp_evaluation_random_sample";
	  	
	    	Path mainDirPath = Paths.get(System.getProperty("user.home"), "treekernel-emse2025");
	    	Path dataDirPath = Paths.get(System.getProperty("user.home"), "treekernel-emse2025", "data", "BigCloneEval","ijadataset");
	    	Path resultDirPath = mainDirPath.resolve("results").resolve(rqNum);
	    	
	    	Path BCDirPath   = Paths.get(System.getProperty("user.home"),"treekernel-emse2025", "data","TestH2Database");
	    	Path newBCDirPath= Paths.get(System.getProperty("user.home"),"treekernel-emse2025","data","BigCloneEval", "bigclone_groundtruth_v3");
	    	
	    	String refFile = BCDirPath.resolve("bigclonedb_clones_alldir_8584153.txt").toString(); //it has selected cols
	    	String typewiseFile = newBCDirPath.resolve(cloneType+"-clones-selected-columns.txt").toString();
 		
 		if (cloneType.equals("T1")) {
			refFile   = newBCDirPath.resolve("T1-clones-selected-columns-no-header.txt").toString(); 
		} else if (cloneType.equals("T2")) {
			refFile   = newBCDirPath.resolve("T2-clones-selected-columns-no-header.txt").toString();	
		} else if (cloneType.equals("T3")) {
			refFile = newBCDirPath.resolve("ST3-VST3-clones-simtoken-selected-columns-no-header.txt").toString();
		} 
	    	
	    	String codesDir = dataDirPath.resolve("bcb_reduced").resolve(String.valueOf(functionality)).toString()+"/";
	    	String sexprDir = dataDirPath.resolve("sexpr_from_gumtree/").resolve(String.valueOf(functionality)).toString()+"/";
		
		
	    
    		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
    		Set<String> typewiseClones = new HashSet<String>(); 
	    	try {
		 	refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
		 	typewiseClones = TypeClones.getTypeWiseFiles(typewiseFile,codesDir);//should have used this for ground truth 
	    	} catch (Exception e) {
			e.printStackTrace();
	    	}
	    	//System.out.println("Ref clones map size:"+refClones.size());
	    	File folder = new File(sexprDir);
	    	ArrayList<File> sexprFiles = new ArrayList<>(Arrays.asList(folder.listFiles()));
	    	assertNotEquals(0,sexprFiles.size());
		    	
	    	List<File> finalSample = Sampler.sampleForRQ2(refClones, typewiseClones, sexprFiles, seed);
		String outputFile = resultDirPath.resolve("final_sample_"+rqNum+"_"+cloneType+"_seed_"+seed+".txt").toString();
		FileSampler2.saveFinalSample(finalSample, outputFile);
			
    		Map<String, ArrayList<String>> sampledClones  = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:"+sampledClones.size());
		   
	    	long startTime = System.currentTimeMillis();
		TKBigCloneEval tkeval = new TKBigCloneEval();
		Map<String, Map<String, Float>> similarityScores = tkeval.evaluate(sexprFiles,finalSample,kernel);
		//Util.saveSimilarityScores(similarityScores, similarityFile);
	    	//Util.printSimilarityScores(similarityScores);
	    	long endTime = System.currentTimeMillis();
	    	double totalTime = (endTime - startTime)/1000; // millisecs to second
	
	    	System.out.println("Sim scores size :"+similarityScores.size()); 
	    	System.out.println("Total time:"+totalTime); 
	    	System.out.println("=".repeat(90));
	    	
	    	System.out.println("Calculating metrics now!");
		String filter = "topk";
		String metricsFile = resultDirPath.resolve("metrics_"+rqNum+"_"+cloneType+"_"+kernel+"_"+filter+".csv").toString();
	   
		PrecisionCalculator calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filter);
		System.out.println("\nPrecision 5");
		float prec5  = calculator2.calculateMeanPrecision(5);
		System.out.println("\n\nPrecision 10");
		float prec10 = calculator2.calculateMeanPrecision(10);
		System.out.println("\n\nMRR");
		MRRCalculator calculator =  new MRRCalculator(similarityScores,sampledClones,threshold,filter);
		float MRR = calculator.calculateOverallMRR();	
		System.out.println("\n\nMAP");
		MAPCalculator calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filter);
		float MAP = calculator3.calculateOverallMAP();
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
		
		
		System.out.println("Calculating metrics now!");
		filter = "topk_threshold";
		metricsFile = resultDirPath.resolve("metrics_"+rqNum+"_"+cloneType+"_"+kernel+"_"+filter+".csv").toString();

		calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filter);
		System.out.println("\nPrecision 5");
		prec5  = calculator2.calculateMeanPrecision(5);
		System.out.println("\n\nPrecision 10");
		prec10 = calculator2.calculateMeanPrecision(10);
		System.out.println("\n\nMRR");
		calculator =  new MRRCalculator(similarityScores,sampledClones,threshold,filter);
		MRR = calculator.calculateOverallMRR();	
		System.out.println("\n\nMAP");
		calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filter);
		MAP = calculator3.calculateOverallMAP();
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
		
		
		System.out.println("Calculating metrics now!");
		filter = "threshold";
		metricsFile = resultDirPath.resolve("metrics_"+rqNum+"_"+cloneType+"_"+kernel+"_"+filter+".csv").toString();

		calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filter);
		System.out.println("\nPrecision 5");
		prec5  = calculator2.calculateMeanPrecision(5);
		System.out.println("\n\nPrecision 10");
		prec10 = calculator2.calculateMeanPrecision(10);
		System.out.println("\n\nMRR");
		calculator =  new MRRCalculator(similarityScores,sampledClones,threshold,filter);
		MRR = calculator.calculateOverallMRR();	
		System.out.println("\n\nMAP");
		calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filter);
		MAP = calculator3.calculateOverallMAP();
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
