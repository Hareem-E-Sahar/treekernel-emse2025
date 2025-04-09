package gem;

import gem.parser.HPRDParser;
import gem.parser.TabDelimitedFileParser;
import gem.util.*;
import sun.awt.SunHints;
import java.util.*;
import static gem.StageAnalyzer.getPos;

/**
 * @author Ozgun Babur
 */
public class ClusterAnalyzer2 implements Constants {

    public static final double THR = 0.05;

    public static void main(String[] args) throws Throwable {
        Set<String> tarSym = getTarSymSet();
        Set<String> modSym = getModSymSet();
        System.out.println("modSym.size() = " + modSym.size());
        modSym.removeAll(tarSym);
        System.out.println("modSym.size() = " + modSym.size());
        Set<String> ids = new HashSet<String>();
        for (String g : tarSym) ids.add(Triplet.getSymbolToGeneMap().get(g));
        for (String g : modSym) ids.add(Triplet.getSymbolToGeneMap().get(g));
        String dir = "resource/expdata/expo";
        Map<String, Gene> map = ExpDataReader.readGenes(ids, dir, 0, 0);
        boolean[][] pos = getPos(dir + "/");
        String[] tisName = StageAnalyzer.getStageNames(dir + "/");
        Set<Gene> tarSet = new HashSet<Gene>(map.values());
        Set<Gene> modSet = new HashSet<Gene>();
        for (String smb : modSym) {
            Gene mod = map.get(Triplet.getSymbolToGeneMap().get(smb));
            if (mod != null) modSet.add(mod);
        }
        tarSet.removeAll(modSet);
        System.out.println("tar size = " + tarSet.size());
        Gene[] tars = tarSet.toArray(new Gene[tarSet.size()]);
        Gene[] mods = modSet.toArray(new Gene[modSet.size()]);
        Arrays.sort(mods);
        Arrays.sort(tars);
        takeLogIfNeeded(tars);
        takeLogIfNeeded(mods);
        int i = 5;
        {
            System.out.print("\n\nTissue: " + tisName[i]);
            System.out.println(",  sample size = " + ArrayUtils.countTrue(pos[i]) + "\n");
            identifyTargetGroups(tars, mods, pos[i]);
        }
    }

    static void identifyTargetGroups(Gene[] tars, Gene[] mods, boolean[] pos) {
        int n = ArrayUtils.countTrue(pos);
        double[][] cm = getCorrelationMatrix(mods, pos);
        double[][] cmt = getCorrelationMatrix(mods, tars, pos);
        double[][] pmt = getCorrelationPvalMatrix(cmt, n);
        List<Integer>[][][] groups = new List[mods.length][3][];
        List<Integer> m1 = null;
        List<Integer> m2 = null;
        for (int i = 0; i < mods.length; i++) {
            groups[i][0] = new List[2];
            groups[i][0][0] = getAffectedTargets(cmt, pmt, cm, i, n, 1);
            groups[i][0][1] = getAffectedTargets(cmt, pmt, cm, i, n, -1);
            List<Integer>[][] halves = identifyTargetsTakeHalves(tars, mods, i, pos);
            groups[i][1] = halves[1];
            groups[i][2] = halves[0];
            if (mods[i].getSymbol().equals("VDR") && (n == 83 || n == 131)) {
            }
            if (mods[i].getSymbol().equals("ESR1") && n == 353) {
                m1 = groups[i][0][0];
            }
            if (mods[i].getSymbol().equals("AR") && n == 353) {
                m2 = groups[i][0][0];
            }
        }
        if (m1 != null && m2 != null) SetUtils.printVenn(new HashSet(m1), new HashSet(m2));
        int[] ord = getOrdering(groups);
        List<Gene> topMods = new ArrayList<Gene>();
        System.out.println("NucRec\tp\tn\tp-\tn-\tp+\tn+");
        for (int i : ord) {
            if (topMods.size() < 10) topMods.add(mods[i]);
            String s = mods[i].getSymbol();
            if (s.length() < 4) s += "  ";
            System.out.println(s + "\t" + groups[i][0][0].size() + "\t" + groups[i][0][1].size() + "\t" + groups[i][1][0].size() + "\t" + groups[i][1][1].size() + "\t" + groups[i][2][0].size() + "\t" + groups[i][2][1].size());
        }
    }

