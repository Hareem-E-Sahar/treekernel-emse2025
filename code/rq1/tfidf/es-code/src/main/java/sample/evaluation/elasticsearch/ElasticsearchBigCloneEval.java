package sample.evaluation.elasticsearch;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
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
 		int functionality = 0;  
 		float threshold   = 0; //threshold is not relevant;
 		String technique = "elasticsearch_evaluation_random_sample";
 		
 		String userhome  = System.getProperty("user.home");
		String mainDir   = userhome + "/treekernel-emse2025/";
 		String dataDir   = mainDir + "data/BigCloneEval/ijadataset/";
 		String refFile   =  mainDir + "data/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
		String codesDir  = dataDir + "bcb_reduced/"+String.valueOf(functionality)+"/";
		String funStr    = dataDir + "functionStr/"+String.valueOf(functionality)+"/";
		String sexprDir  = dataDir + "sexpr_from_gumtree/"+String.valueOf(functionality)+"/";
		String resultDir = mainDir + "results/RQ1/tfidf/";
		
		String similarityFile = resultDir + "es_simialrity_random_sample.csv";
    		String metricsFile    = resultDir   + "metrics_elasticsearch.csv"; 
    	
    		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
	    	try {
	    		refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
	    	} catch (Exception e) {
			e.printStackTrace();
		}
			
	    	System.out.println("Ref clones map size:"+refClones.size());	
	    	File funStrfolder = new File(funStr);
		List<File> funStrFiles = FileMapper.getAllFiles(funStrfolder);  //Get all files recursively
		assertNotEquals(0,funStrFiles.size());
			    
		File sexprFolder = new File(sexprDir);
	    	ArrayList<File> sexprFiles = new ArrayList<>(Arrays.asList(sexprFolder.listFiles()));
		    	
	    	List<File> initialSample = new ArrayList<File>();
		List<File> finalSample = new ArrayList<File>();
    		
		try {
			//About 100 out of these 400/500 will be selected. 
	    		//The selection is made if there is at least one 
	    		//clone for these files in the ground truth.
			//otherwise we don't want to evaluate such files.
		    	
	    		int sampleSize = 400;
	    		initialSample = FileSampler2.sampleFiles(sexprFiles,seed,sampleSize);
			assertNotEquals(0,initialSample.size());
			System.out.println("Initial sample size:"+initialSample.size());
			Map<File, File> mapping = FileMapper.getMapping(initialSample,funStrFiles);
			for (File file : initialSample) {
				if (refClones.containsKey(file.getName())) {
					int numOfClones = refClones.get(file.getName()).size();
					if (numOfClones > 0) {
						File file1 = FileMapper.getFileByName(mapping,file.getName());
						finalSample.add(file1); //if a sampled file has reference clones, use it for evaluation
						
					}
					if (finalSample.size()==100) {
						break;
					}
				}	   
			}
			
			//To test
			//finalSample.add(new File(dataDir+"functionStr/0/selected/2109789_336_360.java")); 
			//finalSample.add(new File(dataDir+"functionStr/0/selected/1775595_13_29.java"));
			//finalSample.add(new File(dataDir+"functionStr/0/default/101041_17_53.java"));
			System.out.println("Final sample size:"+finalSample.size());
			System.out.println(finalSample);
			String outputfile = resultDir+String.valueOf(seed)+".txt";
			FileSampler2.saveFinalSample(finalSample,outputfile);
			
	       
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		Map<String, ArrayList<String>> sampledClones = new HashMap<String, ArrayList<String>>();
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
	    	System.out.println("=".repeat(90));
		System.out.println("Calculating metrics now!");
	    	PrecisionCalculator calculator2 = new PrecisionCalculator(similarityScores,sampledClones,threshold);
	    	float prec5  = calculator2.calculateMeanPrecision(5);
		float prec10 = calculator2.calculateMeanPrecision(10);
		MRRCalculator calculator =  new MRRCalculator(similarityScores,sampledClones,threshold);
		float MRR = calculator.calculateOverallMRR();	
		MAPCalculator calculator3 = new MAPCalculator(similarityScores,sampledClones,threshold);
		float MAP = calculator3.calculateOverallMAP();
		System.out.print("\nPrec5:"+prec5+" Prec10:"+prec10+" MRR:"+MRR+" MAP:"+MAP+" ");
		System.out.println("funct:"+functionality+" time:"+totalTime+ " sec"+" threshold:"+threshold+" technique:"+technique);
		Util.appendResultToFile(prec5, prec10, MRR, MAP, functionality, totalTime, metricsFile, seed, technique);
	}
	
	 public static void main(String[] args) {
	 	long [] all_seeds= {6251,9080,8241,8828,55,2084,1375,2802,3501,3389}; //from Util.generate_seed();
		for (int i=0; i<all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]); 
	 }
}
