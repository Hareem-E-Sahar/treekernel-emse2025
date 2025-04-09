package org.systemsbiology.msbid.align;

import java.io.*;
import java.util.*;
import cern.colt.list.*;
import cern.jet.stat.*;
import org.systemsbiology.msbid.property.PropertiesProvider;

public class FeatureAlign {

    private static final String OUTPUTF = "AlignTable.txt";

    private static final String R_SCRIPT = "runKernDensity.R";

    private String m_RScript;

    private static final String R_INPUTF = "values.txt";

    private static final String R_OUTPUTF = "valuesout.txt";

    private float intenCutOff = 0f;

    private float align_mz_err_tolerance = 0.02f;

    private float pValueCutOff = 0.1f;

    private String peakID_param;

    private int numFeatureFiles;

    private int numFeatures;

    private String inputList;

    private List<String> featureFiles;

    private String project;

    private List<Integer> featureInClass;

    private ArrayList[] features;

    private int[][] matchingIndexes;

    private float[][] probs;

    private int highestColCount;

    FeatureAlign(String inputList, String bin_dir, String user_peakID_param, String project) {
        if (project != null) {
            this.project = project + File.separatorChar;
        } else this.project = "." + File.separatorChar;
        this.m_RScript = bin_dir + File.separatorChar + R_SCRIPT;
        this.inputList = inputList;
        featureFiles = new ArrayList<String>();
        featureInClass = new ArrayList<Integer>();
        this.readFeatureFileName();
        this.numFeatures = 100000;
        this.peakID_param = user_peakID_param;
        features = new ArrayList[numFeatureFiles];
        for (int i = 0; i < numFeatureFiles; i++) features[i] = new ArrayList(4000);
        matchingIndexes = new int[numFeatureFiles][numFeatures];
        probs = new float[numFeatureFiles][numFeatures];
        for (int i = 0; i < numFeatureFiles; i++) {
            for (int j = 0; j < numFeatures; j++) {
                matchingIndexes[i][j] = -2;
                probs[i][j] = -2f;
            }
        }
        highestColCount = 0;
    }