    static void plotPoints(Gene[] tars, Gene mod, List<Integer> module, boolean[] pos) {
        System.out.println("module size = " + module.size());
        double[][] vals = new double[tars.length][];
        for (int i = 0; i < tars.length; i++) {
            vals[i] = ArrayUtils.getPortion(tars[i].value, pos);
        }
        double[] mval = ArrayUtils.getPortion(mod.value, pos);
        Random r = new Random();
        for (int k = 0; k < module.size(); k++) {
            int i = module.get(k);
            if (i == -1) return;
            XYPlotter p = new XYPlotter(mval, vals[i], null, mod.getSymbol(), tars[i].getSymbol());
            p.setVisible(true);
            System.out.println("cor overall = " + Pearson.calcCorrelation(mval, vals[i]));
            boolean[] lower = getHalf(mod.value, pos, false);
            boolean[] upper = getHalf(mod.value, pos, true);
            System.out.println("cor low = " + Pearson.calcCorrelation(mod.value, tars[i].value, lower));
            System.out.println("cor high = " + Pearson.calcCorrelation(mod.value, tars[i].value, upper));
            while (p.isVisible()) Waiter.pause(1000);
        }
    }

    static int findGene(Gene[] genes, String symb) {
        for (int i = 0; i < genes.length; i++) {
            if (genes[i].getSymbol().equals(symb)) return i;
        }
        return -1;
    }

    static List<Integer>[][] identifyTargetsTakeHalves(Gene[] tars, Gene[] mods, int modInd, boolean[] pos) {
        boolean[] lower = getHalf(mods[modInd].value, pos, false);
        boolean[] upper = getHalf(mods[modInd].value, pos, true);
        List<Integer>[][] r = new List[2][];
        r[0] = identifyTargets(tars, mods, modInd, upper);
        r[1] = identifyTargets(tars, mods, modInd, lower);
        return r;
    }

    static List<Integer>[] identifyTargets(Gene[] tars, Gene[] mods, int modIndex, boolean[] pos) {
        int n = ArrayUtils.countTrue(pos);
        double[][] cm = getCorrelationMatrix(mods, pos);
        double[][] cmt = getCorrelationMatrix(mods, tars, pos);
        double[][] pmt = getCorrelationPvalMatrix(cmt, n);
        List<Integer> groupPos = getAffectedTargets(cmt, pmt, cm, modIndex, n, 1);
        List<Integer> groupNeg = getAffectedTargets(cmt, pmt, cm, modIndex, n, -1);
        return new List[] { groupPos, groupNeg };
    }

    static boolean[] getHalf(double[] v, boolean[] pos, boolean high) {
        boolean[] half = new boolean[pos.length];
        double median = Summary.median(v, pos);
        for (int i = 0; i < pos.length; i++) {
            if (pos[i] && ((high && v[i] >= median) || (!high && v[i] <= median))) half[i] = true;
        }
        return half;
    }

    static int[] getOrdering(List<Integer>[][][] g) {
        class Holder implements Comparable {

            int index;

            int[] v;

            Integer score;

            Holder(int index, int... v) {
                this.index = index;
                this.v = v;
                score = Summary.max(v);
            }

            public int compareTo(Object o) {
                return ((Holder) o).score.compareTo(score);
            }
        }
        Holder[] h = new Holder[g.length];
        for (int i = 0; i < h.length; i++) {
            h[i] = new Holder(i, g[i][0][0].size(), g[i][0][1].size(), g[i][1][0].size(), g[i][1][1].size(), g[i][2][0].size(), g[i][2][1].size());
        }
        Arrays.sort(h);
        int[] ord = new int[h.length];
        for (int i = 0; i < h.length; i++) {
            ord[i] = h[i].index;
        }
        return ord;
    }

    static List<Integer> getAffectedTargets(double[][] cmt, double[][] pmt, double[][] cm, int modIndex, int n, int sign) {
        List<Integer> tars = new ArrayList<Integer>();
        for (int i = 0; i < cmt[modIndex].length; i++) {
            if (pmt[modIndex][i] > THR) continue;
            if (cmt[modIndex][i] * sign < 0) continue;
            boolean free = true;
            if (free) tars.add(i);
        }
        return tars;
    }

