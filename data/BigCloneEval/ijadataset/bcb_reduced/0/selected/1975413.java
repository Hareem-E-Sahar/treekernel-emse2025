package gate.chineseSeg;

import gate.Document;
import gate.Factory;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.learning.DocFeatureVectors;
import gate.learning.Label2Id;
import gate.learning.LabelsOfFV;
import gate.learning.LabelsOfFeatureVectorDoc;
import gate.learning.LearningEngineSettings;
import gate.learning.LogService;
import gate.learning.NLPFeaturesList;
import gate.learning.SparseFeatureVector;
import gate.learning.learners.MultiClassLearning;
import gate.learning.learners.PostProcessing;
import gate.learning.learners.SupervisedLearner;
import gate.util.ExtensionFileFilter;
import gate.util.Files;
import gate.util.GateException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

public class ChineseSegMain extends AbstractLanguageAnalyser implements ProcessingResource {

    URL modelURL = null;

    URL textFilesURL = null;

    String textCode = null;

    String learningAlg = null;

    private RunMode learningMode;

    private RunMode learningModeAppl;

    private RunMode learningModeTraining;

    /** Initialise this resource, and return it. */
    public gate.Resource init() throws ResourceInstantiationException {
        this.learningModeAppl = RunMode.SEGMENTING;
        this.learningModeTraining = RunMode.LEARNING;
        return this;
    }

