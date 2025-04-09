package sample.evaluation.kelp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimilarityMap {
    private Map<String, Map<String, Float>> similarityScores;

    public SimilarityMap() {
        similarityScores = new HashMap<>();
    }

    public Map<String, Map<String, Float>> getSimilarityScores() {
		return similarityScores;
	}

	/**
     * Adds or updates the similarity score between two files.
     *
     * @param file1 the first file name
     * @param file2 the second file name
     * @param score the similarity score between file1 and file2
     */
    public void addSimilarity(String file1, String file2, Float score) {
        similarityScores.putIfAbsent(file1, new HashMap<String, Float>());
        similarityScores.get(file1).put(file2, score);
        // To ensure both way access you can add file2-file1 too but it makes a big filek
        // similarityScores.putIfAbsent(file2, new HashMap<String, Float>());
        // similarityScores.get(file2).put(file1, score);  
    }

    /**
     * Retrieves the similarity score between two files.
     *
     * @param file1 the first file name
     * @param file2 the second file name
     * @return the similarity score, or null if no score is present
     */
    public Float getSimilarity(String file1, String file2) {
        if (similarityScores.containsKey(file1) && similarityScores.get(file1).containsKey(file2)) {
            return similarityScores.get(file1).get(file2);
        }
        return null;  // or throw an exception 
    }

   
    public List<Map.Entry<String, Float>> sortSimilarityScores(String key) {
        // Retrieve the scores for the specified key and sort them
        Map<String, Float> scores = similarityScores.get(key);
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
