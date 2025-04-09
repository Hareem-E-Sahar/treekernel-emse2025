package sample.evaluation.elasticsearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimilarityMap {
//do I need the similarity score or not?
    

    private Map<String, Map<String, Float>> matches;

    public SimilarityMap() {
    	matches = new HashMap<>();
    }

    /*public Map<String, ArrayList<String>> getSimilarityScores() {
		return matches;
	}*/

    public Map<String, Map<String, Float>> getSimilarityScores() {
		return matches;
	}

	/**
     * Adds or updates the similarity score between two files.
     *
     * @param file1 the first file name
     * @param file2 the second file name
     * @param score the similarity score between file1 and file2
     */
    public void addSimilarity(String file1, String file2, Float score) {
        matches.putIfAbsent(file1, new HashMap<String, Float>());
        matches.get(file1).put(file2, score);
       //System.out.println("Added similarity between " + file1 + " and " + file2 + " with score: " + score);
    }

    /**
     * Retrieves the similarity score between two files.
     *
     * @param file1 the first file name
     * @param file2 the second file name
     * @return the similarity score, or null if no score is present
     */
    
    public Float getSimilarity(String file1, String file2) {
        if (matches.containsKey(file1) && matches.get(file1).containsKey(file2)) {
            return matches.get(file1).get(file2);
        }
        return null;  // or throw an exception 
    }

   
    
    public List<Map.Entry<String, Float>> sortSimilarityScores(String key) {
        // Retrieve the scores for the specified key and sort them
        Map<String, Float> scores = matches.get(key);
        if (scores == null) {
            System.out.println("No scores available for " + key);
            return new ArrayList<>(); // Return an empty list 
        }

        // Create a list from the entry set of the map and sort it
        List<Map.Entry<String, Float>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort(Map.Entry.<String, Float>comparingByValue().reversed());
        return sortedScores;
    }

}
