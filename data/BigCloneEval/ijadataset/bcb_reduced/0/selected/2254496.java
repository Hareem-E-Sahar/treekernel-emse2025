package gem;

import gem.parser.HPRDParser;
import gem.parser.TabDelimitedFileParser;
import gem.util.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import static gem.StageAnalyzer.getPos;

/**
 * @author Ozgun Babur
 */
public class ClusterAnalyzer implements Constants {

    public static final double CORR_PV_THR = 1;

    public static final double SEED_EDGE_CORR_THR = 0;

    public static final double CLUSTER_SCORE_THR = 0.1;

    public static final int MODULE_SIZE_THR = 5;

    public static final int AVG_BINS_PER_GENE = 5;

    public static void main(String[] args) throws Throwable {
        TabDelimitedFileParser p = new TabDelimitedFileParser("resource/factors/AR-select-small.txt");
        Set<String> tarSym = p.getColumnSet(0);
        List<String> modList = getModSymSet();
        Set<String> ids = new HashSet<String>();
        for (String g : tarSym) ids.add(Triplet.getSymbolToGeneMap().get(g));
        for (String g : modList) ids.add(Triplet.getSymbolToGeneMap().get(g));
        String dir = "resource/expdata/expo";
        Map<String, Gene> map = ExpDataReader.readGenes(ids, dir, 0, 0);
        boolean[][] pos = getPos(dir + "/");
        String[] tisName = StageAnalyzer.getStageNames(dir + "/");
        Set<Gene> tars = new HashSet<Gene>(map.values());
        List<Gene> mods = new ArrayList<Gene>();
        for (String modSym : modList) {
            Gene mod = map.get(Triplet.getSymbolToGeneMap().get(modSym));
            if (mod != null) mods.add(mod);
        }
        tars.removeAll(mods);
        System.out.println("tar size = " + tars.size());
        Gene[] allTars = tars.toArray(new Gene[tars.size()]);
        Gene[] modsArr = mods.toArray(new Gene[mods.size()]);
        Arrays.sort(modsArr);
        takeLogIfNeeded(allTars);
        takeLogIfNeeded(modsArr);
        int[] signAll = getSigns(allTars);
        allTars = filterToPositive(allTars, signAll);
        Arrays.sort(allTars);
        signAll = getSigns(allTars);
        printSignDistr(signAll);
        Gene probe = null;
        List<List<Gene>> moduleList = new ArrayList<List<Gene>>();
        int i = 5;
        {
            System.out.println("\n\nCalculating for tissue: " + tisName[i]);
            System.out.println("sample size = " + ArrayUtils.countTrue(pos[i]));
            printModuleScores(allTars, signAll, pos[i], modsArr);
            List<Gene[]> modules = getCorrelatedModules(allTars, signAll, pos[i], probe);
            System.out.println("Number of modules = " + modules.size() + "\n");
            for (Gene[] module : modules) {
                int[] sign = getSigns(module);
                printModuleInfo(module, sign, pos[i]);
                List<Holder> h = new ArrayList<Holder>();
                List<Gene> correlatingMods = new ArrayList<Gene>();
                System.out.println("Modulator correlations:");
                for (Gene mod : mods) {
                    if (mod == null) continue;
                    Holder h0 = new Holder(mod.getSymbol(), mod, module, sign, pos[i]);
                    boolean[] half = filterHalf(mod, pos[i], true);
                    Holder h1 = new Holder(mod.getSymbol() + "+", mod, module, sign, half);
                    half = filterHalf(mod, pos[i], false);
                    Holder h2 = new Holder(mod.getSymbol() + "-", mod, module, sign, half);
                    h.add(h0);
                }
                Collections.sort(h);
                List<Holder> corHold = new ArrayList<Holder>();
                for (Holder ho : h) {
                    if (ho.pval < 0.05) {
                        System.out.println(ho);
                        correlatingMods.add(ho.mod);
                        corHold.add(ho);
                        if (ho.mod == probe) moduleList.add(Arrays.asList(module));
                    }
                }
                System.out.println();
            }
        }
    }

