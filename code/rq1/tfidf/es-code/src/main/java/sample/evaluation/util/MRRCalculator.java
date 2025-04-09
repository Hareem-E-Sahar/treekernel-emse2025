package sample.evaluation.util;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MRRCalculator {
    private Map<String, Map<String, Float>> similarityScores;
    private Map<String, ArrayList<String>> referenceClones;
    float threshold;
    
    public MRRCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, float thresh) {
        this.similarityScores = simScores;
        this.referenceClones  = refClones;
        this.threshold = thresh;
    }
   
    public float calculateMRR(String query, ArrayList<String> groundTruth) {
    		//System.out.println("Query:"+query+" Ground truth size:"+groundTruth.size());
		Map<String, Float> queryScores = similarityScores.get(query);
		if (queryScores == null || queryScores.isEmpty()) {
	            return 0.0f;  // No scores available for this query
	    	 }
		//only sorting is needed for ES
		List<Map.Entry<String, Float>> sortedScores = ScoreUtils.getTopScores(queryScores);
	    	
	    	
		//System.out.println("Original scores size:"+queryScores.size()+" Filtered & Sorted scores:"+sortedScores.size());
		if (sortedScores.size() > queryScores.size()) {
	        //System.out.println("Error: Filtered list size is larger than original list size!");
	        return 0.0f;
	    }
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
