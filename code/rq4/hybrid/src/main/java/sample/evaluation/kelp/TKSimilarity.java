package sample.evaluation.kelp;

import sample.evaluation.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.List;

import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.kernel.tree.PartialTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubSetTreeKernel;

public class TKSimilarity {
	SimilarityMap simMap;
	float[] selfSimilarities;
	String kernel;
   
	public TKSimilarity() {
		this.kernel = kernel;
		simMap = new SimilarityMap();
	}
	
	public SimilarityMap getSimMap() {
		return simMap;
	}
	
	public void findClones( List<File> sexprFiles, List<File> sampledFiles) {	
		int i=0;int j=0;
		int total_computations = 0;
		selfSimilarities = computeSelfSimilarity(sexprFiles);
		if (sexprFiles == null || sampledFiles == null) {
		    System.out.println("Empty directoy or not enough files.");
		    return;
		}
		System.out.println("sampledFiles.size():"+sampledFiles.size());
        
       	for (i = 0; i < sampledFiles.size(); i++ ) {
        	   for ( j = 0; j < sexprFiles.size(); j++ ) {
		        File file1 = sampledFiles.get(i);
		        File file2 = sexprFiles.get(j);
		        //System.out.println(file1+" "+file2);
		        if ( file1.equals(file2) ) {
		        	continue;
		        }
		        try {
		        	float similarity = computeSimilarity(file1.getAbsolutePath(),file2.getAbsolutePath(),this.kernel);
		        	total_computations++;
		        	//float normalizationFactor = 1; // not normalizing
				float normalizationFactor = (float) (Math.sqrt(selfSimilarities[i]) * Math.sqrt(selfSimilarities[j]));
				//System.out.println(file1.getName()+"---"+file2.getName()+"---"+similarity+"---"+similarity/normalizationFactor);
				simMap.addSimilarity(file1.getName(), file2.getName(), similarity/normalizationFactor);
							           
		        } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            	}
            	System.out.println("Total:"+total_computations);
            }
    	}
	/*public static float computeNormalization(String filePath,String filePath2) {
		float sim1 = computeSimilarity(filePath,filePath);
		float sim2 = computeSimilarity(filePath2,filePath2);
		return (float) (Math.sqrt(sim1) * Math.sqrt(sim2));
	}*/
	
	public float[] computeSelfSimilarity(List<File> listOfFiles) {
		selfSimilarities = new float[listOfFiles.size()]; 
		for (int i = 0; i < listOfFiles.size(); i++) {
	        	File file1 = listOfFiles.get(i);
			try {
			    selfSimilarities[i] = computeSimilarity(file1.getAbsolutePath(), file1.getAbsolutePath(),this.kernel);
			    // adding self similarity will bring MRR down because self is on top
			    // simMap.addSimilarity(file1.getName(), file1.getName(), selfSimilarities[i]/selfSimilarities[i]);
			
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
		return selfSimilarities;
	}
	
	public static float computeSimilarity(String qfile, String otherfile, String kernel) {
		float similarity = 0;
		TreeRepresentation t1    = new TreeRepresentation();
		TreeRepresentation t2    = new TreeRepresentation();
		
		//Note: If I compute PTK using code in this class it will give most values as NaN because during
		//normalization all values get divided by self similariy which is Nan.
		//PartialTreeKernel treeKernel = new PartialTreeKernel(1f,1f,1,"tree");	//MRR=0.571
		//SubTreeKernel treeKernel = new SubTreeKernel(1f,"tree");    			//MRR=0.75   or 0.771 without normalization
		KernelWrapper treeKernel = TreeKernelFactory.createKernel(kernel, 0.4f, "tree");
	    	
		if (treeKernel != null) {
		
		
			try {
				String queryData = Util.read_sexpr_as_string(qfile);
			    	String otherData = Util.read_sexpr_as_string(otherfile);

				if (queryData == null || otherData == null) {
				    similarity = -1; 				// No data available 
				    System.out.println(qfile+" or "+otherfile+" is null");
				    return similarity;
				} 

				t1.setDataFromText(queryData);
				t2.setDataFromText(otherData);
				similarity = treeKernel.kernelComputation(t1, t2);	    
			} catch (IOException e) {
				System.err.println("Error reading files: " + e.getMessage());
				similarity = -1;
		    	} catch (Exception e) {
		       	e.printStackTrace();
				similarity = -1; 
		   	}
		}
	    	return similarity;	
	}	
}
