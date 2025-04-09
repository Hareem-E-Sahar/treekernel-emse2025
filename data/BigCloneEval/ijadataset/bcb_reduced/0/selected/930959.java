package gem;

import gem.parser.TabDelimitedFileParser;
import gem.util.*;
import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class StageAnalyzer {

    static String[] STAGE;

    public static void main(String[] args) throws Throwable {
        String dir = "resource/expdata/GSE9633/";
        StageAnalyzer an = new StageAnalyzer();
        boolean[][] pos = getPos(dir);
        List<Triplet> trips = readTrips(dir);
        CellTypeMatcher.replaceValsToRanks(trips, an.getUnion(pos));
        printTarChange(trips, pos);
        if (true) return;
        List<Mod> mods = an.createGroups(trips);
        Map<String, Integer> controllerCount = an.getControllerCount(trips);
        for (Mod mod : mods) {
            mod.selectMostVariedMod(pos);
            mod.assignAct(pos, controllerCount);
        }
        System.out.println("mods.size() = " + mods.size());
        System.out.println("mods.size() = " + mods.size());
        for (Mod mod : mods) {
            {
                mod.printConfirmationStats(pos);
            }
        }
    }

    public static void removeBadTars(List<Triplet> trips) throws IOException {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        BufferedReader reader = new BufferedReader(new FileReader("resource/expop/followers_andr_m.txt"));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] tokens = line.split("\t");
            Set<String> set = new HashSet<String>();
            map.put(tokens[0], set);
            set.addAll(Arrays.asList(tokens).subList(1, tokens.length));
        }
        reader.close();
        Iterator<Triplet> iter = trips.iterator();
        while (iter.hasNext()) {
            Triplet t = iter.next();
            if (!map.containsKey(t.getMSym())) iter.remove(); else {
                if (!map.get(t.getMSym()).contains(t.target)) {
                    iter.remove();
                }
            }
        }
    }

    public static void removeBadFOXA1Tars(List<Triplet> trips) {
        TabDelimitedFileParser parser = new TabDelimitedFileParser("resource/expdata/Ling/FOXA1-tars.txt");
        Set<String> tars = parser.getColumnSet(0);
        Iterator<Triplet> iter = trips.iterator();
        while (iter.hasNext()) {
            Triplet t = iter.next();
            if (t.getMSym().equals("FOXA1") && !tars.contains(t.getTSym())) {
                iter.remove();
            }
        }
    }

    public static List<Triplet> readTrips(String dir) throws Throwable {
        List<Triplet> trips = Triplet.readTrips("result/Result_fdr0.05_var10.0_AR_expo_andr.xls");
        Triplet.removeNonModulation(trips);
        if ((new File(dir + "platform.txt")).exists()) {
            CrossPlatformMapper.associateAndClean(trips, dir + "data.txt", dir + "platform.txt");
        } else {
            CrossPlatformMapper.associateAndClean(trips, dir + "data.txt");
        }
        return trips;
    }

    public static void removeARisoform(List<Triplet> trips) {
        Iterator<Triplet> iter = trips.iterator();
        while (iter.hasNext()) {
            Triplet t = iter.next();
            if (t.fac_id.startsWith("M")) iter.remove();
        }
    }

    protected static void removeConflicts(List<Triplet> trips) {
        Map<String, Triplet> map = new HashMap<String, Triplet>();
        for (Triplet t : trips) {
            double gamma = Difference.calcGamma(t);
            String key = t.modulator + t.factor + t.target + (gamma > 0 ? "+" : "-");
            map.put(key, t);
        }
        for (String key : new HashSet<String>(map.keySet())) {
            key = key.substring(0, key.length() - 1);
            if (map.containsKey(key + "+") && map.containsKey(key + "-")) {
                map.remove(key + "+");
                map.remove(key + "-");
            }
        }
        trips.clear();
        trips.addAll(map.values());
    }

    protected void keepMostChanged(List<Triplet> trips, boolean[][] pos) {
        boolean[] p = getUnion(pos);
        Map<String, Double> max = new HashMap<String, Double>();
        Map<String, Triplet> select = new HashMap<String, Triplet>();
        for (Triplet t : trips) {
            String key = t.modulator + t.factor + t.target;
            double var = t.T.calcVariance(p);
            if (!max.containsKey(key) || var > max.get(key)) {
                max.put(key, var);
                select.put(key, t);
            }
        }
        trips.clear();
        trips.addAll(select.values());
    }

    public static boolean[][] getPos(String dir) throws IOException {
        String namesfile = dir + "expnames.txt";
        String stagesfile = dir + "stages.txt";
        List<String> expnames = CellTypeMatcher.readSampleNames(namesfile);
        BufferedReader reader = new BufferedReader(new FileReader(stagesfile));
        String line = reader.readLine();
        reader.close();
        String[] stage = line.split("\t");
        for (int i = 0; i < stage.length; i++) {
            stage[i] = removeQuote(stage[i]);
        }
        STAGE = stage;
        boolean[][] pos = new boolean[stage.length][];
        int i = 0;
        for (String st : stage) {
            pos[i++] = CellTypeMatcher.getPositionsOfNames(expnames, stagesfile, st);
        }
        return pos;
    }

    public static String[] getStageNames(String dir) throws IOException {
        return FileUtil.getColumnsArray(dir + "stages.txt");
    }

    protected static String removeQuote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) return s.substring(1, s.length() - 1);
        return s;
    }

    protected Map<String, Integer> getControllerCount(List<Triplet> trips) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Triplet t : trips) {
            if (!map.containsKey(t.target)) map.put(t.target, 0);
            map.put(t.target, map.get(t.target) + 1);
        }
        return map;
    }

    protected boolean[] getUnion(boolean[][] pos) {
        boolean[] u = new boolean[pos[0].length];
        for (int i = 0; i < u.length; i++) {
            for (boolean[] posj : pos) {
                if (posj[i]) {
                    u[i] = true;
                    break;
                }
            }
        }
        return u;
    }

    protected List<Mod> createGroups(List<Triplet> trips) {
        Map<String, Mod> map = new HashMap<String, Mod>();
        for (Triplet t : trips) {
            if (!map.containsKey(t.modulator)) map.put(t.modulator, new Mod());
            map.get(t.modulator).addTrip(t);
        }
        return new ArrayList<Mod>(map.values());
    }

    protected void filterMods(List<Mod> mods) {
        Iterator<Mod> iter = mods.iterator();
        while (iter.hasNext()) {
            Mod mod = iter.next();
            double score = mod.score();
            if (mod.trips.isEmpty() || Double.isNaN(score) || score > 0.05) {
                iter.remove();
            }
        }
    }

    public static boolean[] getUnion(boolean[][] pos, int... i) {
        boolean[] p = new boolean[pos[0].length];
        for (int j = 0; j < p.length; j++) {
            for (int k : i) {
                if (pos[k][j]) {
                    p[j] = true;
                    break;
                }
            }
        }
        return p;
    }

    public static boolean intersects(boolean[] pos1, boolean[] pos2) {
        assert pos1.length == pos2.length;
        for (int i = 0; i < pos1.length; i++) {
            if (pos1[i] && pos2[i]) return true;
        }
        return false;
    }

    private static void printTarChange(List<Triplet> trips, boolean[][] pos) {
        boolean[][] p = new boolean[2][];
        p[0] = pos[0];
        p[1] = pos[1];
        Set<Gene> tars = new HashSet<Gene>();
        TermCounter tc = new TermCounter();
        int norm_prim_cnt = 0;
        for (Triplet t : trips) {
            Gene gene = t.M;
            if (tars.contains(gene)) continue; else tars.add(gene);
            double[] v = new double[p.length];
            for (int i = 0; i < p.length; i++) {
                v[i] = CellTypeMatcher.getMeanValue(gene, p[i]);
            }
            String s = Triplet.getGeneToSymbolMap().get(gene.geneid);
            for (int i = 0; i < v.length - 1; i++) {
                for (int j = i + 1; j < v.length; j++) {
                    s += "\t";
                    if (CellTypeMatcher.getChangePvalBetweenTissues(gene, p[i], p[j]) < 0.05) {
                        int ch = v[i] > v[j] ? -1 : 1;
                        s = s + ch;
                        if (i == 0 && j == 1) norm_prim_cnt++;
                    } else s += "0";
                }
            }
            System.out.println(s);
            tc.addTerm(s.substring(s.indexOf("\t")));
        }
        tc.print();
        System.out.println("norm_prim_cnt = " + norm_prim_cnt);
    }

    protected class Mod implements Comparable {

        Gene mod;

        List<Triplet> trips;

        double[] modExpr;

        double[] activity;

        double[][] actChPval;

        double[][] expChPval;

        Histogram[] h;

        public Mod() {
            this.trips = new ArrayList<Triplet>();
        }

        public void addTrip(Triplet t) {
            if (this.mod == null) mod = t.M; else {
                assert this.mod == t.M;
            }
            trips.add(t);
        }

        void assignAct(boolean[][] pos, Map<String, Integer> controllerCount) {
            h = new Histogram[pos.length];
            modExpr = new double[pos.length];
            activity = new double[pos.length];
            for (int i = 0; i < pos.length; i++) {
                modExpr[i] = CellTypeMatcher.getMeanValue(mod, pos[i]);
                double total = 0;
                double act = 0;
                for (Triplet t : trips) {
                    double mv = CellTypeMatcher.getMeanValue(t.T, pos[i]);
                    if (Double.isNaN(mv)) continue;
                    act += Math.signum(Difference.calcGamma(t)) * mv / controllerCount.get(t.target);
                    total += 1D / controllerCount.get(t.target);
                }
                activity[i] = total == 0 ? Double.NaN : act / total;
            }
            expChPval = new double[pos.length][pos.length];
            for (int i = 0; i < pos.length; i++) {
                for (int j = i; j < pos.length; j++) {
                    if (j == i) expChPval[i][j] = 1; else {
                        expChPval[i][j] = CellTypeMatcher.getChangePvalBetweenTissues(mod, pos[i], pos[j]);
                        expChPval[j][i] = expChPval[i][j];
                    }
                }
            }
            actChPval = new double[pos.length][pos.length];
            for (int i = 0; i < pos.length; i++) {
                for (int j = i; j < pos.length; j++) {
                    if (j == i) actChPval[i][j] = 1; else {
                        int total = 0;
                        int samesign = 0;
                        for (Triplet t : trips) {
                            if (CellTypeMatcher.getChangePvalBetweenTissues(t.T, pos[i], pos[j]) < 0.05) {
                                double v1 = CellTypeMatcher.getMeanValue(t.T, pos[i]);
                                double v2 = CellTypeMatcher.getMeanValue(t.T, pos[j]);
                                if (Math.signum(Difference.calcGamma(t)) == Math.signum(v2 - v1)) {
                                    samesign++;
                                }
                                total++;
                            }
                        }
                        if (total == 0) actChPval[i][j] = 1; else actChPval[i][j] = Binomial.getPval(samesign, total - samesign);
                        actChPval[j][i] = actChPval[i][j];
                    }
                }
            }
        }

        void normalize() {
            normalize(modExpr, 2, -1);
            normalize(activity, 2, -1);
        }

        double[] getNormalizedExpr(Gene gene, boolean[][] pos) {
            boolean significant = false;
            for (int i = 0; i < pos.length - 1; i++) {
                for (int j = i + 1; j < pos.length; j++) {
                    double pval = CellTypeMatcher.getChangePvalBetweenTissues(mod, pos[i], pos[j]);
                    if (pval < 0.05) {
                        significant = true;
                        break;
                    }
                }
                if (significant) break;
            }
            if (!significant) return null;
            double[] expr = new double[pos.length];
            for (int i = 0; i < pos.length; i++) {
                expr[i] = CellTypeMatcher.getMeanValue(gene, pos[i]);
            }
            normalize(expr, 2, -1);
            return expr;
        }

        void normalize(double[] v, double range, double initial) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (double i : v) {
                if (i > max) max = i;
                if (i < min) min = i;
            }
            double rangeRatio = range / (max - min);
            for (int i = 0; i < v.length; i++) {
                v[i] = initial + ((v[i] - min) * rangeRatio);
            }
        }

        void selectMostVariedMod(boolean[][] pos) {
            boolean[] p = getUnion(pos);
            double max = -1;
            for (Triplet t : trips) {
                double var = t.M.calcVariance(p);
                if (var > max) {
                    max = var;
                    this.mod = t.M;
                }
            }
        }

        @Override
        public String toString() {
            boolean drawExpr = true;
            String s = trips.size() + "\t" + expScore() + "\t" + actScore() + "\n\n";
            s += trips.iterator().next().M.getSymbol();
            if (drawExpr) s += "\tExpression";
            s += "\tActivity";
            for (int i = 0; i < modExpr.length; i++) {
                s += "\n" + STAGE[i];
                if (drawExpr) s += "\t" + modExpr[i];
                s += "\t" + activity[i];
            }
            return s;
        }

        Double score() {
            return actScore();
        }

        double actScore() {
            double min = 1;
            for (double[] pvals : actChPval) {
                for (double pval : pvals) {
                    if (pval < min) min = pval;
                }
            }
            return min;
        }

        double expScore() {
            double min = 1;
            for (double[] pvals : expChPval) {
                for (double pval : pvals) {
                    if (pval < min) min = pval;
                }
            }
            return min;
        }

        public int compareTo(Object o) {
            Mod m = (Mod) o;
            return score().compareTo(m.score());
        }

        public void printHistogramDensities() {
            for (Histogram his : h) {
                his.printDensity();
            }
        }

        public void printChangeList(boolean[] pos1, boolean[] pos2) {
            List<Triplet> good = new ArrayList<Triplet>();
            List<Triplet> bad = new ArrayList<Triplet>();
            String s = "";
            int tripCnt = 0;
            for (Triplet t : trips) {
                double gamma = Difference.calcGamma(t);
                double betaF = Difference.calcBetaF(t);
                double betaM = Difference.calcBetaM(t);
                double betaF_pval = Difference.calcBetaFpval(t);
                double betaM_pval = Difference.calcBetaMpval(t);
                double mch = CellTypeMatcher.getMeanChange(t.T, pos1, pos2);
                double pval = CellTypeMatcher.getChangePvalBetweenTissues(t.T, pos1, pos2);
                if (betaM_pval > 0.05) continue;
                if (betaF_pval > 0.05) continue;
                tripCnt++;
                if (pval > 0.05) continue;
                if (betaF * mch < 0) good.add(t); else bad.add(t);
            }
            System.out.println("tripCnt = " + tripCnt);
            System.out.println("\n" + mod.getSymbol());
            System.out.println("good = " + good.size());
            System.out.println("bad = " + bad.size());
            for (Triplet t : bad) {
                System.out.print("\t" + t.target);
            }
        }

        public void printConfirmationStats(boolean[][] pos) {
            double thr = 0.05;
            Set<Triplet> g1 = new HashSet<Triplet>();
            Set<Triplet> b1 = new HashSet<Triplet>();
            Set<Triplet> g2 = new HashSet<Triplet>();
            Set<Triplet> b2 = new HashSet<Triplet>();
            Set<Triplet> g3 = new HashSet<Triplet>();
            Set<Triplet> b3 = new HashSet<Triplet>();
            Set<Triplet> g4 = new HashSet<Triplet>();
            Set<Triplet> b4 = new HashSet<Triplet>();
            for (Triplet t : trips) {
                double gamma = Difference.calcGamma(t);
                double betaF = Difference.calcBetaF(t);
                double alphaF = Difference.calcAlphaF(t);
                double betaM = Difference.calcBetaM(t);
                double bf_pv = Difference.calcBetaFpval(t);
                double af_pv = Difference.calcAlphaFpval(t);
                double mch = CellTypeMatcher.getMeanChange(t.T, pos[1], pos[3]);
                double pval = CellTypeMatcher.getChangePvalBetweenTissues(t.T, pos[1], pos[3]);
                if (pval < thr && bf_pv < thr) {
                    if (betaF * mch < 0) g1.add(t); else b1.add(t);
                }
                mch = CellTypeMatcher.getMeanChange(t.T, pos[0], pos[2]);
                pval = CellTypeMatcher.getChangePvalBetweenTissues(t.T, pos[0], pos[2]);
                if (pval < thr && bf_pv < thr) {
                    if (betaF * mch < 0) g2.add(t); else b2.add(t);
                }
                mch = CellTypeMatcher.getMeanChange(t.T, pos[1], pos[5]);
                pval = CellTypeMatcher.getChangePvalBetweenTissues(t.T, pos[1], pos[5]);
                if (pval < thr) {
                    if (betaM * mch < 0) g3.add(t); else b3.add(t);
                }
                mch = CellTypeMatcher.getMeanChange(t.T, pos[0], pos[4]);
                pval = CellTypeMatcher.getChangePvalBetweenTissues(t.T, pos[0], pos[4]);
                if (pval < thr) {
                    if (betaM * mch < 0) g4.add(t); else b4.add(t);
                }
            }
            System.out.println("\n" + mod.getSymbol());
            System.out.println(g1.size() + "\t" + b1.size() + "\t" + Binomial.getPval(g1.size(), b1.size()));
            System.out.println(g2.size() + "\t" + b2.size() + "\t" + Binomial.getPval(g2.size(), b2.size()));
            System.out.println(g3.size() + "\t" + b3.size() + "\t" + Binomial.getPval(g3.size(), b3.size()));
            System.out.println(g4.size() + "\t" + b4.size() + "\t" + Binomial.getPval(g4.size(), b4.size()));
        }
    }
}