    static boolean freeFromOtherMod(double mt, double ot, double mo, int n) {
        if (!(Pearson.calcCorrSignificance(mt, n) <= THR)) {
            System.out.println();
        }
        assert Pearson.calcCorrSignificance(mt, n) <= THR;
        double exp = ot * mo;
        if (exp * mt < 0) return true;
        double dif = mt - exp;
        if (dif * mt < 0) return false;
        double pv = Pearson.calcCorrSignificance(dif, n);
        if (pv <= THR) return true;
        double odif = ot - (mo * mt);
        if (odif * ot < 0) return true;
        double opv = Pearson.calcCorrSignificance(odif, n);
        if (opv <= THR) return false;
        if (Math.abs(mt) > Math.abs(ot)) return true;
        return false;
    }

    static double[][] getCorrelationMatrix(Gene[] genes, boolean[] pos) {
        double[][] c = new double[genes.length][genes.length];
        for (int i = 0; i < c.length - 1; i++) {
            for (int j = i + 1; j < c.length; j++) {
                c[i][j] = Pearson.calcCorrelation(genes[i].value, genes[j].value, pos);
                c[j][i] = c[i][j];
            }
        }
        return c;
    }

    static double[][] getCorrelationMatrix(Gene[] mods, Gene[] tars, boolean[] pos) {
        double[][] c = new double[mods.length][tars.length];
        for (int i = 0; i < mods.length; i++) {
            for (int j = 0; j < tars.length; j++) {
                c[i][j] = Pearson.calcCorrelation(mods[i].value, tars[j].value, pos);
            }
        }
        return c;
    }

    static double[][] getCorrelationPvalMatrix(double[][] c, int n) {
        double[][] p = new double[c.length][c[0].length];
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c[i].length; j++) {
                p[i][j] = Pearson.calcCorrSignificance(c[i][j], n);
            }
        }
        return p;
    }

    public static Set<String> getTarSymSet() throws Throwable {
        TabDelimitedFileParser p = new TabDelimitedFileParser("resource/factors/AR-select-small.txt");
        Set<String> tarSym = p.getColumnSet(0);
        p = new TabDelimitedFileParser("resource/factors/AR_andr_small.txt");
        Map<String, String> score = p.getOneToOneMap("Target", "Score");
        for (String s : new HashSet<String>(tarSym)) {
            assert score.containsKey(s);
            if (score.get(s).startsWith("-")) {
                tarSym.remove(s);
            }
        }
        return tarSym;
    }

    public static Set<String> getModSymSet() throws Throwable {
        Set<String> set = TabDelimitedFileParser.getColumnSet("resource/NuclearReceptors.txt", 0);
        return set;
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

    static void printCorrelationMatrix(Gene[] gene, boolean[] pos) {
        for (Gene g : gene) {
            System.out.print("\t" + g.getSymbol());
        }
        for (Gene g1 : gene) {
            System.out.print("\n" + g1.getSymbol());
            for (Gene g2 : gene) {
                System.out.print("\t");
                if (g1 != g2) {
                    System.out.print(fmt.format(Pearson.calcCorrelation(g1.value, g2.value, pos)));
                }
            }
        }
        System.out.println();
    }

    static void printModEffectOnTargetRegressionError(Gene[] tars, List<Integer> module, Gene mod, boolean[] pos) {
        double[] mval = ArrayUtils.getPortion(mod.value, pos);
        double[][] tval = new double[module.size()][];
        int j = 0;
        for (int i = 0; i < tars.length; i++) {
            if (module.contains(i)) tval[j++] = ArrayUtils.getPortion(tars[i].value, pos);
        }
        printModEffectOnTargetRegressionError(tval, mval);
    }

    public static void printModEffectOnTargetRegressionError(double[][] tval, double[] mval) {
        int[] rank = ArrayUtils.getRankOrderedIndexes(mval);
        double[][][] c = new double[tval.length][tval.length][];
        for (int i = 0; i < c.length - 1; i++) {
            for (int j = i + 1; j < c.length; j++) {
                c[i][j] = LinearRegression.regress(tval[i], tval[j]);
                c[j][i] = c[i][j];
            }
        }
        int index = 0;
        for (int k : rank) {
            double error = 0;
            for (int i = 0; i < c.length - 1; i++) {
                for (int j = i + 1; j < c.length; j++) {
                    error += LinearRegression.getDistanceOfPoint(1, 0, tval[i][k], tval[j][k]);
                }
            }
            System.out.println((++index) + "\t" + error);
        }
    }
}
