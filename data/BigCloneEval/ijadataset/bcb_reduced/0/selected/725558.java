package org.hironico.scrat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author hironico
 */
public class ScratBigFileSorter implements Runnable {

    protected String workingFileName = "SCRAT_tempfile_";

    protected int iterationNum = 0;

    protected SortedMap<String, String> sortedMap = new TreeMap<String, String>();

    protected File sourceFile;

    protected File destinationFile;

    protected File workingDirectory;

    public enum ChunkSizeUnit {

        NUMBER_OF_LINES, PERCENTAGE_OF_ORIGINAL
    }

    protected Integer maxChunkSize = 10000;

    protected ChunkSizeUnit maxChunkSizeUnit = ChunkSizeUnit.NUMBER_OF_LINES;

    protected List<ScratBigFileSorterListener> listeners = new ArrayList<ScratBigFileSorterListener>();

    protected boolean zipOutputFile = false;

    protected boolean dropTempFiles = false;

    protected int[] sourceFileColumnKeyIndexes = new int[0];

    public ScratBigFileSorter(File sourceFile, File destinationFile, File workingDirectory, int[] sourceFileColumnKeyIndexes) {
        this.sourceFile = sourceFile;
        this.destinationFile = destinationFile;
        this.workingDirectory = workingDirectory;
        this.sourceFileColumnKeyIndexes = sourceFileColumnKeyIndexes;
        assert workingDirectory.isDirectory() : "The provided working directory does not exist !";
    }

