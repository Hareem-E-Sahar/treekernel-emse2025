package sample.evaluation.util;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MRRCalculator {
    private Map<String, Map<String, Float>> similarityScores;
    private Map<String, ArrayList<String>> referenceClones;
    float threshold;
    String filter;
    
    public MRRCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, float thresh,String filter) {
        this.similarityScores = simScores;
        this.referenceClones  = refClones;
        this.threshold = thresh;
	this.filter = filter;
    }
   
    public float calculateMRR(String query, ArrayList<String> groundTruth) {
		
	//System.out.println("Query:"+query+" Ground truth size:"+groundTruth.size());
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
	if (queryScores == null || queryScores.isEmpty()) {
            return 0.0f;  // No scores available for this query
    	}
    	
	//System.out.println("Original scores size:"+queryScores.size()+" Filtered & Sorted scores:"+sortedScores.size());
	assert sortedScores.size() <= queryScores.size() : "Error: Filtered list size is larger than original list size!";
	       
		
        int rank = 0;
    	// Finding the rank of the first relevant item from the ground truth
        for (Entry<String, Float> entry : sortedScores) {
        	rank++;
	        String foundClone = entry.getKey();
	        if (groundTruth.contains(foundClone)) {
	        	//System.out.println("found clone "+foundClone+" rank "+rank);
		        return (float) (1.0 / (rank)); //Reciprocal rank of the first relevant item
	        }
        }
    	return 0; // No relevant scores found
    }
    
    public float calculateOverallMRR() {
        float sum = 0;
        int count = 0;
        for (String query : referenceClones.keySet()) {
    		ArrayList<String> groundTruth = referenceClones.get(query);
           	float mrr = calculateMRR(query,groundTruth);
    		if (mrr > 0) {
    			//System.out.println("MRR for "+query+" "+mrr);
                sum += mrr;
                count++;    
            }
        }
        return count > 0 ? sum / count : 0;
    }
}
