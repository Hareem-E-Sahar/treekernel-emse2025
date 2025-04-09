package sample.evaluation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PrecisionCalculator {
	//Prec@K is the proportion of recommended items in the top-k set that are relevant
	 private Map<String, Map<String, Float>> similarityScores;
	 private Map<String, ArrayList<String>> referenceClones;
	 float threshold ;
	 String filter;

	 public PrecisionCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, float thresh, String filter ) {
		 this.similarityScores = simScores;
		 this.referenceClones  = refClones;
		 this.threshold = thresh;
		 this.filter = filter;
	 }
	 
	 private float calculatePrecision(String query, ArrayList<String> groundTruth, int K) {
		System.out.println("\nQuery:"+query+" Ground truth size:"+groundTruth.size());
		Map<String, Float> queryScores = similarityScores.get(query);
		if (queryScores == null || queryScores.isEmpty()) {
	            System.out.println();
	            return 0.0f;  // No scores available for this query
	            
	    	}
		List<Map.Entry<String, Float>> sortedScores = null;
	        if (this.filter.equals("topk_threshold")) { 
		       System.out.println("topk_threshold");
		       sortedScores = ScoreUtils.getTopScores(queryScores,100, threshold);
    	        } else if (this.filter.equals("topk")) {
		      //use only top 100
		      System.out.println("topk");
                      sortedScores = ScoreUtils.getTopScores(queryScores, 100);
	        } else if(this.filter.equals("threshold")) {
		       //filter by threshold only
		       System.out.println("threshold");
                       sortedScores = ScoreUtils.getTopScores(queryScores, threshold); 
	        } else if (this.filter.equals("no_filter")) {
	        	System.out.println("No filter");
	        	sortedScores = ScoreUtils.getTopScores(queryScores);
	        } else {
	        	System.out.println("No filter specified");
	        }
	     	System.out.println("Original scores size:"+queryScores.size()+" Filtered & Sorted scores:"+sortedScores.size());
	     	
	     	assert sortedScores.size() <= queryScores.size() : "Error: Filtered list size is larger than original list size!";
	        /* if (sortedScores.size() > queryScores.size()) {
	        	 System.out.println("Error: Filtered list size is larger than original list size!");
	        	 return 0.0f;
	        }*/
	     
	     int relevantItems = 0;
	     int examinedItems = 0;
	     for (Entry<String, Float> scoreEntry : sortedScores) {
        	if (examinedItems >= K) {
               	break;
             }
        	//System.out.println(scoreEntry.getKey());
            if (groundTruth.contains(scoreEntry.getKey())) {
            	//System.out.println("Score key:"+scoreEntry.getKey()+" "+scoreEntry.getValue());
                relevantItems++;
            }
            examinedItems++;            
         }
         // If no valid clones are examined, precision is zero
         if (examinedItems == 0) {
             return 0.0f;
         }
         //System.out.println("Relevant items "+relevantItems+" Examined items "+examinedItems+"  K "+K);
         float precisionAtK = relevantItems /(float) examinedItems;  // Calculate precision at k
		 return precisionAtK;
	 }
	 
	 public float calculateMeanPrecision(int K) {
		 float totalPrecision = 0;
		 int queriesCount = 0;
		 for (String query: referenceClones.keySet()) {	
			 ArrayList<String> groundTruth = referenceClones.get(query);
			 float precisionAtK = calculatePrecision(query,groundTruth,K);
			 totalPrecision += precisionAtK;
	         	 queriesCount++;
		 }
		 float averagePrecisionAtK = queriesCount > 0 ? totalPrecision / queriesCount : 0;
		 return averagePrecisionAtK;
	 }
	 
	 public static void main(String[] args) {
		 /* Map<String, Map<String, Float>> similarityScores = new HashMap<String, Map<String, Float>>();
		 
		  similarityScores.put("Query1", Map.of("item1", 0.9f, "item2", 0.8f, "item3", 0.7f, "item4", 0.6f, "item5", 0.5f, "item6", 0.4f));
		  Map<String, ArrayList<String>> referenceClones = new HashMap<String, ArrayList<String>> ();
		  referenceClones.put("Query1", new ArrayList<>(Arrays.asList("item1", "item2", "item3", "item4")));
		  PrecisionCalculator calculator2 =  new PrecisionCalculator(similarityScores,referenceClones, (float)0.3);
		  float prec5  = calculator2.calculateMeanPrecision(5);
		  float prec10 = calculator2.calculateMeanPrecision(10);
		  //System.out.println(prec5+" -- "+prec10);
	 	*/
	 	}  
	 }

