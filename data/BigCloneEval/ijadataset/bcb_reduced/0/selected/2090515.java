package trans.main;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import util.gen.*;

/**Runs entire TiMAT2 suite based on a parameter file.*/
public class T2 {

    private File parameterFile;

    private T2Parameter parameters;

    private boolean cluster = false;

    private ChromSet[] replicas;

    private int replicaIndex;

    private File oligoPositions;

    private File[] treatmentReplicas;

    private File[] controlReplicas;

    private File windowDirectory;

    private File enrichedWindows;

    private File reducedWindows = null;

    private File intervalDirectory = null;

    public T2(String[] args) {
        processArgs(args);
        System.out.println("\nParsing your T2 parameter file...");
        parameters = new T2Parameter(parameterFile);
        if (parameters.getTreatmentCelFiles()[0][0].getName().endsWith("cela") == false) {
            System.out.println("\nConverting text xxx.cel files to binary xxx.cela files...");
            convertCelFiles();
            parameters.renameCelFiles();
        } else System.out.println("\nCel files already converted to xxx.cela...");
        System.out.println("\nNormalizing xxx.cela files...");
        if (parameters.isMedianScaleUsingControlSeqs()) System.out.println("\tMedian scaling based on the intensities of the control sequences."); else System.out.println("\tMedian scaling based on all of the array intensities.");
        if (parameters.isQuantileNormalizeAll()) System.out.println("\tQuantile normalizing across treatment and control files."); else System.out.println("\tQuantile normalizing treatment and control files separately.");
        normalizeCelFiles();
        System.out.println("\nMaking chromosome sets...");
        makeChromosomeSets();
        deleteSplitNormalizedFiles();
        splitChromSetArray();
        System.out.println("\nScanning chromosomes...");
        scanChromosomes();
        System.out.println("\nMerging Window arrays...");
        mergeWindows();
        if (parameters.getNumberOfIntervals() != null) {
            System.out.println("\nMaking Interval arrays...");
            setNumberIntervalMaker();
            System.out.println("\nLoading Interval arrays...");
            loadIntervalArrays();
            if (parameters.getSubWindowSize() != 0) {
                System.out.println("\nFinding Interval peaks...");
                findSubBindingRegions();
            }
            if (parameters.getRepeatRegionFiles() != null) {
                System.out.println("\nFlagging Intervals with repetative, low complexity, and simple repeats...");
                filterIntervals();
            }
            System.out.println("\nPrinting Interval spread sheet reports...");
            printSpreadSheets();
            System.out.println("\nPrinting Interval graphs...");
            printIntervalGraphs();
        }
        System.out.println("\nT2 is Done!\n");
    }

    /**Returns the number of intervals that remain to be made.*/
    public int numberOfIntervalsToMake() {
        String[] numInts = parameters.getNumberOfIntervals().split(",");
        int number = numInts.length;
        if (reducedWindows != null && reducedWindows.exists()) number *= 2;
        File dir = enrichedWindows.getParentFile();
        String[] fileNames = dir.list();
        int numMade = 0;
        for (int i = 0; i < fileNames.length; i++) {
            if (fileNames[i].indexOf("Indx") != -1) numMade++;
        }
        return number - numMade;
    }

