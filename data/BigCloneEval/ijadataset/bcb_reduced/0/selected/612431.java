package gem;

import gem.util.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class ClusterSpectra {

    public static double[][] quatro(double[][] c, double minCor) {
        double[][] s = new double[c.length][c.length];
        for (int i = 0; i < c.length - 3; i++) {
            for (int j = i + 1; j < c.length - 2; j++) {
                if (c[i][j] < minCor) continue;
                for (int k = j + 1; k < c.length - 1; k++) {
                    if (c[i][k] < minCor) continue;
                    if (c[k][j] < minCor) continue;
                    for (int a = k + 1; a < c.length; a++) {
                        double[] v = new double[] { c[i][j], c[i][k], c[j][k], c[i][a], c[a][k], c[j][a] };
                        double min = Summary.min(v);
                        if (min >= minCor) {
                            s[i][j] += min;
                            s[j][i] += min;
                            s[j][k] += min;
                            s[k][j] += min;
                            s[i][k] += min;
                            s[k][i] += min;
                            s[i][a] += min;
                            s[a][i] += min;
                            s[j][a] += min;
                            s[a][j] += min;
                            s[a][k] += min;
                            s[k][a] += min;
                        }
                    }
                }
            }
        }
        return s;
    }

    public static double[][] triangle(double[][] c, double minCor) {
        double[][] s = new double[c.length][c.length];
        for (int i = 0; i < c.length - 2; i++) {
            for (int j = i + 1; j < c.length - 1; j++) {
                if (c[i][j] < minCor) continue;
                for (int k = j + 1; k < c.length; k++) {
                    double min = Math.min(Math.min(c[i][j], c[j][k]), c[i][k]);
                    if (min >= minCor) {
                        s[i][j] += min;
                        s[j][i] += min;
                        s[j][k] += min;
                        s[k][j] += min;
                        s[i][k] += min;
                        s[k][i] += min;
                    }
                }
            }
        }
        return s;
    }

    public static List<Holder> getOrdering(double[][] s, Holder[][] h) {
        List<Holder> list = new ArrayList<Holder>();
        for (int i = 0; i < s.length - 1; i++) {
            for (int j = i + 1; j < s.length; j++) {
                h[i][j] = new Holder(i, j, s[i][j]);
                h[j][i] = h[i][j];
                list.add(h[i][j]);
            }
        }
        Collections.sort(list);
        for (Holder ho : list) {
        }
        return list;
    }

    static class Holder implements Comparable {

        int i;

        int j;

        Double v;

        Holder(int i, int j, Double v) {
            this.i = i;
            this.j = j;
            this.v = v;
        }

        public int compareTo(Object o) {
            return ((Holder) o).v.compareTo(v);
        }

        @Override
        public String toString() {
            return i + "\t" + j + "\t" + v;
        }
    }

    static void printMatrix(double[][] x) {
        for (int i = 0; i < x.length; i++) {
            System.out.print("\t" + i);
        }
        for (int i = 0; i < x.length; i++) {
            System.out.print("\n" + i);
            for (int j = 0; j < x.length; j++) {
                System.out.print("\t" + x[i][j]);
            }
        }
        System.out.println();
    }

    static void removeUsed(List<Holder> list, Holder[][] h, int ind, List<Integer> module) {
        for (Integer j : module) {
            list.remove(h[ind][j]);
        }
    }

    static void removeUsedBeforeMerge(List<Holder> list, Holder[][] h, List<Integer> mod1, List<Integer> mod2) {
        List<Integer> A = new ArrayList<Integer>(SetUtils.getDiff(mod1, mod2));
        List<Integer> B = new ArrayList<Integer>(SetUtils.getDiff(mod2, mod1));
        for (Integer i : A) {
            for (Integer j : B) {
                list.remove(h[i][j]);
            }
        }
    }

    static int iter = 1;

    static void printModules(List<Module> modules, double[] x) {
        System.out.println("-------" + (iter++) + "----");
        for (Module module : modules) {
            if (x == null) System.out.println(module); else System.out.println(module.toString(x));
        }
    }

    static Module choseModuleToEnlarge(List<Module> modules) {
        double max = 0;
        Module maxMod = null;
        for (Module module : modules) {
            double score = module.getNextScore();
            if (score > max) {
                max = score;
                maxMod = module;
            }
        }
        return maxMod;
    }

    static List<List<Integer>> growModules(double[][] s, double[][] c, double[] x) {
        Holder[][] h = new Holder[s.length][s.length];
        List<Holder> ord = getOrdering(s, h);
        List<Module> modules = new ArrayList<Module>();
        List<List<Integer>> result = new ArrayList<List<Integer>>();
        double cutThr = getAverageAbsoluteCor(c);
        Holder ho = ord.remove(0);
        List<Integer> list = new ArrayList<Integer>();
        list.add(ho.i);
        list.add(ho.j);
        Module module = new Module(list, c);
        module.fillNextNodes(cutThr);
        modules.add(module);
        while (true) {
            Holder next = null;
            if (!ord.isEmpty()) next = ord.get(0);
            boolean nextIsOnTheQueue = false;
            for (Module mod : modules) {
                int[] nn = mod.getNextNodes(5);
                if (nn[0] == -1) continue;
                if (next != null) for (int nni : nn) {
                    if ((next.i == nni && mod.contains(next.j)) || (next.j == nni && mod.contains(next.i))) nextIsOnTheQueue = true;
                }
            }
            Module mod = choseModuleToEnlarge(modules);
            if (mod == null) break;
            int nn = mod.getNextNodes(1)[0];
            removeUsed(ord, h, nn, mod.mem);
            mod.advance();
            if (mod.leastScore < cutThr) {
                mod.remove(nn);
                break;
            }
            if (!nextIsOnTheQueue && next != null && next.v > 0 && c[next.i][next.j] > cutThr) {
                ord.remove(next);
                list = new ArrayList<Integer>();
                list.add(next.i);
                list.add(next.j);
                module = new Module(list, c);
                module.fillNextNodes(cutThr);
                modules.add(module);
            }
            mergeHighOverlaps(modules, 0.7, ord, h, result, cutThr, c);
        }
        for (Module mod : modules) {
            result.add(mod.mem);
        }
        return result;
    }

    static List<Integer> getModuleOfMostPositivelyCorrelating(double[][] c, double[] x) {
        List<Integer> top = getTopIndexes(x, 3);
        Module module = growModuleToBestFit(c, x, top);
        return module.mem;
    }

    static List<Integer> getModuleOfMostNegativelyCorrelating(double[][] c, double[] x) {
        List<Integer> top = getBottomIndexes(x, 3);
        Module module = growModuleToBestNegativeFit(c, x, top);
        return module.mem;
    }

    private static Module growModuleFromSeed(double[][] c, List<Integer> top) {
        double cutThr = getAverageAbsoluteCor(c);
        Module module = new Module(top, c);
        module.fillNextNodes(cutThr);
        while (module.advance()) ;
        return module;
    }

    public static Module growModuleFromSeed(double[][] c, double[][] x, String[] modName, List<Integer> seed) {
        assert c.length == x[0].length;
        Module module = new Module(seed, c);
        module.fillNextNodes(0);
        for (String name : modName) {
            System.out.print("\t" + name);
        }
        int i = 0;
        while (module.advance()) {
            System.out.println();
            System.out.print((i++));
            double[] s = module.getFitnessScores(x);
            for (double v : s) {
                System.out.print("\t" + v);
            }
            System.out.print("\t" + module);
        }
        System.out.println();
        return module;
    }

    public static Module growModuleToBestFit(double[][] c, double[] x, List<Integer> seed) {
        assert c.length == x.length;
        Module module = new Module(seed, c);
        module.fillNextNodes(0);
        double maxScore = -1;
        int atStep = -1;
        int step = 0;
        while (module.advance()) {
            step++;
            double score = module.getFitnessScore(x);
            if (score > maxScore) {
                maxScore = score;
                atStep = step;
            }
        }
        module = new Module(seed, c);
        module.fillNextNodes(0);
        for (int i = 0; i < atStep; i++) {
            module.advance();
        }
        return module;
    }

    public static Module growModuleToBestNegativeFit(double[][] c, double[] x, List<Integer> seed) {
        assert c.length == x.length;
        Module module = new Module(seed, c);
        module.fillNextNodes(0);
        double minScore = 100;
        int atStep = -1;
        int step = 0;
        while (module.advance()) {
            step++;
            double score = module.getNegativeFitnessScore(x);
            if (score < minScore) {
                minScore = score;
                atStep = step;
            }
        }
        module = new Module(seed, c);
        module.fillNextNodes(0);
        for (int i = 0; i < atStep; i++) {
            module.advance();
        }
        return module;
    }

    static List<Integer> getTopIndexes(double[] x, int n) {
        Holder2[] h = new Holder2[x.length];
        for (int i = 0; i < x.length; i++) {
            h[i] = new Holder2(i, x[i]);
        }
        Arrays.sort(h);
        List<Integer> top = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            top.add(h[i].index);
        }
        return top;
    }

    static List<Integer> getBottomIndexes(double[] x, int n) {
        Holder2[] h = new Holder2[x.length];
        for (int i = 0; i < x.length; i++) {
            h[i] = new Holder2(i, x[i]);
        }
        Arrays.sort(h);
        List<Integer> top = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            top.add(h[h.length - 1 - i].index);
        }
        return top;
    }

    static class Holder2 implements Comparable {

        Integer index;

        Double val;

        Holder2(Integer index, Double val) {
            this.index = index;
            this.val = val;
        }

        public int compareTo(Object o) {
            return ((Holder2) o).val.compareTo(val);
        }
    }

    static List<List<Integer>> analyzeInANewWay(double[][] c, double[] x) {
        double[][] s = triangle(c, 0);
        System.out.println(" - - - - - -");
        for (int i = 0; i < 0; i++) {
        }
        return growModules(s, c, x);
    }

    public static double[][] getCorrelations(double[][] v) {
        double[][] c = new double[v.length][v.length];
        for (int i = 0; i < v.length - 1; i++) {
            for (int j = i + 1; j < v.length; j++) {
                c[i][j] = Pearson.calcCorrelation(v[i], v[j]);
                c[j][i] = c[i][j];
            }
        }
        return c;
    }

    public static double getAverageCorrelation(List<Integer> module, double[][] c) {
        double sum = 0;
        for (Integer i : module) {
            for (Integer j : module) {
                if (i < j) sum += c[i][j];
            }
        }
        return sum / (module.size() * (module.size() - 1) / 2);
    }

    public static double getAverageAbsoluteCor(double[][] c) {
        Histogram h = new Histogram(0.05);
        double sum = 0;
        int cnt = 0;
        for (int i = 0; i < c.length - 1; i++) {
            for (int j = i + 1; j < c.length; j++) {
                double x = Math.abs(c[i][j]);
                {
                    sum += x;
                    cnt++;
                }
                h.count(x);
            }
        }
        return sum / cnt;
    }

    private static void mergeHighOverlaps(List<Module> modules, double thr, List<Holder> list, Holder[][] h, List<List<Integer>> result, double cutThr, double[][] c) {
        for (Module module1 : new ArrayList<Module>(modules)) {
            if (!modules.contains(module1)) continue;
            for (Module module2 : new ArrayList<Module>(modules)) {
                if (module1 == module2) continue;
                int com = SetUtils.countCommon(module1.mem, module2.mem);
                if (com / (double) module1.size() > thr || com / (double) module2.size() > thr) {
                    Module rem = module1.size() > module2.size() ? module2 : module1;
                    removeUsedBeforeMerge(list, h, module1.mem, module2.mem);
                    {
                    }
                    modules.remove(rem);
                    break;
                }
            }
        }
    }

    private static void swap(List<Integer> m1, List<Integer> m2) {
        List<Integer> temp = new ArrayList<Integer>(m1);
        m1.clear();
        m1.addAll(m2);
        m2.clear();
        m2.addAll(temp);
    }

    static class Module {

        List<Integer> mem;

        List<Integer> next;

        List<Double> score;

        double[][] c;

        Map<Integer, Double> sum;

        Double leastScore;

        Module(List<Integer> mem, double[][] c) {
            this.c = c;
            this.mem = new ArrayList<Integer>();
            sum = new HashMap<Integer, Double>();
            leastScore = -Double.MAX_VALUE;
            for (Integer i : mem) {
                add(i);
            }
        }

        void add(Integer i) {
            assert !mem.contains(i);
            leastScore = Double.MAX_VALUE;
            sum.put(i, 0D);
            for (Integer j : mem) {
                double scr = sum.get(j) + c[i][j];
                sum.put(j, scr);
                if (leastScore > scr) leastScore = scr;
                sum.put(i, sum.get(i) + c[i][j]);
            }
            double scr = sum.get(i);
            if (leastScore > scr) leastScore = scr;
            leastScore /= mem.size();
            mem.add(i);
        }

        void remove(Integer i) {
            assert mem.contains(i);
            leastScore = Double.MAX_VALUE;
            sum.remove(i);
            mem.remove(i);
            for (Integer j : mem) {
                double score = sum.get(j) - c[i][j];
                sum.put(j, score);
                if (leastScore > score) leastScore = score;
            }
            leastScore /= mem.size() - 1;
        }

        boolean contains(Integer i) {
            return mem.contains(i);
        }

        void fillNextNodes(double cutThr) {
            next = new ArrayList<Integer>();
            score = new ArrayList<Double>();
            Module mod = new Module(mem, c);
            double minScore = cutThr + 1;
            while (minScore > cutThr) {
                double max = -Double.MAX_VALUE;
                Integer maxInd = null;
                for (int i = 0; i < c.length; i++) {
                    if (mod.contains(i)) continue;
                    mod.add(i);
                    if (mod.leastScore > max) {
                        max = mod.leastScore;
                        maxInd = i;
                    }
                    mod.remove(i);
                }
                if (max < cutThr) break;
                next.add(maxInd);
                score.add(max);
                mod.add(maxInd);
            }
        }

        int[] getNextNodes(int size) {
            int[] n = new int[size];
            for (int i = 0; i < n.length; i++) {
                if (next.size() > i) n[i] = next.get(i); else n[i] = -1;
            }
            return n;
        }

        double getNextScore() {
            if (!score.isEmpty()) return score.get(0);
            return -Double.MAX_VALUE;
        }

        boolean advance() {
            if (next.isEmpty()) return false;
            add(next.remove(0));
            score.remove(0);
            return true;
        }

        int size() {
            return mem.size();
        }

        double getAvgCorr(double[] x) {
            double sum = 0;
            for (Integer i : mem) {
                sum += x[i];
            }
            return sum / mem.size();
        }

        public double[] getFitnessScores(double[][] x) {
            double[] s = new double[x.length];
            for (int i = 0; i < s.length; i++) {
                s[i] = getFitnessScore(x[i]);
            }
            return s;
        }

        public double getFitnessScore(double[] x) {
            double[] v = cropWithMembers(x);
            Arrays.sort(v);
            return Summary.meanOrderWeighted(v) * size();
        }

        public double getNegativeFitnessScore(double[] x) {
            double[] v = cropWithMembers(x);
            Arrays.sort(v);
            ArrayUtils.reverse(v);
            return Summary.meanOrderWeighted(v) * size();
        }

        private double[] cropWithMembers(double[] x) {
            double[] v = new double[size()];
            for (int j = 0; j < mem.size(); j++) {
                v[j] = x[mem.get(j)];
            }
            return v;
        }

        @Override
        public String toString() {
            String s = getMemsInString();
            s += leastScore;
            return s;
        }

        private String getMemsInString() {
            String s = "";
            List<Integer> temp = mem;
            Collections.sort(temp);
            for (Integer i : temp) {
                s += i + "\t";
            }
            return s;
        }

        public String toString(double[] x) {
            String s = getMemsInString();
            s += getAvgCorr(x);
            return s;
        }
    }

    public static void main(String[] args) {
        Random r = new Random(5);
        int n = 100;
        int n1 = 10;
        int n2 = 10;
        int n3 = 10;
        int n4 = 10;
        double c1 = 0.5;
        double c2 = 0.5;
        double c3 = 0.5;
        double c4 = 0.5;
        double[][] v = new double[n1 + n2 + n3 + n4][n];
        double[] x = new double[n];
        double[] y = new double[n];
        double[] b = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = r.nextDouble();
            y[i] = r.nextDouble();
            b[i] = r.nextDouble();
            for (int j = 0; j < n1; j++) v[j][i] = corRand(x[i], c1, r);
            for (int j = n1; j < n1 + n2; j++) v[j][i] = corRand(y[i], c2, r);
            for (int j = n1 + n2; j < n1 + n2 + n3; j++) v[j][i] = corRand((x[i] + y[i]) / 2, c3, r);
            for (int j = n1 + n2 + n3; j < n1 + n2 + n3 + n4; j++) v[j][i] = corRand(b[i], c4, r);
        }
        double[][] c = getCorrelations(v);
        double avgCor = getAverageAbsoluteCor(c);
        System.out.println("\navgCor = " + avgCor);
        double[][] modcor = new double[3][v.length];
        double[][] mod = new double[][] { x, y, b };
        for (int i = 0; i < mod.length; i++) {
            for (int j = 0; j < v.length; j++) {
                modcor[i][j] = Pearson.calcCorrelation(mod[i], v[j]);
            }
        }
        growModuleFromSeed(c, modcor, new String[] { "x", "y", "b" }, Arrays.asList(5, 6));
    }

    public static double corRand(double x, double cor, Random r) {
        boolean negative = cor < 0;
        if (negative) cor = -cor;
        double num = (cor * x) + (Math.sqrt(1 - (cor * cor)) * r.nextDouble());
        if (negative) num = 1 - num;
        return num;
    }
}
