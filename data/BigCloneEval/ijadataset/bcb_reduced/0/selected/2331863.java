package algorithm.search;

public class MaxSum {

    private static int seqStart, seqEnd;

    public static void main(String[] args) {
        int[] a = new int[args.length];
        for (int i = 0; i < a.length && i < args.length; i++) {
            a[i] = Integer.parseInt(args[i]);
        }
        System.out.println("Recursivo: " + maxSumRecursive(a, 0, a.length - 1));
        System.out.println("Linear: " + maxSubSumLinear(a));
    }

    private static int maxSum(int[] a) {
        int min = 0;
        int sum = 0;
        int max = a.length;
        int maxSum = 0;
        for (int i = 0; i < a.length; i++) {
            for (int j = min; j < max; j++) {
                sum += a[j];
                if (sum > maxSum) {
                    maxSum = sum;
                }
            }
            min++;
            sum = 0;
        }
        return max;
    }

    /**
     * Cubic maximum contiguous subsequence sum algorithm.
     * seqStart and seqEnd represent the actual best sequence.
     */
    public static int maxSubSum1(int[] a) {
        int maxSum = 0;
        for (int i = 0; i < a.length; i++) for (int j = i; j < a.length; j++) {
            int thisSum = 0;
            for (int k = i; k <= j; k++) thisSum += a[k];
            if (thisSum > maxSum) {
                maxSum = thisSum;
                seqStart = i;
                seqEnd = j;
            }
        }
        return maxSum;
    }

    /**
     * Quadratic maximum contiguous subsequence sum algorithm.
     * seqStart and seqEnd represent the actual best sequence.
     */
    public static int maxSubSum2(int[] a) {
        int maxSum = 0;
        for (int i = 0; i < a.length; i++) {
            int thisSum = 0;
            for (int j = i; j < a.length; j++) {
                thisSum += a[j];
                if (thisSum > maxSum) {
                    maxSum = thisSum;
                    seqStart = i;
                    seqEnd = j;
                }
            }
        }
        return maxSum;
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
                seqStart = i;
                seqEnd = j;
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