    /**
   * Run the resource.
   * 
   * @throws ExecutionException
   */
    public void execute() throws ExecutionException {
        System.out.println("\n\n------------ new session starts ------------\n");
        if (corpus != null) {
            if (corpus.size() != 0) if (corpus.indexOf(document) > 0) return;
        }
        boolean isUpdateFeatList = true;
        boolean isTraining = false;
        File wdResults = null;
        File logFile = null;
        int verbosityLogService = 1;
        BufferedWriter outFeatureVectors = null;
        isTraining = false;
        if (this.learningMode.equals(this.learningModeTraining)) isTraining = true; else isTraining = false;
        isUpdateFeatList = isTraining;
        if (isTraining) {
            System.out.println("Learning a new model from the segmented text...");
            System.out.println("Learning algorithm used is " + this.learningAlg);
            System.out.println("the model files will be stored in " + modelURL.toString());
            System.out.println("the text used for learning are in " + this.textFilesURL.toExternalForm());
        } else {
            System.out.println("Applying the learned model to segment Chinese text...");
            System.out.println("Learning algorithm used is " + this.learningAlg);
            System.out.println("the model files used are stored in " + modelURL.toString());
            System.out.println("the text for segmenting are in " + this.textFilesURL.toExternalForm());
        }
        verbosityLogService = 1;
        wdResults = Files.fileFromURL(modelURL);
        if (!wdResults.exists()) wdResults.mkdir();
        logFile = new File(wdResults, ConstantParameters.FILENAMEOFLOGFILE);
        try {
            LogService.init(logFile, true, verbosityLogService);
            NLPFeaturesList featuresList = null;
            featuresList = new NLPFeaturesList();
            featuresList.loadFromFile(wdResults, ConstantParameters.FILENAME_TERMS, this.textCode);
            if (!featuresList.featuresList.containsKey(ConstantParameters.NONFEATURE)) {
                int size = featuresList.featuresList.size() + 1;
                featuresList.featuresList.put(ConstantParameters.NONFEATURE, new Integer(size));
                featuresList.idfFeatures.put(ConstantParameters.NONFEATURE, new Integer(1));
            }
            Label2Id labelsAndId;
            labelsAndId = new Label2Id();
            labelsAndId.loadLabelAndIdFromFile(wdResults, ConstantParameters.FILENAMEOFLabelList);
            ExtensionFileFilter fileFilter = null;
            File[] xmlFiles = Files.fileFromURL(this.textFilesURL).listFiles(fileFilter);
            Arrays.sort(xmlFiles, new Comparator<File>() {

                public int compare(File a, File b) {
                    return a.getName().compareTo(b.getName());
                }
            });
            if (isTraining) {
                outFeatureVectors = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults, ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));
            }
            File dirSeg = null;
            if (!isTraining) {
                dirSeg = new File(this.textFilesURL.getPath(), ConstantParameters.FILENAME_resultsDir);
                if (!dirSeg.exists()) dirSeg.mkdir();
            }
            int numDocs = 0;
            for (File f : xmlFiles) {
                if (!f.isDirectory()) {
                    ++numDocs;
                    Document doc = Factory.newDocument(f.toURI().toURL(), this.textCode);
                    doc.setName(f.getName());
                    System.out.println(numDocs + ", docName=" + doc.getName());
                    String text = doc.getContent().toString();
                    char[] chs = new char[text.length()];
                    int num;
                    StringBuffer letterNum = new StringBuffer();
                    num = convert2Chs(text, chs, letterNum);
                    String[] labels = new String[chs.length];
                    if (isTraining) {
                        num = obtainLabels(num, chs, labels);
                        labelsAndId.updateMultiLabelFromDoc(labels);
                    }
                    String[] termC1 = new String[num + 2];
                    String[] termC12 = new String[num + 1];
                    String[] termC13 = new String[num];
                    obtainTerms(num, chs, termC1, termC12, termC13);
                    if (isUpdateFeatList) {
                        updateFeatList(featuresList, termC1);
                        updateFeatList(featuresList, termC12);
                        updateFeatList(featuresList, termC13);
                    }
                    DocFeatureVectors docFV = new DocFeatureVectors();
                    docFV.docId = new String(doc.getName());
                    putFeatsIntoDocFV(featuresList, termC1, termC12, termC13, docFV);
                    LabelsOfFV[] multiLabels = new LabelsOfFV[num];
                    for (int j = 0; j < num; ++j) {
                        int[] labelsId = new int[1];
                        if (isTraining) labelsId[0] = new Integer(labelsAndId.label2Id.get(labels[j]).toString()).intValue(); else labelsId[0] = -1;
                        float[] labelPr = new float[1];
                        labelPr[0] = 1;
                        multiLabels[j] = new LabelsOfFV(1, labelsId, labelPr);
                    }
                    System.out.println("numInstance=" + docFV.numInstances);
                    BufferedWriter outSegText = null;
                    if (!isTraining) {
                        outFeatureVectors = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults, ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));
                        outSegText = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(dirSeg, f.getName() + ".seg.txt")), this.textCode));
                    }
                    docFV.addDocFVsMultiLabelToFile(numDocs, outFeatureVectors, multiLabels);
                    if (!isTraining) {
                        outFeatureVectors.flush();
                        outFeatureVectors.close();
                    }
                    if (!isTraining) {
                        int[] selectedLabels = null;
                        selectedLabels = segementText(wdResults, this.learningAlg);
                        String[] terms = letterNum.toString().split(ConstantParameters.SEPARATTORLN);
                        int kk = 0;
                        StringBuffer textSeg = new StringBuffer();
                        for (int j = 0; j < num; ++j) {
                            String labelC = null;
                            String iObj = new Integer(selectedLabels[j] + 1).toString();
                            if (labelsAndId.id2Label.containsKey(iObj)) labelC = labelsAndId.id2Label.get(iObj).toString();
                            if (chs[j] == ConstantParameters.REPLACEMENT_Digit || chs[j] == ConstantParameters.REPLACEMENT_Letter || chs[j] == ConstantParameters.NEWLINE_Char) {
                                textSeg.append(terms[kk]);
                                if (labelC.equals(ConstantParameters.LABEL_R) || labelC.equals(ConstantParameters.LABEL_S)) textSeg.append(ConstantParameters.SEPARATTOR_BLANK);
                                ++kk;
                            } else if (chs[j] == ConstantParameters.REPLACEMENT_BLANK) {
                                textSeg.append(ConstantParameters.SEPARATTOR_BLANK);
                            } else {
                                textSeg.append(chs[j]);
                                if (labelC.equals(ConstantParameters.LABEL_R) || labelC.equals(ConstantParameters.LABEL_S)) textSeg.append(ConstantParameters.SEPARATTOR_BLANK);
                            }
                        }
                        outSegText.append(textSeg);
                        outSegText.flush();
                        outSegText.close();
                    }
                    Factory.deleteResource(doc);
                }
            }
            if (isTraining) {
                outFeatureVectors.flush();
                outFeatureVectors.close();
            }
            if (isUpdateFeatList) {
                featuresList.writeListIntoFile(wdResults, ConstantParameters.FILENAME_TERMS, this.textCode);
            }
            if (isTraining) {
                labelsAndId.writeLabelAndIdToFile(wdResults, ConstantParameters.FILENAMEOFLabelList);
            }
            if (isTraining) learningNewModel(wdResults, numDocs, this.learningAlg);
            System.out.println("Number of documents used is " + numDocs);
            System.out.println("Finished!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResourceInstantiationException e) {
            e.printStackTrace();
        } catch (GateException e) {
            e.printStackTrace();
        }
    }

    static void learningNewModel(File wdResults, int numDocs, String learningAlg) throws GateException {
        String fvFileName = wdResults.toString() + File.separator + ConstantParameters.FILENAMEOFFeatureVectorData;
        File dataFile = new File(fvFileName);
        String modelFileName = wdResults.toString() + File.separator + ConstantParameters.FILENAMEOFModels;
        File modelFile = new File(modelFileName);
        String learningCommand = "  ";
        String dataSetFile = null;
        String learningParas = null;
        String learningExec = null;
        SupervisedLearner paumLearner = null;
        if (learningAlg.equalsIgnoreCase("SVM")) {
            learningParas = " -c 0.7 -t 0 -m 100 -tau 0.8 ";
            learningExec = " ";
            learningCommand = learningExec + " " + learningParas;
            paumLearner = MultiClassLearning.obtainLearnerFromName("SVMLibSvmJava", learningCommand, dataSetFile);
        } else if (learningAlg.startsWith("PAUMExec ")) {
            learningParas = " -p 20 -n 1 ";
            String[] items = learningAlg.split(" ");
            if (items.length < 4) {
                throw new GateException("There is no enough parameter for the learning " + "algorithm PAUM");
            }
            learningExec = items[1];
            learningCommand = learningExec + " " + learningParas + " " + items[2] + " " + items[3];
            paumLearner = MultiClassLearning.obtainLearnerFromName("PAUMExec", learningCommand, dataSetFile);
        } else {
            learningParas = " -p 20 -n 1 ";
            learningExec = " ";
            learningCommand = learningExec + " " + learningParas;
            paumLearner = MultiClassLearning.obtainLearnerFromName("PAUM", learningCommand, dataSetFile);
        }
        paumLearner.setLearnerExecutable(learningExec);
        paumLearner.setLearnerParams(learningParas);
        MultiClassLearning chunkLearning = new MultiClassLearning(LearningEngineSettings.OneVSOtherMode);
        File tempDataFile = new File(wdResults, ConstantParameters.TempFILENAMEofFVData);
        boolean isUsingTempDataFile = false;
        if (paumLearner.getLearnerName().equals("SVMExec") || paumLearner.getLearnerName().equals("PAUMExec")) isUsingTempDataFile = true;
        chunkLearning.getDataFromFile(numDocs, dataFile, isUsingTempDataFile, tempDataFile);
        chunkLearning.trainingNoThread(paumLearner, modelFile, isUsingTempDataFile, tempDataFile);
    }

    /**
   * segement the text using the learned model.
   * 
   * @throws GateException
   */
    static int[] segementText(File wdResults, String learningAlg) throws GateException {
        int numDocs = 1;
        String fvFileName = wdResults.toString() + File.separator + ConstantParameters.FILENAMEOFFeatureVectorData;
        File dataFile = new File(fvFileName);
        String modelFileName = wdResults.toString() + File.separator + ConstantParameters.FILENAMEOFModels;
        File modelFile = new File(modelFileName);
        String learningCommand = "  ";
        String dataSetFile = null;
        String learningParas = null;
        String learningExec = null;
        SupervisedLearner paumLearner = null;
        if (learningAlg.equalsIgnoreCase("SVM")) {
            learningParas = " -c 0.7 -t 0 -m 100 -tau 0.8 ";
            learningExec = " ";
            learningCommand = learningExec + " " + learningParas;
            paumLearner = MultiClassLearning.obtainLearnerFromName("SVMLibSvmJava", learningCommand, dataSetFile);
        } else if (learningAlg.startsWith("PAUMExec ")) {
            learningParas = " -p 20 -n 1 -optB 0.0 ";
            String[] items = learningAlg.split(" ");
            if (items.length < 4) {
                throw new GateException("There is no enough parameter for the learning " + "algorithm PAUM");
            }
            learningExec = items[1];
            learningCommand = learningExec + " " + learningParas + " " + items[2] + " " + items[3];
            paumLearner = MultiClassLearning.obtainLearnerFromName("PAUMExec", learningCommand, dataSetFile);
        } else {
            learningParas = " -p 20 -n 1 -optB 0.0 ";
            learningExec = " ";
            learningCommand = learningExec + " " + learningParas;
            paumLearner = MultiClassLearning.obtainLearnerFromName("PAUM", learningCommand, dataSetFile);
        }
        paumLearner.setLearnerExecutable(learningExec);
        paumLearner.setLearnerParams(learningParas);
        MultiClassLearning chunkLearning = new MultiClassLearning(LearningEngineSettings.OneVSOtherMode);
        File tempDataFile = new File(wdResults, ConstantParameters.TempFILENAMEofFVData);
        boolean isUsingTempDataFile = false;
        chunkLearning.getDataFromFile(numDocs, dataFile, isUsingTempDataFile, tempDataFile);
        chunkLearning.applyNoThread(paumLearner, modelFile);
        LabelsOfFeatureVectorDoc[] labelsFVDoc = null;
        labelsFVDoc = chunkLearning.dataFVinDoc.labelsFVDoc;
        Label2Id labelsAndId = new Label2Id();
        labelsAndId.loadLabelAndIdFromFile(wdResults, ConstantParameters.FILENAMEOFLabelList);
        float boundaryP = 0;
        float entityP = 0;
        float thresholdClassificaton = -999;
        PostProcessing postPr = new PostProcessing(boundaryP, entityP, thresholdClassificaton);
        int[] selectedLabels = new int[labelsFVDoc[0].multiLabels.length];
        float[] valuesLabels = new float[labelsFVDoc[0].multiLabels.length];
        postPr.postProcessingClassification((short) 3, labelsFVDoc[0].multiLabels, selectedLabels, valuesLabels);
        return selectedLabels;
    }

    /**
   * convert a text into an array of characters with replacements of letters and
   * numbers.
   */
    public static int convert2Chs(String text, char[] chs, StringBuffer letterNum) {
        int num = 0;
        boolean isL = false;
        boolean isN = false;
        boolean isR = false;
        for (int ind = 0; ind < text.length(); ++ind) {
            Character ch = text.charAt(ind);
            if (isDelim(ch)) {
                chs[num] = ConstantParameters.REPLACEMENT_BLANK;
                ++num;
                continue;
            }
            int tc = Character.getType(ch);
            if (tc == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING) continue;
            if (tc == Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE) {
                letterNum.append(ch);
                if (!isR) {
                    if (isL || isN) letterNum.append(ConstantParameters.SEPARATTORLN);
                    chs[num] = ConstantParameters.NEWLINE_Char;
                    ++num;
                    isR = true;
                    isN = false;
                    isL = false;
                }
            } else if (tc == Character.LOWERCASE_LETTER || tc == Character.UPPERCASE_LETTER || tc == Character.TITLECASE_LETTER) {
                letterNum.append(ch);
                if (!isL) {
                    if (isN || isR) letterNum.append(ConstantParameters.SEPARATTORLN);
                    chs[num] = ConstantParameters.REPLACEMENT_Letter;
                    ++num;
                    isL = true;
                    isN = false;
                    isR = false;
                }
            } else if (Character.isDigit(ch)) {
                letterNum.append(ch);
                if (!isN) {
                    chs[num] = ConstantParameters.REPLACEMENT_Digit;
                    ++num;
                    if (isL || isR) letterNum.append(ConstantParameters.SEPARATTORLN);
                    isN = true;
                    isL = false;
                    isR = false;
                }
            } else {
                if (isL || isN || isR) {
                    letterNum.append(ConstantParameters.SEPARATTORLN);
                    isL = false;
                    isN = false;
                    isR = false;
                }
                chs[num] = ch;
                ++num;
            }
        }
        return num;
    }

    /** Obtain the labels for the text, i.e. l, m, r and s */
    public static int obtainLabels(int numkk, char[] chs, String[] labels) {
        int num = 0;
        char[] chsSim11 = new char[3 * numkk + 1];
        for (int id = 0; id < numkk; ++id) {
            if (Character.isLetterOrDigit(chs[id]) || isDelim(chs[id])) {
                chsSim11[num++] = chs[id];
            } else {
                chsSim11[num++] = ConstantParameters.REPLACEMENT_BLANK;
                chsSim11[num++] = chs[id];
                chsSim11[num++] = ConstantParameters.REPLACEMENT_BLANK;
            }
        }
        char[] chsSim = new char[3 * numkk + 1];
        boolean[] isDelA = new boolean[3 * numkk + 1];
        int num11 = 0;
        boolean isD = false;
        for (int id = 0; id < num; ++id) {
            if (chsSim11[id] == ConstantParameters.REPLACEMENT_BLANK) {
                if (!isD) {
                    isD = true;
                    chsSim[num11] = chsSim11[id];
                    isDelA[num11] = true;
                    ++num11;
                }
            } else {
                isD = false;
                chsSim[num11] = chsSim11[id];
                isDelA[num11] = false;
                ++num11;
            }
        }
        isDelA[num11++] = true;
        int wS = 0;
        int lenW = 0;
        num = 0;
        for (int id = 0; id < num11; ++id) {
            if (isDelA[id]) {
                lenW = id - wS;
                if (lenW == 1) {
                    labels[num] = ConstantParameters.LABEL_S;
                    chs[num] = chsSim[id - 1];
                    ++num;
                    wS = id + 1;
                } else if (lenW > 1) {
                    labels[num] = ConstantParameters.LABEL_L;
                    chs[num] = chsSim[id - lenW];
                    for (int i = 1; i < lenW - 1; ++i) {
                        labels[num + i] = ConstantParameters.LABEL_M;
                        chs[num + i] = chsSim[id - lenW + i];
                    }
                    labels[num + lenW - 1] = ConstantParameters.LABEL_R;
                    chs[num + lenW - 1] = chsSim[id - 1];
                    num += lenW;
                    wS = id + 1;
                }
            }
        }
        return num;
    }

    static boolean isDelim(char ch) {
        if (ch == ConstantParameters.SEPARATTOR_BLANK) return true;
        if (ch == ConstantParameters.SEPARATTOR_BLANK_wide) return true;
        return false;
    }

    /** obtain the terms from the list of characters for the text */
    static void obtainTerms(int num00, char[] chs, String[] termC1, String[] termC12, String[] termC13) {
        termC1[0] = new Character(ConstantParameters.BEGIN_Char).toString();
        for (int i = 1; i <= num00; ++i) termC1[i] = new String(chs, i - 1, 1);
        termC1[num00 + 1] = new Character(ConstantParameters.END_Char).toString();
        for (int i = 0; i <= num00; ++i) termC12[i] = termC1[i] + termC1[i + 1];
        for (int i = 0; i < num00; ++i) {
            termC13[i] = termC1[i] + termC1[i + 2];
        }
    }

    /** using the terms to update the feature list. */
    static void updateFeatList(NLPFeaturesList featuresList, String[] terms) {
        int size = featuresList.featuresList.size();
        for (int ind = 0; ind < terms.length; ++ind) {
            if (size < ConstantParameters.MAXIMUMFEATURES) {
                if (!featuresList.featuresList.containsKey(terms[ind])) {
                    ++size;
                    featuresList.featuresList.put(terms[ind], new Long(size));
                    featuresList.idfFeatures.put(terms[ind], new Long(1));
                } else {
                    featuresList.idfFeatures.put(terms[ind], new Long((new Long(featuresList.idfFeatures.get(terms[ind]).toString())).longValue() + 1));
                }
            } else {
                System.out.println("There are more NLP features from the training docuemnts");
                System.out.println(" than the pre-defined maximal number" + new Long(ConstantParameters.MAXIMUMFEATURES));
                return;
            }
        }
    }

    /** add the feature into docFV */
    static void putFeatsIntoDocFV(NLPFeaturesList featuresList, String[] termC1, String[] termC12, String[] termC13, DocFeatureVectors docFV) {
        int num = termC1.length - 2;
        int num11 = termC1.length;
        docFV.numInstances = num;
        docFV.fvs = new SparseFeatureVector[docFV.numInstances];
        for (int ind = 0; ind < num; ++ind) {
            String[] feats = new String[10];
            feats[0] = termC1[ind + 1];
            feats[1] = termC1[ind];
            if (ind - 1 >= 0) feats[2] = termC1[ind - 1]; else feats[2] = ConstantParameters.NONFEATURE;
            feats[3] = termC1[ind + 2];
            if (ind + 3 < termC1.length) feats[4] = termC1[ind + 3]; else feats[4] = ConstantParameters.NONFEATURE;
            feats[5] = termC12[ind + 1];
            feats[6] = termC12[ind];
            if (ind - 1 >= 0) feats[7] = termC12[ind - 1]; else feats[7] = ConstantParameters.NONFEATURE;
            if (ind + 2 < termC12.length) feats[8] = termC12[ind + 2]; else feats[8] = ConstantParameters.NONFEATURE;
            feats[9] = termC13[ind];
            docFV.fvs[ind] = new SparseFeatureVector(10);
            for (int i = 0; i < 10; i++) {
                if (featuresList.featuresList.containsKey(feats[i])) {
                    docFV.fvs[ind].nodes[i].index = new Integer(featuresList.featuresList.get(feats[i]).toString()).intValue() + i * ConstantParameters.MAXIMUMFEATURES;
                    docFV.fvs[ind].nodes[i].value = 1;
                } else {
                    docFV.fvs[ind].nodes[i].index = i * ConstantParameters.MAXIMUMFEATURES;
                    docFV.fvs[ind].nodes[i].value = 0;
                }
            }
        }
    }

    public void setModelURL(URL modelU) {
        this.modelURL = modelU;
    }

    public URL getModelURL() {
        return this.modelURL;
    }

    public void setTextFilesURL(URL modelU) {
        this.textFilesURL = modelU;
    }

    public URL getTextFilesURL() {
        return this.textFilesURL;
    }

    public RunMode getLearningMode() {
        return this.learningMode;
    }

    public void setLearningMode(RunMode learningM) {
        this.learningMode = learningM;
    }

    public String getTextCode() {
        return this.textCode;
    }

    public void setTextCode(String tcode) {
        this.textCode = tcode;
    }

    public String getLearningAlg() {
        return this.learningAlg;
    }

    public void setLearningAlg(String la) {
        this.learningAlg = la;
    }
}
