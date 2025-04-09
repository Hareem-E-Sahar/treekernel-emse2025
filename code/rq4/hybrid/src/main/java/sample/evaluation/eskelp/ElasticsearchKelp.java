package sample.evaluation.eskelp;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import sample.evaluation.util.*;

public class ElasticsearchKelp {
	 private String indexName;
	 @SuppressWarnings("deprecation")
	 RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
		
	 public ElasticsearchKelp(String indexName) {
		 this.indexName = indexName; 
	 }
	 
	 public Map<String, Map<String, Float>> evaluate(List<File> funStrFiles, List<File> sampledFiles, String funStr, String sexprDir, ArrayList<File> sexprFiles, String kernel){
		int numberOfDocuments = 100; 
		ESKelpSimilarity esSimilarity = new ESKelpSimilarity(this.client);
		//esSimilarity.deleteIndex(this.indexName);
		//esSimilarity.populateIndex(funStr,this.indexName, this.client);
		esSimilarity.findSimilarDocs(numberOfDocuments,this.indexName,funStrFiles,sampledFiles,sexprDir, sexprFiles,kernel);
		Map<String, Map<String, Float>> similarityScores = esSimilarity.getSimMap().getSimilarityScores();    	    
		esSimilarity.closeClient();
	    	return similarityScores; 
	 }
	 
	 public static void evaluateOnSeed(long seed) {
		int functionality = 0;  
 		float threshold   = 0.3f;
 		String kernel = "SSTK";
 		String technique  = "es_kelp_evaluation_random_sample";
 		String indexName = "bigclone_index_0_for_sampled_evaluation";
    		
    		String userhome  = System.getProperty("user.home");
		String mainDir   = userhome + "/treekernel-emse2025/";
 		String dataDir   = mainDir + "data/BigCloneEval/ijadataset/";
 		String refFile   = mainDir + "data/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
		String codesDir  = dataDir + "bcb_reduced/"+String.valueOf(functionality)+"/";
		String funStr    = dataDir + "functionStr/"+String.valueOf(functionality)+"/";
		String sexprDir  = dataDir + "sexpr_from_gumtree/"+String.valueOf(functionality)+"/";
		String resultDir = mainDir + "results/RQ4/";
		
		
    		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
	    	try {
			refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	   	System.out.println("funStr:"+funStr);

	    	System.out.println("Ref clones map size:"+refClones.size());
		
	    	File funStrFolder = new File(funStr);
		List<File> funStrFiles = FileMapper.getAllFiles(funStrFolder); // Get all files recursively	
		assertNotEquals("Error: Dir shall have some files!",funStrFiles.size(),0);
    	
		File sexprFolder = new File(sexprDir);
	    	ArrayList<File> sexprFiles = new ArrayList<>(Arrays.asList(sexprFolder.listFiles()));
    	
    		List<File> initialSample = new ArrayList<File>();
    		List<File> finalSample   = new ArrayList<File>();
    		try {
			int sampleSize = 400;
			initialSample = FileSampler2.sampleFiles(sexprFiles,seed,sampleSize);
			
			assertNotEquals("Error: Initial Sample list should not be size 0!", initialSample.size(), 0);
			System.out.println("Initial sample size:"+initialSample.size());
			
			for (File file : initialSample) {
				if (refClones.containsKey(file.getName())) {
					int numOfClones = refClones.get(file.getName()).size();
					if (numOfClones > 0) {//if a sampled file has reference clones, use it for evaluation
						finalSample.add(file); 
					}
					if (finalSample.size()==100)
						break;
				}	   
			}
		
			
		
	       	System.out.println("Final sample size:"+finalSample.size());
			String outputfile = resultDir+seed+".txt";
			FileSampler2.saveFinalSample(finalSample,outputfile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    		Map<String, ArrayList<String>> sampledClones = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:"+sampledClones.size());
    	
		long startTime = System.currentTimeMillis();
    		ElasticsearchKelp evalobj = new ElasticsearchKelp(indexName);
	    	Map<String, Map<String, Float>> similarityScores = evalobj.evaluate(funStrFiles,finalSample,funStr,sexprDir,sexprFiles,kernel);
	    	
    		long endTime = System.currentTimeMillis();
	    	double totalTime = (endTime - startTime)/1000; // millisecs to seconds
	    	System.out.println("Sim scores map size :"+similarityScores.size()); 
	    	System.out.println("Total time:"+totalTime); 
	    	System.out.println("Calculating metrics now!");
	    	
	    	String filteringCriteria  = "topk_threshold"; 
    		String metricsFile = resultDir+"metrics_rq4_hybrid_"+kernel+"_"+filteringCriteria+".csv";   
	    	PrecisionCalculator calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
	    	//System.out.println("\nPrecision 5");
	    	float prec5  = calculator2.calculateMeanPrecision(5);
		//System.out.println("\nPrecision 10");
		float prec10 = calculator2.calculateMeanPrecision(10);
		//System.out.println("\nMRR");
		MRRCalculator calculator = new MRRCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		float MRR = calculator.calculateOverallMRR();	
		//System.out.println("\nMAP");
		MAPCalculator calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		float MAP = calculator3.calculateOverallMAP();
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold+" technique:"+technique);
		System.out.println(metricsFile);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
		
		
	    	filteringCriteria  = "topk"; 
    		metricsFile = resultDir+"metrics_rq4_hybrid_"+kernel+"_"+filteringCriteria+".csv";   
	    	calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
	    	prec5  = calculator2.calculateMeanPrecision(5);
		prec10 = calculator2.calculateMeanPrecision(10);
		calculator = new MRRCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MRR = calculator.calculateOverallMRR();	
		calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MAP = calculator3.calculateOverallMAP();
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold+" technique:"+technique);
		System.out.println(metricsFile);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
		
		
	    	filteringCriteria  = "threshold"; 
    		metricsFile = resultDir+"metrics_rq4_hybrid_"+kernel+"_"+filteringCriteria+".csv";   
	    	calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
	    	prec5  = calculator2.calculateMeanPrecision(5);
		prec10 = calculator2.calculateMeanPrecision(10);
		calculator = new MRRCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MRR = calculator.calculateOverallMRR();	
		calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MAP = calculator3.calculateOverallMAP();		
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold+" technique:"+technique);
		System.out.println(metricsFile);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
		
		
		filteringCriteria  = "no_filter"; 
    		metricsFile = resultDir+"metrics_rq4_hybrid_"+kernel+"_"+filteringCriteria+".csv";   
	    	calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
	    	prec5  = calculator2.calculateMeanPrecision(5);
		prec10 = calculator2.calculateMeanPrecision(10);
		calculator = new MRRCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MRR = calculator.calculateOverallMRR();	
		calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold,filteringCriteria);
		MAP = calculator3.calculateOverallMAP();		
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold+" technique:"+technique);
		System.out.println(metricsFile);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
		
		
	}
	
	public static void main(String[] args) {
		long [] all_seeds= {6251,9080,8241,8828,55,2084,1375,2802,3501,3389}; //from Util.generate_seed();
		for (int i=0; i<all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]); 
		
	}
}