    public Integer getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(Integer maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public ChunkSizeUnit getMaxChunkSizeUnit() {
        return maxChunkSizeUnit;
    }

    public void setMaxChunkSizeUnit(ChunkSizeUnit maxChunkSizeUnit) {
        this.maxChunkSizeUnit = maxChunkSizeUnit;
    }

    public boolean isZipOutputFile() {
        return zipOutputFile;
    }

    public void setZipOutputFile(boolean zipOutputFile) {
        this.zipOutputFile = zipOutputFile;
    }

    public boolean isDropTempFiles() {
        return dropTempFiles;
    }

    public void setDropTempFiles(boolean dropTempFiles) {
        this.dropTempFiles = dropTempFiles;
    }

    public void addListener(ScratBigFileSorterListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeListener(ScratBigFileSorterListener listener) {
        listeners.remove(listener);
    }

    protected void fireStarted() {
        for (ScratBigFileSorterListener listener : listeners) listener.started();
    }

    protected void fireStopped() {
        for (ScratBigFileSorterListener listener : listeners) listener.stopped();
    }

    protected void fireChunkStarted(String fileName) {
        for (ScratBigFileSorterListener listener : listeners) listener.startedChunk(fileName);
    }

    protected void fireChunkStopped(String fileName) {
        for (ScratBigFileSorterListener listener : listeners) listener.stoppedChunk(fileName);
    }

    protected String generateKeyFromLine(String ligne) {
        String[] columns = ligne.split(";");
        String key = "";
        for (Integer index : sourceFileColumnKeyIndexes) {
            key += columns[index];
        }
        return key;
    }

    protected ElementDescriptor getSmallestDescriptor(SortedMap<String, ElementDescriptor> sortedMap) {
        SortedSet<String> copy = new TreeSet(sortedMap.keySet());
        String smallestKey = copy.iterator().next();
        return sortedMap.get(smallestKey);
    }

    protected void mergeSortedFiles(File outFile) throws IOException {
        SortedMap<String, ElementDescriptor> sortedDescriptors = new TreeMap<String, ElementDescriptor>();
        for (int index = 0; index <= iterationNum; index++) {
            String fileName = workingDirectory.getPath() + File.separator + workingFileName + index + ".csv";
            System.out.println("Adding file to merge: " + fileName);
            File file = new File(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            ElementDescriptor desc = new ElementDescriptor();
            desc.ligne = reader.readLine();
            desc.reader = reader;
            desc.fileName = fileName;
            desc.key = generateKeyFromLine(desc.ligne);
            sortedDescriptors.put(desc.key, desc);
        }
        OutputStream fos = new FileOutputStream(outFile);
        if (zipOutputFile) {
            fos = new ZipOutputStream(fos);
            ((ZipOutputStream) fos).setLevel(9);
            int index = outFile.getName().toLowerCase().indexOf(".zip");
            ZipEntry entry = new ZipEntry(outFile.getName().substring(0, index));
            ((ZipOutputStream) fos).putNextEntry(entry);
        }
        while (sortedDescriptors.size() > 0) {
            ElementDescriptor smallestDesc = getSmallestDescriptor(sortedDescriptors);
            fos.write((smallestDesc.ligne + "\n").getBytes());
            sortedDescriptors.remove(smallestDesc.key);
            smallestDesc.ligne = smallestDesc.reader.readLine();
            if (smallestDesc.ligne != null) {
                smallestDesc.key = generateKeyFromLine(smallestDesc.ligne);
                sortedDescriptors.put(smallestDesc.key, smallestDesc);
            } else {
                smallestDesc.reader.close();
                if (dropTempFiles) {
                    File descFile = new File(smallestDesc.fileName);
                    if (!descFile.delete()) System.out.println("Chunk file: '" + smallestDesc.fileName + "' could not be deleted. Still opened?"); else System.out.println("Chunk file: '" + smallestDesc.fileName + "' has been deleted properly.");
                }
            }
        }
        fos.flush();
        fos.close();
    }

    protected void flushSortedMap() throws IOException {
        String fileName = workingDirectory.getPath() + File.separator + workingFileName + Integer.toString(iterationNum) + ".csv";
        System.out.println("Starting writing of sorted file: " + fileName);
        long startTime = System.currentTimeMillis();
        File outFile = new File(fileName);
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
        for (String key : sortedMap.keySet()) {
            out.write(sortedMap.get(key) + "\n");
        }
        out.flush();
        out.close();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long throughput = duration / sortedMap.size();
        System.out.println("Finished writing output file in " + duration + " ms");
        System.out.println("Write rate: " + throughput + " ms per line.");
    }

    @Override
    public void run() {
        if (!sourceFile.exists()) {
            System.out.println("Error, file not found: '" + sourceFile.getPath() + "'");
            return;
        }
        try {
            fireStarted();
            System.out.println("Starting file reading...");
            long startTime = System.currentTimeMillis();
            BufferedReader in = new BufferedReader(new FileReader(sourceFile));
            String ligne = in.readLine();
            iterationNum = 0;
            long totalByteRead = ligne.length();
            long sourceFileSize = sourceFile.length();
            long percentageMaxChunkSize = sourceFileSize * maxChunkSize / 100;
            boolean startNextChunk = false;
            fireChunkStarted("Chunk file #" + iterationNum);
            while (ligne != null) {
                switch(maxChunkSizeUnit) {
                    case PERCENTAGE_OF_ORIGINAL:
                        startNextChunk = totalByteRead >= percentageMaxChunkSize;
                        break;
                    case NUMBER_OF_LINES:
                        startNextChunk = sortedMap.size() > maxChunkSize;
                        break;
                }
                if (startNextChunk) {
                    flushSortedMap();
                    fireChunkStopped("Chunk file #" + iterationNum);
                    sortedMap.clear();
                    totalByteRead = 0;
                    iterationNum++;
                    fireChunkStarted("Chunk file #" + iterationNum);
                }
                String key = generateKeyFromLine(ligne);
                sortedMap.put(key, ligne);
                ligne = in.readLine();
                if (ligne != null) totalByteRead += ligne.length();
            }
            flushSortedMap();
            fireChunkStopped("Chunk file #" + iterationNum);
            in.close();
            long endTime = System.currentTimeMillis();
            System.out.println("Finished reading file.");
            long duration = endTime - startTime;
            long throughput = duration / sortedMap.size();
            System.out.println("Read file in: " + duration + " ms.");
            System.out.println("Read rate: " + throughput + " ms per line.");
            if (destinationFile.exists()) {
                System.out.println("WARNING: Destination file will be overwritten: '" + destinationFile.getPath() + "'");
            }
            System.out.println("Starting writing final file to '" + destinationFile + ".");
            startTime = System.currentTimeMillis();
            mergeSortedFiles(destinationFile);
            endTime = System.currentTimeMillis();
            duration = endTime - startTime;
            System.out.println("Finished writing final file in " + duration + " ms.");
            fireStopped();
            System.out.println("End of Scrat Big File Sorter.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
    }
}
