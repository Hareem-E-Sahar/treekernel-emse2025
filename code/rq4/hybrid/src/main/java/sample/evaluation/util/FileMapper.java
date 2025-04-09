package sample.evaluation.util;
import java.io.File;
import java.util.*;

public class FileMapper {
	
    public static List<File> getAllFiles(File folder) {
        List<File> files = new ArrayList<>();
        File[] filesTemp = folder.listFiles();

        if (filesTemp != null) {
            for (File file : filesTemp) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    files.addAll(getAllFiles(file)); // Recursive call for subdirectories
                }
            }
        }
        return files;
    }

    public static Map<File, File> getMapping(List<File> sampledFiles,List<File> funStrFiles ) {
        Map<File, File> fileMapping = new HashMap<>();
        // For each file in sexprDir, find its matching file in funStrDir by name
        for (File sexprFile : sampledFiles) {
            String sexprFileName = sexprFile.getName();

            for (File funStrFile : funStrFiles) {
                String funStrFileName = funStrFile.getName();

                if (sexprFileName.equals(funStrFileName)) {
                    fileMapping.put(sexprFile, funStrFile);
                    break;  
                }
            }
        }
        return fileMapping;
    }
    
    public static File getFileByName(Map<File, File> fileMapping, String fileName) {
        // Iterate over the map to find the file based on file name
        for (Map.Entry<File, File> entry : fileMapping.entrySet()) {
            if (entry.getKey().getName().equals(fileName)) {
                // If the file is found in sexprDir, return the mapped file from funStrDir
                return entry.getValue();
            } else if (entry.getValue().getName().equals(fileName)) {
                // If the file is found in funStrDir, return the mapped file from sexprDir
                return entry.getKey();
            }
        }
        return null; //if no match is found
    }
    
    //just an alternate way of implementing getFileByName
    public static Optional<File> findFileByName(List<File> files, String targetFileName) {
        return files.stream()
                    .filter(file -> file.getName().equals(targetFileName))
                    .findFirst();  // Returns an Optional, empty if not found
    }


    public static void main(String[] args) {
    	String f1 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/52202_46_54.java";
    	String f2 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/583742_215_246.java";
    	String f3 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/155262_261_288.java";
    	String f4 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/380075_487_515.java";
    	String f5 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/402336_95_123.java";
    	String f6 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/1192265_31_55.java";
    	String f7 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/87199_3_21.java";
    	String f8 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/2109789_336_360.java";
    	String f9 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/101041_17_53.java";
    	String f10 = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/SetUpScrollingGraphicalViewer_2_5.java";
    	
    	List<File> initialSample = new ArrayList<File>();
    	initialSample.add(new File(f1));
    	initialSample.add(new File(f2));
    	initialSample.add(new File(f3));
    	initialSample.add(new File(f4));
    	initialSample.add(new File(f5));
    	initialSample.add(new File(f6));
    	initialSample.add(new File(f7));
    	initialSample.add(new File(f8));
    	initialSample.add(new File(f9));
    	initialSample.add(new File(f10));

    	
	
	String funStrFolder = "/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/functionStr/0";
	List<File> funStrFiles = FileMapper.getAllFiles(new File(funStrFolder));  //Get all files recursively
	System.out.println(funStrFiles.size());
	
    	Map<File, File> mapping = FileMapper.getMapping(initialSample,funStrFiles);
    	File file = new File("/home/hareem/UofA2023/Tree_Kernel2024/BigCloneEval/ijadataset/sexpr_from_gumtree/0/2109789_336_360.java");
    	System.out.println(file.getName());
    	File file1 = FileMapper.getFileByName(mapping,file.getName());
    	System.out.println(file1);
						
    }
}
