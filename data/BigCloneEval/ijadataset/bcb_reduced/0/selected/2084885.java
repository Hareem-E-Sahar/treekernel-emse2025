package dynamicClustering;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.*;
import dynamicClustering.GOLoader.GOnumber;
import dynamicClustering.GOLoader.loadGOinfo;
import dynamicClustering.GUI.InformationPanel;
import dynamicClustering.GUI.progressBarGUI;
import dynamicClustering.GUI.runInfo;
import dynamicClustering.ResultViewer.Viewables.GeneListViewer;
import dynamicClustering.clusterSignificance.TMEVBridge;
import dynamicClustering.ClusteringMethods.ClusterMethod;
import dynamicClustering.ClusteringMethods.KMCClusteringMethod;
import dynamicClustering.ClusteringMethods.KMSClusteringMethod;
import dynamicClustering.DataTracking.*;

public class sigClusterFinder {

    /**
	 * Program:	BIRBU
	 * Authors: Benjamin Saunders, Volker Grabe, Jeffrey Blanchard
	 * Version: Version 1.0
	 * Input:	Expresion Data in any Format
	 * 			Gene Annotation File
	 * 			Gene Description File
	 * 			Significance Thresholds
	 * Explination:		This program takes in a series of time Course Data.
	 * The program uses a variety of clustering methods and sizes to find the 
	 * most biologically and graphically significant clusters.	
	 */
    public static void main(String[] args) {
        String annotationFilePath;
        String descriptionFilePath;
        int metric = 4;
        int clusterMethod = 0;
        double bioSignificanceThreshold;
        double graphicalSigThreshold;
        TMEVBridge tmev;
        loadGOinfo goloader;
        runInfo runInfor;
        Vector<ClusterMethod> methods = new Vector<ClusterMethod>();
        Map<String, GOnumber> allGOnum;
        Map<String, String> geneAnnotationHash;
        int BCF = 0;
        dataProcessing dataProcesser;
        progressBarGUI progress;
        methods.add(new KMCClusteringMethod());
        methods.add(new KMSClusteringMethod());
        runInfor = new runInfo(methods);
        Thread runInforThread = new Thread(runInfor);
        runInforThread.setPriority(Thread.NORM_PRIORITY);
        runInforThread.start();
        try {
            runInforThread.join();
        } catch (InterruptedException e) {
            System.err.println("Option frame was interrupted!");
        }
        annotationFilePath = runInfor.getAnnotationFilePath();
        bioSignificanceThreshold = runInfor.getBioSignificanceThreshold();
        graphicalSigThreshold = runInfor.getGraphicalSigThreshold();
        metric = runInfor.getMetric();
        clusterMethod = runInfor.getClusterMethod();
        descriptionFilePath = runInfor.getGODescriptionFile();
        tmev = new TMEVBridge();
        tmev.loadData();
        goloader = new loadGOinfo(annotationFilePath, tmev, descriptionFilePath);
        allGOnum = goloader.getAllGOnum();
        geneAnnotationHash = goloader.getGeneAnnotationHash();
        BCF = goloader.getBCF();
        int finish = 0;
        methods.get(clusterMethod).run(metric, geneAnnotationHash, allGOnum, BCF, tmev.getDataFrame());
        finish = methods.get(clusterMethod).getNumberOfSteps();
        File resultsDir = new File("Results");
        if (resultsDir.isDirectory()) deleteDir(resultsDir);
        resultsDir.mkdir();
        File logDir = new File("Results" + File.separator + "LogFiles");
        logDir.mkdir();
        File clusterDir = new File("Results" + File.separator + "Cluster");
        clusterDir.mkdir();
        dataProcesser = new dataProcessing(bioSignificanceThreshold, graphicalSigThreshold, allGOnum, finish);
        progress = new progressBarGUI(0, (finish * 5) + 10);
        methods.get(clusterMethod).setGenes(tmev.getPopulationList());
        methods.get(clusterMethod).cluster(tmev, progress, dataProcesser);
        progress.update("DONE CLUSTERING");
        progress.update("Printing out Data");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("Results" + File.separator + "settings.txt"));
            writer.write("Clustering Method: " + methods.get(clusterMethod).getClusterMethodString() + "\n");
            writer.close();
        } catch (IOException e) {
        }
        Vector<String> geneList = new Vector<String>();
        try {
            BufferedReader genereader = new BufferedReader(new FileReader("Results" + File.separator + "genelist.txt"));
            String line;
            while ((line = genereader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    geneList.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        try {
            boolean notFirst;
            BufferedWriter writer = new BufferedWriter(new FileWriter("Results" + File.separator + "geneInputData.txt"));
            BufferedReader reader = new BufferedReader(new FileReader(descriptionFilePath));
            String line;
            StringTokenizer token1, token2;
            writer.write("ontology\tdescription\tcontrolledByFound");
            writer.newLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    token1 = new StringTokenizer(line, "\t");
                    String gene = token1.nextToken().trim();
                    if (geneList.contains(gene)) geneList.remove(gene);
                    String des = "description unknown";
                    if (token1.hasMoreTokens()) des = token1.nextToken().trim();
                    writer.write(gene + "\t" + des + "\t");
                    if (geneAnnotationHash.get(gene) != null) {
                        token2 = new StringTokenizer(geneAnnotationHash.get(gene), ";");
                        notFirst = false;
                        while (token2.hasMoreTokens()) {
                            if (notFirst) writer.write("; "); else notFirst = true;
                            writer.write(token2.nextToken().trim());
                        }
                    }
                    writer.newLine();
                }
            }
            for (String gene : geneList) {
                writer.write(gene + "\tdescription unknown\t");
                if (geneAnnotationHash.get(gene) != null) {
                    token2 = new StringTokenizer(geneAnnotationHash.get(gene), ";");
                    notFirst = false;
                    while (token2.hasMoreTokens()) {
                        if (notFirst) writer.write("; "); else notFirst = true;
                        writer.write(token2.nextToken().trim());
                    }
                }
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Could not create file for input data!");
            e.printStackTrace(System.err);
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(annotationFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter("Results" + File.separator + "geneAnnotation.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write("\n");
            }
            writer.close();
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not copy input file");
        }
        dataProcesser.printGOseries();
        progress.update("Printing GOseries Logs");
        dataProcesser.printBioSigTrend();
        progress.update("Printing Bio Series Trends");
        dataProcesser.flagBioGraphSig();
        progress.update("Printing Graphical Series Trends");
    }

    private static void systemTest() {
        final String VERSION = "4.0.01";
        final String birbuVersion = "1.0";
        final String os = System.getProperty("os.name");
        try {
            System.out.println("BIRBU - Version " + birbuVersion);
            System.out.println("MultiExperimentViewer - version " + VERSION + " - " + System.getProperty("os.name"));
            String Java3DTitle, Java3DVendor, Java3DVersion;
            try {
                InformationPanel info = new InformationPanel();
                Java3DTitle = info.getJava3DRunTimeEnvironment();
                Java3DVendor = info.getJava3DVendor();
                Java3DVersion = info.getJava3DVersion();
            } catch (Exception e) {
                Java3DTitle = "not installed";
                Java3DVendor = "not available";
                Java3DVersion = "not available";
            }
            System.out.println("Java Runtime Environment version: " + System.getProperty("java.version"));
            System.out.println("Java Runtime Environment vendor: " + System.getProperty("java.vendor"));
            System.out.println("Java Virtual Machine name: " + System.getProperty("java.vm.name"));
            System.out.println("Java Virtual Machine version: " + System.getProperty("java.vm.version"));
            System.out.println("Java Virtual Machine vendor: " + System.getProperty("java.vm.vendor"));
            System.out.println("Java 3D Runtime Environment: " + Java3DTitle);
            System.out.println("Java 3D Runtime Environment vendor: " + Java3DVendor);
            System.out.println("Java 3D Runtime Environment version:" + Java3DVersion);
            System.out.println("Operating System name: " + os);
            System.out.println("Operating System version: " + System.getProperty("os.version"));
            System.out.println("Operating System architecture: " + System.getProperty("os.arch"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n\n");
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
