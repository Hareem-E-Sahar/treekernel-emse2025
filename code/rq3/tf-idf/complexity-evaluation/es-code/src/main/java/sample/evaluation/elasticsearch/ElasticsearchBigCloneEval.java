package sample.evaluation.elasticsearch;

import static org.junit.Assert.assertNotEquals;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import sample.evaluation.util.ComplexityUtils;
import sample.evaluation.util.FileMapper;
import sample.evaluation.util.FileSampler2;
import sample.evaluation.util.MAPCalculator;
import sample.evaluation.util.MRRCalculator;
import sample.evaluation.util.PrecisionCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.Util;
import java.util.stream.Collectors;


public class ElasticsearchBigCloneEval {
	 private String indexName;
	 @SuppressWarnings("deprecation")
	 RestHighLevelClient client = new RestHighLevelClient(
             RestClient.builder(new HttpHost("localhost", 9200, "http"))
     );
		
	 public ElasticsearchBigCloneEval(String indexName) {
		 this.indexName = indexName; 
	 }
	 
	 public Map<String, Map<String, Float>> evaluate(List<File> funStrFiles, List<File> sampledFiles, String funStrDir) {
		int numberOfDocuments = 100; 
		MoreLikeThisSearch esSimilarity = new MoreLikeThisSearch(this.client);
		//esSimilarity.deleteIndex(this.indexName);
		//esSimilarity.populateIndex(funStrDir,this.indexName, this.client);
		esSimilarity.findSimilarDocs(numberOfDocuments,this.indexName,funStrFiles,sampledFiles);
		Map<String, Map<String, Float>> similarityScores = esSimilarity.getSimMap().getSimilarityScores();    	    
		esSimilarity.closeClient();
	    return similarityScores; 
	 }
	 
	
	 public static void evaluateOnSeed(long seed) {
 		int functionality  = 0;  
 		float threshold    = 0; //threshold is not relevant for TF-IDF;
 		String technique   = "elasticsearch_evaluation_random_sample_RQ3";
 		String mainDir     = "/home/hareem/UofA2023/Tree_Kernel2024/clone_detection/";
 		String dataDir     = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/";
 		String refFile     = "/home/hareem/eclipse-workspace/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
		String codesDir    = dataDir + "bcb_reduced/"+String.valueOf(functionality)+"/";
		String funStr      = dataDir + "functionStr/"+String.valueOf(functionality)+"/";
		String sexprDir    = dataDir + "sexpr_from_gumtree/"+String.valueOf(functionality)+"/";
		String resultDir   = mainDir + "RQ3-Complexity/"; 
 		String complexityFile = mainDir + "RQ3-Complexity/complexity/0/checkstyle_complexity_all.csv";
	    	String complexity = "high";
    		String rqNum = "RQ3";
    		String metricsFile = resultDir + "metrics_elasticsearch_"+rqNum+"_"+complexity+"_complexity.csv"; 

    		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
	    	try {
	    		refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
	    	} catch (Exception e) {
			e.printStackTrace();
		}
			
	    	System.out.println("Ref clones map size:"+refClones.size());	
	    	File funStrfolder = new File(funStr);
		List<File> funStrFiles = FileMapper.getAllFiles(funStrfolder);  //Get all files recursively
		assertNotEquals(funStrFiles.size(), 0);
			    
		File sexprFolder = new File(sexprDir);
	    	ArrayList<File> sexprFiles = new ArrayList<>(Arrays.asList(sexprFolder.listFiles()));
		    	
		List<File> initialSample = new ArrayList<File>();	    	
		List<File> finalSample   = new ArrayList<File>();
    		
    		int sampleSize = 1000;
    		Map<String, Integer> complexityMap = ComplexityUtils.populateComplexityMap(complexityFile);

		// Split files into two disjoint lists
		List<File> lessThan10Complexity = sexprFiles.stream()
				.filter(file -> ComplexityUtils.getComplexity(file,complexityMap) <= 10)
				.collect(Collectors.toList());
		System.out.println("lessThan10Complexity:"+lessThan10Complexity.size());
			
				
		List<File> moreThan10Complexity = sexprFiles.stream()
				.filter(file -> ComplexityUtils.getComplexity(file,complexityMap) > 10)
				.collect(Collectors.toList());
		System.out.println("moreThan10Complexity:"+moreThan10Complexity.size());
		
			
		// Sample from each list and run experiment once
		List<File> sampleHighComplexity = FileSampler2.sampleFiles(moreThan10Complexity, seed, sampleSize);
		List<File> sampleLowComplexity  = FileSampler2.sampleFiles(lessThan10Complexity,  seed, sampleSize);	
		if (complexity.equals("high"))
			initialSample = sampleHighComplexity; 
		else if (complexity.equals("low"))
			initialSample = sampleLowComplexity; 	
		System.out.println("Initial sample size:"+initialSample.size());
			
		finalSample = FileSampler2.sampleEvaluationFiles( seed, initialSample, sexprFiles, funStrFiles, refClones);
		String outputFile = resultDir+"FinalSample_"+rqNum+"_seed_"+seed+"_complexity_"+complexity+".txt";
			
		System.out.println("Final sample size:"+finalSample.size());
		FileSampler2.saveFinalSample(finalSample,outputFile);
		//System.out.println(finalSample);	
		
		assertNotEquals(finalSample.size(), 0);
		       
    		Map<String, ArrayList<String>> sampledClones = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:"+sampledClones.size());
  
	    	long startTime = System.currentTimeMillis();
	    	String indexName  = "bigclone_index_0_for_sampled_evaluation";
	    	ElasticsearchBigCloneEval evalobj = new ElasticsearchBigCloneEval(indexName);
	    	Map<String, Map<String, Float>> similarityScores = evalobj.evaluate(funStrFiles,finalSample,funStr);
	    	
	    	//String similarityFile = resultDir + "es_simialrity_random_sample.csv";
	    	//Util.saveSimilarityScores(similarityScores, similarityFile);
	    	//Util.printSimilarityScores(similarityScores);
	    	long endTime = System.currentTimeMillis();
	    	double totalTime = (endTime - startTime)/1000; // millisecs to second
	
	    	System.out.println("Sim scores map size :"+similarityScores.size()); 
	    	System.out.println("Total time:"+totalTime); 
	    	System.out.println("=".repeat(90));
		System.out.println("Calculating metrics now!");
	    
	    	PrecisionCalculator calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold);
	    	//System.out.println("\nPrecision 5");
	    	float prec5  = calculator2.calculateMeanPrecision(5);
	    	
	    	//System.out.println("\nPrecision 10");
		float prec10 = calculator2.calculateMeanPrecision(10);
		
		//System.out.println("\nMRR");
		MRRCalculator calculator =  new MRRCalculator(similarityScores,sampledClones,threshold);
		float MRR = calculator.calculateOverallMRR();	
		
		//System.out.println("\nMAP");
		MAPCalculator calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold);
		float MAP = calculator3.calculateOverallMAP();
		
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
