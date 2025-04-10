package sample.evaluation.elasticsearch;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Comparator;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.stream.Collectors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import sample.evaluation.util.FileMapper;
import sample.evaluation.util.FileSampler2;
import sample.evaluation.util.MAPCalculator;
import sample.evaluation.util.MRRCalculator;
import sample.evaluation.util.PrecisionCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.Util;
import sample.evaluation.util.TypeClones;



public class ElasticsearchBigCloneEval {
	 private String indexName;
	 @SuppressWarnings("deprecation")
	 RestHighLevelClient client = new RestHighLevelClient(
             RestClient.builder(new HttpHost("localhost", 9200, "http")));
		
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
 		String technique   = "elasticsearch_evaluation_random_sample_RQ2";

		String userhome  = System.getProperty("user.home");
		String mainDir   = userhome + "/treekernel-emse2025/";
 		String dataDir   = mainDir + "data/BigCloneEval/ijadataset/";
 		String refFile   =  mainDir + "data/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
		String codesDir  = dataDir + "bcb_reduced/"+String.valueOf(functionality)+"/";	
		String funStr      = dataDir + "functionStr/"+String.valueOf(functionality)+"/";
		String sexprDir    = dataDir + "sexpr_from_gumtree/"+String.valueOf(functionality)+"/";
		String resultDir   = mainDir + "/RQ2-baseline-results/"; 
		String cloneType   = "T3";
		System.out.println("Type:"+cloneType);
    		String metricsFile = mainDir + "RQ2/metrics-elastic-rq2-type-specific-refclones.csv"; 
    		Path newBCDirPath  = Paths.get(System.getProperty("user.home"),"UofA2023","Tree_Kernel2024","BigCloneEval", "bigclone_groundtruth_v3");

		if (cloneType.equals("T1")) {
			refFile   = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/bigclone_groundtruth_v3/T1-clones-selected-columns-no-header.txt"; 
		} else if (cloneType.equals("T2")) {
			refFile   = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/bigclone_groundtruth_v3/T2-clones-selected-columns-no-header.txt";	
		} else if (cloneType.equals("T3")) {
			refFile = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/bigclone_groundtruth_v3/ST3-VST3-clones-simtoken-selected-columns-no-header.txt";
		} 
    		Path typewiseFilePath = newBCDirPath.resolve(cloneType+"-clones-selected-columns.txt");
		String typewiseFile = typewiseFilePath.toString();
    		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
    		Set<String> typewiseClones = new HashSet<String>(); 
    		
    		
	    	try {
	    		refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
	    		typewiseClones = TypeClones.getTypeWiseFiles(typewiseFile, codesDir) ;
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
		List<File> finalSample = new ArrayList<File>();
    		int sampleSize = 10000;
    		
		initialSample = FileSampler2.sampleFiles(sexprFiles,seed,sampleSize);
		System.out.println("Initial sample size:"+ initialSample.size());
		assertNotEquals(initialSample.size(), 0);
		
		//FileSampler2.sampleEvaluationFiles(initialSample,sexprFiles, funStrFiles, refClones);
		FileSampler2.generateFinalSampleTypewise(refClones, initialSample, finalSample, typewiseClones, funStrFiles);
		System.out.println("Final sample size:"+finalSample.size());
		System.out.println(finalSample);	
		assertEquals(finalSample.size(), 100);
		String outputfile = mainDir + "/RQ2/nicad/sampled_files_third_attempt/"+cloneType+"/"+seed;
		FileSampler2.saveFinalSample(finalSample,outputfile);
		
		
		Map<String, ArrayList<String>> sampledClones  = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:"+sampledClones.size());
		
	    	long startTime = System.currentTimeMillis();
	    	String indexName  = "bigclone_index_0_for_sampled_evaluation";
	    	ElasticsearchBigCloneEval evalobj = new ElasticsearchBigCloneEval(indexName);
	    	Map<String, Map<String, Float>> similarityScores = evalobj.evaluate(funStrFiles,finalSample,funStr);
	    	
	    	long endTime = System.currentTimeMillis();
	    	double totalTime = (endTime - startTime)/1000; // millisecs to second
	
	    	System.out.println("Sim scores map size :"+similarityScores.size()); 
	    	System.out.println("Total time:"+totalTime); 
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
