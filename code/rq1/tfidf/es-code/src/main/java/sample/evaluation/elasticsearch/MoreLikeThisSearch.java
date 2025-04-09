package sample.evaluation.elasticsearch;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.SearchHit;
import org.apache.http.HttpHost;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class MoreLikeThisSearch {
    private RestHighLevelClient client;
    
    SimilarityMap simMap;
	float[] selfSimilarities;
   
	public SimilarityMap getSimMap() {
		return simMap;
	}
    
	
	@SuppressWarnings("deprecation")
	public MoreLikeThisSearch(RestHighLevelClient client) {
        this.client = client;
        
        simMap = new SimilarityMap();
       
    }

    public void closeClient() {
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("deprecation")
	public void populateIndex(String funStr, String indexName, RestHighLevelClient client) {
		 ESFileIndexer fileIndexer = new ESFileIndexer(this.client);
		 fileIndexer.indexDocs(funStr,indexName);
	}
    
    public List<SearchHit> executeMLTQuery(int numberOfDocuments, String indexName, String queryContent) throws IOException {
        // The fields to search against
        String[] fields = {"content"};

        MoreLikeThisQueryBuilder moreLikeThisQuery = QueryBuilders.moreLikeThisQuery(
            fields, 
            new String[] { queryContent }, 
            null // Optional query builder for additional parameters
        ).minTermFreq(1)    // Min number of times a term must appear in the document
        .minDocFreq(1);     // Min number of docs that must contain the term

        // Build the search request
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(moreLikeThisQuery);
        sourceBuilder.size(numberOfDocuments); 
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);
        // Execute the search
        SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
        return List.of(searchResponse.getHits().getHits());
    }

    public void findSimilarDocs(int numberOfDocuments, String indexName, List<File> funStrFiles, List<File> sampledFiles) {
    	for(File queryfile : sampledFiles) {
    		System.out.println("\nProcessing file: " + queryfile.getName());
    		Path queryFilePath = Paths.get(queryfile.getAbsolutePath());
    		try {
            	String queryContent = new String(Files.readAllBytes(queryFilePath));
            	List<SearchHit> results = executeMLTQuery(numberOfDocuments,indexName,queryContent);
            	System.out.println("Hits:"+results.size());
            	
            	for (SearchHit hit : results) {
                    //System.out.println("Found document with ID: " + hit.getId());
                    //System.out.println("Source: " + hit.getSourceAsString());
                    Map<String, Object> source = hit.getSourceAsMap();
                    String absolutePath = (String) source.get("filepath");
                    Path path = Paths.get(absolutePath);
                    String fileName = path.getFileName().toString();
                    float esSimilarityScore = hit.getScore(); 
                    
                    if (!queryfile.getName().equals(fileName)) {
                    	System.out.println("Hit:"+fileName+" Similarity score: " + esSimilarityScore);
                    	simMap.addSimilarity(queryfile.getName(), fileName, esSimilarityScore);
                    }

                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } 
    		//System.out.println("Similarity Scores:"+simMap.getSimilarityScores());
    	}     
    }
    
    @SuppressWarnings("deprecation")
	public void deleteIndex(String indexName) {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        try {
        	AcknowledgedResponse deleteResponse = this.client.indices().delete(request, RequestOptions.DEFAULT);
            System.out.println("Index deleted: " + deleteResponse.isAcknowledged());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* public static void main(String[] args) {
        MoreLikeThisSearch search = new MoreLikeThisSearch("localhost", 9200, "http");
        Path queryFilePath = Paths.get("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/functionStr/5/selected/57467_108_137.java");
        try {
        	int numberOfDocuments = 15; 
        	String queryContent = new String(Files.readAllBytes(queryFilePath));
            search.executeMLTQuery(numberOfDocuments, "eval_sample_index",  queryContent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                search.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } */
}