    /**File management for intervals.*/
    public void makeAndMoveIntervals() {
        intervalDirectory = new File(parameters.getResultsDirectory(), "Intervals");
        if (intervalDirectory.exists() == false) intervalDirectory.mkdir();
        File[] files = enrichedWindows.getParentFile().listFiles();
        Pattern pat = Pattern.compile(".+Indx(\\d+)$");
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.indexOf("Indx") != -1) {
                String prefix = "en";
                if (name.startsWith("reduced")) prefix = "rd";
                Matcher mat = pat.matcher(name);
                mat.matches();
                String numInts = mat.group(1);
                File moveTo = new File(intervalDirectory, prefix + parameters.getResultsName() + numInts);
                files[i].renameTo(moveTo);
            }
        }
    }

    /**Creates set number of intervals.*/
    public void setNumberIntervalMaker() {
        if (enrichedWindows.exists() == false || (parameters.isMakeReducedWindows() && reducedWindows.exists() == false)) {
            Misc.printExit("\nError: cannot find merged window array(s)?\n");
        }
        ArrayList command = new ArrayList();
        command.add(parameters.getAppsDirectory() + "/SetNumberIntervalMaker");
        command.add("-a");
        command.add("-s");
        command.add("1");
        command.add("-o");
        command.add(parameters.getMinimumNumberOfOligos() + "");
        command.add("-z");
        command.add(parameters.getSizeOfOligo() + "");
        command.add("-m");
        command.add(parameters.getMaxGap() + "");
        command.add("-n");
        String[] numOfIntsToMake = null;
        if (cluster) {
            numOfIntsToMake = parameters.getNumberOfIntervals().split(",");
        } else {
            numOfIntsToMake = new String[] { parameters.getNumberOfIntervals() };
        }
        for (int i = 0; i < numOfIntsToMake.length; i++) {
            ArrayList all = new ArrayList();
            all.addAll(command);
            all.add(numOfIntsToMake[i]);
            all.add("-f");
            all.add(enrichedWindows.toString());
            launchJQSub(all, parameters.getMaxMemory());
            if (reducedWindows != null && reducedWindows.exists()) {
                all.clear();
                all.addAll(command);
                all.add(numOfIntsToMake[i]);
                all.add("-f");
                all.add(reducedWindows.toString());
                launchJQSub(all, parameters.getMaxMemory());
            }
        }
        int timer = 120;
        System.out.print("\t# to make ");
        while (timer != 0) {
            int numToMake = numberOfIntervalsToMake();
            System.out.print(numToMake + " ");
            if (numToMake == 0) break;
            timer--;
            Misc.sleep(60);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out on making intervals.\n"); else {
            makeAndMoveIntervals();
        }
    }

    /**Creates set number of intervals.*/
    public void loadIntervalArrays() {
        ArrayList command = new ArrayList();
        command.add(parameters.getAppsDirectory() + "/LoadChipSetIntervalOligoInfo");
        if (parameters.getGenomeFastaDirectory() != null) {
            command.add("-s");
            command.add(parameters.getGenomeFastaDirectory().toString());
        }
        command.add("-o");
        command.add(oligoPositions.toString());
        command.add("-i");
        File[] intervalFiles = IO.extractOnlyFiles(intervalDirectory);
        int numToLoad = intervalFiles.length;
        String treatmentDirs = IO.concatinateFileFullPathNames(treatmentReplicas, ",");
        String controlDirs = IO.concatinateFileFullPathNames(controlReplicas, ",");
        for (int i = 0; i < numToLoad; i++) {
            ArrayList all = new ArrayList();
            all.addAll(command);
            all.add(intervalFiles[i].toString());
            String t = "-t";
            String c = "-c";
            if (intervalFiles[i].toString().startsWith("rd")) {
                t = "-c";
                c = "-t";
            }
            all.add(t);
            all.add(treatmentDirs);
            all.add(c);
            all.add(controlDirs);
            launchJQSub(all, parameters.getMaxMemory());
        }
        int timer = 240;
        System.out.print("\t# to load ");
        while (timer != 0) {
            int num = numToLoad - IO.numberFilesExist(intervalDirectory, "Ld");
            System.out.print(num + " ");
            if (num == 0) break;
            timer--;
            Misc.sleep(60);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out on loading intervals.\n"); else {
            IO.deleteFilesNotEndingInExtension(intervalDirectory, "Ld");
            IO.removeExtension(intervalDirectory, "Ld");
        }
    }

    /**Creates set number of intervals.*/
    public void findSubBindingRegions() {
        ArrayList command = new ArrayList();
        command.add(parameters.getAppsDirectory() + "/FindSubBindingRegions");
        command.add("-w");
        command.add(parameters.getSubWindowSize() + "");
        command.add("-n");
        command.add(parameters.getMinNumberOligosInSubWin() + "");
        command.add("-s");
        command.add(parameters.getPeakPickerWindowSize() + "");
        command.add("-m");
        command.add(parameters.getMaxNumberPeaks() + "");
        command.add("-i");
        File[] intervalFiles = IO.extractOnlyFiles(intervalDirectory);
        int numToSub = intervalFiles.length;
        for (int i = 0; i < numToSub; i++) {
            ArrayList all = new ArrayList();
            all.addAll(command);
            all.add(intervalFiles[i].toString());
            launchJQSub(all, parameters.getMaxMemory());
        }
        int timer = 120;
        System.out.print("\t# to pick peaks ");
        while (timer != 0) {
            int num = numToSub - IO.numberFilesExist(intervalDirectory, "Sub");
            System.out.print(num + " ");
            if (num == 0) break;
            timer--;
            Misc.sleep(60);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out picking interval peaks.\n"); else {
            IO.deleteFilesNotEndingInExtension(intervalDirectory, "Sub");
            IO.removeExtension(intervalDirectory, "Sub");
        }
    }

    /**Prints interval reports in a spread sheet via IntervalReportPrinter.*/
    public void printSpreadSheets() {
        ArrayList command = new ArrayList();
        command.add(parameters.getAppsDirectory() + "/IntervalReportPrinter");
        command.add("-i");
        command.add("1");
        command.add("-a");
        command.add("-b");
        command.add("-f");
        File[] intervalFiles = IO.extractFiles(intervalDirectory);
        int numToProc = intervalFiles.length;
        for (int i = 0; i < numToProc; i++) {
            ArrayList all = new ArrayList();
            all.addAll(command);
            all.add(intervalFiles[i].toString());
            launchJQSub(all, parameters.getMaxMemory());
        }
        int timer = 60;
        System.out.print("\t# to print ");
        while (timer != 0) {
            int num = numToProc - IO.numberFilesExist(intervalDirectory, "xls");
            System.out.print(num + " ");
            if (num == 0) break;
            timer--;
            Misc.sleep(20);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out printing Intervals spread sheet reports.\n"); else {
            File spreadSheetDir = new File(intervalDirectory, "SpreadSheetReports");
            if (spreadSheetDir.exists() == false) spreadSheetDir.mkdir();
            File[] res = IO.extractFiles(intervalDirectory, ".xls");
            for (int i = 0; i < res.length; i++) {
                File moved = new File(spreadSheetDir, res[i].getName());
                res[i].renameTo(moved);
            }
        }
    }

    /**Prints interval bed and sgr files via IntervalGraphPrinter.*/
    public void printIntervalGraphs() {
        ArrayList command = new ArrayList();
        command.add(parameters.getAppsDirectory() + "/IntervalGraphPrinter");
        command.add("-f");
        File[] intervalFiles = IO.extractOnlyFiles(intervalDirectory);
        int numToProc = intervalFiles.length;
        for (int i = 0; i < numToProc; i++) {
            ArrayList all = new ArrayList();
            all.addAll(command);
            all.add(intervalFiles[i].toString());
            launchJQSub(all, parameters.getMaxMemory());
        }
        int timer = 60;
        System.out.print("\t# to graph ");
        while (timer != 0) {
            int num = numToProc - IO.numberFilesExist(intervalDirectory, ".bed");
            System.out.print(num + " ");
            if (num == 0) break;
            timer--;
            Misc.sleep(20);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out printing Intervals graphs.\n"); else {
            File dir = new File(intervalDirectory, "Sgr");
            if (dir.exists() == false) dir.mkdir();
            File[] res = IO.extractFiles(intervalDirectory, ".sgr.zip");
            for (int i = 0; i < res.length; i++) {
                File moved = new File(dir, res[i].getName());
                res[i].renameTo(moved);
            }
            dir = new File(intervalDirectory, "Bed");
            if (dir.exists() == false) dir.mkdir();
            res = IO.extractFiles(intervalDirectory, ".bed");
            for (int i = 0; i < res.length; i++) {
                File moved = new File(dir, res[i].getName());
                res[i].renameTo(moved);
            }
        }
    }

    /**Creates set number of intervals.*/
    public void filterIntervals() {
        ArrayList command = new ArrayList();
        command.add(parameters.getAppsDirectory() + "/IntervalFilter");
        command.add("-i");
        command.add("1");
        command.add("-m");
        command.add("1.5");
        command.add("-e");
        command.add(IO.concatinateFileFullPathNames(parameters.getRepeatRegionFiles(), ","));
        command.add("-k");
        File[] intervalFiles = IO.extractFiles(intervalDirectory);
        int numToProc = intervalFiles.length;
        for (int i = 0; i < numToProc; i++) {
            ArrayList all = new ArrayList();
            all.addAll(command);
            all.add(intervalFiles[i].toString());
            launchJQSub(all, parameters.getMaxMemory());
        }
        int timer = 600;
        System.out.print("\t# to filter ");
        while (timer != 0) {
            int num = numToProc - IO.numberFilesExist(intervalDirectory, "Good");
            System.out.print(num + " ");
            if (num == 0) break;
            timer--;
            Misc.sleep(60);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out filtering intervals.\n"); else {
            IO.deleteFilesNotEndingInExtension(intervalDirectory, "Good");
            IO.removeExtension(intervalDirectory, "Good");
        }
    }

    /**Creats one or two window merged window arrays containing all of the chromosomes.*/
    public void mergeWindows() {
        if (windowDirectory.exists() == false) Misc.printExit("\nError: cannot merge windows, the window directory does not exist. " + windowDirectory + "\n");
        ArrayList command = new ArrayList();
        command.add(parameters.getAppsDirectory() + "/MergeWindowArrays");
        command.add("-r");
        command.add("-t");
        command.add("0.1");
        command.add("-i");
        command.add("1");
        command.add("-f");
        command.add(windowDirectory.toString());
        command.add("-n");
        enrichedWindows = new File(windowDirectory, "enrichedWindows");
        if (enrichedWindows.exists() == false) {
            ArrayList all = new ArrayList();
            all.addAll(command);
            all.add(enrichedWindows.toString());
            launchJQSub(all, parameters.getMaxMemory());
        }
        if (parameters.isMakeReducedWindows()) {
            reducedWindows = new File(windowDirectory, "reducedWindows");
            ArrayList all = new ArrayList();
            all.addAll(command);
            all.add(reducedWindows.toString());
            all.add("-m");
            launchJQSub(all, parameters.getMaxMemory());
        }
        int timer = 60;
        System.out.print("\t# to merge ");
        while (timer != 0) {
            int numToMerge = 0;
            if (enrichedWindows.exists() == false) numToMerge++;
            if (parameters.isMakeReducedWindows() && reducedWindows.exists() == false) numToMerge++;
            System.out.print(numToMerge + " ");
            if (numToMerge == 0) break;
            timer--;
            Misc.sleep(60);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out on merging split window files.\n");
    }

    /**Pulls and places replica directories to this.*/
    public void splitChromSetArray() {
        ArrayList t = new ArrayList();
        ArrayList c = new ArrayList();
        for (int i = 0; i < replicas.length; i++) {
            if (replicas[i].isTreatmentReplica()) t.add(replicas[i].getSaveDirectory()); else c.add(replicas[i].getSaveDirectory());
        }
        treatmentReplicas = new File[t.size()];
        controlReplicas = new File[c.size()];
        t.toArray(treatmentReplicas);
        c.toArray(controlReplicas);
    }

    public void scanChromosomes() {
        windowDirectory = new File(parameters.getResultsDirectory(), "Win");
        if (windowDirectory.exists() == false) windowDirectory.mkdir();
        File[] windowArrays;
        String[] chromsToProcess = null;
        if (cluster) {
            String[] chroms = findChromPairs();
            windowArrays = new File[chroms.length];
            ArrayList chromsToProcessAL = new ArrayList();
            for (int i = 0; i < chroms.length; i++) {
                String concat = chroms[i].replaceAll(",", "_").replaceAll("chr", "");
                windowArrays[i] = new File(windowDirectory, concat + "_Win");
                if (windowArrays[i].exists() == false) chromsToProcessAL.add(chroms[i]);
            }
            chromsToProcess = Misc.stringArrayListToStringArray(chromsToProcessAL);
        } else {
            windowArrays = new File[1];
            windowArrays[0] = new File(windowDirectory, "all_Win");
        }
        int numToScan = windowArrays.length - IO.numberFilesExist(windowArrays);
        if (numToScan == 0) {
            System.out.println("\tAll chromosomes scanned. Delete " + windowDirectory + " to rescan.");
            return;
        }
        ArrayList command = new ArrayList();
        command.add(parameters.getAppsDirectory() + "/ScanChromosomes");
        command.add("-o");
        command.add(oligoPositions.toString());
        command.add("-r");
        command.add(parameters.getResultsDirectory().toString());
        command.add("-t");
        command.add(IO.concatinateFileFullPathNames(treatmentReplicas, ","));
        command.add("-c");
        command.add(IO.concatinateFileFullPathNames(controlReplicas, ","));
        command.add("-v");
        command.add(parameters.getGenomeVersion());
        command.add("-k");
        command.add(parameters.getStrand());
        command.add("-z");
        command.add(parameters.getSizeOfOligo() + "");
        command.add("-w");
        command.add(parameters.getWindowSize() + "");
        command.add("-m");
        command.add(parameters.getMinimumNumberOfOligos() + "");
        if (parameters.isUsePseudoMedian()) command.add("-a");
        if (parameters.isUseRandomPermutation()) {
            if (parameters.getPermutationType().startsWith("l")) command.add("-l"); else command.add("-u");
            command.add("-n");
            command.add(parameters.getNumberOfRandomPermutations() + "");
        }
        if (parameters.isRandomizeData()) command.add("-x");
        if (parameters.isUseSymmetricNull()) {
            command.add("-p");
            command.add("-s");
            command.add(parameters.getSymmetricNullApp().toString());
            command.add("-q");
            command.add(parameters.getRApp().toString());
        }
        command.add("-j");
        command.add("-i");
        command.add("-e");
        command.add("-d");
        if (cluster) {
            command.add("-f");
            for (int i = 0; i < chromsToProcess.length; i++) {
                ArrayList all = new ArrayList();
                all.addAll(command);
                all.add(chromsToProcess[i]);
                launchJQSub(all, parameters.getMaxMemory());
            }
        } else launchJQSub(command, parameters.getMaxMemory());
        int timer = 600;
        numToScan = windowArrays.length - IO.numberFilesExist(windowArrays);
        System.out.print("\t# to scan " + numToScan);
        while (timer != 0) {
            if (numToScan == 0) break;
            timer--;
            Misc.sleep(60);
            numToScan = windowArrays.length - IO.numberFilesExist(windowArrays);
            System.out.print(" " + numToScan);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out on scanning chromosomes.\n");
    }

    /**Attempts to join smallest chromosomes together so long as the combine file doesn't exceed the size of the biggest.*/
    public String[] findChromPairs() {
        File[] files = IO.extractFiles(oligoPositions);
        FileSize[] fs = new FileSize[files.length];
        for (int i = 0; i < fs.length; i++) fs[i] = new FileSize(files[i]);
        Arrays.sort(fs);
        long biggest = fs[fs.length - 1].getSize();
        long current = 0;
        int index = 0;
        ArrayList al = new ArrayList();
        for (int i = 0; i < fs.length; i++) {
            long testSize = fs[i].getSize();
            if ((current + testSize) <= biggest) {
                al.add(fs[i].getName());
                current += fs[i].getSize();
                index = i;
            } else break;
        }
        index++;
        String fusion = Misc.stringArrayListToString(al, ",");
        int sizeArray = fs.length - index + 1;
        String[] chromList = new String[sizeArray];
        chromList[0] = fusion;
        int counter = 1;
        for (int i = index; i < fs.length; i++) {
            chromList[counter++] = fs[i].getName();
        }
        return chromList;
    }

    public void deleteSplitNormalizedFiles() {
        for (int i = 0; i < replicas.length; i++) {
            File[] f = replicas[i].getChipSetDirectories();
            for (int j = 0; j < f.length; j++) {
                IO.deleteDirectory(f[j]);
            }
        }
    }

    public void makeChromSet(File[][] setRep, boolean treatmentSet) {
        File pCels = new File(parameters.getResultsDirectory(), "PCels");
        if (pCels.exists() == false) pCels.mkdir();
        for (int r = 0; r < setRep[0].length; r++) {
            File saveDir;
            if (treatmentSet) {
                saveDir = new File(pCels, "TreatmentReplica" + (r + 1));
                replicas[replicaIndex].setTreatmentReplica(true);
            } else {
                saveDir = new File(pCels, "ControlReplica" + (r + 1));
                replicas[replicaIndex].setTreatmentReplica(false);
            }
            replicas[replicaIndex].setSaveDirectory(saveDir);
            File[] set = new File[setRep.length];
            for (int j = 0; j < set.length; j++) {
                String name = Misc.removeExtension(setRep[j][r].getName());
                File test = new File(setRep[j][r].getParentFile(), name);
                if (test.exists() == false) Misc.printExit("Missing normalized file! " + test);
                set[j] = test;
            }
            replicas[replicaIndex].setChipSetDirectories(set);
            replicaIndex++;
        }
    }

    public int numberReplicasToMake() {
        int num = 0;
        for (int i = 0; i < replicas.length; i++) {
            replicas[i].loadNumberOligos();
            if (replicas[i].getNumberOligos() == 0) num++;
        }
        return num;
    }

    public void makeChromosomeSets() {
        int numToProcess = parameters.getTreatmentCelFiles()[0].length + parameters.getControlCelFiles()[0].length;
        replicas = new ChromSet[numToProcess];
        for (int i = 0; i < numToProcess; i++) replicas[i] = new ChromSet();
        replicaIndex = 0;
        makeChromSet(parameters.getTreatmentCelFiles(), true);
        makeChromSet(parameters.getControlCelFiles(), false);
        int numToMake = numberReplicasToMake();
        if (numToMake != 0) {
            for (int i = 0; i < replicas.length; i++) {
                if (replicas[i].getNumberOligos() != 0) continue;
                ArrayList command = new ArrayList();
                command.add(parameters.getAppsDirectory() + "/MakeChromosomeSets");
                command.add("-d");
                command.add(IO.concatinateFileFullPathNames(replicas[i].getChipSetDirectories(), ","));
                command.add("-n");
                command.add(replicas[i].getSaveDirectory().toString());
                if (i != 0) command.add("-s");
                launchJQSub(command, parameters.getMaxMemory());
            }
        }
        int timer = 60;
        numToMake = numberReplicasToMake();
        System.out.print("\t# to merge " + numToMake);
        while (timer != 0) {
            if (numToMake == 0) break;
            timer--;
            Misc.sleep(60);
            numToMake = numberReplicasToMake();
            System.out.print(" " + numToMake);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out on merging split normalized files.\n");
        long num = 0;
        for (int i = 0; i < replicas.length; i++) {
            replicas[i].loadNumberOligos();
            if (num == 0) num = replicas[i].getNumberOligos(); else if (num != replicas[i].getNumberOligos()) Misc.printExit("\nError: the number of oligos in one of your replicas differ! " + replicas[i].getSaveDirectory() + "\n");
        }
        oligoPositions = new File(parameters.getResultsDirectory(), "OligoPositions");
        File oldOP = new File(replicas[0].getSaveDirectory(), "OligoPositions");
        oldOP.renameTo(oligoPositions);
    }

    public void normalizeCelFiles() {
        File qcDir = new File(parameters.getResultsDirectory(), "QC");
        if (qcDir.exists() == false) qcDir.mkdir();
        int numChipSets = parameters.getTreatmentCelFiles().length;
        File[][] normalizedFiles = new File[numChipSets][];
        for (int k = 0; k < numChipSets; k++) {
            File[] t = parameters.getTreatmentCelFiles()[k];
            File[] c = parameters.getControlCelFiles()[k];
            File[] combine = new File[t.length + c.length];
            int counter = 0;
            for (int i = 0; i < t.length; i++) combine[counter++] = t[i];
            for (int i = 0; i < c.length; i++) combine[counter++] = c[i];
            boolean allNormalized = true;
            normalizedFiles[k] = new File[combine.length];
            for (int i = 0; i < combine.length; i++) {
                String name = Misc.removeExtension(combine[i].getName());
                File test = new File(t[0].getParentFile(), name);
                if (test.exists() == false) allNormalized = false;
                normalizedFiles[k][i] = test;
            }
            if (allNormalized == false) {
                ArrayList command = new ArrayList();
                command.add(parameters.getAppsDirectory() + "/CelProcessor");
                if (parameters.isMedianScaleUsingControlSeqs()) command.add("-c");
                command.add("-t");
                command.add(parameters.getTpmapFiles()[k].toString());
                command.add("-m");
                command.add(parameters.getTargetMedian() + "");
                command.add("-r");
                if (parameters.isQuantileNormalizeAll()) {
                    File clusterName = new File(qcDir, parameters.getResultsName() + "ChipSubSet" + (1 + k) + "ClusterPlot.png");
                    launchCelNormalization(combine, command, clusterName);
                } else {
                    File clusterName = new File(qcDir, parameters.getResultsName() + "TreatmentChipSubSet" + (1 + k) + "ClusterPlot.png");
                    launchCelNormalization(t, command, clusterName);
                    clusterName = new File(qcDir, parameters.getResultsName() + "ControlChipSubSet" + (1 + k) + "ClusterPlot.png");
                    launchCelNormalization(c, command, clusterName);
                }
            }
        }
        int timer = 200;
        System.out.print("\t# to normalize ");
        File[] expectedNormed = IO.collapseFileArray(normalizedFiles);
        while (timer != 0) {
            timer--;
            int numFound = 0;
            for (int i = 0; i < expectedNormed.length; i++) {
                if (expectedNormed[i].exists()) {
                    File[] files = IO.extractFiles(expectedNormed[i]);
                    if (files.length > 0) numFound++;
                }
            }
            int num = expectedNormed.length - numFound;
            System.out.print(num + " ");
            if (num == 0) break;
            Misc.sleep(60);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out on normalizing xxx.cela files.\n");
    }

    public void launchCelNormalization(File[] celaFiles, ArrayList generalCmd, File clusterName) {
        ArrayList finalCmd = new ArrayList();
        finalCmd.addAll(generalCmd);
        finalCmd.add("-d");
        finalCmd.add(IO.concatinateFileFullPathNames(celaFiles, ","));
        finalCmd.add("-i");
        finalCmd.add(clusterName.toString());
        launchJQSub(finalCmd, parameters.getMaxMemory());
    }

    public void convertCelFiles() {
        File aCels = new File(parameters.getResultsDirectory(), "ACels");
        if (aCels.exists() == false) aCels.mkdir();
        File[] t = IO.collapseFileArray(parameters.getTreatmentCelFiles());
        File[] c = IO.collapseFileArray(parameters.getControlCelFiles());
        File[] combine = new File[t.length + c.length];
        for (int i = 0; i < t.length; i++) combine[i] = t[i];
        int counter = 0;
        for (int i = t.length; i < combine.length; i++) combine[i] = c[counter++];
        ArrayList toConvert = new ArrayList();
        Pattern p = Pattern.compile("\\.cel$", Pattern.CASE_INSENSITIVE);
        for (int i = 0; i < combine.length; i++) {
            Matcher mat = p.matcher(combine[i].getName());
            String name = mat.replaceFirst(".cela");
            File test = new File(aCels, name);
            if (test.exists() == false) toConvert.add(combine[i]);
        }
        int numToConvert = toConvert.size();
        for (int i = 0; i < numToConvert; i++) {
            ArrayList command = new ArrayList();
            command.add(parameters.getAppsDirectory() + "/CelFileConverter");
            command.add("-f");
            command.add(((File) toConvert.get(i)).toString());
            command.add("-s");
            command.add(aCels.toString());
            launchJQSub(command, parameters.getMaxMemory());
        }
        int timer = 100;
        System.out.print("\t# to convert ");
        File[] converted = null;
        while (timer != 0) {
            timer--;
            converted = IO.extractFiles(aCels, "cela");
            int numConverted = 0;
            if (converted != null) numConverted = converted.length;
            int num = combine.length - numConverted;
            System.out.print(num + " ");
            if (num == 0) break;
            Misc.sleep(60);
        }
        System.out.println();
        if (timer == 0) Misc.printExit("\nError: timed out on converting xxx.cel files.\n");
        long size = converted[0].length();
        for (int i = 1; i < converted.length; i++) {
            if (converted[i].length() != size) Misc.printExit("\nError: one of your converted xxx.cela files differs in size -> " + converted[i] + " delete and restart.\n");
        }
    }

    public void launchJQSub(ArrayList args, int memory) {
        args.add(0, "-jar");
        args.add(0, "-Xmx" + memory + "M");
        args.add(0, parameters.getJava().toString());
        if (cluster) {
            args.add(0, parameters.getAppsDirectory() + "/JQSub");
            args.add(0, "-jar");
            args.add(0, parameters.getJava().toString());
        }
        String[] cmd = Misc.stringArrayListToStringArray(args);
        System.out.println("\tLaunching: " + Misc.stringArrayToString(cmd, " "));
        String[] results = IO.executeCommandLineReturnAll(cmd);
        System.out.println("\t" + Misc.stringArrayToString(results, "\n"));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printDocs();
            System.exit(0);
        }
        new T2(args);
    }

    /**This method will process each argument and assign new varibles*/
    public void processArgs(String[] args) {
        Pattern pat = Pattern.compile("-[a-z]");
        for (int i = 0; i < args.length; i++) {
            String lcArg = args[i].toLowerCase();
            Matcher mat = pat.matcher(lcArg);
            if (mat.matches()) {
                char test = args[i].charAt(1);
                try {
                    switch(test) {
                        case 'p':
                            parameterFile = new File(args[i + 1]);
                            i++;
                            break;
                        case 'c':
                            cluster = true;
                            break;
                        case 'h':
                            printDocs();
                            System.exit(0);
                        default:
                            Misc.printExit("\nError: unknown option! " + mat.group());
                    }
                } catch (Exception e) {
                    Misc.printExit("\nSorry, something doesn't look right with this parameter: -" + test + "\n");
                }
            }
        }
        if (parameterFile == null || parameterFile.exists() == false) Misc.printExit("\nCannot find or read your parameter file '" + parameterFile + "'. See the t2ParamFileTemplate.xls for an example.\n");
    }

    public static void printDocs() {
        System.out.println("\n" + "**************************************************************************************\n" + "**                                T2: April 2007                                    **\n" + "**************************************************************************************\n" + "T2 launches many of the TiMAT2 applications based on a tab delimited parameter file\n" + "(see T2_xxx/Documentation/t2ParamFileTemplate.xls). It converts, normalizes, splits,\n" + "and merges txt cel files and then launches ScanChromosomes.  If you wish to make use\n" + "of a cluster for parallel processing, configure and test the JQSub TiMAT2 app.\n\n" + "-p Full path file text for the tab delimited parameter file.\n" + "-c Use the cluster(s) specified by JQSub.\n\n" + "Example: java pathTo/T2/Apps/T2 -c -p /my/t2ParamFile.txt\n\n" + "**************************************************************************************\n");
    }
}
