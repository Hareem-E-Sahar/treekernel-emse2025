package sample.evaluation.util;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.HashSet;


public class DirectoryProcessor {
    private List<Path> fileList = new ArrayList<>(); 

    public List<Path> processDirectory(Path dirPath) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    fileList.addAll(processDirectory(path)); // Recurse for subdirectories
                } else {
                    fileList.add(path); 
                }
            }
        }
        return fileList; 
    }
    
    public ArrayList<File> processDirectory2(Path dirPath) throws IOException {
    	System.out.println("processDirectory2");
    	ArrayList<File> fileList = new ArrayList<>(); 
	try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
		for (Path path : stream) {
		    if (Files.isDirectory(path)) {
		        fileList.addAll(processDirectory2(path)); // Use processDirectory2 to recurse
		    } else {
		        fileList.add(path.toFile()); // Convert Path to File and add to list
		    }
		}
	    }
	return fileList; 
    }

	public static ArrayList<File> findCommonFilesByName2(ArrayList<File> list1, ArrayList<File> list2) {
	    
	    ArrayList<File> commonFiles = new ArrayList<>();
	    HashSet<String> fileNamesSet = new HashSet<>();

	    // Add all file names from the first list to the HashSet
	    for (File file : list1) {
		fileNamesSet.add(file.getName());
	    }

	    // Check for common files in the second list
	    for (File file : list2) {
		if (fileNamesSet.contains(file.getName())) {
		    commonFiles.add(file);
		}
	    }
		
    	    System.out.println("common files size:"+commonFiles.size());
    	    return commonFiles;
	}

    
    public static void main(String[] args) {
    	int functionality = 0;
    	 DirectoryProcessor processor = new DirectoryProcessor();
         Path dataDirPath = Paths.get(System.getProperty("user.home"), "UofA2023", "Tree_Kernel2024", "BigCloneEval","ijadataset");
         Path codesDirPath = dataDirPath.resolve("bcb_reduced").resolve(String.valueOf(functionality));


        try {
            List<Path> files = processor.processDirectory(codesDirPath);
            //files.forEach(System.out::println); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

