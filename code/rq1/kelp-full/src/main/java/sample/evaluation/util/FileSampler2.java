package sample.evaluation.util;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FileSampler2 {
    public static List<File> sampleFiles(List<File> allFiles,long seed, int sampleSize) {
        Random random = new Random(seed);
        // Shuffle the list of files with the seeded random
        Collections.shuffle(allFiles, random);
        // Take a subset of the first 'sampleSize' elements
        List<File> sampledFiles = allFiles.subList(0, Math.min(sampleSize, allFiles.size()));
        return sampledFiles;
    }
}

