package edu.lium.mira;

import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.text.*;
import gnu.trove.*;

class MiraSparse implements Serializable {

    static final long serialVersionUID = 5L;

    DecimalFormat formatter = new DecimalFormat("0.0000");

    class Example implements Serializable {

        static final long serialVersionUID = 1L;

        Vector<String> lines;

        double score;

        double scores[][];

        public int[] labels;

        public int[][] unigrams;

        public int[][] bigrams;

        public void printLabels() {
            for (int i = 0; i < labels.length; i++) System.err.print(labels[i] + " ");
            System.err.println(score);
        }
    }

    Vector<Template> templates = null;

    TObjectIntHashMap<String> unigramIds = new TObjectIntHashMap<String>();

    TObjectIntHashMap<String> bigramIds = new TObjectIntHashMap<String>();

    TObjectIntHashMap<String> knownLabels = new TObjectIntHashMap<String>();

    String labels[];

    int numSharedLabels = 0;

    int sharedLabelMapping[][] = null;

    int numLabels;

    int numUnigramFeatures;

    int numBigramFeatures;

    int numWeights = 0;

    transient int shiftColumns = 0;

    public transient int nbest = 1;

    public double weights[];

    public double avgWeights[];

    public int lookup[];

    double clip = 1;

    int xsize = 1;

    public void setClip(double clip) {
        this.clip = clip;
    }

    public boolean iobScorer = false;

    public void setIobScorer() {
        iobScorer = true;
    }

    Pattern templateRegex = Pattern.compile("%x\\[(-?\\d+),(\\d+)\\]");

    class Template implements Serializable {

        static final long serialVersionUID = 1L;

        public String definition;

        String[] prefix;

        String suffix;

        int[] rows;

        int[] columns;

        public Template(String definition) {
            this.definition = definition;
            Vector<String> prefix = new Vector<String>();
            Vector<Integer> rows = new Vector<Integer>();
            Vector<Integer> columns = new Vector<Integer>();
            Matcher matcher = templateRegex.matcher(definition);
            int last = 0;
            while (matcher.find()) {
                rows.add(new Integer(matcher.group(1)));
                columns.add(new Integer(matcher.group(2)));
                prefix.add(definition.substring(last, matcher.start()));
                last = matcher.end();
            }
            this.suffix = definition.substring(last);
            if (this.suffix.length() == 0) this.suffix = null;
            this.prefix = new String[prefix.size()];
            this.rows = new int[rows.size()];
            this.columns = new int[columns.size()];
            for (int i = 0; i < rows.size(); i++) {
                this.rows[i] = rows.get(i).intValue();
                this.columns[i] = columns.get(i).intValue();
                if (prefix.get(i).length() == 0) this.prefix[i] = null; else this.prefix[i] = prefix.get(i);
            }
        }

