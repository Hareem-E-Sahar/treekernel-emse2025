import java.util.Random;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.TreeSet;
import java.text.DecimalFormat;
import java.util.HashMap;
import gnu.trove.TIntDoubleHashMap;
import de.spieleck.app.cngram.NGramProfile;
import de.spieleck.app.cngram.NGram;
import de.spieleck.app.cngram.NGramProfileImpl;
import de.spieleck.app.cngram.NGramMetric;
import de.spieleck.app.cngram.SqMetric;
import de.spieleck.app.cngram.RawMetric;
import de.spieleck.app.cngram.CosMetric;
import de.spieleck.app.cngram.C2Metric;
import de.spieleck.app.cngram.C2aMetric;
import de.spieleck.app.cngram.C2xMetric;

/**
 * A simple demonstration script to show a fairly automated
 * and entirely dictionary free method to solve Vignere codes.
 * Caution: This is demonstration or proof of concept code!
 */
public class BF4 {

    private static final Random rand = new Random(42);

    private NGramProfile profReference;

    private NGramMetric metric;

    private String alph;

    private boolean autoKey;

    private String code;

    public BF4(NGramProfile profReference, NGramMetric metric, String alph, boolean autoKey) {
        this.profReference = profReference;
        this.metric = metric;
        this.alph = alph;
        this.autoKey = autoKey;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.out.println("Usage: BF4 alphabet language autoKey(+/-) keyLen codedtext");
            System.out.println("   alphabet is the set of handled characters.");
            System.out.println("   language is the name of the profile to use.");
            System.out.println("   keyLen is not used any more.");
            System.exit(1);
        }
        String alph = args[0];
        System.out.println("-- AlphaLen=" + alph.length());
        String lang = args[1];
        boolean autoKey = "+".equals(args[2]);
        int keyLen = Integer.parseInt(args[3]);
        String code = args[4];
        System.out.println("-- CodeLen=" + code.length());
        System.out.println(code);
        NGramProfileImpl profReference = new NGramProfileImpl(lang);
        profReference.load(profReference.getClass().getResourceAsStream(lang + "." + NGramProfile.NGRAM_PROFILE_EXTENSION), NGramProfileImpl.MODE_NOBLANK);
        double es = phi(profReference, alph);
        System.out.println("-- Es(" + lang + ")=" + es);
        BF4 cryptograph = new BF4(profReference, new CosWeightMetric(), alph, autoKey);
        cryptograph.setCode(code);
        double sumOKappa = 0.0;
        int countOKappa = 0;
        int upperKeyLimit = Math.max(15, (4 + code.length()) / 5);
        double[] kappa = new double[upperKeyLimit];
        for (int r = 1; r < upperKeyLimit; r++) {
            int c = 0;
            for (int i = r; i < code.length(); i++) {
                if (code.charAt(i) == code.charAt(i - r)) c++;
            }
            kappa[r] = 1.0 * c / (code.length() - r);
            System.out.println("Kappa (" + r + ")=" + kappa[r]);
            sumOKappa += kappa[r];
            countOKappa += 1;
            if (r * r > code.length()) break;
        }
        System.out.println("Kappa (MEAN)=" + (sumOKappa / countOKappa));
        double friedmanK = friedman(es, sumOKappa / countOKappa, alph.length(), code.length());
        System.out.println("FriedmannK: " + friedmanK);
        NGramProfileImpl profCipher = new NGramProfileImpl("code");
        profCipher.analyze(code);
        double phiT = phi(profCipher, alph);
        System.out.println("-- phiT=" + phiT);
        TreeSet<SortedSomething<String>> singleKeys = new TreeSet<SortedSomething<String>>();
        for (int i = 0; i < alph.length(); i++) {
            String singleKey = new String(new char[] { alph.charAt(i) });
            double score = cryptograph.scoreKey(singleKey);
            singleKeys.add(new SortedSomething<String>(singleKey, score));
        }
        SortedSomething<String> last1 = singleKeys.last();
        char char0 = last1.getKey().charAt(0);
        SortedSomething<String> last2 = singleKeys.headSet(last1).last();
        char char1 = last2.getKey().charAt(0);
        System.out.println("Optimum single letter: " + last1 + " " + last2);
        double baseLineScore = singleKeys.last().getValue();
        double er = 1.0 / alph.length();
        double friedman = friedman(es, phiT, alph.length(), code.length());
        System.out.println("Friedmann: " + friedman);
        Iterator codeGrams = profCipher.getSorted();
        TIntDoubleHashMap keyLenCands = new TIntDoubleHashMap();
        while (codeGrams.hasNext()) {
            NGram ng = (NGram) codeGrams.next();
            if (ng.getCount() == 1) break;
            if (ng.length() == 1) continue;
            System.out.print(ng + " " + ng.getCount() + ": ");
            String codeUp = code.toUpperCase();
            String ngStr = ng.toString().toUpperCase();
            int p0 = -1, p1;
            for (int i = 0; i < ng.getCount(); i++) {
                p1 = codeUp.indexOf(ngStr, p0 + 1);
                if (p0 != -1) {
                    int delta = p1 - p0;
                    System.out.print(delta + " ");
                    int[] primes = primes(delta);
                    double weight = ng.length() * Math.pow(2.0, ng.length()) / Math.sqrt(primes.length) / delta;
                    spread(1, primes, 0, weight, keyLenCands);
                }
                p0 = p1;
            }
            System.out.println();
        }
        double maxScore = -1;
        int maxCand = 0;
        System.out.println("upperKeyLimit=" + upperKeyLimit);
        TreeSet<SortedSomething<Integer>> sortedLens = new TreeSet<SortedSomething<Integer>>();
        HashMap<Integer, TreeSet<SortedSomething<String>>> seeds = new HashMap<Integer, TreeSet<SortedSomething<String>>>();
        DecimalFormat df = new DecimalFormat("0.000");
        double codeLenWeight = Math.sqrt(code.length());
        for (int cand = 2; cand < Math.min(upperKeyLimit, 99); cand++) {
            double bonus = 0.2 * cand * (upperKeyLimit - cand) / code.length() / code.length();
            double score = bonus;
            if (keyLenCands.contains(cand)) {
                score += keyLenCands.get(cand);
            }
            if (friedman > 2.0) {
                score += 0.5 * codeLenWeight * Math.exp(-0.4 * Math.abs(cand - friedman));
            }
            if (friedmanK > 2.0) {
                score += 0.5 * codeLenWeight * Math.exp(-0.4 * Math.abs(cand - friedmanK));
            }
            score += codeLenWeight * kappa[cand];
            TreeSet<SortedSomething<String>> topKeys = cryptograph.seedSet(cand, char0, char1);
            seeds.put(cand, topKeys);
            double instantMove = (baseLineScore - topKeys.last().getValue()) * code.length() / cand;
            score += instantMove;
            if (cand == 2) score /= 3.0;
            System.out.println(cand + " --> " + df.format(keyLenCands.get(cand)) + " " + df.format(instantMove) + " " + df.format(score));
            sortedLens.add(new SortedSomething<Integer>(cand, score));
            if (score > maxScore) {
                maxScore = score;
                maxCand = cand;
            }
        }
        ArrayList<TreeSet<SortedSomething<String>>> keySets = new ArrayList<TreeSet<SortedSomething<String>>>();
        double limit = 0.1 * sortedLens.first().getValue();
        for (SortedSomething<Integer> len : sortedLens) {
            if (len.getValue() < limit) break;
            TreeSet<SortedSomething<String>> topKeys = seeds.get(len.getKey());
            System.out.println("***  " + len + " " + topKeys.last());
            keySets.add(topKeys);
        }
        int trial = 0;
        int trialResetOnNewTop = 0;
        SortedSomething<String> newTopKey = null;
        SortedSomething<String> newSecondKey = null;
        int alphaFaktor = alph.length();
        do {
            TreeSet<SortedSomething<String>> optimalKeySet = null;
            TreeSet<SortedSomething<String>> worstKeySet = null;
            double optScore = Double.MAX_VALUE;
            double worstScore = -Double.MAX_VALUE;
            double sum1 = 0.0;
            double sum2 = 0.0;
            SortedSomething<String> preLoopTopTop = null;
            for (int i = 0; i < keySets.size(); i++) {
                TreeSet<SortedSomething<String>> topKeys = keySets.get(i);
                SortedSomething<String> topKey = cryptograph.doGeneticStep(topKeys, trialResetOnNewTop % 2 == 0 && trialResetOnNewTop < 4 * alphaFaktor);
                newTopKey = topKeys.last();
                newSecondKey = topKeys.headSet(newTopKey).last();
                sum1 += newTopKey.getValue() + newSecondKey.getValue();
                sum2 += newTopKey.getValue() * newTopKey.getValue() + newSecondKey.getValue() * newSecondKey.getValue();
                if (newTopKey.getValue() < optScore) {
                    optScore = newTopKey.getValue();
                    optimalKeySet = topKeys;
                    preLoopTopTop = topKey;
                }
                if (newTopKey.getValue() > worstScore && trial >= alphaFaktor * newTopKey.getKey().length()) {
                    worstScore = newTopKey.getValue();
                    worstKeySet = topKeys;
                }
            }
            newTopKey = optimalKeySet.last();
            SortedSomething<String> secondTopKey = optimalKeySet.headSet(newTopKey).last();
            SortedSomething<String> lastTopKey = optimalKeySet.first();
            int n = keySets.size() * 2;
            double sigma = Math.sqrt((sum2 - sum1 / n * sum1) / (n - 1));
            if (newTopKey.getKey() != preLoopTopTop.getKey()) {
                System.out.println(trial + " " + newTopKey + secondTopKey + ".." + optimalKeySet.first() + " " + df.format(sigma));
                System.out.println(cryptograph.decodeByKey(newTopKey.getKey()));
                trialResetOnNewTop = 0;
            } else if (worstKeySet != null && keySets.size() > 1) {
                if (worstKeySet.last().getValue() > secondTopKey.getValue() + 2 * sigma) {
                    System.out.println(trial + " bestWorst=" + worstKeySet.last() + " sigma=" + df.format(sigma) + " 2ndBest=" + secondTopKey);
                    keySets.remove(worstKeySet);
                    System.out.println("  Eliminating keyLen=[" + worstKeySet.last().getKey().length() + "]  " + keySets.size() + " sets remaining, best length=<" + newTopKey.getKey().length() + ">.");
                }
            }
            trial++;
            trialResetOnNewTop++;
        } while (trialResetOnNewTop < alphaFaktor * newTopKey.getKey().length() * 5 || trial < alphaFaktor * newTopKey.getKey().length() * 7);
        System.out.println("***");
        System.out.println(trial + " " + newTopKey);
        String decoded = cryptograph.decodeByKey(newTopKey.getKey());
        System.out.println(decoded);
        cryptograph.blankify(decoded, profReference);
    }

    private SortedSomething<String> doGeneticStep(TreeSet<SortedSomething<String>> topKeys, boolean doubled) {
        SortedSomething<String> oldTopKey = topKeys.last();
        SortedSomething<String> keyPair = oldTopKey;
        int h = rand.nextInt(topKeys.size());
        Iterator<SortedSomething<String>> iter = topKeys.iterator();
        for (int i = -1; i < h; i++) keyPair = iter.next();
        String newKey = crossOverKey(oldTopKey.getKey(), keyPair.getKey(), doubled);
        double score0 = scoreKey(newKey);
        topKeys.add(new SortedSomething<String>(newKey, score0));
        if (topKeys.size() > 100) topKeys.remove(topKeys.first());
        return oldTopKey;
    }

    /**
     * Seed a keyLen by one character and full mutation.
     */
    public TreeSet<SortedSomething<String>> seedSet(int keyLen, char char0) {
        TreeSet<SortedSomething<String>> res = new TreeSet<SortedSomething<String>>();
        char[] key = new char[keyLen];
        for (int i = 0; i < keyLen; i++) key[i] = char0;
        String key0 = new String(key);
        SortedSomething<String> seed;
        seed = new SortedSomething<String>(key0, scoreKey(key0));
        res.add(seed);
        String key1 = key0;
        for (int i = 0; i < 10; i++) {
            key1 = crossOverKey(key0, key1, true);
            seed = new SortedSomething<String>(key1, scoreKey(key1));
            res.add(seed);
        }
        return res;
    }

    /**
     * Seed a keyLen by two characters and random mixtures.
     */
    public TreeSet<SortedSomething<String>> seedSet(int keyLen, char char0, char char1) {
        TreeSet<SortedSomething<String>> res = new TreeSet<SortedSomething<String>>();
        char[] key = new char[keyLen];
        SortedSomething<String> seed;
        for (int j = 0; j < 30; j++) {
            for (int i = 0; i < keyLen; i++) key[i] = rand.nextBoolean() ? char0 : char1;
            String key0 = new String(key);
            seed = new SortedSomething<String>(key0, scoreKey(key0));
            res.add(seed);
        }
        return res;
    }

    public double scoreKey(String keyStr) {
        return scoreKey(keyStr, code);
    }

    public double scoreKey(String keyStr, String code) {
        String res = decodeByKey(keyStr, code);
        return scoreString(res);
    }

    public String decodeByKey(String keyStr) {
        return decodeByKey(keyStr, code);
    }

    public String decodeByKey(String keyStr, String code) {
        Vig v = new Vig(alph, keyStr, autoKey);
        return v.decode(code);
    }

    public double scoreString(String test) {
        NGramProfileImpl prof = new NGramProfileImpl("test");
        prof.analyze(test);
        return metric.diff(profReference, prof);
    }

    public String crossOverKey(String key1, String key2) {
        return crossOverKey(key1, key2, false);
    }

    public String crossOverKey(String key1, String key2, boolean doubled) {
        int keyLen = key1.length();
        if (keyLen != key2.length()) throw new RuntimeException("Cannot relyable crossover this!");
        char[] key = new char[keyLen];
        int mutate1 = rand.nextInt(keyLen);
        int mutate2 = doubled ? rand.nextInt(keyLen) : -1;
        for (int i = 0; i < keyLen; i++) {
            if (i == mutate1 || i == mutate2) key[i] = alph.charAt(rand.nextInt(alph.length())); else if (rand.nextInt(2) == 0) key[i] = key1.charAt(i); else key[i] = key2.charAt(i);
        }
        return new String(key);
    }

    private String check(TreeSet<SortedSomething<String>> topKeys) {
        int trial = 0;
        int trialLimit = 15 * topKeys.last().getKey().length() + 100;
        while (trial < trialLimit) {
            SortedSomething<String> topKey = topKeys.last();
            String newKey = crossOverKey(topKeys.last().getKey(), topKeys.first().getKey());
            double score0 = scoreKey(newKey);
            topKeys.add(new SortedSomething<String>(newKey, score0));
            SortedSomething<String> newTopKey = topKeys.last();
            if (newTopKey.getKey() != topKey.getKey()) {
                System.out.println(trial + " " + newTopKey + topKeys.headSet(topKeys.last()).last() + ".." + topKeys.first() + " " + topKeys.size());
                System.out.println(decodeByKey(newKey));
                trialLimit = Math.max(trialLimit, 3 * trial + 2 * newKey.length());
            }
            if (topKeys.size() > 100) topKeys.remove(topKeys.first());
            trial++;
        }
        return topKeys.last().getKey();
    }

    /** Very simple prime factorization */
    public static int[] primes(int width) {
        int[] primes = new int[width / 2];
        int iPrime = 0;
        int work = width;
        while (work % 2 == 0) {
            primes[iPrime++] = 2;
            work /= 2;
        }
        int prime = 3;
        while (work > 1) {
            if (work % prime == 0) {
                primes[iPrime++] = prime;
                work /= prime;
            } else {
                prime += 2;
            }
        }
        int[] res = new int[iPrime];
        System.arraycopy(primes, 0, res, 0, iPrime);
        return res;
    }

    public static void spread(int base, int[] primes, int index0, double weight, TIntDoubleHashMap map) {
        for (int i = index0; i < primes.length; i++) {
            int v = base * primes[i];
            map.adjustOrPutValue(v, weight, weight);
            spread(v, primes, i + 1, weight, map);
        }
    }

    public static double phi(NGramProfile prof, String alph) {
        long s2 = 0;
        int[] m = new int[alph.length()];
        for (int i = 0; i < alph.length(); i++) {
            String s = "" + Character.toLowerCase(alph.charAt(i));
            NGram ng = prof.get(s);
            if (ng != null) {
                m[i] = ng.getCount();
                s2 += m[i];
            }
        }
        long sqs = 0;
        for (int i = 0; i < alph.length(); i++) {
            sqs += (long) m[i] * (m[i] - 1);
        }
        return 1.0 * sqs / s2 / (s2 - 1);
    }

    public static String blankify(String text, NGramProfile profReference) {
        String top = text, trial = text;
        double score = Double.MAX_VALUE;
        NGramMetric metric = new CosWeightMetric(3.0, Integer.MAX_VALUE);
        while (true) {
            double minScore = Double.MAX_VALUE;
            String iTop = top;
            for (int h = 1; h < top.length(); h++) {
                if (top.charAt(h) == ' ') {
                    System.out.println("- " + h);
                    System.out.println("**" + top);
                    System.out.println("**" + top.substring(0, h) + top.substring(h + 1));
                    trial = top.substring(0, h) + top.substring(h + 1);
                } else {
                    trial = top.substring(0, h) + ' ' + top.substring(h);
                }
                NGramProfileImpl prof = new NGramProfileImpl("test");
                prof.analyze(trial);
                double newScore = metric.diff(profReference, prof);
                if (newScore < minScore) {
                    System.out.println(" --> " + newScore);
                    System.out.println(trial);
                    minScore = newScore;
                    iTop = trial;
                }
            }
            if (minScore <= score) {
                top = iTop;
                score = minScore;
            } else {
                System.out.println("minScore=" + minScore + ", score=" + score);
                System.out.println("iTop=" + iTop);
                System.out.println(" Top=" + top);
                break;
            }
        }
        return top;
    }

    public static String blankify1(String text, NGramProfile profReference) {
        String top = text, trial = text;
        double score = Double.MAX_VALUE;
        NGramMetric metric = new CosMetric();
        int count = 0, countPlus = 0, countMinus = 0, countReset = 0;
        while (true) {
            count++;
            NGramProfileImpl prof = new NGramProfileImpl("test");
            prof.analyze(trial);
            double newScore = metric.diff(profReference, prof);
            if (newScore < score) {
                System.out.println("   ---> " + newScore + ", len=" + trial.length() + ", count=" + count + " (+)" + countPlus + " (-)" + countMinus + " (r)" + countReset);
                System.out.println("   ---> " + trial);
                top = trial;
                score = newScore;
            } else if (rand.nextDouble() < 0.01 + 5.0 * (newScore - score)) {
                trial = top;
                countReset++;
            }
            int h = rand.nextInt(trial.length() - 1) + 1;
            if (trial.charAt(h) == ' ') {
                trial = trial.substring(0, h) + trial.substring(h + 1);
                countMinus++;
            } else {
                trial = trial.substring(0, h) + ' ' + trial.substring(h);
                countPlus++;
            }
        }
    }

    /** 
     * friedman estimate.
     * es = Es = property of language.
     * phiT = property of sample.
     * n = length of alphabet.
     * k = length of sample.
     */
    public static double friedman(double es, double phiT, int n, int k) {
        double e0 = 1.0 / n;
        return k * (es - e0) / ((k - 1) * phiT - k * e0 + es);
    }

    private static class SortedSomething<E> implements Comparable {

        private E o;

        private double value;

        public SortedSomething(E o, double value) {
            this.o = o;
            this.value = value;
        }

        public E getKey() {
            return o;
        }

        public double getValue() {
            return value;
        }

        public int compareTo(Object o) {
            if (!(o instanceof SortedSomething)) return +1;
            SortedSomething s = (SortedSomething) o;
            if (this.value > s.value) return -1;
            if (this.value < s.value) return +1;
            return 0;
        }

        public String toString() {
            DecimalFormat df = new DecimalFormat("0.0000");
            return "[" + o + ": " + df.format(value) + "]";
        }
    }

    private static class RankedKeyStore extends TreeSet<SortedSomething<String>> {
    }
}