    public void readPeakIDParam() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.peakID_param));
            String thisLine = null;
            while ((thisLine = br.readLine()) != null && !(thisLine.equals(""))) {
                if (thisLine.startsWith("intensity")) {
                    String[] parts = thisLine.split("=");
                    intenCutOff = Float.parseFloat(parts[1]);
                }
                if (thisLine.startsWith("align_mz_err_tolerance")) {
                    String[] parts = thisLine.split("=");
                    align_mz_err_tolerance = Float.parseFloat(parts[1]);
                    System.out.println("align_mz_err_tolerance value read from file: " + align_mz_err_tolerance);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readFeatureFileName() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.inputList));
            int i = 0;
            String thisLine = null;
            while ((thisLine = br.readLine()) != null && !(thisLine.equals("") && !(thisLine.equals("\n")))) {
                if (!thisLine.startsWith("#")) {
                    featureFiles.add((thisLine.split("\\t"))[0].trim());
                    int t = Integer.parseInt((thisLine.split("\\t"))[1].trim());
                    featureInClass.add(new Integer(t));
                    i++;
                }
            }
            br.close();
            this.numFeatureFiles = i;
        } catch (Exception e) {
            System.err.println("Error happened in reading in feature file list!");
            e.printStackTrace();
            FeatureAlign.printUsage();
        }
    }

    public void readAllFeatures() {
        try {
            BufferedReader br = null;
            String[] parts = null;
            String thisLine = null;
            String peptide = null;
            for (int i = 0; i < numFeatureFiles; i++) {
                br = new BufferedReader(new FileReader(featureFiles.get(i)));
                br.readLine();
                while ((thisLine = br.readLine()) != null && !(thisLine.equals(""))) {
                    thisLine = thisLine.trim();
                    parts = thisLine.split("\\t");
                    if (parts.length == 10) peptide = parts[9].trim(); else peptide = "NotMapped";
                    features[i].add(new AlignPeak(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Integer.parseInt(parts[3]), Float.parseFloat(parts[4]), peptide));
                }
                br.close();
            }
        } catch (Exception e) {
            System.out.println("Error happened in read in features!");
            e.printStackTrace();
            FeatureAlign.printUsage();
        }
    }

    public void pairwiseMatch() {
        int class1 = 0;
        int class2 = 0;
        ArrayList pairMatchIndex;
        for (int i = 0; i < numFeatureFiles; i++) {
            class1 = featureInClass.get(i);
            for (int j = i + 1; j < numFeatureFiles; j++) {
                class2 = featureInClass.get(j);
                System.out.println("matching " + featureFiles.get(i) + " " + featureFiles.get(j));
                pairMatchIndex = match(features[i], class1, features[j], class2);
                fillMatchingIndexes(pairMatchIndex, i, j);
            }
        }
    }

    /**
     *@after fill in table, reduce similar entries
     */
    public void reduceData() {
        FloatArrayList mz = new FloatArrayList(100000);
        FloatArrayList rt = new FloatArrayList(100000);
        float[][] mzAndrt;
        for (int column = 0; column < highestColCount; column++) {
            if (!isNaNColumn(column)) {
                mzAndrt = getMeanMZandRT(column);
                for (int row = 0; row < numFeatureFiles; row++) {
                    if (mzAndrt[numFeatureFiles][0] != -2f) {
                        mz.add(mzAndrt[row][0] - mzAndrt[numFeatureFiles][0]);
                        rt.add(mzAndrt[row][1] - mzAndrt[numFeatureFiles][1]);
                    }
                }
            }
        }
        mz.sort();
        rt.sort();
        float mz99percentile = Math.max(align_mz_err_tolerance, mz.get((int) (Math.round(mz.size() * 0.99))));
        float rt99percentile = rt.get((int) (Math.round(rt.size() * 0.99)));
        float baseMeanMZ = 0f;
        float baseMeanRT = 0f;
        float compMeanMZ = 0f;
        float compMeanRT = 0f;
        for (int column = 0; column < highestColCount; column++) {
            if (!(isNaNColumn(column))) {
                baseMeanMZ = getMeanMZ(column);
                baseMeanRT = getMeanRT(column);
                for (int j = column + 1; j < highestColCount; j++) {
                    if (!(isNaNColumn(j))) {
                        compMeanMZ = getMeanMZ(j);
                        compMeanRT = getMeanRT(j);
                        if (compMeanMZ < baseMeanMZ + mz99percentile && compMeanMZ > baseMeanMZ - mz99percentile && compMeanRT < baseMeanRT + rt99percentile && compMeanRT > baseMeanRT - rt99percentile) {
                            updateIndexes(column, j);
                        }
                    }
                }
            }
        }
        calColumnPVal();
    }

    public float getMeanMZ(int column) {
        int count = 0;
        int index = -1;
        float totalMZ = 0f;
        float mz = 0f;
        for (int row = 0; row < numFeatureFiles; row++) {
            index = matchingIndexes[row][column];
            if (index != -2) {
                count++;
                mz = ((AlignPeak) (features[row].get(index))).monoPeakMz;
                totalMZ += mz;
            }
        }
        return (totalMZ / count);
    }

    public float getMeanRT(int column) {
        int count = 0;
        int index = -1;
        float totalRT = 0f;
        float rt = 0f;
        for (int row = 0; row < numFeatureFiles; row++) {
            index = matchingIndexes[row][column];
            if (index != -2) {
                count++;
                rt = ((AlignPeak) (features[row].get(index))).monoPeakRT;
                totalRT += rt;
            }
        }
        return (totalRT / count);
    }

    public void updateIndexes(int baseColumn, int compColumn) {
        for (int row = 0; row < numFeatureFiles; row++) {
            if (matchingIndexes[row][baseColumn] == -2) {
                if (matchingIndexes[row][compColumn] != -2) {
                    matchingIndexes[row][baseColumn] = matchingIndexes[row][compColumn];
                    if (probs[row][compColumn] != -2f) probs[row][baseColumn] = probs[row][compColumn];
                }
            } else {
                if (matchingIndexes[row][compColumn] != -2 && matchingIndexes[row][baseColumn] > matchingIndexes[row][compColumn]) {
                    matchingIndexes[row][baseColumn] = matchingIndexes[row][compColumn];
                    if (probs[row][compColumn] != -2f) probs[row][baseColumn] = probs[row][compColumn];
                }
            }
        }
        for (int row = 0; row < numFeatureFiles; row++) {
            matchingIndexes[row][compColumn] = -2;
            probs[row][compColumn] = -2f;
        }
    }

    public void calColumnPVal() {
        int count = 0;
        double overallPVal = 0;
        for (int column = 0; column < highestColCount; column++) {
            if (!isNaNColumn(column)) {
                count = 0;
                overallPVal = 0;
                for (int row = 1; row < numFeatureFiles; row++) {
                    if (probs[row][column] != -2f) {
                        count++;
                        overallPVal -= 2 * Math.log(probs[row][column]);
                    }
                }
            }
            probs[0][column] = (float) (Probability.chiSquareComplemented(2 * count, overallPVal));
        }
    }

    public float[][] getMeanMZandRT(int column) {
        float[][] mzAndrt = new float[numFeatureFiles + 1][2];
        int index = -1;
        float totalMZ = 0f;
        float totalRT = 0f;
        int count = 0;
        for (int i = 0; i < numFeatureFiles; i++) {
            index = matchingIndexes[i][column];
            if (index != -2) {
                mzAndrt[i][0] = ((AlignPeak) (features[i].get(index))).monoPeakMz;
                totalMZ += mzAndrt[i][0];
                mzAndrt[i][1] = ((AlignPeak) (features[i].get(index))).monoPeakRT;
                totalRT += mzAndrt[i][1];
                count++;
            } else {
                mzAndrt[i][0] = -2f;
                mzAndrt[i][1] = -2f;
            }
        }
        if (count != 0) {
            mzAndrt[numFeatureFiles][0] = totalMZ / count;
            mzAndrt[numFeatureFiles][1] = totalRT / count;
        } else {
            mzAndrt[numFeatureFiles][0] = -2f;
            mzAndrt[numFeatureFiles][1] = -2f;
        }
        return mzAndrt;
    }

    public void fillMatchingIndexes(ArrayList pairMatchIndex, int seedIndex, int compIndex) {
        boolean isReverse = false;
        if (isNaNRow(compIndex)) isReverse = false; else isReverse = true;
        MatchUnitPval tmpUnit = null;
        int baseIndex = -1;
        int corrIndex = -1;
        int foundIndex = -1;
        for (int i = 0; i < pairMatchIndex.size(); i++) {
            tmpUnit = (MatchUnitPval) pairMatchIndex.get(i);
            baseIndex = tmpUnit.index1;
            corrIndex = tmpUnit.index2;
            foundIndex = findValue(matchingIndexes[seedIndex], baseIndex);
            if (foundIndex != -1) {
                doForward(compIndex, foundIndex, corrIndex, tmpUnit.pval);
            } else {
                if (isReverse) {
                    foundIndex = findValue(matchingIndexes[compIndex], corrIndex);
                    if (foundIndex != -1) doReverse(seedIndex, compIndex, foundIndex, baseIndex, tmpUnit.pval); else doFill(seedIndex, compIndex, baseIndex, corrIndex, tmpUnit.pval);
                } else doFill(seedIndex, compIndex, baseIndex, corrIndex, tmpUnit.pval);
            }
        }
    }

    public boolean isNaNRow(int rowIndex) {
        for (int i = 0; i < highestColCount; i++) {
            if (matchingIndexes[rowIndex][i] != -2) return false;
        }
        return true;
    }

    public void doForward(int compIndex, int foundIndex, int corrIndex, float pval) {
        if (matchingIndexes[compIndex][foundIndex] == -2) {
            matchingIndexes[compIndex][foundIndex] = corrIndex;
            probs[compIndex][foundIndex] = pval;
        } else {
            if (matchingIndexes[compIndex][foundIndex] > corrIndex) {
                matchingIndexes[compIndex][foundIndex] = corrIndex;
                probs[compIndex][foundIndex] = pval;
            }
        }
    }

    public void doReverse(int seedIndex, int compIndex, int foundIndex, int baseIndex, float pval) {
        if (matchingIndexes[seedIndex][foundIndex] == -2) {
            matchingIndexes[seedIndex][foundIndex] = baseIndex;
            probs[compIndex][foundIndex] = pval;
        } else {
            if (matchingIndexes[seedIndex][foundIndex] > baseIndex) {
                matchingIndexes[seedIndex][foundIndex] = baseIndex;
                probs[compIndex][foundIndex] = pval;
            }
        }
    }

    public void doFill(int seedIndex, int compIndex, int baseIndex, int corrIndex, float pval) {
        matchingIndexes[seedIndex][highestColCount] = baseIndex;
        matchingIndexes[compIndex][highestColCount] = corrIndex;
        probs[compIndex][highestColCount] = pval;
        highestColCount++;
    }

    public int findValue(int[] values, int key) {
        for (int i = 0; i < highestColCount; i++) {
            if (key == values[i]) return i;
        }
        return -1;
    }

    public ArrayList match(ArrayList seedFeature, int class1, ArrayList compFeature, int class2) {
        float[] weight = { 0.5f, 0.5f };
        float err = 1f;
        float sse = -1f;
        float oldsse = -1f;
        ArrayList forward = new ArrayList(1000);
        ArrayList backward = new ArrayList(1000);
        ArrayList commonIntersect = null;
        while (err > 0.02) {
            if (!(forward.isEmpty())) forward.clear();
            if (!(backward.isEmpty())) backward.clear();
            directionMatch(seedFeature, class1, compFeature, class2, weight, forward);
            directionMatch(compFeature, class2, seedFeature, class1, weight, backward);
            if (forward.size() > 0 && backward.size() > 0) commonIntersect = intersect(forward, backward);
            if (commonIntersect.isEmpty()) break; else {
                float[] sseVector = getsseVector(seedFeature, compFeature, commonIntersect);
                sse = sseVector[0] + sseVector[1];
                weight[0] = sseVector[1] / sse;
                weight[1] = sseVector[0] / sse;
            }
            if (oldsse == -1f) {
                oldsse = sse;
                err = 1f;
            } else {
                err = (Math.abs(oldsse - sse)) / sse;
                oldsse = sse;
            }
        }
        if (weight[0] > 0.95) {
            weight[0] = 0.95f;
            weight[1] = 0.05f;
            if (!forward.isEmpty()) forward.clear();
            if (!backward.isEmpty()) backward.clear();
            directionMatch(seedFeature, class1, compFeature, class2, weight, forward);
            directionMatch(compFeature, class2, seedFeature, class1, weight, backward);
            if (!(forward.isEmpty()) && !(backward.isEmpty())) commonIntersect = intersect(forward, backward);
        }
        float[][] mzANDrtdist = getmzANDrtdist(seedFeature, compFeature, commonIntersect, weight);
        float[][] rtDis = callR(mzANDrtdist[1]);
        float[][] allPairDiff = getAbsmzANDrtDiff(seedFeature, compFeature, commonIntersect, forward, backward);
        float[] pValrt = getPVal(allPairDiff[1], allPairDiff[2], rtDis);
        float[] overallPVal = pValrt;
        return (getTrueCommon(seedFeature, compFeature, commonIntersect, forward, backward, overallPVal));
    }

    public void directionMatch(ArrayList seedfeature, int seedclass, ArrayList compfeature, int compclass, float[] weight, ArrayList result) {
        float[][] matchPoint = new float[1000][4];
        int dataNum = 0;
        AlignPeak fromSeed = null;
        for (int i = 0; i < seedfeature.size(); i++) {
            reset(matchPoint);
            fromSeed = (AlignPeak) seedfeature.get(i);
            dataNum = getMatchPoint(fromSeed, compfeature, matchPoint);
            if (dataNum > 0) {
                float minSUM = 0f;
                float sum = 0f;
                int index = -1;
                if ((seedclass == compclass) && seedclass != -1) {
                    float[] newWeight = { weight[0] - 0.025f, weight[1], 0.025f };
                    for (int a = 0; a < dataNum; a++) {
                        sum = (fromSeed.monoPeakMz - matchPoint[a][0]) * (fromSeed.monoPeakMz - matchPoint[a][0]) * newWeight[0] + (fromSeed.monoPeakRT - matchPoint[a][1]) * (fromSeed.monoPeakRT - matchPoint[a][1]) * newWeight[1] + (fromSeed.monoPeakInten - matchPoint[a][2]) * (fromSeed.monoPeakInten - matchPoint[a][2]) * newWeight[2];
                        if (a == 0) {
                            minSUM = sum;
                            index = (int) matchPoint[a][3];
                        } else {
                            if (sum < minSUM) {
                                minSUM = sum;
                                index = (int) matchPoint[a][3];
                            }
                        }
                    }
                    result.add(new MatchUnit(i, index));
                } else {
                    for (int a = 0; a < dataNum; a++) {
                        sum = ((fromSeed.monoPeakMz - matchPoint[a][0]) * (fromSeed.monoPeakMz - matchPoint[a][0]) * weight[0] + (fromSeed.monoPeakRT - matchPoint[a][1]) * (fromSeed.monoPeakRT - matchPoint[a][1]) * weight[1]) * (1f / matchPoint[a][2]);
                        if (a == 0) {
                            minSUM = sum;
                            index = (int) matchPoint[a][3];
                        } else {
                            if (sum < minSUM) {
                                minSUM = sum;
                                index = (int) matchPoint[a][3];
                            }
                        }
                    }
                    result.add(new MatchUnit(i, index));
                }
            }
        }
    }

    public void reset(float[][] target) {
        for (int i = 0; i < target.length; i++) {
            target[i][0] = 0f;
            target[i][1] = 0f;
            target[i][2] = 0f;
            target[i][3] = 0f;
        }
    }

    public int getMatchPoint(AlignPeak tmpPeak, ArrayList compfeature, float[][] matchPoint) {
        int dataNum = 0;
        AlignPeak partner = null;
        for (int i = 0; i < compfeature.size(); i++) {
            partner = (AlignPeak) compfeature.get(i);
            if (Math.abs(tmpPeak.monoPeakMz - partner.monoPeakMz) < align_mz_err_tolerance && (tmpPeak.charge == partner.charge)) {
                matchPoint[dataNum][0] = partner.monoPeakMz;
                matchPoint[dataNum][1] = partner.monoPeakRT;
                matchPoint[dataNum][2] = partner.monoPeakInten;
                matchPoint[dataNum][3] = i;
                dataNum++;
            }
        }
        return dataNum;
    }

    public ArrayList intersect(ArrayList forward, ArrayList backward) {
        ArrayList returnValue = new ArrayList(1000);
        Iterator forwardit = forward.iterator();
        MatchUnit tmpUnit = null;
        boolean isIntersect = false;
        while (forwardit.hasNext()) {
            tmpUnit = (MatchUnit) forwardit.next();
            if (testIntersect(tmpUnit, backward)) {
                returnValue.add(tmpUnit);
                forwardit.remove();
            }
        }
        return returnValue;
    }

    public boolean testIntersect(MatchUnit test, ArrayList backward) {
        boolean ismatch = false;
        MatchUnit partner = null;
        Iterator backwardit = backward.iterator();
        while (backwardit.hasNext()) {
            partner = (MatchUnit) backwardit.next();
            if (test.index1 == partner.index2 && test.index2 == partner.index1) {
                ismatch = true;
                backwardit.remove();
                break;
            }
        }
        return ismatch;
    }

    public float[] getsseVector(ArrayList seedfeature, ArrayList compfeature, ArrayList intersect) {
        float[] returnVector = new float[2];
        MatchUnit tmpUnit = null;
        AlignPeak seedPeak = null;
        AlignPeak compPeak = null;
        for (int i = 0; i < intersect.size(); i++) {
            tmpUnit = (MatchUnit) intersect.get(i);
            seedPeak = (AlignPeak) seedfeature.get(tmpUnit.index1);
            compPeak = (AlignPeak) compfeature.get(tmpUnit.index2);
            returnVector[0] += (seedPeak.monoPeakMz - compPeak.monoPeakMz) * (seedPeak.monoPeakMz - compPeak.monoPeakMz);
            returnVector[1] += (seedPeak.monoPeakRT - compPeak.monoPeakRT) * (seedPeak.monoPeakRT - compPeak.monoPeakRT);
        }
        return returnVector;
    }

    public float[][] getmzANDrtdist(ArrayList seedFeature, ArrayList compFeature, ArrayList intersect, float[] weight) {
        float[][] mzANDrtdist = new float[2][intersect.size() * 5];
        MatchUnit tmpUnit = null;
        int seedIndex = -1;
        int compIndex = -1;
        AlignPeak seedPeak = null;
        int count = 0;
        float[][] minFive = null;
        for (int i = 0; i < intersect.size(); i++) {
            tmpUnit = (MatchUnit) intersect.get(i);
            seedIndex = tmpUnit.index1;
            compIndex = tmpUnit.index2;
            seedPeak = (AlignPeak) seedFeature.get(seedIndex);
            minFive = getMinFive(seedPeak, intersect, compIndex, weight, compFeature);
            for (int a = 0; a < 2; a++) {
                for (int b = 0; b < 5; b++) {
                    mzANDrtdist[a][count + b] = minFive[a][b];
                }
            }
            count += 5;
        }
        return mzANDrtdist;
    }

    public float[][] getMinFive(AlignPeak seedPeak, ArrayList intersect, int compIndex, float[] weight, ArrayList compFeature) {
        ArrayList disList = new ArrayList(1000);
        float[][] returnValue = new float[2][5];
        MatchUnit tmpUnit = null;
        float dis = -1f;
        AlignPeak compPeak = null;
        for (int i = 0; i < intersect.size(); i++) {
            tmpUnit = (MatchUnit) intersect.get(i);
            if (tmpUnit.index2 != compIndex) {
                compPeak = (AlignPeak) compFeature.get(tmpUnit.index2);
                dis = (seedPeak.monoPeakMz - compPeak.monoPeakMz) * (seedPeak.monoPeakMz - compPeak.monoPeakMz) * weight[0] + (seedPeak.monoPeakRT - compPeak.monoPeakRT) * (seedPeak.monoPeakRT - compPeak.monoPeakRT) * weight[1];
                disList.add(new DistIndex(dis, tmpUnit.index2));
            }
        }
        Collections.sort(disList);
        for (int i = 0; i < 5; i++) {
            compPeak = (AlignPeak) compFeature.get(((DistIndex) disList.get(i)).index);
            returnValue[0][i] = Math.abs(seedPeak.monoPeakMz - compPeak.monoPeakMz);
            returnValue[1][i] = Math.abs(seedPeak.monoPeakRT - compPeak.monoPeakRT);
        }
        return returnValue;
    }

    public float[][] getAbsmzANDrtDiff(ArrayList seedFeature, ArrayList compFeature, ArrayList commonIntersect, ArrayList forward, ArrayList backward) {
        float[][] returnValue = new float[3][commonIntersect.size() + forward.size() + backward.size()];
        AlignPeak seedPeak = null;
        AlignPeak compPeak = null;
        MatchUnit tmpUnit = null;
        int count = 0;
        for (int i = 0; i < commonIntersect.size(); i++) {
            tmpUnit = (MatchUnit) commonIntersect.get(i);
            seedPeak = (AlignPeak) seedFeature.get(tmpUnit.index1);
            compPeak = (AlignPeak) compFeature.get(tmpUnit.index2);
            returnValue[0][i] = Math.abs(seedPeak.monoPeakMz - compPeak.monoPeakMz);
            returnValue[1][i] = Math.abs(seedPeak.monoPeakRT - compPeak.monoPeakRT);
            if (seedPeak.charge == compPeak.charge) returnValue[2][i] = 0; else returnValue[2][i] = 1;
        }
        count += commonIntersect.size();
        for (int a = 0; a < forward.size(); a++) {
            tmpUnit = (MatchUnit) forward.get(a);
            seedPeak = (AlignPeak) seedFeature.get(tmpUnit.index1);
            compPeak = (AlignPeak) compFeature.get(tmpUnit.index2);
            returnValue[0][count + a] = Math.abs(seedPeak.monoPeakMz - compPeak.monoPeakMz);
            returnValue[1][count + a] = Math.abs(seedPeak.monoPeakRT - compPeak.monoPeakRT);
            if (seedPeak.charge == compPeak.charge) returnValue[2][count + a] = 0; else returnValue[2][count + a] = 1;
        }
        count += forward.size();
        for (int b = 0; b < backward.size(); b++) {
            tmpUnit = (MatchUnit) backward.get(b);
            seedPeak = (AlignPeak) seedFeature.get(tmpUnit.index2);
            compPeak = (AlignPeak) compFeature.get(tmpUnit.index1);
            returnValue[0][count + b] = Math.abs(seedPeak.monoPeakMz - compPeak.monoPeakMz);
            returnValue[1][count + b] = Math.abs(seedPeak.monoPeakRT - compPeak.monoPeakRT);
            if (seedPeak.charge == compPeak.charge) returnValue[2][count + b] = 0; else returnValue[2][count + b] = 1;
        }
        return returnValue;
    }

    public float[] getPVal(float[] values, float[] chargeEquals, float[][] dist) {
        float[] pval = new float[values.length];
        int nearestNeighborIndex = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= dist[0][dist[0].length - 1]) pval[i] = 1; else if (values[i] <= dist[0][0]) pval[i] = 0; else {
                nearestNeighborIndex = findNeighbor(values[i], dist[0]);
                pval[i] = trapz(nearestNeighborIndex, dist);
                if (chargeEquals[i] == 1) pval[i] = Math.min(0.95f, pval[i] * 20f);
            }
        }
        return pval;
    }

    public int findNeighbor(float targetVal, float[] values) {
        int low = 0;
        int high = values.length - 1;
        int middle;
        while (low <= high) {
            middle = (low + high) / 2;
            if (targetVal == values[middle]) return middle; else if (targetVal < values[middle]) high = middle - 1; else low = middle + 1;
        }
        if (Math.abs(targetVal - values[high]) > Math.abs(targetVal - values[low])) return low; else return high;
    }

    public float[] getOverallPVal(float[] pValmz, float[] pValrt) {
        float[] returnValue = new float[pValmz.length];
        for (int i = 0; i < pValmz.length; i++) {
            returnValue[i] = (float) (Math.sqrt(pValmz[i] * pValrt[i]));
        }
        return returnValue;
    }

    public ArrayList getTrueCommon(ArrayList seedFeature, ArrayList compFeature, ArrayList common, ArrayList forward, ArrayList backward, float[] pVal) {
        ArrayList returnValue = new ArrayList(1000);
        MatchUnit tmpUnit = null;
        AlignPeak seedPeak = null;
        AlignPeak compPeak = null;
        int count = 0;
        for (int i = 0; i < common.size(); i++) {
            tmpUnit = (MatchUnit) common.get(i);
            seedPeak = (AlignPeak) seedFeature.get(tmpUnit.index1);
            compPeak = (AlignPeak) compFeature.get(tmpUnit.index2);
            if (seedPeak.charge == compPeak.charge && pVal[i] <= pValueCutOff) {
                returnValue.add(new MatchUnitPval(tmpUnit.index1, tmpUnit.index2, pVal[i]));
            }
        }
        count += common.size();
        for (int a = 0; a < forward.size(); a++) {
            tmpUnit = (MatchUnit) forward.get(a);
            seedPeak = (AlignPeak) seedFeature.get(tmpUnit.index1);
            compPeak = (AlignPeak) compFeature.get(tmpUnit.index2);
            if (seedPeak.charge == compPeak.charge && pVal[count + a] <= pValueCutOff) {
                returnValue.add(new MatchUnitPval(tmpUnit.index1, tmpUnit.index2, pVal[count + a]));
            }
        }
        count += forward.size();
        for (int b = 0; b < backward.size(); b++) {
            tmpUnit = (MatchUnit) backward.get(b);
            seedPeak = (AlignPeak) seedFeature.get(tmpUnit.index2);
            compPeak = (AlignPeak) compFeature.get(tmpUnit.index1);
            if (seedPeak.charge == compPeak.charge && pVal[count + b] <= pValueCutOff) {
                returnValue.add(new MatchUnitPval(tmpUnit.index2, tmpUnit.index1, pVal[count + b]));
            }
        }
        return returnValue;
    }

    public void output() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(project + OUTPUTF));
            AlignPeak tmpPeak = null;
            String index = "";
            String mz = "";
            String rt = "";
            String inten = "";
            String totalInten = "";
            String pValue = "";
            String charge = "";
            String peptide = "";
            int count = 0;
            String firstLine = "";
            for (int i = 0; i < numFeatureFiles; i++) {
                index += featureFiles.get(i) + "\t";
                mz += "MonoPeakmz " + i + "\t";
                rt += "MonoPeakRT " + i + "\t";
                inten += "MonoPeakInten " + i + "\t";
                totalInten += "TotalInten " + i + "\t";
                pValue += "p_value " + i + "\t";
                charge += "Charge " + i + "\t";
                peptide += "Peptide " + i + "\t";
            }
            bw.write(index + mz + rt + inten + totalInten + pValue + charge + peptide);
            bw.newLine();
            for (int i = 0; i < highestColCount; i++) {
                index = "";
                mz = "";
                rt = "";
                inten = "";
                totalInten = "";
                pValue = "";
                charge = "";
                peptide = "";
                count = 0;
                if (!isNaNColumn(i)) {
                    for (int j = 0; j < numFeatureFiles; j++) {
                        if (matchingIndexes[j][i] != -2) {
                            count++;
                            index += (matchingIndexes[j][i] + 1) + "\t";
                            tmpPeak = (AlignPeak) features[j].get(matchingIndexes[j][i]);
                            mz += tmpPeak.monoPeakMz + "\t";
                            rt += tmpPeak.monoPeakRT + "\t";
                            inten += tmpPeak.monoPeakInten + "\t";
                            totalInten += tmpPeak.totalInten + "\t";
                            if (probs[j][i] != -2f) pValue += probs[j][i] + "\t"; else pValue += "NaN\t";
                            charge += tmpPeak.charge + "\t";
                            peptide += tmpPeak.idMatchPeptide + "\t";
                        } else {
                            index += "N" + i + "\t";
                            mz += "NaN" + "\t";
                            rt += "NaN" + "\t";
                            inten += intenCutOff + "\t";
                            totalInten += "NaN" + "\t";
                            if (probs[j][i] == -2f) pValue += "NaN" + "\t"; else pValue += probs[j][i] + "\t";
                            charge += "NaN" + "\t";
                            peptide += "NaN" + "\t";
                        }
                    }
                    if (count >= (int) (numFeatureFiles / 3)) {
                        bw.write(index + mz + rt + inten + totalInten + pValue + charge + peptide);
                        bw.newLine();
                    }
                }
            }
            bw.close();
        } catch (Exception e) {
            System.out.println("Error happened in outputing AlignTable!");
            e.printStackTrace();
        }
    }

    public boolean isNaNColumn(int column) {
        for (int row = 0; row < numFeatureFiles; row++) {
            if (matchingIndexes[row][column] != -2) return false;
        }
        return true;
    }

    public float trapz(int index, float[][] dist) {
        float area = 0f;
        for (int i = 0; i < index; i++) {
            area += (dist[1][i] + dist[1][i + 1]) * (dist[0][i + 1] - dist[0][i]) / 2;
        }
        return area;
    }

    public float[][] callR(float[] values) {
        float[][] returnValue = null;
        try {
            writeToDisk(values);
            String r_args = "--args working_dir=" + project + " input_file=" + R_INPUTF + " output_file=" + R_OUTPUTF;
            String[] r_cmd = { "R", "CMD", "BATCH", "--slave", r_args, m_RScript, project + File.separatorChar + "callR.Rout" };
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(r_cmd);
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
            outputGobbler.start();
            errorGobbler.start();
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);
            returnValue = readFromDisk();
        } catch (Exception e) {
            System.out.println("Error happened in callR!");
            e.printStackTrace();
        }
        System.out.println("readback length " + returnValue[0].length);
        return returnValue;
    }

    public void deleteFiles() {
        try {
            File income = new File(R_INPUTF);
            income.delete();
            income = new File(R_OUTPUTF);
            income.delete();
        } catch (Exception e) {
            System.out.println("Error happened in deleting tmp files!");
            e.printStackTrace();
        }
    }

    public void writeToDisk(float[] values) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(project + R_INPUTF)));
            for (int i = 0; i < (values.length - 1); i++) {
                bw.write(values[i] + " ");
            }
            bw.write(values[values.length - 1] + "\n");
            bw.close();
        } catch (Exception e) {
            System.out.println("Error happened in writeToDisk!");
            e.printStackTrace();
        }
    }

    public float[][] readFromDisk() {
        float[][] returnValue = new float[2][401];
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(project + R_OUTPUTF)));
            String thisLine = null;
            float xAxis = 0f;
            float yAxis = 0f;
            int count = 0;
            br.readLine();
            while ((thisLine = br.readLine()) != null) {
                xAxis = Float.parseFloat((thisLine.split("\\s"))[1]);
                yAxis = Float.parseFloat((thisLine.split("\\s"))[2]);
                returnValue[0][count] = xAxis;
                returnValue[1][count] = yAxis;
                count++;
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Error happened in readFromDisk()!");
            e.printStackTrace();
        }
        return returnValue;
    }

    public void printMatchingIndexes() {
        System.out.println("Index table");
        String index = "";
        int count = 0;
        for (int column = 0; column < highestColCount; column++) {
            index = "";
            if (!(isNaNColumn(column))) {
                count++;
                index += count + ".\t";
                for (int row = 0; row < numFeatureFiles; row++) {
                    index += matchingIndexes[row][column] + "\t";
                }
                System.out.println(index);
            }
        }
        System.out.println("index columnCount " + count);
    }

    public void printProbs() {
        System.out.println("probs table");
        String index = "";
        int count = 0;
        for (int column = 0; column < highestColCount; column++) {
            index = "";
            if (!(isNaNColumn(column))) {
                count++;
                index += count + ".\t";
                for (int row = 0; row < numFeatureFiles; row++) {
                    index += probs[row][column] + "\t";
                }
                System.out.println(index);
            }
        }
        System.out.println("probs columncount " + count);
    }

    public static void main(String[] args) {
        int iarg = 0;
        String temp_str[];
        String option;
        String feature_list = null;
        String project = null;
        String peakID_param = null;
        String bin_dir = null;
        while (iarg < args.length) {
            option = args[iarg];
            if (option.indexOf("-feature_list") != -1) {
                temp_str = option.split("=");
                feature_list = temp_str[1];
            } else if (option.indexOf("-bin") != -1) {
                temp_str = option.split("=");
                bin_dir = temp_str[1];
            } else if (option.indexOf("-param") != -1) {
                temp_str = option.split("=");
                peakID_param = temp_str[1];
            } else if (option.indexOf("-project") != -1) {
                temp_str = option.split("=");
                project = temp_str[1];
            } else {
                printUsage();
            }
            iarg++;
        }
        if ((feature_list == null) || (bin_dir == null) || (peakID_param == null)) {
            printUsage();
        }
        FeatureAlign align = new FeatureAlign(feature_list, bin_dir, peakID_param, project);
        align.readPeakIDParam();
        align.readAllFeatures();
        align.pairwiseMatch();
        align.reduceData();
        align.output();
    }

    private static void printUsage() {
        System.err.println("Usage: java " + FeatureAlign.class.getName());
        System.err.println("\t-feature_list=<your feature list file>");
        System.err.println("\t-bin=<the directory of binary files>");
        System.err.println("\t-param=<the location of peakID.param>");
        System.err.println("\t-project=<the directory of the output files located> (optional) ");
        System.err.println("\n");
        System.err.println("Example: java " + FeatureAlign.class.getName() + " -feature_list=FeatureList -bin=Directory -param=peakID.param -project=.");
        System.exit(1);
    }
}
