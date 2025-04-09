public class MaxSum {

    private static int seqInicio, seqFim;

    public static void main(String[] args) {
        int[] a = new int[args.length];
        for (int i = 0; i < a.length && i < args.length; i++) {
            a[i] = Integer.parseInt(args[i]);
        }
        ChronoTimer timer = new ChronoTimer("MaxSum");
        timer.start();
        int maxSum = maxSumRecursive(a, 0, a.length - 1);
        timer.stop();
        System.out.println("Recursivo: " + maxSum);
        timer.showTimeElapsed(ChronoTimerUnits.MILLISECONDS);
        timer.start();
        maxSum = maxSubSumLinear(a);
        timer.stop();
        System.out.println("Linear: " + maxSum);
        timer.showTimeElapsed(ChronoTimerUnits.MILLISECONDS);
    }

    /**
     * soma m�xima feita em O(n)
     * seqStart e seqEnd representam o limite da melhor soma
     */
    public static int maxSubSumLinear(int[] a) {
        int maxSum = 0;
        int thisSum = 0;
        for (int i = 0, j = 0; j < a.length; j++) {
            thisSum += a[j];
            if (thisSum > maxSum) {
                maxSum = thisSum;
                seqInicio = i;
                seqFim = j;
            } else if (thisSum < 0) {
                i = j + 1;
                thisSum = 0;
            }
        }
        return maxSum;
    }

    /**
     * soma m�xima de forma recursiva
     * encontra o m�ximo da soma dos elementos do vetor a
     */
    private static int maxSumRecursive(int[] a, int left, int right) {
        int somaEsquerdaMax = 0, somaDireitaMax = 0;
        int somaEsquerda = 0, somaDireita = 0;
        int center = (left + right) / 2;
        if (left == right) return a[left] > 0 ? a[left] : 0;
        int maxLeftSum = maxSumRecursive(a, left, center);
        int maxRightSum = maxSumRecursive(a, center + 1, right);
        for (int i = center; i >= left; i--) {
            somaEsquerda += a[i];
            if (somaEsquerda > somaEsquerdaMax) somaEsquerdaMax = somaEsquerda;
        }
        for (int i = center + 1; i <= right; i++) {
            somaDireita += a[i];
            if (somaDireita > somaDireitaMax) somaDireitaMax = somaDireita;
        }
        return max3(maxLeftSum, maxRightSum, somaEsquerdaMax + somaDireitaMax);
    }

    private static int max3(int a, int b, int c) {
        return a > b ? a > c ? a : c : b > c ? b : c;
    }
}
