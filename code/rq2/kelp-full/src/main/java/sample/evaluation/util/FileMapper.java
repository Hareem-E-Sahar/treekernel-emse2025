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

    public static Map<File, File>  getMapping(List<File> sampledFiles,List<File> funStrFiles ) {
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
        // Return null if no match is found
        return null;
    }

    public static void main(String[] args) {
            
	}
}
