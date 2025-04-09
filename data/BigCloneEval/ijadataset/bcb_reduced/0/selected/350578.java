package org.velma.tools;

import java.util.Random;

/**
 * unknown contributor. no tests for robustness were done.
 * @deprecated
 */
public class SequenceMutator {

    private String matrixOrder;

    private double transitionMatrix[][];

    public SequenceMutator() {
        this(SubstitutionMatrixFactory.getSubstitutionMatrixFactory().getMatrix("blosum50"));
    }

    public SequenceMutator(String matrixName) {
        this(SubstitutionMatrixFactory.getSubstitutionMatrixFactory().getMatrix(matrixName));
    }

    public SequenceMutator(Short substitutionMatrix[][]) {
        matrixOrder = "ABCDEFGHIKLMNPQRSTVWXYZ-";
        transitionMatrix = new double[matrixOrder.length()][matrixOrder.length()];
        double transMatRaw[][] = new double[matrixOrder.length()][matrixOrder.length()], columnSubotals[] = new double[matrixOrder.length()], probabilitySubtotal[] = new double[matrixOrder.length()];
        for (int i = 0; i < transMatRaw.length; i++) for (int j = 0; j < transMatRaw[i].length; j++) {
            if (i == transMatRaw.length - 1) if (j == transMatRaw.length - 1) transMatRaw[i][j] = Math.exp(0); else transMatRaw[i][j] = Math.exp(-2); else if (j == transMatRaw.length - 1) transMatRaw[i][j] = Math.exp(-2); else transMatRaw[i][j] = Math.exp(substitutionMatrix[i < j ? j : i][i < j ? i : j]);
        }
        for (int i = 0; i < transMatRaw.length; i++) for (int j = 0; j <= i; j++) columnSubotals[j] += transMatRaw[i][j];
        for (int j = 0; j < columnSubotals.length; j++) {
            for (int i = 0; i < columnSubotals.length; i++) {
                if (i < j) {
                    transitionMatrix[i][j] = transitionMatrix[j][i];
                    probabilitySubtotal[j] += transitionMatrix[j][i];
                } else transitionMatrix[i][j] = (1 - probabilitySubtotal[j]) * (transMatRaw[i][j] / columnSubotals[j]);
            }
        }
    }

    public String mutate(String sequence, Random random) {
        char mutant[] = sequence.toCharArray();
        double sum, rand;
        int index, rownum;
        for (int i = 0; i < sequence.length(); i++) {
            sum = 0;
            rand = random.nextDouble();
            rownum = matrixOrder.indexOf(sequence.charAt(i));
            for (index = 0; sum < rand; index++) sum += transitionMatrix[rownum][index];
            mutant[i] = matrixOrder.charAt(index - 1);
        }
        return new String(mutant);
    }

    public static void main(String args[]) {
        SequenceMutator sm = args.length >= 1 ? new SequenceMutator(args[0]) : new SequenceMutator();
        char seq[] = new char[75];
        Random random = new Random(System.currentTimeMillis());
        String letters = "ABCDEFGHIKLMNPQRSTVWXYZ", endl = System.getProperty("line.separator");
        for (int i = 0; i < seq.length; i++) {
            seq[i] = letters.charAt(random.nextInt(letters.length()));
        }
        java.util.ArrayList<String> pop = new java.util.ArrayList<String>();
        pop.add(new String(seq));
        while (pop.size() < 375) pop.add(sm.mutate(pop.get(pop.size() - 1), random));
        for (int i = 0; i < pop.size(); i++) {
            System.out.print(">seq" + (i + 1000) + endl + pop.get(i) + endl + (i == (pop.size() - 1) ? "" : endl));
        }
    }
}