        public String apply(Vector<String[]> parts, int current, int shiftColumns, boolean includeBorderFeatures) {
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < prefix.length; i++) {
                if (prefix[i] != null) output.append(prefix[i]);
                int row = rows[i] + current;
                int column = columns[i] + shiftColumns;
                if (row >= 0 && row < parts.size()) {
                    String[] line = parts.get(row);
                    if (column >= 0 && column < line.length) {
                        output.append(line[column]);
                    } else {
                        System.err.print("ERROR: wrong column in template \"" + definition + "\" for line \"");
                        for (int j = 0; j < line.length - 1; j++) {
                            System.err.print(line[j] + " ");
                        }
                        System.err.println(line[line.length - 1] + "\"");
                        System.exit(1);
                    }
                } else if (includeBorderFeatures) {
                    output.append("_B");
                    output.append(row);
                }
            }
            if (suffix != null) output.append(suffix);
            return output.toString();
        }
    }

    public void loadTemplates(String filename) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filename));
        String line;
        templates = new Vector<Template>();
        while (null != (line = input.readLine())) {
            line = line.split("#")[0];
            line = line.trim();
            if (line.equals("")) continue;
            templates.add(new Template(line));
        }
        System.err.println("read " + templates.size() + " templates from \"" + filename + "\"");
    }

    public String[] predict(Vector<String[]> parts) {
        Example example = encodeFeatures(parts, false, true);
        Example prediction;
        if (bigramIds.size() > 0) prediction = decodeViterbi(example); else prediction = decodeUnigram(example);
        String output[] = new String[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            output[i] = labels[prediction.labels[i]];
        }
        return output;
    }

    public Example encodeFeatures(Vector<String[]> parts, boolean newFeatures, boolean includeBorderFeatures) {
        Example example = new Example();
        example.labels = new int[parts.size()];
        example.unigrams = new int[parts.size()][];
        example.bigrams = new int[parts.size()][];
        Vector<String> unigrams = new Vector<String>();
        Vector<String> bigrams = new Vector<String>();
        for (int i = 0; i < parts.size(); i++) {
            String[] current = parts.get(i);
            if (newFeatures) {
                example.labels[i] = knownLabels.adjustOrPutValue(current[current.length - 1], 0, knownLabels.size());
            } else {
                example.labels[i] = knownLabels.get(current[current.length - 1]);
            }
            unigrams.clear();
            bigrams.clear();
            for (int j = 0; j < templates.size(); j++) {
                String feature = templates.get(j).apply(parts, i, shiftColumns, includeBorderFeatures);
                if (feature != null) {
                    if (feature.startsWith("U")) unigrams.add(feature); else if (feature.startsWith("B")) bigrams.add(feature);
                }
            }
            example.unigrams[i] = new int[unigrams.size()];
            int last = 0;
            for (int j = 0; j < unigrams.size(); j++) {
                if (newFeatures) {
                    example.unigrams[i][j] = unigramIds.adjustOrPutValue(unigrams.get(j), 0, unigramIds.size());
                } else {
                    if (unigramIds.containsKey(unigrams.get(j))) {
                        example.unigrams[i][last] = unigramIds.get(unigrams.get(j));
                        last++;
                    }
                }
            }
            if (!newFeatures && last < example.unigrams[i].length) {
                int old[] = example.unigrams[i];
                example.unigrams[i] = new int[last];
                System.arraycopy(old, 0, example.unigrams[i], 0, last);
            }
            example.bigrams[i] = new int[bigrams.size()];
            last = 0;
            for (int j = 0; j < bigrams.size(); j++) {
                if (newFeatures) {
                    example.bigrams[i][j] = bigramIds.adjustOrPutValue(bigrams.get(j), 0, bigramIds.size());
                } else {
                    if (bigramIds.containsKey(bigrams.get(j))) {
                        example.bigrams[i][last] = bigramIds.get(bigrams.get(j));
                        last++;
                    }
                }
            }
            if (!newFeatures && last < example.bigrams[i].length) {
                int old[] = example.bigrams[i];
                example.bigrams[i] = new int[last];
                System.arraycopy(old, 0, example.bigrams[i], 0, last);
            }
        }
        return example;
    }

    public Example nextExample(BufferedReader input, boolean newFeatures) throws IOException {
        Vector<String> lines = new Vector<String>();
        Vector<String[]> parts = new Vector<String[]>();
        String line;
        while (null != (line = input.readLine())) {
            line = line.trim();
            if (line.length() == 0) break;
            String tokens[] = line.split("\\s+");
            if (tokens.length > xsize + 1 && newFeatures) xsize = tokens.length - 1;
            parts.add(tokens);
            lines.add(line);
        }
        if (line == null && parts.size() == 0) return null;
        Example example = encodeFeatures(parts, newFeatures, true);
        example.lines = lines;
        return example;
    }

    public void initModel(boolean randomInit) {
        weights = new double[numWeights];
        avgWeights = new double[numWeights];
        if (randomInit) {
            for (int i = 0; i < weights.length; i++) {
                weights[i] = 2 * Math.random() - 1;
                avgWeights[i] = 2 * Math.random() - 1;
            }
        }
        System.err.println("model: " + numWeights + " weights");
    }

    public void loadSharedLabels(String filename) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filename));
        TObjectIntHashMap<String> knownSharedLabels = new TObjectIntHashMap<String>();
        sharedLabelMapping = new int[numLabels][];
        String line;
        while (null != (line = input.readLine())) {
            String tokens[] = line.trim().split(" ");
            if (tokens.length == 0) continue;
            int label = knownLabels.get(tokens[0]);
            sharedLabelMapping[label] = new int[tokens.length];
            sharedLabelMapping[label][0] = label;
            for (int i = 1; i < tokens.length; i++) {
                int sharedLabel = knownSharedLabels.adjustOrPutValue(tokens[i], 0, knownSharedLabels.size());
                sharedLabelMapping[label][i] = numLabels + sharedLabel;
            }
        }
        numSharedLabels = knownSharedLabels.size();
        System.err.println("shared labels: " + numSharedLabels);
    }

    public void initWeightsWithFrequency(String filename) throws IOException {
        if (numSharedLabels > 0) {
            System.err.println("ERROR: frequency init unsupported with shared labels");
            return;
        }
        BufferedReader input = new BufferedReader(new FileReader(filename));
        int num = 0;
        Example example;
        while (null != (example = nextExample(input, false))) {
            for (int slot = 0; slot < example.labels.length; slot++) {
                for (int i = 0; i < example.unigrams[slot].length; i++) {
                    for (int j = 0; j < numLabels; j++) {
                        int id = getId(example.unigrams[slot][i], j);
                        if (j == example.labels[slot]) weights[id] += 1; else weights[id] -= 1 / numLabels;
                    }
                }
                for (int i = 0; i < example.bigrams[slot].length; i++) {
                    if (slot > 0) {
                        for (int j = 0; j < numLabels; j++) {
                            for (int k = 0; k < numLabels; k++) {
                                int id = getId(example.bigrams[slot][i], j, k);
                                if (j == example.labels[slot] && k == example.labels[slot - 1]) weights[id] += 1; else weights[id] -= 1 / (numLabels * numLabels);
                            }
                        }
                    }
                }
            }
            if (num % 100 == 0) System.err.print("\rfrequency init: " + num);
            num += 1;
        }
        System.err.println("\rfrequency init: " + num);
    }

    public int count(String filename, int cutoff) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filename));
        int num = 0;
        Example example;
        TIntIntHashMap unigramCounts = new TIntIntHashMap();
        TIntObjectHashMap<TIntHashSet> unigramLabels = new TIntObjectHashMap<TIntHashSet>();
        TIntIntHashMap bigramCounts = new TIntIntHashMap();
        while (null != (example = nextExample(input, true))) {
            for (int slot = 0; slot < example.unigrams.length; slot++) {
                for (int i = 0; i < example.unigrams[slot].length; i++) {
                    unigramCounts.adjustOrPutValue(example.unigrams[slot][i], 1, 1);
                    TIntHashSet labelSet = unigramLabels.get(example.unigrams[slot][i]);
                    if (labelSet == null) {
                        labelSet = new TIntHashSet();
                        labelSet.add(example.labels[slot]);
                        unigramLabels.put(example.unigrams[slot][i], labelSet);
                    } else {
                        labelSet.add(example.labels[slot]);
                    }
                }
                for (int i = 0; i < example.bigrams[slot].length; i++) {
                    bigramCounts.adjustOrPutValue(example.bigrams[slot][i], 1, 1);
                }
            }
            num += 1;
        }
        System.err.println("\rcounting: " + num);
        System.err.println("unigrams: " + unigramIds.size() + ", bigrams: " + bigramIds.size());
        numLabels = knownLabels.size();
        TIntObjectHashMap<TIntHashSet> newUnigramLabels = new TIntObjectHashMap<TIntHashSet>();
        TObjectIntHashMap<String> newUnigrams = new TObjectIntHashMap<String>();
        String keys[] = unigramIds.keys(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            int id = unigramIds.get(keys[i]);
            if (unigramCounts.get(id) >= cutoff) {
                int newId = newUnigrams.size();
                newUnigrams.put(keys[i], newId);
                newUnigramLabels.put(newId, unigramLabels.get(id));
            }
        }
        unigramIds = newUnigrams;
        numUnigramFeatures = unigramIds.size();
        unigramLabels = newUnigramLabels;
        TObjectIntHashMap<String> newBigrams = new TObjectIntHashMap<String>();
        keys = bigramIds.keys(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            int id = bigramIds.get(keys[i]);
            if (bigramCounts.get(id) >= cutoff) {
                newBigrams.put(keys[i], newBigrams.size() * numLabels * numLabels + numUnigramFeatures * numLabels);
            }
        }
        bigramIds = newBigrams;
        numBigramFeatures = bigramIds.size();
        System.err.println("unigrams: " + unigramIds.size() + ", bigrams: " + bigramIds.size() + ", cutoff: " + cutoff);
        System.err.println("labels: " + knownLabels.size());
        labels = new String[numLabels];
        for (int i = 0; i < numLabels; i++) {
            labels[knownLabels.get((String) knownLabels.keys()[i])] = (String) knownLabels.keys()[i];
        }
        int lookupSize = 0;
        keys = unigramIds.keys(new String[0]);
        for (String key : keys) {
            int id = unigramIds.get(key);
            lookupSize += 2 + 2 * unigramLabels.get(id).size();
        }
        lookup = new int[lookupSize];
        int nextWeight = 0;
        int nextLookupId = 0;
        int maxLabelSet = 0;
        for (String key : keys) {
            int id = unigramIds.get(key);
            TIntHashSet labelSet = unigramLabels.get(id);
            unigramIds.put(key, nextLookupId);
            lookup[nextLookupId] = labelSet.size();
            int labelSetAsArray[] = labelSet.toArray();
            Arrays.sort(labelSetAsArray);
            if (labelSetAsArray.length > maxLabelSet) maxLabelSet = labelSetAsArray.length;
            for (int i = 0; i < labelSetAsArray.length; i++) {
                lookup[nextLookupId + 1 + 2 * i] = labelSetAsArray[i];
                lookup[nextLookupId + 1 + 2 * i + 1] = nextWeight;
                nextWeight += 1;
            }
            lookup[nextLookupId + 1 + 2 * labelSetAsArray.length] = nextWeight;
            nextWeight += 1;
            nextLookupId += 2 + 2 * labelSet.size();
        }
        keys = bigramIds.keys(new String[0]);
        for (String key : keys) {
            bigramIds.put(key, nextWeight);
            nextWeight += numLabels * numLabels;
        }
        numWeights = nextWeight;
        System.err.println("lookup size: " + lookup.length + ", max labels: " + maxLabelSet);
        return num;
    }

    public class FScorer {

        public double numRef = 0;

        public double numHyp = 0;

        public double numOk = 0;

        public void assess(Example example, Example prediction) {
            for (int i = 0; i < example.labels.length; i++) {
                if (example.labels[i] == prediction.labels[i]) numOk++;
            }
            numRef += example.labels.length;
            numHyp += example.labels.length;
        }

        public double recall() {
            if (numRef == 0) return 0;
            return numOk / numRef;
        }

        public double precision() {
            if (numHyp == 0) return 0;
            return numOk / numHyp;
        }

        public double fscore() {
            double recall = this.recall();
            double precision = this.precision();
            if (recall == 0 && precision == 0) return 0;
            return 2 * recall * precision / (recall + precision);
        }
    }

    public class IOBScorer extends FScorer {

        String outside = "O";

        String beginPrefix = "B";

        String insidePrefix = "I";

        public void assess(Example example, Example prediction) {
            Vector<String> ref = new Vector<String>();
            Vector<String> hyp = new Vector<String>();
            String oldHyp = null;
            for (int i = 0; i < example.labels.length; i++) {
                String refLabel = labels[example.labels[i]];
                String hypLabel = labels[prediction.labels[i]];
                oldHyp = hypLabel;
                if ((hypLabel.startsWith(beginPrefix) || hypLabel.equals(outside)) && (refLabel.startsWith(beginPrefix) || refLabel.equals(outside))) {
                    if (ref.size() > 0 && ref.equals(hyp)) numOk++;
                }
                if (hypLabel.startsWith(beginPrefix)) {
                    numHyp++;
                    hyp.clear();
                    hyp.add(hypLabel);
                }
                if (hypLabel.equals(outside)) hyp.clear();
                if (hypLabel.startsWith(insidePrefix)) hyp.add(hypLabel);
                if (refLabel.startsWith(beginPrefix)) {
                    numRef++;
                    ref.clear();
                    ref.add(refLabel);
                }
                if (refLabel.equals(outside)) ref.clear();
                if (refLabel.startsWith(insidePrefix)) ref.add(refLabel);
            }
            if (ref.size() > 0 && ref.equals(hyp)) {
                numOk++;
            }
        }
    }

    public void train(String filename, int numIters, int numExamples, int iteration) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filename));
        int num = 0;
        double totalLoss = 0;
        double maxLoss = 0;
        Example example;
        FScorer scorer = new FScorer();
        if (iobScorer) scorer = new IOBScorer();
        while (null != (example = nextExample(input, false))) {
            Example prediction;
            if (bigramIds.size() > 0) prediction = decodeViterbi(example); else prediction = decodeUnigram(example);
            double avgUpdate = (double) (numIters * numExamples - (numExamples * ((iteration + 1) - 1) + (num + 1)) + 1);
            double loss = computeLoss(example, prediction);
            loss = update(example, prediction, avgUpdate, loss);
            totalLoss += loss;
            maxLoss += example.labels.length;
            num++;
            scorer.assess(example, prediction);
            if (num % 100 == 0) System.err.print("\r  train: " + num + " examples, terr=" + formatter.format(totalLoss / maxLoss) + " fscore=" + formatter.format(scorer.fscore()));
        }
        System.err.println("\r  train: " + num + " examples, terr=" + formatter.format(totalLoss / maxLoss) + " fscore=" + formatter.format(scorer.fscore()));
    }

    public void test(BufferedReader input, PrintStream output) throws IOException {
        int num = 0;
        double loss = 0;
        double maxLoss = 0;
        Example example;
        FScorer scorer = new FScorer();
        if (iobScorer) scorer = new IOBScorer();
        while (null != (example = nextExample(input, false))) {
            Example prediction;
            if (bigramIds.size() > 0) prediction = decodeViterbi(example); else prediction = decodeUnigram(example);
            if (prediction != null) {
                loss += computeLoss(example, prediction);
                maxLoss += example.labels.length;
                num += 1;
                scorer.assess(example, prediction);
                if (output == null && num % 100 == 0) System.err.print("\r  test: " + num + " examples, terr=" + formatter.format(loss / maxLoss) + " fscore=" + formatter.format(scorer.fscore()));
                if (output != null) {
                    for (int i = 0; i < example.lines.size(); i++) {
                        if (nbest == 1 || prediction.scores == null) {
                            output.println(example.lines.get(i) + " " + labels[prediction.labels[i]]);
                        } else {
                            TreeMap<Double, Integer> nbestLabels = new TreeMap<Double, Integer>();
                            for (int label = 0; label < numLabels; label++) {
                                nbestLabels.put(-prediction.scores[i][label], label);
                                if (nbestLabels.size() > nbest) nbestLabels.remove(nbestLabels.lastKey());
                            }
                            output.print(example.lines.get(i));
                            for (Integer label : nbestLabels.values()) {
                                output.print(" " + labels[label]);
                            }
                            output.println();
                        }
                    }
                    output.println();
                }
            }
        }
        if (output == null) System.err.println("\r  test: " + num + " examples, terr=" + formatter.format(loss / maxLoss) + " fscore=" + formatter.format(scorer.fscore()) + " (ok=" + (int) scorer.numOk + " ref=" + (int) scorer.numRef + " hyp=" + (int) scorer.numHyp + ")");
    }

    protected final int getId(int feature, int currentLabel) {
        int num = lookup[feature];
        int min = 0;
        int max = num - 1;
        int mid = 0;
        boolean found = false;
        while (min + 10 <= max) {
            mid = (min + max) / 2;
            if (lookup[feature + 1 + mid * 2] < currentLabel) {
                min = mid + 1;
            } else if (lookup[feature + 1 + mid * 2] > currentLabel) {
                max = mid - 1;
            } else {
                found = true;
                break;
            }
        }
        if (!found) {
            mid = min;
            while (!found && mid <= max) {
                if (lookup[feature + 1 + mid * 2] == currentLabel) {
                    found = true;
                    break;
                }
                mid++;
            }
        }
        if (found) return lookup[feature + 1 + mid * 2 + 1];
        return lookup[feature + 1 + num * 2];
    }

    protected final int getId(int feature, int previousLabel, int currentLabel) {
        return feature + previousLabel * numLabels + currentLabel;
    }

    protected final double computeScore(Example example, int position, int label) {
        double score = 0;
        if (numSharedLabels == 0) {
            for (int i = 0; i < example.unigrams[position].length; i++) {
                final int id = getId(example.unigrams[position][i], label);
                if (id != -1) score += weights[id];
            }
        } else {
            for (int i = 0; i < example.unigrams[position].length; i++) {
                for (int j = 0; j < sharedLabelMapping[label].length; j++) {
                    final int id2 = getId(example.unigrams[position][i], sharedLabelMapping[label][j]);
                    if (id2 != -1) score += weights[id2];
                }
            }
        }
        return score;
    }

    protected final double computeScore(Example example, int position1, int label1, int label2) {
        double score = 0;
        if (numSharedLabels == 0) {
            for (int i = 0; i < example.bigrams[position1].length; i++) {
                final int id = getId(example.bigrams[position1][i], label2, label1);
                if (id != -1) score += weights[id];
            }
        } else {
            for (int i = 0; i < example.bigrams[position1].length; i++) {
                for (int j = 0; j < sharedLabelMapping[label1].length; j++) {
                    for (int k = 0; k < sharedLabelMapping[label2].length; k++) {
                        final int id2 = getId(example.bigrams[position1][i], sharedLabelMapping[label2][k], sharedLabelMapping[label1][j]);
                        if (id2 != -1) score += weights[id2];
                    }
                }
            }
        }
        return score;
    }

    class Decision implements Comparable<Decision> {

        public Decision previous = null;

        public double score = 0;

        public int label;

        public Decision(int label, double score, Decision previous) {
            this.label = label;
            this.score = score;
            this.previous = previous;
        }

        public final int compareTo(Decision peer) {
            if (this.score < peer.score) return 1;
            return -1;
        }
    }

    protected Example decodeViterbiBeam(Example example) {
        int width = 100;
        if (example.labels.length == 0) {
            Example prediction = new Example();
            prediction.labels = new int[0];
            return prediction;
        }
        Decision beam[][] = new Decision[example.labels.length][numLabels];
        for (int position = 0; position < example.labels.length; position++) {
            for (int label = 0; label < numLabels; label++) {
                beam[position][label] = new Decision(label, computeScore(example, position, label), null);
            }
            Arrays.sort(beam[position]);
        }
        for (int position = 1; position < example.labels.length; position++) {
            Arrays.sort(beam[position - 1]);
            for (int i = 0; i < numLabels / 2; i++) {
                double max = 0;
                int argmax = -1;
                for (int j = 0; j < width; j++) {
                    double score = beam[position - 1][j].score + beam[position][i].score + computeScore(example, position, beam[position][i].label, beam[position - 1][j].label);
                    if (argmax == -1 || score > max) {
                        max = score;
                        argmax = j;
                    }
                }
                beam[position][i].score = max;
                beam[position][i].previous = beam[position - 1][argmax];
            }
        }
        double max = 0;
        Decision argmax = null;
        for (int i = 0; i < width; i++) {
            if (argmax == null || beam[example.labels.length - 1][i].score > max) {
                max = beam[example.labels.length - 1][i].score;
                argmax = beam[example.labels.length - 1][i];
            }
        }
        Example prediction = new Example();
        prediction.labels = new int[example.labels.length];
        prediction.score = max;
        int current = example.labels.length - 1;
        while (current >= 0) {
            prediction.labels[current] = argmax.label;
            argmax = argmax.previous;
            current -= 1;
        }
        example.score = 0;
        for (int position = 0; position < example.labels.length; position++) {
            example.score += computeScore(example, position, example.labels[position]);
            if (position > 0) example.score += computeScore(example, position, example.labels[position], example.labels[position - 1]);
        }
        return prediction;
    }

    protected Example decodeViterbi(Example example) {
        if (example.labels.length == 0) {
            Example prediction = new Example();
            prediction.labels = new int[0];
            return prediction;
        }
        double score[][] = new double[example.labels.length][numLabels];
        int previous[][] = new int[example.labels.length][numLabels];
        for (int position = 0; position < example.labels.length; position++) {
            if (position == 0) {
                for (int label = 0; label < numLabels; label++) {
                    score[position][label] = computeScore(example, position, label);
                    previous[position][label] = -1;
                }
            } else {
                for (int label = 0; label < numLabels; label++) {
                    double max = 0;
                    int argmax = -1;
                    double labelScore = computeScore(example, position, label);
                    for (int previousLabel = 0; previousLabel < numLabels; previousLabel++) {
                        double scoreByPrevious = labelScore + computeScore(example, position, label, previousLabel) + score[position - 1][previousLabel];
                        if (scoreByPrevious > max || argmax == -1) {
                            max = scoreByPrevious;
                            argmax = previousLabel;
                        }
                    }
                    score[position][label] = max;
                    previous[position][label] = argmax;
                }
            }
        }
        double max = 0;
        int argmax = -1;
        for (int label = 0; label < numLabels; label++) {
            if (argmax == -1 || score[example.labels.length - 1][label] > max) {
                max = score[example.labels.length - 1][label];
                argmax = label;
            }
        }
        Example prediction = new Example();
        prediction.labels = new int[example.labels.length];
        prediction.score = max;
        int current = example.labels.length - 1;
        while (current >= 0) {
            prediction.labels[current] = argmax;
            argmax = previous[current][argmax];
            current -= 1;
        }
        example.score = 0;
        for (int position = 0; position < example.labels.length; position++) {
            example.score += computeScore(example, position, example.labels[position]);
            if (position > 0) example.score += computeScore(example, position, example.labels[position], example.labels[position - 1]);
        }
        return prediction;
    }

    protected Example decodeUnigram(Example example) {
        example.score = 0;
        for (int position = 0; position < example.labels.length; position++) {
            example.score += computeScore(example, position, example.labels[position]);
        }
        Example prediction = new Example();
        prediction.labels = new int[example.labels.length];
        prediction.score = 0;
        if (nbest > 1) prediction.scores = new double[example.labels.length][numLabels];
        for (int position = 0; position < example.labels.length; position++) {
            double max = 0;
            int argmax = -1;
            for (int label = 0; label < numLabels; label++) {
                double score = computeScore(example, position, label) + prediction.score;
                if (nbest > 1) prediction.scores[position][label] = score;
                if (argmax == -1 || score > max) {
                    max = score;
                    argmax = label;
                }
            }
            prediction.labels[position] = argmax;
            prediction.score = max;
        }
        return prediction;
    }

    public double computeLoss(Example example, Example prediction) {
        if (prediction.labels == null || prediction.labels.length != example.labels.length) {
            System.err.println("ERROR: unexpeced prediction");
            return Double.MAX_VALUE;
        }
        double loss = 0;
        for (int i = 0; i < example.labels.length; i++) {
            if (example.labels[i] != prediction.labels[i]) loss++;
        }
        return loss;
    }

    double squaredFeatureDifference(Example example, Example prediction) {
        double output = 0;
        for (int i = 0; i < example.labels.length; i++) {
            if (example.labels[i] != prediction.labels[i]) {
                output += 2 * example.unigrams[i].length;
            }
            if (i > 0 && (example.labels[i] != prediction.labels[i] || example.labels[i - 1] != prediction.labels[i - 1])) {
                output += 2 * example.bigrams[i].length;
            }
        }
        return output;
    }

    double squaredFeatureDifference2(Example example, Example prediction) {
        double output = 0;
        TIntIntHashMap features = new TIntIntHashMap();
        for (int i = 0; i < example.labels.length; i++) {
            for (int j = 0; j < example.unigrams[i].length; j++) {
                if (numSharedLabels == 0) {
                    int example_id = getId(example.unigrams[i][j], example.labels[i]);
                    int prediction_id = getId(example.unigrams[i][j], prediction.labels[i]);
                    if (example_id >= 0) features.adjustOrPutValue(example_id, 1, 1);
                    if (prediction_id >= 0) features.adjustOrPutValue(prediction_id, -1, -1);
                } else {
                    for (int label = 0; label < sharedLabelMapping[example.labels[i]].length; label++) {
                        int shared_id = getId(example.unigrams[i][j], sharedLabelMapping[example.labels[i]][label]);
                        features.adjustOrPutValue(shared_id, 1, 1);
                    }
                    for (int label = 0; label < sharedLabelMapping[prediction.labels[i]].length; label++) {
                        int shared_id = getId(example.unigrams[i][j], sharedLabelMapping[prediction.labels[i]][label]);
                        features.adjustOrPutValue(shared_id, -1, -1);
                    }
                }
            }
            if (i > 0) {
                for (int j = 0; j < example.bigrams[i].length; j++) {
                    if (numSharedLabels == 0) {
                        int example_id = getId(example.bigrams[i][j], example.labels[i - 1], example.labels[i]);
                        int prediction_id = getId(example.bigrams[i][j], prediction.labels[i - 1], prediction.labels[i]);
                        if (example_id >= 0) features.adjustOrPutValue(example_id, 1, 1);
                        if (prediction_id >= 0) features.adjustOrPutValue(prediction_id, -1, -1);
                    } else {
                        for (int label = 0; label < sharedLabelMapping[example.labels[i]].length; label++) {
                            for (int previousLabel = 0; previousLabel < sharedLabelMapping[example.labels[i - 1]].length; previousLabel++) {
                                int shared_id = getId(example.bigrams[i][j], sharedLabelMapping[example.labels[i - 1]][previousLabel], sharedLabelMapping[example.labels[i]][label]);
                                features.adjustOrPutValue(shared_id, 1, 1);
                            }
                        }
                        for (int label = 0; label < sharedLabelMapping[prediction.labels[i]].length; label++) {
                            for (int previousLabel = 0; previousLabel < sharedLabelMapping[prediction.labels[i - 1]].length; previousLabel++) {
                                int shared_id = getId(example.bigrams[i][j], sharedLabelMapping[prediction.labels[i - 1]][previousLabel], sharedLabelMapping[prediction.labels[i]][label]);
                                features.adjustOrPutValue(shared_id, -1, -1);
                            }
                        }
                    }
                }
            }
        }
        int feature_list[] = features.keys();
        for (int i = 0; i < feature_list.length; i++) {
            double value = features.get(feature_list[i]);
            output += value * value;
        }
        return output;
    }

    public double update(Example example, Example prediction, double avgUpdate, double loss) {
        if (example.score > prediction.score) {
            return loss;
        }
        double alpha = loss - (example.score - prediction.score);
        double sfd = squaredFeatureDifference2(example, prediction);
        alpha /= sfd;
        if (loss == 0) return 0;
        if (alpha < 0) alpha = 0.0;
        if (alpha > clip) alpha = clip;
        for (int i = 0; i < example.labels.length; i++) {
            if (example.labels[i] != prediction.labels[i]) {
                for (int j = 0; j < example.unigrams[i].length; j++) {
                    if (numSharedLabels == 0) {
                        int example_id = getId(example.unigrams[i][j], example.labels[i]);
                        if (example_id != -1) {
                            weights[example_id] += alpha;
                            avgWeights[example_id] += avgUpdate * alpha;
                        }
                        int prediction_id = getId(example.unigrams[i][j], prediction.labels[i]);
                        if (prediction_id != -1) {
                            weights[prediction_id] -= alpha;
                            avgWeights[prediction_id] -= avgUpdate * alpha;
                        }
                    } else {
                        for (int label = 0; label < sharedLabelMapping[example.labels[i]].length; label++) {
                            int shared_example_id = getId(example.unigrams[i][j], sharedLabelMapping[example.labels[i]][label]);
                            if (shared_example_id != -1) {
                                weights[shared_example_id] += alpha;
                                avgWeights[shared_example_id] += avgUpdate * alpha;
                            }
                        }
                        for (int label = 0; label < sharedLabelMapping[prediction.labels[i]].length; label++) {
                            int shared_prediction_id = getId(example.unigrams[i][j], sharedLabelMapping[prediction.labels[i]][label]);
                            if (shared_prediction_id != -1) {
                                weights[shared_prediction_id] -= alpha;
                                avgWeights[shared_prediction_id] -= avgUpdate * alpha;
                            }
                        }
                    }
                }
            }
            if (i > 0 && (example.labels[i] != prediction.labels[i] || example.labels[i - 1] != prediction.labels[i - 1])) {
                for (int j = 0; j < example.bigrams[i].length; j++) {
                    if (numSharedLabels == 0) {
                        int example_id = getId(example.bigrams[i][j], example.labels[i - 1], example.labels[i]);
                        if (example_id != -1) {
                            weights[example_id] += alpha;
                            avgWeights[example_id] += avgUpdate * alpha;
                        }
                        int prediction_id = getId(example.bigrams[i][j], prediction.labels[i - 1], prediction.labels[i]);
                        if (prediction_id != -1) {
                            weights[prediction_id] -= alpha;
                            avgWeights[prediction_id] -= avgUpdate * alpha;
                        }
                    } else {
                        for (int label = 0; label < sharedLabelMapping[example.labels[i]].length; label++) {
                            for (int previousLabel = 0; previousLabel < sharedLabelMapping[example.labels[i - 1]].length; previousLabel++) {
                                int shared_example_id = getId(example.bigrams[i][j], sharedLabelMapping[example.labels[i - 1]][previousLabel], sharedLabelMapping[example.labels[i]][label]);
                                if (shared_example_id != -1) {
                                    weights[shared_example_id] += alpha;
                                    avgWeights[shared_example_id] += avgUpdate * alpha;
                                }
                            }
                        }
                        for (int label = 0; label < sharedLabelMapping[prediction.labels[i]].length; label++) {
                            for (int previousLabel = 0; previousLabel < sharedLabelMapping[prediction.labels[i - 1]].length; previousLabel++) {
                                int shared_prediction_id = getId(example.bigrams[i][j], sharedLabelMapping[prediction.labels[i - 1]][previousLabel], sharedLabelMapping[prediction.labels[i]][label]);
                                if (shared_prediction_id != -1) {
                                    weights[shared_prediction_id] -= alpha;
                                    avgWeights[shared_prediction_id] -= avgUpdate * alpha;
                                }
                            }
                        }
                    }
                }
            }
        }
        return loss;
    }

    void averageWeights(double factor) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] = avgWeights[i] / factor;
        }
    }

    public void saveModel(String filename) throws IOException {
        System.err.println("writing model: " + filename);
        ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filename)));
        knownLabels.writeExternal(output);
        unigramIds.writeExternal(output);
        bigramIds.writeExternal(output);
        output.writeObject(templates);
        output.writeObject(labels);
        output.writeInt(numLabels);
        output.writeInt(numUnigramFeatures);
        output.writeInt(numBigramFeatures);
        output.writeInt(xsize);
        output.writeObject(weights);
        output.writeObject(lookup);
        output.writeInt(numSharedLabels);
        output.writeObject(sharedLabelMapping);
        output.close();
    }

    public void saveTextModel(String filename) throws IOException {
        if (numSharedLabels != 0) {
            System.err.println("ERROR: shared labels are not supported in text models");
            return;
        }
        System.err.println("writing text model: " + filename);
        PrintStream output = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)), false);
        output.println("version: 100");
        output.println("cost-factor: 1");
        output.println("maxid: " + weights.length);
        output.println("xsize: " + xsize);
        output.println();
        for (int i = 0; i < labels.length; i++) output.println(labels[i]);
        output.println();
        for (int i = 0; i < templates.size(); i++) if (!templates.get(i).definition.equals("")) output.println(templates.get(i).definition);
        output.println();
        String unigramNames[] = unigramIds.keys(new String[0]);
        for (int i = 0; i < unigramNames.length; i++) output.println("" + unigramIds.get(unigramNames[i]) + " " + unigramNames[i]);
        String bigramNames[] = bigramIds.keys(new String[0]);
        for (int i = 0; i < bigramNames.length; i++) output.println("" + bigramIds.get(bigramNames[i]) + " " + bigramNames[i]);
        output.println();
        for (int i = 0; i < weights.length; i++) output.println(weights[i]);
        output.close();
    }

    public void loadTextModel(String filename) throws IOException, NumberFormatException {
        System.err.println("ERROR: reading text models not implemented yet with sparseness");
        if (true) return;
        System.err.println("reading text model: " + filename);
        BufferedReader input = new BufferedReader(new FileReader(filename));
        String line;
        int stage = 0;
        knownLabels = new TObjectIntHashMap<String>();
        unigramIds = new TObjectIntHashMap<String>();
        bigramIds = new TObjectIntHashMap<String>();
        templates = new Vector<Template>();
        int currentWeight = 0;
        int lineNum = 0;
        while (null != (line = input.readLine())) {
            lineNum++;
            line = line.trim();
            if (line.equals("")) stage++; else if (stage == 0) {
                String tokens[] = line.split(" ");
                if (tokens.length != 2) {
                    System.err.println("ERROR: unrecognized header \"" + line + "\", line " + lineNum);
                    return;
                } else if (tokens[0].equals("version:") && !tokens[1].equals("100")) {
                    System.err.println("ERROR: unrecognized model version " + tokens[1] + ", line " + lineNum);
                    return;
                } else if (tokens[0].equals("cost-factor:")) {
                    clip = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("maxid:")) {
                    weights = new double[Integer.parseInt(tokens[1])];
                } else if (tokens[0].equals("xsize:")) {
                    xsize = Integer.parseInt(tokens[1]);
                }
            } else if (stage == 1) {
                knownLabels.put(line, knownLabels.size());
            } else if (stage == 2) {
                templates.add(new Template(line));
            } else if (stage == 3) {
                String tokens[] = line.split(" ");
                if (tokens.length != 2) {
                    System.err.println("ERROR: unrecognized feature id \"" + line + "\", line " + lineNum);
                    return;
                }
                int id = Integer.parseInt(tokens[0]);
                if (id < 0 || id >= weights.length) {
                    System.err.println("ERROR: unexpected id \"" + line + "\", line " + lineNum);
                    return;
                }
                if (tokens[1].startsWith("U")) {
                    unigramIds.put(tokens[1], id);
                } else if (tokens[1].startsWith("B")) {
                    bigramIds.put(tokens[1], id);
                } else {
                    System.err.println("ERROR: unexpected feature type \"" + line + "\", line " + lineNum);
                    return;
                }
            } else if (stage == 4) {
                weights[currentWeight] = Double.parseDouble(line);
                currentWeight++;
            }
        }
        numLabels = knownLabels.size();
        labels = new String[numLabels];
        for (int i = 0; i < numLabels; i++) {
            labels[knownLabels.get((String) knownLabels.keys()[i])] = (String) knownLabels.keys()[i];
        }
        numUnigramFeatures = unigramIds.size();
        numBigramFeatures = bigramIds.size();
    }

    @SuppressWarnings("unchecked")
    public void loadModel(String filename) throws IOException, ClassNotFoundException {
        System.err.println("reading model: " + filename);
        ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));
        knownLabels = new TObjectIntHashMap<String>();
        knownLabels.readExternal(input);
        unigramIds = new TObjectIntHashMap<String>();
        unigramIds.readExternal(input);
        bigramIds = new TObjectIntHashMap<String>();
        bigramIds.readExternal(input);
        templates = (Vector<Template>) input.readObject();
        labels = (String[]) input.readObject();
        numLabels = input.readInt();
        numUnigramFeatures = input.readInt();
        numBigramFeatures = input.readInt();
        xsize = input.readInt();
        weights = (double[]) input.readObject();
        lookup = (int[]) input.readObject();
        numSharedLabels = input.readInt();
        sharedLabelMapping = (int[][]) input.readObject();
        input.close();
    }

    @SuppressWarnings("unchecked")
    public void mergeModel(String filename) throws IOException, ClassNotFoundException {
        System.err.println("merging model: " + filename);
        ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));
        TObjectIntHashMap<String> knownLabels = new TObjectIntHashMap<String>();
        knownLabels.readExternal(input);
        TObjectIntHashMap<String> unigramIds = new TObjectIntHashMap<String>();
        unigramIds.readExternal(input);
        TObjectIntHashMap<String> bigramIds = new TObjectIntHashMap<String>();
        bigramIds.readExternal(input);
        Vector<Template> templates = (Vector<Template>) input.readObject();
        String[] labels = (String[]) input.readObject();
        int numLabels = input.readInt();
        int numUnigramFeatures = input.readInt();
        int numBigramFeatures = input.readInt();
        double[] weights = (double[]) input.readObject();
        int numSharedLabels = input.readInt();
        if (numSharedLabels != 0) {
            System.err.println("ERROR: merging models with shared labels is not supported yet");
            return;
        }
        int[][] sharedLabelMapping = (int[][]) input.readObject();
        input.close();
        if (numLabels != this.numLabels) {
            System.err.println("ERROR: number of label mismatch in " + filename);
            return;
        }
        if (numUnigramFeatures != this.numUnigramFeatures) {
            System.err.println("ERROR: number of unigram features mismatch in " + filename);
            return;
        }
        if (numBigramFeatures != this.numBigramFeatures) {
            System.err.println("ERROR: number of bigram features mismatch in " + filename);
            return;
        }
        if (weights.length != this.weights.length) {
            System.err.println("ERROR: number of weights mismatch in " + filename);
            return;
        }
        if (templates.size() != this.templates.size()) {
            System.err.println("ERROR: templates mismatch in " + filename);
        }
        int labelMapping[] = new int[numLabels];
        for (int label = 0; label < numLabels; label++) {
            labelMapping[label] = this.knownLabels.get(labels[label]);
        }
        int unigramMapping[] = new int[numUnigramFeatures];
        String unigramFeatures[] = unigramIds.keys(new String[0]);
        for (int unigram = 0; unigram < numUnigramFeatures; unigram++) {
            int id = unigramIds.get(unigramFeatures[unigram]);
            unigramMapping[id] = this.unigramIds.get(unigramFeatures[unigram]);
        }
        for (int unigram = 0; unigram < numUnigramFeatures; unigram++) {
            for (int label = 0; label < numLabels; label++) {
                int id = unigram + label * numUnigramFeatures;
                int localId = unigramMapping[unigram] + labelMapping[label] * numUnigramFeatures;
                this.weights[localId] += weights[id];
            }
        }
        int bigramMapping[] = new int[numBigramFeatures];
        String bigramFeatures[] = bigramIds.keys(new String[0]);
        for (int bigram = 0; bigram < numBigramFeatures; bigram++) {
            int id = bigramIds.get(bigramFeatures[bigram]);
            bigramMapping[id] = this.bigramIds.get(bigramFeatures[bigram]);
        }
        for (int bigram = 0; bigram < numBigramFeatures; bigram++) {
            for (int label1 = 0; label1 < numLabels; label1++) {
                for (int label2 = 0; label2 < numLabels; label2++) {
                    int id = getId(bigram, label1, label2);
                    int localId = getId(bigramMapping[bigram], labelMapping[label1], labelMapping[label2]);
                    this.weights[localId] += weights[id];
                }
            }
        }
    }

    public void setShiftColumns(int shiftColumns) {
        this.shiftColumns = shiftColumns;
    }

    public void usage(String error) {
        if (error != null) {
            System.err.println("Unrecognized argument \"" + error + "\"");
        }
        System.err.println("TRAIN: java -Xmx2G MiraSparse -t [-f <cutoff>|-s <sigma>|-n <iter>|-i <model>|-r|-iob|-fi] <template> <train> <model> [heldout]");
        System.err.println("  -t                 learn model weights given training data");
        System.err.println("  -f <cutoff>        remove features with counts less than or equal to cutoff");
        System.err.println("  -s <sigma>         hyper parameter corresponding to the maximum weight update after seeing an example");
        System.err.println("  -n <iter>          perform n iterations (due to averaging, different number of iterations => different iterations)");
        System.err.println("  -i <model>         input an already trained model");
        System.err.println("  -r                 randomize starting weights");
        System.err.println("  -iob               compute f-score using I- (inside), O (outside), B- (begin) prefixes in labels");
        System.err.println("  -fi                init weights with normalized frequency of features");
        System.err.println("  -labels <file>     shared weight label mapping file");
        System.err.println("  <template>         template definition file");
        System.err.println("  <train>            training data");
        System.err.println("  <model>            model file name");
        System.err.println("  [heldout]          compute error rate on an dataset not used for training");
        System.err.println("PREDICT: java -Xmx2G MiraSparse -p [-shift n] <model> [test]");
        System.err.println("  -p                 predict labels on test data given a model");
        System.err.println("  -shift <n>         shift column ids in template by <n> (lets you pass new columns through at test time)");
        System.err.println("  -nbest <n>         display n-best labels for unigram models only");
        System.err.println("  <model>            model file name");
        System.err.println("  [test]             test file name, stdin if not specified");
        System.err.println("CONVERT: java -Xmx2G MiraSparse -c [<model> <model.txt>|<model.txt> <model>]");
        System.err.println("  -c                 convert a binary model to text or vice versa");
        System.err.println("  <model>            input/output binary model");
        System.err.println("  <model.txt>        input/output text model (must end in .txt)");
        System.err.println("MERGE: java -Xmx2G MiraSparse -m <output_model> <model1> <model2> ...");
        System.err.println("  -c                 merge multiple models by adding their weights (no normalization)");
        System.exit(1);
    }

    public static void main(String args[]) {
        try {
            MiraSparse mira = new MiraSparse();
            int current = 0;
            int mode = -1;
            int frequency = 0;
            double sigma = 1;
            int iterations = 10;
            int shiftColumns = 0;
            int nbest = 1;
            String templateName = null;
            String trainName = null;
            String modelName = null;
            String convertModelName = null;
            String testName = null;
            String initialModelName = null;
            String sharedLabelFile = null;
            Vector<String> mergeModelNames = new Vector<String>();
            boolean randomInit = false;
            boolean frequencyInit = false;
            boolean iobScorer = false;
            while (current < args.length) {
                if (args[current].equals("-t")) mode = 0; else if (args[current].equals("-p")) mode = 1; else if (args[current].equals("-c")) mode = 2; else if (args[current].equals("-m")) mode = 3; else if (mode == 0 && args[current].equals("-f")) frequency = Integer.parseInt(args[++current]); else if (mode == 0 && args[current].equals("-s")) sigma = Double.parseDouble(args[++current]); else if (mode == 0 && args[current].equals("-n")) iterations = Integer.parseInt(args[++current]); else if (mode == 0 && args[current].equals("-i")) initialModelName = args[++current]; else if (mode == 0 && args[current].equals("-r")) randomInit = true; else if (mode == 0 && args[current].equals("-iob")) iobScorer = true; else if (mode == 0 && args[current].equals("-fi")) frequencyInit = true; else if (mode == 0 && args[current].equals("-labels")) sharedLabelFile = args[++current]; else if (mode == 0 && templateName == null) templateName = args[current]; else if (mode == 0 && trainName == null) trainName = args[current]; else if (mode == 0 && modelName == null) modelName = args[current]; else if (mode == 0 && testName == null) testName = args[current]; else if (mode == 1 && args[current].equals("-shift")) shiftColumns = Integer.parseInt(args[++current]); else if (mode == 1 && args[current].equals("-nbest")) nbest = Integer.parseInt(args[++current]); else if (mode == 1 && modelName == null) modelName = args[current]; else if (mode == 1 && testName == null) testName = args[current]; else if (mode == 2 && modelName == null) modelName = args[current]; else if (mode == 2 && convertModelName == null) convertModelName = args[current]; else if (mode == 3 && modelName == null) modelName = args[current]; else if (mode == 3 && modelName != null) mergeModelNames.add(args[current]); else mira.usage(args[current]);
                current++;
            }
            if (modelName == null || mode < 0) mira.usage(null);
            if (mode == 0) {
                if (iobScorer) mira.setIobScorer();
                mira.loadTemplates(templateName);
                mira.setClip(sigma);
                int numExamples = mira.count(trainName, frequency);
                if (sharedLabelFile != null) mira.loadSharedLabels(sharedLabelFile);
                mira.initModel(randomInit);
                if (frequencyInit) mira.initWeightsWithFrequency(trainName);
                if (initialModelName != null) mira.mergeModel(initialModelName);
                for (int i = 0; i < iterations; i++) {
                    System.err.println("iteration " + i);
                    mira.train(trainName, iterations, numExamples, i);
                    mira.averageWeights(iterations * numExamples);
                    if (testName != null) {
                        BufferedReader input = new BufferedReader(new FileReader(testName));
                        mira.test(input, null);
                    }
                }
                mira.saveModel(modelName);
            } else if (mode == 1) {
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                if (testName != null) input = new BufferedReader(new FileReader(testName));
                if (modelName.endsWith(".txt")) mira.loadTextModel(modelName); else mira.loadModel(modelName);
                mira.setShiftColumns(shiftColumns);
                mira.nbest = nbest;
                mira.test(input, System.out);
            } else if (mode == 2) {
                if (modelName.endsWith(".txt")) mira.loadTextModel(modelName); else mira.loadModel(modelName);
                if (convertModelName.endsWith(".txt")) mira.saveTextModel(convertModelName); else mira.saveModel(convertModelName);
            } else if (mode == 3) {
                for (int i = 0; i < mergeModelNames.size(); i++) {
                    if (i == 0) {
                        if (modelName.endsWith(".txt")) mira.loadTextModel(modelName); else mira.loadModel(modelName);
                    } else mira.mergeModel(mergeModelNames.get(i));
                }
                mira.saveModel(modelName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
