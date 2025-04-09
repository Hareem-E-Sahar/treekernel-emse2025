package sample.evaluation.util;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScoreUtils {
        //filter based on threshold and keep top 100
        public static List<Map.Entry<String, Float>> getTopScores(Map<String, Float> queryScores,int topDocs, float threshold) {
                 return queryScores.entrySet().stream()
                   	.filter(entry -> entry.getValue() >=threshold)                                            	   				.sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                   	.limit(topDocs)
                   	.collect(Collectors.toList());
        }

        //filter based on threshold
        public static List<Map.Entry<String, Float>> getTopScores(Map<String, Float> queryScores, float threshold) {
                return queryScores.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .sorted(Map.Entry.<String,Float>comparingByValue().reversed())
                .collect(Collectors.toList());
         }


        //limit to top 100
        public static List<Map.Entry<String, Float>> getTopScores(Map<String, Float> queryScores, int topDocs) {
                return queryScores.entrySet().stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .limit(topDocs)
                .collect(Collectors.toList());
         }
}

