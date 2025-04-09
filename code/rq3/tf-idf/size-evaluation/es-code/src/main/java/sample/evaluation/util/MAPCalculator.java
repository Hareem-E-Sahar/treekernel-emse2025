package sample.evaluation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MAPCalculator {
	 private Map<String, Map<String, Float>> similarityScores;
	 private Map<String, ArrayList<String>>  referenceClones;
	 double threshold;
	 //MAP is a more comprehensive metric that considers the entire precision-recall curve,
	 //while Precision@K is a single-point measure at a specific cutoff point, K.
	 
	 public MAPCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, double thresh) {
		 this.similarityScores = simScores;
		 this.referenceClones  = refClones;
		 this.threshold = thresh;
	 }
	 
	 private float calculateAveragePrecision(String query, ArrayList<String> groundTruth) {
		/*if (!similarityScores.containsKey(query) || groundTruth.isEmpty())
			 return 0;
		*/
		//System.out.println("\nQuery:"+query+" Ground truth size:"+groundTruth.size());
		Map<String, Float> queryScores = similarityScores.get(query);
        	
        	if (queryScores == null || queryScores.isEmpty()) {
			return 0.0f;  // No scores available for this query
		}
	    	 //only sorting is needed for ES
		 List<Map.Entry<String, Float>> sortedScores = ScoreUtils.getTopScores(queryScores);
	    		 
	    	
		System.out.println("Original scores size:"+queryScores.size()+" Filtered & Sorted scores:"+sortedScores.size());
		if (sortedScores.size() > queryScores.size()) {
			System.out.println("Error: Filtered list size is larger than original list size!");
			return 0.0f;
		}
		
	    
		float sumPrecision = 0;
		int relevantCount = 0;
		int rank = 0;
		for (Entry<String, Float> entry : sortedScores) {
		    rank++;
		    if (groundTruth.contains(entry.getKey())) {
		    	//System.out.println("Rank of "+entry.getKey()+" is "+rank);
		        relevantCount++;
		        sumPrecision += (float) relevantCount / rank;
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
