package sample.evaluation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MAPCalculator {
	 private Map<String, Map<String, Float>> similarityScores;
	 private Map<String, ArrayList<String>>  referenceClones;
	 float threshold;
	 String filter;
	 //MAP is a more comprehensive metric that considers the entire precision-recall curve,
	 //while Precision@K is a single-point measure at a specific cutoff point, K.
	 
	 public MAPCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, float thresh, String filter) {
		 this.similarityScores = simScores;
		 this.referenceClones  = refClones;
		 this.threshold = thresh;
		 this.filter = filter;
	 }
	 
	 private float calculateAveragePrecision(String query, ArrayList<String> groundTruth) {
		
		Map<String, Float> queryScores = similarityScores.get(query);
		/*//sort and filter (originally used in all evaluations of RQ2 so far)
	    	List<Map.Entry<String, Float>> sortedScores = queryScores.entrySet().stream()
	    		    .filter(entry -> entry.getValue() >= threshold)
	    		    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
	    		    .collect(Collectors.toList());
	    	*/
               if (queryScores == null || queryScores.isEmpty()) {
			return 0.0f;  // No scores available for this query
	       }
	    
               List<Map.Entry<String, Float>> sortedScores = null;	   
               if(this.filter.equals("topk_threshold")) {
                       //filter by threshold and keep only top 100
                      sortedScores = ScoreUtils.getTopScores(queryScores,100, threshold);
               } else if (this.filter.equals("topk")) { 
                        //use only top 100
                      sortedScores = ScoreUtils.getTopScores(queryScores, 100);
               } else if (this.filter.equals("threshold")) {
                        //filter by threshold only
                      sortedScores = ScoreUtils.getTopScores(queryScores, threshold);  
               } else {
                        System.out.println("No filtered specified");
               }
			    
		//System.out.println("Original scores size:"+queryScores.size()+" Filtered & Sorted scores:"+sortedScores.size());
		if (sortedScores.size() > queryScores.size()) {
			System.out.println("Error: Filtered list size is larger than original list size!");
			return 0.0f;
		}
		if (sortedScores == null || sortedScores.isEmpty()) {
			return 0.0f;  // No scores available for this query
		}
	    
		float sumPrecision = 0;
		int relevantCount = 0;
		int rank = 0;
		for (Entry<String, Float> entry : sortedScores) {
		    rank++;
		    if (groundTruth.contains(entry.getKey())) {
		    	//System.out.println("Rank of "+entry.getKey()+" is "+rank);
		        relevantCount++;
		        sumPrecision += (float) relevantCount / rank; //Prec(K)
		        //System.out.println("Relevant:"+relevantCount+","+rank+","+sumPrecision);
		    }
        	}
        	return relevantCount > 0 ? sumPrecision / groundTruth.size() : 0;
	 }
		    
	 public float calculateOverallMAP() {
	 	float sum = 0;
	     	int count = 0;
	     	System.out.println("Reference clones keyset size:"+referenceClones.keySet().size());
	     	for (String query : referenceClones.keySet()) {
	    		ArrayList<String> groundTruth = referenceClones.get(query);
	    		float AP = calculateAveragePrecision(query, groundTruth);
	    		if (AP > 0) {
	    			sum += AP;
		         	count++;    
		     	}	 
	     }
	     return count > 0 ? sum / count : 0;
	 }
}
