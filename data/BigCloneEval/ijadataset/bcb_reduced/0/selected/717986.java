package org.nltk.mallet;

import java.io.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.*;
import java.util.zip.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.fst.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import org.nltk.mallet.*;

public class TrainCRF {

    private static final CommandOption.File trainFileOption = new CommandOption.File(TrainCRF.class, "train-file", "FILENAME", true, null, "The filename for the training data.", null);

    private static final CommandOption.File modelFileOption = new CommandOption.File(TrainCRF.class, "model-file", "FILENAME", true, null, "The CRF model file, a zip file containing crf-info.xml." + "TrainCRF will add crf-model.ser to this file.", null);

    private static final CommandOption.List commandOptions = new CommandOption.List("Train a CRF tagger.", new CommandOption[] { trainFileOption, modelFileOption });

    public static CRF4 createCRF(File trainingFile, CRFInfo crfInfo) throws FileNotFoundException {
        Reader trainingFileReader = new FileReader(trainingFile);
        Pipe p = new SimpleTagger.SimpleTaggerSentence2FeatureVectorSequence();
        p.setTargetProcessing(true);
        p.getTargetAlphabet().lookupIndex(crfInfo.defaultLabel);
        InstanceList trainingData = new InstanceList(p);
        trainingData.add(new LineGroupIterator(trainingFileReader, Pattern.compile("^\\s*$"), true));
        CRF4 crf = new CRF4(p, null);
        crf.setGaussianPriorVariance(crfInfo.gaussianVariance);
        crf.setTransductionType(crfInfo.transductionType);
        if (crfInfo.stateInfoList != null) {
            Iterator stateIter = crfInfo.stateInfoList.iterator();
            while (stateIter.hasNext()) {
                CRFInfo.StateInfo state = (CRFInfo.StateInfo) stateIter.next();
                crf.addState(state.name, state.initialCost, state.finalCost, state.destinationNames, state.labelNames, state.weightNames);
            }
        } else if (crfInfo.stateStructure == CRFInfo.FULLY_CONNECTED_STRUCTURE) crf.addStatesForLabelsConnectedAsIn(trainingData); else if (crfInfo.stateStructure == CRFInfo.HALF_CONNECTED_STRUCTURE) crf.addStatesForHalfLabelsConnectedAsIn(trainingData); else if (crfInfo.stateStructure == CRFInfo.THREE_QUARTERS_CONNECTED_STRUCTURE) crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingData); else if (crfInfo.stateStructure == CRFInfo.BILABELS_STRUCTURE) crf.addStatesForBiLabelsConnectedAsIn(trainingData); else throw new RuntimeException("Unexpected state structure " + crfInfo.stateStructure);
        if (crfInfo.weightGroupInfoList != null) {
            Iterator wgIter = crfInfo.weightGroupInfoList.iterator();
            while (wgIter.hasNext()) {
                CRFInfo.WeightGroupInfo wg = (CRFInfo.WeightGroupInfo) wgIter.next();
                FeatureSelection fs = FeatureSelection.createFromRegex(crf.getInputAlphabet(), Pattern.compile(wg.featureSelectionRegex));
                crf.setFeatureSelection(crf.getWeightsIndex(wg.name), fs);
            }
        }
        crf.train(trainingData, null, null, null, crfInfo.maxIterations);
        return crf;
    }

    /** This is (mostly) copied from CRF4.java */
    public boolean[][] labelConnectionsIn(Alphabet outputAlphabet, InstanceList trainingSet, String start) {
        int numLabels = outputAlphabet.size();
        boolean[][] connections = new boolean[numLabels][numLabels];
        for (int i = 0; i < trainingSet.size(); i++) {
            Instance instance = trainingSet.getInstance(i);
            FeatureSequence output = (FeatureSequence) instance.getTarget();
            for (int j = 1; j < output.size(); j++) {
                int sourceIndex = outputAlphabet.lookupIndex(output.get(j - 1));
                int destIndex = outputAlphabet.lookupIndex(output.get(j));
                assert (sourceIndex >= 0 && destIndex >= 0);
                connections[sourceIndex][destIndex] = true;
            }
        }
        if (start != null) {
            int startIndex = outputAlphabet.lookupIndex(start);
            for (int j = 0; j < outputAlphabet.size(); j++) {
                connections[startIndex][j] = true;
            }
        }
        return connections;
    }

    public static void main(String[] args) throws Exception {
        Reader trainingFile = null;
        int restArgs = commandOptions.processOptions(args);
        if (restArgs != args.length) {
            commandOptions.printUsage(true);
            throw new IllegalArgumentException("Unexpected arg " + args[restArgs]);
        }
        if (trainFileOption.value == null) {
            commandOptions.printUsage(true);
            throw new IllegalArgumentException("Expected --train-file FILE");
        }
        if (modelFileOption.value == null) {
            commandOptions.printUsage(true);
            throw new IllegalArgumentException("Expected --model-file FILE");
        }
        ZipFile zipFile = new ZipFile(modelFileOption.value);
        ZipEntry zipEntry = zipFile.getEntry("crf-info.xml");
        CRFInfo crfInfo = new CRFInfo(zipFile.getInputStream(zipEntry));
        byte[] crfInfoBytes = new byte[(int) zipEntry.getSize()];
        zipFile.getInputStream(zipEntry).read(crfInfoBytes);
        CRF4 crf = createCRF(trainFileOption.value, crfInfo);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(modelFileOption.value));
        zos.putNextEntry(new ZipEntry("crf-info.xml"));
        zos.write(crfInfoBytes);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry("crf-model.ser"));
        ObjectOutputStream oos = new ObjectOutputStream(zos);
        oos.writeObject(crf);
        oos.flush();
        zos.closeEntry();
        zos.close();
    }
}
