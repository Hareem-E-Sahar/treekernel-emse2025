package sample.evaluation.kelp;

import static org.junit.Assert.assertNotEquals;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Arrays;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import sample.evaluation.util.ComplexityUtils;
import sample.evaluation.util.FileSampler2;
import sample.evaluation.util.MAPCalculator;
import sample.evaluation.util.MRRCalculator;
import sample.evaluation.util.PrecisionCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.Util;
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
	  	String kernel     = "STK"; 
	  	String technique  = "kelp_evaluation_RQ3";
		String rqNum      = "RQ3";
		
		String funComplexity = "High"; //Low, High
		String funSize       = "Less"; //More, Less
		String evaluationType = "complexity_eval"; //complexity_eval, size_eval
	    	
		
	    	Path mainDirPath = Paths.get(System.getProperty("user.home"), "treekernel-emse2025");
	    	Path dataDirPath = Paths.get(System.getProperty("user.home"), "treekernel-emse2025", "data", "BigCloneEval","ijadataset");
	    	Path resultDirPath = mainDirPath.resolve("results").resolve(rqNum);
	    	
	    	
	    	Path BCDirPath   = Paths.get(System.getProperty("user.home"),"treekernel-emse2025", "data","TestH2Database");
	    	String codesDir = dataDirPath.resolve("bcb_reduced").resolve(String.valueOf(functionality)).toString()+"/";
	    	String sexprDir = dataDirPath.resolve("sexpr_from_gumtree/").resolve(String.valueOf(functionality)).toString()+"/";
	    	String complexityFile = Paths.get(System.getProperty("user.home"), "treekernel-emse2025", "data").resolve("checkstyle_complexity_all.csv").toString();
 		String refFile = BCDirPath.resolve("bigclonedb_clones_alldir_8584153.txt").toString();
	    	
    		Map<String, ArrayList<String>> refClones = new HashMap<>();
	    	ArrayList<File> sexprFiles  = new ArrayList<>(Arrays.asList(new File(sexprDir).listFiles()));
		System.out.println("Sexpr Files:"+sexprFiles.size());
			
	    	try {
			refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
	    		System.out.println("refClones:"+refFile+"  "+refClones.size());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}
    		
    				
		List<File> finalSample = Sampler.sampleForRQ3(refClones, sexprFiles, complexityFile, evaluationType, funComplexity, funSize, seed);
		assertEquals(100, finalSample.size());
		
		String outputFile = 		resultDirPath.resolve("final_sample_"+rqNum+"_seed_"+seed+"_complexity_"+funComplexity+".txt").toString();
		FileSampler2.saveFinalSample(finalSample,outputFile);
	    	
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
		
	    	//System.out.println("Calculating metrics now!");
	    	String filteringCriteria    = "topk_threshold"; 
    		String metricsFile = resultDirPath.resolve("metrics_"+rqNum+"_"+kernel+"_"+filteringCriteria+".csv").toString();   
		PrecisionCalculator calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		float prec5  = calculator2.calculateMeanPrecision(5);
		float prec10 = calculator2.calculateMeanPrecision(10);
		MRRCalculator calculator = new MRRCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		float MRR = calculator.calculateOverallMRR();	
		MAPCalculator calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		float MAP = calculator3.calculateOverallMAP();
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
		
		
	    	//System.out.println("Calculating metrics now!");
	    	filteringCriteria    = "threshold"; 
    		metricsFile = resultDirPath.resolve("metrics_"+rqNum+"_"+kernel+"_"+filteringCriteria+".csv").toString();   
		calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		prec5  = calculator2.calculateMeanPrecision(5);
		prec10 = calculator2.calculateMeanPrecision(10);
		calculator = new MRRCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MRR = calculator.calculateOverallMRR();	
		calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MAP = calculator3.calculateOverallMAP();
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
		
		
	    	//System.out.println("Calculating metrics now!");
	    	filteringCriteria    = "topk"; 
    		metricsFile = resultDirPath.resolve("metrics_"+rqNum+"_"+kernel+"_"+filteringCriteria+".csv").toString();   
		calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		prec5  = calculator2.calculateMeanPrecision(5);
		prec10 = calculator2.calculateMeanPrecision(10);
		calculator = new MRRCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MRR = calculator.calculateOverallMRR();	
		calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MAP = calculator3.calculateOverallMAP();
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);

	}
	
	public static void main(String[] args) {
		long [] all_seeds= {6251}; //{6251, 9080, 8241, 8828, 55, 2084, 1375, 2802, 3501, 3389}; //from Util.generate_seed();
		for (int i=0; i<all_seeds.length; i++) {
			evaluateOnSeed(all_seeds[i]); 
		}				
	}
}