    private static void writeGeneVals(Gene[] gene, boolean[] pos) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("sample.csv"));
        String s = "";
        for (int i = 0; i < pos.length; i++) {
            if (pos[i]) s += "\"exp" + i + "\",";
        }
        s = s.substring(0, s.length() - 1);
        writer.write(s);
        for (Gene g : gene) {
            writer.write("\n");
            s = "";
            for (int i = 0; i < pos.length; i++) {
                if (pos[i]) s += g.value[i] + ",";
            }
            s = s.substring(0, s.length() - 1);
            writer.write(s);
        }
        writer.close();
    }

    private static Gene[] filterToPositive(Gene[] gene, int[] sign) {
        List<Gene> filtered = new ArrayList<Gene>();
        for (int i = 0; i < gene.length; i++) {
            if (sign[i] == 1) filtered.add(gene[i]);
        }
        return filtered.toArray(new Gene[filtered.size()]);
    }

    static Gene get(Gene[] genes, String symb) {
        for (Gene gene : genes) {
            if (gene.getSymbol().equals(symb)) return gene;
        }
        return null;
    }

    static void plotValueHisto(Gene g1, Gene g2) {
        Histogram2D h = new Histogram2D(0.1);
        for (int i = 0; i < g1.value.length; i++) {
            h.count(g1.value[i], g2.value[i]);
        }
        h.plot();
    }

    private static void printModuleInfo(Gene[] module, int[] sign, boolean[] pos) {
        System.out.println("module size = " + module.length);
        System.out.println("avg expr = " + (int) getAverageExpression(module));
        System.out.println("avg corr = " + fmt.format(getAverageCorrelation(module, sign, pos)));
    }

    static void addNecessary(List<Holder> list, Holder... h) {
        List<Holder> pos = new ArrayList<Holder>();
        List<Holder> neg = new ArrayList<Holder>();
        for (Holder hld : h) {
            if (hld.cor > 0) pos.add(hld); else neg.add(hld);
        }
        Holder toAdd = null;
        for (Holder ho : pos) {
            if (toAdd == null) toAdd = ho; else if (ho.pval < toAdd.pval) toAdd = ho;
        }
        if (toAdd != null) list.add(toAdd);
        toAdd = null;
        for (Holder ho : neg) {
            if (toAdd == null) toAdd = ho; else if (ho.pval < toAdd.pval) toAdd = ho;
        }
        if (toAdd != null) list.add(toAdd);
    }

    static class Holder implements Comparable {

        String name;

        Gene mod;

        double cor;

        Double cmp;

        boolean[] pos;

        Gene[] module;

        int[] sign;

        Double pval;

        Holder(String name, Gene mod, Gene[] module, int[] sign, boolean[] pos) {
            this.mod = mod;
            this.module = module;
            this.sign = sign;
            this.pos = pos;
            if (name.length() < 4) name += "  ";
            this.name = name;
            this.cor = calcAverageCorrelation();
            cmp = Math.abs(cor);
            this.pval = Pearson.calcCorrSignificance(cor, ArrayUtils.countTrue(pos));
        }

        double calcAverageCorrelation() {
            double total = 0;
            int cnt = 0;
            int n = pos == null ? mod.value.length : ArrayUtils.countTrue(pos);
            for (int i = 0; i < module.length; i++) {
                double cor = pos == null ? Pearson.calcCorrelation(mod.value, module[i].value) : Pearson.calcCorrelation(mod.value, module[i].value, pos);
                double pv = Pearson.calcCorrSignificance(cor, n);
                if (pv < CORR_PV_THR) {
                    total += sign == null ? cor : cor * sign[i];
                    cnt++;
                }
            }
            return total / cnt;
        }

        public int compareTo(Object o) {
            int c = pval.compareTo(((Holder) o).pval);
            if (c != 0) return c;
            return ((Holder) o).cmp.compareTo(cmp);
        }

        @Override
        public String toString() {
            return name + "\t" + fmt.format(cor);
        }
    }

    public static List<String> getModSymSet() throws Throwable {
        List<String> list = new ArrayList<String>(TabDelimitedFileParser.getColumnSet("resource/NuclearReceptors.txt", 0));
        Collections.sort(list);
        return list;
    }

    public static boolean[] filterHalf(Gene mod, boolean[] pos, boolean upperHalf) {
        boolean[] fil = new boolean[pos.length];
        double median = Summary.median(mod.value, pos);
        for (int i = 0; i < pos.length; i++) {
            fil[i] = pos[i] && ((upperHalf && mod.value[i] > median) || (!upperHalf && mod.value[i] < median));
        }
        return fil;
    }

    public static void printCorrMatrix(List<Holder> corHold) {
        List<Gene> genes = new ArrayList<Gene>();
        for (Holder h : corHold) if (!genes.contains(h.mod)) genes.add(h.mod);
        System.out.print("    ");
        for (Holder h2 : corHold) {
            System.out.print("\t" + h2.name);
        }
        for (Gene g : genes) {
            String s = g.getSymbol();
            if (s.length() < 4) s += "  ";
            System.out.print("\n" + s);
            for (Holder h2 : corHold) {
                if (g == h2.mod) System.out.print("\t    "); else System.out.print("\t" + fmt.format(Pearson.calcCorrelation(g.value, h2.mod.value, h2.pos)));
            }
        }
        System.out.println();
    }

    public static void printModDependency(List<Holder> corHold) {
        List<Gene> genes = new ArrayList<Gene>();
        for (Holder h : corHold) if (!genes.contains(h.mod)) genes.add(h.mod);
        System.out.print("    ");
        for (Holder h2 : corHold) {
            System.out.print("\t" + h2.name);
        }
        for (Gene g : genes) {
            String s = g.getSymbol();
            if (s.length() < 4) s += "  ";
            System.out.print("\n" + s);
            for (Holder h2 : corHold) {
                if (g == h2.mod || !secondModDependsFirst(g, h2)) System.out.print("\t    "); else System.out.print("\t----");
            }
        }
        System.out.println();
    }

    private static boolean secondModDependsFirst(Gene g1, Holder h2) {
        double cor = Pearson.calcCorrelation(g1.value, h2.mod.value, h2.pos);
        Holder h = new Holder("", g1, h2.module, h2.sign, h2.pos);
        double v = h.cor * cor;
        if (v * h2.cor < 0) return false;
        if (Math.abs(h2.cor) < Math.abs(v)) return true;
        double dif = Math.abs(h2.cor - v);
        double pv = Pearson.calcCorrSignificance(dif, ArrayUtils.countTrue(h2.pos));
        return pv > 0.05;
    }

    public static double[][] getCorrMatrix(Gene[] gene) {
        double[][] c = new double[gene.length][gene.length];
        for (int i = 0; i < c.length - 1; i++) {
            for (int j = i + 1; j < c.length; j++) {
                c[i][j] = Pearson.calcCorrelation(gene[i].value, gene[j].value);
                c[j][i] = c[i][j];
            }
        }
        return c;
    }

    public static void printSignDistr(int[] sign) {
        int pos = 0;
        int neg = 0;
        for (int i = 0; i < sign.length; i++) {
            if (sign[i] == 1) pos++; else neg++;
        }
        System.out.println("pos = " + pos + "\tneg = " + neg);
    }

    public static void writeCorrGraph(Gene[] gene) throws IOException {
        double[][] c = getCorrMatrix(gene);
        BufferedWriter writer = new BufferedWriter(new FileWriter("graph.graphml"));
        GraphML.writeHeader(writer);
        Set<Gene> created = new HashSet<Gene>();
        for (int i = 0; i < c.length - 1; i++) {
            for (int j = i + 1; j < c.length; j++) {
                if (Math.abs(c[i][j]) > 0.7) {
                    createNode(gene[i], writer, created);
                    createNode(gene[j], writer, created);
                    writer.write(GraphML.createEdgeData(gene[i].getSymbol(), gene[j].getSymbol(), GraphML.getColor(c[i][j]), true, false));
                }
            }
        }
        GraphML.writeFooter(writer);
        writer.close();
    }

    private static void createNode(Gene o, BufferedWriter writer, Set<Gene> created) throws IOException {
        if (!created.contains(o)) {
            writer.write(GraphML.createNodeData(o.getSymbol(), o.getSymbol(), Color.WHITE, 0, true));
            created.add(o);
        }
    }

    public static List<Gene[]> getCorrelatedModules(Gene[] genes, int[] sign, boolean[] pos, Gene probe) {
        double[][] correlation = getCorrelationMatrix(genes, sign, pos);
        double[] x = null;
        if (probe != null) {
            x = new double[correlation.length];
            for (int i = 0; i < x.length; i++) {
                x[i] = Pearson.calcCorrelation(probe.value, genes[i].value, pos);
            }
        }
        List<List<Integer>> moduleIndexes = null;
        if (x == null) {
            moduleIndexes = ClusterSpectra.analyzeInANewWay(correlation, x);
        } else {
            moduleIndexes = new ArrayList<List<Integer>>();
            moduleIndexes.add(ClusterSpectra.getModuleOfMostPositivelyCorrelating(correlation, x));
            moduleIndexes.add(ClusterSpectra.getModuleOfMostNegativelyCorrelating(correlation, x));
        }
        List<Gene[]> modules = new ArrayList<Gene[]>();
        for (List<Integer> moduleIndex : moduleIndexes) {
            Gene[] module = new Gene[moduleIndex.size()];
            int i = 0;
            for (Integer ind : moduleIndex) {
                module[i++] = genes[ind];
            }
            assert i == moduleIndex.size();
            modules.add(module);
        }
        return modules;
    }

    public static void printModuleScores(Gene[] genes, int[] sign, boolean[] pos, Gene[] modulator) {
        double[][] correlation = getCorrelationMatrix(genes, sign, pos);
        double[][] x = new double[modulator.length][correlation.length];
        for (int i = 0; i < modulator.length; i++) {
            for (int j = 0; j < correlation.length; j++) {
                x[i][j] = Pearson.calcCorrelation(modulator[i].value, genes[j].value, pos);
            }
        }
        List<Holder3> list = new ArrayList<Holder3>();
        for (int i = 0; i < modulator.length; i++) {
            List<Integer> posit = ClusterSpectra.getModuleOfMostPositivelyCorrelating(correlation, x[i]);
            List<Integer> negat = ClusterSpectra.getModuleOfMostNegativelyCorrelating(correlation, x[i]);
            double positCor = getAvgCor(x[i], posit);
            double negatCor = getAvgCor(x[i], negat);
            list.add(new Holder3(modulator[i], posit, negat, positCor, negatCor));
            if (modulator[i].getSymbol().equals("VDR") && ArrayUtils.countTrue(pos) == 83) {
                boolean[][] w = ArrayUtils.getSlidingWindow(modulator[i].value, pos, ArrayUtils.countTrue(pos) / 2);
                Gene[] module = cropGenes(genes, negat);
                for (int j = 0; j < w.length; j++) {
                    System.out.println(j + "\t" + getAverageCorrelation(module, sign, w[j]));
                }
            }
        }
        Collections.sort(list);
        for (Holder3 h : list) {
            if (h.score > 4) System.out.println(h);
        }
    }

    static Gene[] cropGenes(Gene[] genes, List<Integer> module) {
        Gene[] crop = new Gene[module.size()];
        int j = 0;
        for (Integer i : module) {
            crop[j++] = genes[i];
        }
        return crop;
    }

    static class Holder3 implements Comparable {

        Gene mod;

        List<Integer> posit;

        List<Integer> negat;

        double positCor;

        double negatCor;

        Double score;

        Holder3(Gene mod, List<Integer> posit, List<Integer> negat, double positCor, double negatCor) {
            this.mod = mod;
            this.posit = posit;
            this.negat = negat;
            this.positCor = positCor;
            this.negatCor = negatCor;
            score = Math.max(posit.size() * Math.abs(positCor), negat.size() * Math.abs(negatCor));
        }

        public int compareTo(Object o) {
            return ((Holder3) o).score.compareTo(score);
        }

        @Override
        public String toString() {
            return mod.getSymbol() + "\t" + posit.size() + "\t" + positCor + "\t" + negat.size() + "\t" + negatCor;
        }
    }

    private static double getAvgCor(double[] cor, List<Integer> module) {
        double mean = 0;
        for (Integer i : module) {
            mean += cor[i];
        }
        mean /= module.size();
        return mean;
    }

    private static double[][] getCorrelationMatrix(Gene[] genes, int[] sign, boolean[] pos) {
        double[][] correlation = new double[genes.length][genes.length];
        int expSize = pos == null ? genes[0].value.length : ArrayUtils.countTrue(pos);
        for (int i = 0; i < genes.length - 1; i++) {
            for (int j = i + 1; j < genes.length; j++) {
                double[][] val = new double[2][];
                val[0] = genes[i].value;
                val[1] = genes[j].value;
                double cor = pos == null ? Pearson.calcCorrelation(val) : Pearson.calcCorrelation(val, pos);
                double pv = Pearson.calcCorrSignificance(cor, expSize);
                if (pv < CORR_PV_THR) {
                    correlation[i][j] = cor * sign[i] * sign[j];
                    correlation[j][i] = correlation[i][j];
                }
            }
        }
        return correlation;
    }

    private static void printModuleOVerlapMatrix(List<List<Integer>> modules) {
        System.out.println("Module overlaps");
        for (List module : modules) {
            System.out.print("\t" + module.size());
        }
        for (List module : modules) {
            System.out.print("\n" + module.size());
            for (List mod : modules) {
                System.out.print("\t" + (mod == module ? "" : SetUtils.countCommon(mod, module)));
            }
        }
        System.out.println();
    }

    private static void printModuleOVerlapMatrix(List<List<Gene>> modules, int tarNum) {
        Map<Gene, Integer> cnt = new HashMap<Gene, Integer>();
        TermCounter tc = new TermCounter();
        System.out.println("Module overlaps");
        for (List<Gene> module : modules) {
            System.out.print("\t" + module.size());
            for (Gene g : module) {
                if (!cnt.containsKey(g)) cnt.put(g, 1); else cnt.put(g, cnt.get(g) + 1);
                tc.addTerm(g.getSymbol());
            }
        }
        for (List module : modules) {
            System.out.print("\n" + module.size());
            int com = 0;
            int msize = 0;
            for (List mod : modules) {
                com = SetUtils.countCommon(mod, module);
                msize = mod.size();
                System.out.print("\t" + (mod == module ? "" : com));
            }
            System.out.print("\t" + (com / ((module.size() * msize) / (double) tarNum)));
        }
        System.out.println();
    }

    private static List<Integer>[] getFrequencyDistribution(Map<Integer, Integer> count, Gene[] gene, double[][] correlation) {
        int maxFreq = 0;
        for (Integer id : count.keySet()) {
            Integer cnt = count.get(id);
            if (maxFreq < cnt) maxFreq = cnt;
        }
        int totalBins = gene.length / AVG_BINS_PER_GENE;
        List<Integer>[] bin = new List[totalBins];
        for (int i = 0; i < bin.length; i++) bin[i] = new ArrayList<Integer>();
        for (Integer id : count.keySet()) {
            Integer cnt = count.get(id);
            int i = (int) Math.floor((cnt / (double) maxFreq) * totalBins);
            if (i == totalBins) i--;
            bin[i].add(id);
        }
        for (int i = 0; i < bin.length; i++) {
            System.out.println(i + "\t" + bin[i].size());
        }
        return bin;
    }

    private static void printCorrelationDistribution(double[][] correlation, Set<Integer> ids) {
        Histogram h = new Histogram(0.1);
        for (Integer id1 : ids) {
            for (Integer id2 : ids) {
                if (id1 >= id2) continue;
                h.count(correlation[id1][id2]);
            }
        }
        System.out.println("Distribution of correlations inside top scored module");
        h.print();
    }

    private static void printAvgCorrelationDistribution(double[][] correlation, Set<Integer> ids) {
        Histogram h = new Histogram(0.05);
        Map<Integer, Double> avg = new HashMap<Integer, Double>();
        for (Integer id1 : ids) {
            avg.put(id1, 0.);
            for (Integer id2 : ids) {
                if (id1.equals(id2)) continue;
                avg.put(id1, avg.get(id1) + correlation[id1][id2]);
            }
            avg.put(id1, avg.get(id1) / (ids.size() - 1));
            h.count(avg.get(id1));
        }
        System.out.println("Distribution of average correlations inside module");
        h.print();
    }

    private static double getAverageExpression(Gene[] gene) {
        double exp = 0;
        for (Gene g : gene) {
            for (double v : g.value) {
                exp += Math.exp(v);
            }
        }
        exp /= gene.length * gene[0].value.length;
        return exp;
    }

    private static double getAverageCorrelation(Gene[] gene, int[] sign, boolean[] pos) {
        double cor = 0;
        for (int i = 0; i < gene.length - 1; i++) {
            for (int j = i + 1; j < gene.length; j++) {
                cor += Pearson.calcCorrelation(gene[i].value, gene[j].value, pos) * sign[i] * sign[j];
            }
        }
        cor /= (gene.length * (gene.length - 1)) / 2;
        return cor;
    }

    private static void printInterAndIntraCorrAverages(double[][] correlation, Set<Integer> ids1, Set<Integer> ids2) {
        double inter1 = 0;
        double inter2 = 0;
        double intra = 0;
        for (Integer i : ids1) for (Integer j : ids1) if (i < j) inter1 += correlation[i][j];
        for (Integer i : ids2) for (Integer j : ids2) if (i < j) inter2 += correlation[i][j];
        for (Integer i : ids1) for (Integer j : ids2) intra += correlation[i][j];
        inter1 /= ids1.size() * ids1.size() / 2;
        inter2 /= ids2.size() * ids2.size() / 2;
        intra /= ids1.size() * ids2.size();
        System.out.println("inter1 = " + inter1);
        System.out.println("inter2 = " + inter2);
        System.out.println("intra = " + intra);
    }

    private static int[] getSigns(Gene[] genes) {
        int[] signs = new int[genes.length];
        TabDelimitedFileParser p = new TabDelimitedFileParser("resource/factors/AR_andr_small.txt");
        Map<String, String> score = p.getOneToOneMap("Target", "Score");
        for (int i = 0; i < genes.length; i++) {
            signs[i] = score.get(genes[i].getSymbol()).startsWith("-") ? -1 : 1;
        }
        return signs;
    }

    private static List<Integer> getCluster(double[][] correlation, int ind1, int ind2, double scoreAdditionThr) {
        int size = correlation.length;
        List<Integer> list = new ArrayList<Integer>();
        list.add(ind1);
        list.add(ind2);
        while (true) {
            double maxIncrease = 0;
            int indexOfMax = -1;
            for (int i = 0; i < size; i++) {
                if (list.contains(i)) continue;
                double addition = 0;
                for (Integer ind : list) {
                    addition += correlation[i][ind];
                }
                if (addition > maxIncrease) {
                    maxIncrease = addition;
                    indexOfMax = i;
                }
            }
            if (indexOfMax == -1) break;
            if (maxIncrease / list.size() > scoreAdditionThr) {
                list.add(indexOfMax);
            } else break;
        }
        return list;
    }

    public static double getAverageAbsoluteCor(double[][] c) {
        double sum = 0;
        int cnt = 0;
        for (int i = 0; i < c.length - 1; i++) {
            for (int j = i + 1; j < c.length; j++) {
                double x = Math.abs(c[i][j]);
                sum += x;
                cnt++;
            }
        }
        return sum / cnt;
    }

    private static List<List<Integer>> getModules(double[][] correlation, List<Integer>[] d, double minCorAdd) {
        List<List<Integer>> modules = new ArrayList<List<Integer>>();
        boolean[] p = getPeaks(d);
        for (int i = 0; i < p.length; i++) {
            if (i < p.length / 4D) continue;
            if (p[i]) {
                List<Integer> module = getModuleSeed(d[i], correlation, minCorAdd);
                if (module.size() < 3) continue;
                int j = 1;
                while (i - j >= 0 || i + j < p.length) {
                    if (i + j < p.length) enlargeModule(correlation, module, d[i + j], minCorAdd);
                    if (i - j >= 0) enlargeModule(correlation, module, d[i - j], minCorAdd);
                    j++;
                }
                if (module.size() >= MODULE_SIZE_THR) modules.add(module);
            }
        }
        return modules;
    }

    private static List<Integer> getModuleSeed(List<Integer> ids, double[][] correlation, double minCorAdd) {
        ids = new ArrayList<Integer>(ids);
        while (true) {
            double minScore = Double.MAX_VALUE;
            Integer minID = -1;
            for (Integer i : ids) {
                double score = 0;
                for (Integer j : ids) {
                    if (i.equals(j)) continue;
                    score += correlation[i][j];
                }
                score /= ids.size() - 1;
                if (score < minScore) {
                    minScore = score;
                    minID = i;
                }
            }
            if (minScore < minCorAdd) {
                ids.remove(minID);
            } else break;
        }
        return ids;
    }

    private static void enlargeModule(double[][] correlation, List<Integer> module, List<Integer> cand, double minCorAdd) {
        cand = new ArrayList<Integer>(cand);
        while (!cand.isEmpty()) {
            double maxScore = -Double.MAX_VALUE;
            Integer maxInd = -1;
            for (Integer i : cand) {
                double score = 0;
                for (Integer j : module) {
                    assert !i.equals(j);
                    score += correlation[i][j];
                }
                score /= module.size();
                if (score > maxScore) {
                    maxScore = score;
                    maxInd = i;
                }
            }
            if (maxScore > minCorAdd) {
                module.add(maxInd);
                cand.remove(maxInd);
            } else break;
        }
    }

    private static boolean[] getPeaks(List<Integer>[] distr) {
        boolean[] p = new boolean[distr.length];
        int[] s = new int[distr.length];
        int[] r = new int[distr.length];
        int[] l = new int[distr.length];
        for (int i = 0; i < p.length; i++) s[i] = distr[i].size();
        for (int i = 0; i < p.length; i++) {
            if (i < p.length - 1) r[i] = s[i] < s[i + 1] ? -1 : s[i] > s[i + 1] ? 1 : 0; else r[i] = s[i] > 0 ? 1 : 0;
            if (i > 0) l[i] = s[i] < s[i - 1] ? -1 : s[i] > s[i - 1] ? 1 : 0; else l[i] = s[i] > 0 ? 1 : 0;
        }
        boolean goingStraight = false;
        for (int i = 0; i < p.length; i++) {
            if (r[i] == 1 && l[i] == 1) {
                p[i] = true;
                goingStraight = false;
            } else if (r[i] == 0 && l[i] == 1) {
                goingStraight = true;
                p[i] = false;
            } else if (r[i] == 0 && l[i] == 0 && goingStraight) {
                p[i] = false;
            } else if (r[i] == 1 && l[i] == 0 && goingStraight) {
                p[i] = true;
                goingStraight = false;
            } else {
                p[i] = false;
                goingStraight = false;
            }
        }
        return p;
    }

    private static void mergeSimilarModules(List<List<Integer>> modules) {
        for (List<Integer> module1 : new ArrayList<List<Integer>>(modules)) {
            for (List<Integer> module2 : new ArrayList<List<Integer>>(modules)) {
                if (module1 == module2) continue;
                int com = SetUtils.countCommon(module1, module2);
                if (com / (double) module1.size() > .8 && com / (double) module2.size() > .8) {
                    for (Integer id : module1) {
                        if (!module2.contains(id)) module2.add(id);
                    }
                    modules.remove(module1);
                }
            }
        }
    }

    public static void printResistanceGeneEnrichment(Gene[] tar, Gene[] mod, int[] sign) {
        TabDelimitedFileParser p = new TabDelimitedFileParser("resource/factors/AR-resistance.txt");
        Set<String> resSet = p.getColumnSet(0);
        Map<Gene, Set<String>> corMap = new HashMap<Gene, Set<String>>();
        for (int i = 0; i < mod.length; i++) {
            for (int j = 0; j < tar.length; j++) {
                double cor = Pearson.calcCorrelation(mod[i].value, tar[j].value);
                double pv = Pearson.calcCorrSignificance(cor, mod[i].value.length);
                if (pv > CORR_PV_THR) {
                    if (!corMap.containsKey(mod[i])) corMap.put(mod[i], new HashSet<String>());
                    corMap.get(mod[i]).add(tar[j].getSymbol());
                }
            }
        }
        Histogram h = new Histogram(0.1);
        for (int i = 0; i < mod.length; i++) {
            int com = SetUtils.countCommon(resSet, corMap.get(mod[i]));
            double ratio = com / (double) corMap.get(mod[i]).size();
            System.out.println(mod[i].getSymbol() + "\t" + com + "\t" + corMap.get(mod[i]).size() + "\t" + ratio);
            h.count(ratio);
        }
        h.print();
    }

    public static void takeLogIfNeeded(Gene[] gene) {
        boolean needed = false;
        for (double v : gene[0].value) {
            if (v > 20) {
                needed = true;
                break;
            }
        }
        if (needed) {
            for (int i = 0; i < gene.length; i++) {
                for (int j = 0; j < gene[i].value.length; j++) {
                    gene[i].value[j] = Math.log(gene[i].value[j]);
                }
            }
            System.out.println("Took log of expressions");
        }
    }

    public static List<Gene> getGRResponseModule(Gene[] tars) {
        TabDelimitedFileParser p = new TabDelimitedFileParser("resource/resistance_resp1.txt");
        Set<String> resp = p.getColumnSet(0);
        List<Gene> list = new ArrayList<Gene>();
        for (Gene tar : tars) {
            if (resp.contains(tar.getSymbol())) list.add(tar);
        }
        return list;
    }

    public static final Set<String> GR_TARG = new HashSet<String>(Arrays.asList(("SLC45A3\n" + "FKBP5\n" + "CBLL1\n" + "TMEM140\n" + "CXCR7\n" + "INSIG1\n" + "SLC35F2\n" + "STK39\n" + "PPAP2A\n" + "ACSL3\n" + "TSKU\n" + "CENPN\n" + "SGK1\n" + "KLK2\n" + "GPER\n" + "PAK1IP1\n" + "UBE2J1\n" + "PPFIBP2\n" + "ATAD2\n" + "PRKCH\n" + "FZD5\n" + "DBI\n" + "HOMER2\n" + "TMEM38B\n" + "MPHOSPH9\n" + "GRB10\n" + "HOXC13\n" + "TNFAIP8\n" + "KCNN2\n" + "LRRN1\n" + "ST7").split("\n")));
}
