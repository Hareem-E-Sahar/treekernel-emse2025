package grobid.impl.trainer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import grobid.impl.sax.TEIAuthorSaxParser;

public class NameCitationTrainer implements Trainer {

    private String path = null;

    private String crfLearnPath = null;

    private void getTrainingDataPath() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("grobid.properties"));
            String where = props.getProperty("resource_path");
            path = where + "/dataset/names/citation";
            crfLearnPath = where + "/../bin/crfpp/crf_learn";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int createTrainingData() {
        getTrainingDataPath();
        return addFeaturesAuthor(path + "/corpus", path + "/training");
    }

    public void train() {
        String trainingDataPath = path + "/training/data.train";
        String templatePath = path + "/crfpp-templates/header.template";
        String modelPath = path + "/crfpp-models/model.crf";
        try {
            File f = new File(modelPath);
            f.renameTo(new File(modelPath + ".old"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String command = crfLearnPath + " " + templatePath + " " + trainingDataPath + " " + modelPath;
        try {
            Process child = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(child.getInputStream()));
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
            child.waitFor();
            System.out.println("Status: " + child.exitValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Add the selected features to the author model training for headers 
     */
    private int addFeaturesAuthor(String sourcePathLabel, String outputPath) {
        int totalExamples = 0;
        try {
            System.out.println("sourcePathLabel: " + sourcePathLabel);
            System.out.println("outputPath: " + outputPath);
            File input = new File(sourcePathLabel);
            File path = new File(sourcePathLabel);
            File[] refFiles = path.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    if (name.endsWith(".tei.xml")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            if (refFiles == null) {
                return 0;
            }
            System.out.println(refFiles.length + " tei files");
            File outFile2 = new File(outputPath + "/data.train");
            OutputStream os2 = new FileOutputStream(outFile2);
            Writer writer2 = new OutputStreamWriter(os2, "UTF8");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            int n = 0;
            for (; n < refFiles.length; n++) {
                File teifile = refFiles[n];
                String name = teifile.getName();
                System.out.println(name);
                TEIAuthorSaxParser parser2 = new TEIAuthorSaxParser();
                SAXParser p = spf.newSAXParser();
                p.parse(teifile, parser2);
                ArrayList<String> labeled = parser2.getLabeledResult();
                totalExamples += parser2.n;
                String header = FeatureTrainerUtil.addFeaturesHeader(labeled, false);
                writer2.write(header + "\n");
            }
            writer2.close();
            os2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalExamples;
    }

    /**
     * Command line execution.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        NameCitationTrainer nht = new NameCitationTrainer();
        int nbExamples = nht.createTrainingData();
        nht.train();
        long end = System.currentTimeMillis();
        System.out.println("Model for names in citation created in " + (end - start) + " milliseconds, based on " + nbExamples + " annotated examples.");
    }
}
