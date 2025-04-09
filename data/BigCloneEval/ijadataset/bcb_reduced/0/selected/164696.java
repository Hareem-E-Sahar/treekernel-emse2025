package jaga.testing.evolve;

import jaga.evolve.*;
import jaga.*;
import java.util.Vector;
import debug.DebugLib;

/**
 *
 * @author  Michael Garvie
 * @version 
 */
public class TestSelectors extends Object {

    static final double[] fitnesses = { 1d / 4, 1d / 128, 1d / 64, 1d / 8, 1d / 32, 0, 1d / 2 };

    static final double[] ranks = { 1, 0 };

    static Population pop = new Population();

    static StringBuffer narrator = new StringBuffer();

    static Selector FPSelector = new FitnessProportionateSelector();

    static Selector RSelector = new RankSelector();

    static Selector SRSelector = new RankSelector(ranks);

    static Vector popMirr = new Vector();

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        debug.DebugLib.trcLogger.isLogging = true;
        testAllExtremesSelector(SRSelector);
        debug.DebugLib.flush();
    }

    static void testSelector(Selector s) {
        for (int gl = 0; gl < fitnesses.length; gl++) {
            Genotype blob = new Genotype();
            blob.setFitness(fitnesses[gl]);
            narrator.append("ind#" + gl + " => " + fitnesses[gl] + "\n");
            pop.add(blob);
            popMirr.add(blob);
        }
        testPickMany(s);
    }

    private static void testPickMany(Selector s) {
        DebugLib.trcLogger.isLogging = true;
        narrator.append("Now will test pick() method used to select different individuals\n");
        narrator.append("according to their fitness, we'll set up individuals with fitnesses:\n");
        int[] hits = new int[fitnesses.length];
        for (int pl = 0; pl < 100000; pl++) {
            hits[popMirr.indexOf(s.select(pop))]++;
        }
        narrator.append("Test results in hits per individual:\n");
        for (int il = 0; il < fitnesses.length; il++) {
            narrator.append("ind#" + il + " => " + ((double) (hits[il])) + "\n");
        }
        System.out.println(narrator + "\n");
    }

    /** Chooses a position from an array according to the cumulative 
     * probabilities stored in that array.  Use binary search to find
     * individual corresponding to cumulative fitness range in which random
     * number is.
     * First cumulative frequency must be 0!
     * WARNING: does not work with negative fitnesses
     */
    public static int pickPosition(double[] cumProb) {
        int roof = cumProb.length - 1;
        int floor = 1;
        double totalProb = cumProb[roof];
        double r = Math.random() * totalProb;
        int pos = (floor + roof) / 2;
        boolean inUpper = false, inLower = false;
        while (!(inUpper && inLower)) {
            System.out.println("fl=" + floor + " ps=" + pos + " rf=" + roof + "   r=" + r + " !pos=" + cumProb[pos]);
            inUpper = r <= cumProb[pos];
            inLower = r >= cumProb[pos - 1];
            if (!inUpper) {
                floor = pos + 1;
            } else if (!inLower) {
                roof = pos - 1;
            }
            pos = (roof + floor) / 2;
        }
        System.out.println("ret" + pos);
        return pos;
    }

    static void testPick() {
        double[] cf = new double[fitnesses.length + 1];
        cf[0] = 0;
        double acc = 0;
        for (int dl = 0; dl < fitnesses.length; dl++) {
            acc += fitnesses[dl];
            cf[dl + 1] = acc;
            System.out.println("cf " + (dl + 1) + " = " + cf[dl + 1]);
        }
        int[] hits = new int[fitnesses.length + 1];
        for (int pl = 0; pl < 10; pl++) {
            hits[pickPosition(cf)]++;
        }
        for (int il = 0; il <= fitnesses.length; il++) {
            System.out.println("ind#" + il + " => " + ((double) (hits[il])) + "\n");
        }
    }

    static void testAllExtremesSelector(Selector s) {
        BitSet.setCodeBase(1);
        StringBuffer narrator = new StringBuffer("Testing extreme cases for selector:" + s);
        Genotype A = new Genotype("00000000");
        A.setFitness(0.5);
        narrator.append("\nInd A = " + A);
        Genotype B = new Genotype("11111");
        B.setFitness(0.4);
        narrator.append("\nInd B = " + B);
        Population pop = new Population();
        pop.add(A);
        pop.add(B);
        int[] hits = new int[pop.size()];
        for (int tl = 0; tl < 10; tl++) {
            narrator.append("\nselected " + s.select(pop));
        }
        System.out.println(narrator);
    }
}
