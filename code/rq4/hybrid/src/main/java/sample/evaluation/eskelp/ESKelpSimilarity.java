package sample.evaluation.eskelp;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import sample.evaluation.elasticsearch.ESFileIndexer;
import sample.evaluation.elasticsearch.SimilarityMap;
import sample.evaluation.kelp.TKSimilarity;
import sample.evaluation.util.FileMapper;

import org.elasticsearch.search.SearchHit;
import org.apache.http.HttpHost;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ESKelpSimilarity {
    @SuppressWarnings("deprecation")
	private RestHighLevelClient client;
    
    SimilarityMap simMap;
	float[] selfSimilarities;
	
   	
	@SuppressWarnings("deprecation")
	public ESKelpSimilarity(RestHighLevelClient client) {
        this.client = client;
        
        simMap = new SimilarityMap();
       
    }

	public SimilarityMap getSimMap() {
		return simMap;
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
    
    public List<SearchHit> executeMLTQuery(int numDocs, String indexName, String queryContent) throws IOException {
        // The fields to search against
        String[] fields = {"content"};

        MoreLikeThisQueryBuilder moreLikeThisQuery = QueryBuilders.moreLikeThisQuery(
            fields, 
            new String[] { queryContent }, 
            null // Optional query builder for additional parameters
        ).minTermFreq(1)    // Min number of times a term must appear in the document
         .minDocFreq(1);    // Min number of docs that must contain the term

        // Build the search request
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(moreLikeThisQuery);
        sourceBuilder.size(numDocs); 
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        // Execute the search
        SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
        return List.of(searchResponse.getHits().getHits());
        
    }
   
    public void findSimilarDocs(int numDocs,String indexName,List<File> funStrFiles,List<File> sampledFiles,String sexprDir,List<File> sexprFiles,String kernel) { 
    	Map<File, File> mapping = FileMapper.getMapping(sexprFiles,funStrFiles);
		
    	for (File queryfile : sampledFiles) {
    		System.out.println("\nProcessing file: " + queryfile.getName()+" "+queryfile.getAbsolutePath());
		
		//Need funStr path instead of sexpr path - method2 
        	//Optional<File> result = FileMapper.findFileByName(funStrFiles, queryfile.getName());
		//Path esQueryPath = result.orElse(null).toPath();  // Will be null if not found
		
		//Need funStr path instead of sexpr path - method1 
		File file = FileMapper.getFileByName(mapping,queryfile.getName()); //This gives funstr path
		Path esQueryPath = file.toPath();
		
    		try {
		    	String queryContent = new String(Files.readAllBytes(esQueryPath));
		    	List<SearchHit> results = executeMLTQuery(numDocs,indexName,queryContent);
		    	System.out.println("Hits:"+results.size());
            	
		    	for (SearchHit hit : results) { 
		            //System.out.println("Found document with ID: " + hit.getId());
		            //System.out.println("Source: " + hit.getSourceAsString());
		            Map<String, Object> source = hit.getSourceAsMap();
		            String absolutePath = (String) source.get("filepath");
		            Path path = Paths.get(absolutePath);
		            String fileName = path.getFileName().toString();
		            float esSimilarityScore = hit.getScore(); 
		            
		            if (queryfile.getName().equals(fileName)) {
		            	continue;	 //exclude self matches
		            }
		            //KELP Re-ranking SubSetTreeKernel
		            float kelpSimilarity = TKSimilarity.computeSimilarity(sexprDir+queryfile.getName(),sexprDir+fileName,kernel);
			    float normalizationFactor = 1; //TKSimilarity.computeNormalization(sexprDir+queryfile.getName(),sexprDir+fileName);
			    
			    simMap.addSimilarity(queryfile.getName(), fileName, kelpSimilarity/normalizationFactor);
			   
		            System.out.println("Hit:"+fileName+" Similarity score:" + esSimilarityScore+" kelp similarity:"+kelpSimilarity/normalizationFactor);
		            	
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
        	int numDocs = 15; 
        	String queryContent = new String(Files.readAllBytes(queryFilePath));
            search.executeMLTQuery(numDocs, "eval_sample_index",  queryContent);
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
