package sample.evaluation.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeClones {
	String codeDir = null;
	
    	public TypeClones(String codeDir) {
		this.codeDir = codeDir;
	}

	public static Set<String>  getTypeWiseFiles(String typewiseFile, String codeDir) throws Exception {
		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
		ArrayList<String> uniqueFiles = new ArrayList<>();
		String currentline;
		String fileName="", fileClone="";
		int startLine1=0, startLine2=0;
		int endLine1=0, endLine2=0;
       
     
    		BufferedReader dsLookup = new BufferedReader(new FileReader(typewiseFile));
    		String header = dsLookup.readLine();  
    		while ((currentline = dsLookup.readLine())!= null) {
        		String[] lineArray = currentline.split(",");
        		
        		try {
			    fileName   = lineArray[1];
			    startLine1 = Integer.parseInt(lineArray[2]);
			    endLine1   = Integer.parseInt(lineArray[3]);
			    
			    fileClone  = lineArray[5];
			    startLine2 = Integer.parseInt(lineArray[6]);
			    endLine2   = Integer.parseInt(lineArray[7]);
			    
			    File f1 = new File(codeDir + lineArray[0] + "/" + fileName); // bcb_reduced/9/selected/filename.java
			    File f2 = new File(codeDir + lineArray[4] + "/" + fileClone);
	            
			    if(f1.exists() && f2.exists()) 	{//exists in bcb_reduced/9/selected or not
			    	String strFileName  = fileName.substring(0, fileName.lastIndexOf('.'))+"_"+startLine1+"_"+endLine1;
				String strCloneName = fileClone.substring(0,fileClone.lastIndexOf('.'))+"_"+startLine2+"_"+endLine2;
				   
				uniqueFiles.add(strFileName+".java");
		            	uniqueFiles.add(strCloneName+".java");
			    }   
            		} catch(ArrayIndexOutOfBoundsException e) {
        	  		e.printStackTrace();
            		}
    		}
	    	dsLookup.close();
	    	Set<String> uniqueSet = new HashSet<>(uniqueFiles);
	    	//System.out.println("Unique function string extracted:"+uniqueSet.size());
		return uniqueSet;
    }
}
