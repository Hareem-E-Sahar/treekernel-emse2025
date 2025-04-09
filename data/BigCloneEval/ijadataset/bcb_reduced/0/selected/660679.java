package genomemap.lhood.genetic;

public class SimulateData {

    /**
     * We simulate data using a different poisson process in each interval. We
     * want to check if we can estimate the probability of a crossover(a
     * pseudo-crossover) closely to the model parameter.Note that occurance of a
     * single crossover is not the only way a recombinant can be produced, it
     * depends on the strands involved in the crossover. For example, if all the
     * four strands are involved in 2 crossovers then all four meiotic products
     * are recombinant. In other words, occurence of 2 crossovers doesn`t
     * gurantee that all products will be parental. The interpretation of the
     * model parameter 'c' is thus the probability of a chiasmata in the
     * corresponding interval. Note that significantly different values of c
     * would indicate that chiasmata probabilities change across intervals.
     */
    int n;

    int loci;

    double[] cProb;

    public SimulateData(int n, double[] cProb) {
        this.n = n;
        this.cProb = cProb;
        this.loci = cProb.length + 1;
    }

    public int[][] simulate() {
        int[][] data = new int[n][loci];
        int[][] tetrad = new int[4][loci];
        int[] iAndj = new int[2];
        for (int i = 0; i < n; i++) {
            tetrad = new int[4][loci];
            tetrad[0][0] = 1;
            tetrad[1][0] = 1;
            tetrad[2][0] = 0;
            tetrad[3][0] = 0;
            for (int index = 1; index < loci; index++) {
                iAndj = getIandJ(cProb[index - 1]);
                int swap = 0;
                switch(iAndj[0]) {
                    case 0:
                        tetrad[0][index] = tetrad[0][index - 1];
                        tetrad[1][index] = tetrad[1][index - 1];
                        tetrad[2][index] = tetrad[2][index - 1];
                        tetrad[3][index] = tetrad[3][index - 1];
                        break;
                    case 1:
                        tetrad[0][index] = tetrad[2][index - 1];
                        tetrad[1][index] = tetrad[1][index - 1];
                        tetrad[2][index] = tetrad[0][index - 1];
                        tetrad[3][index] = tetrad[3][index - 1];
                        break;
                    case 2:
                        tetrad[0][index] = tetrad[0][index - 1];
                        tetrad[1][index] = tetrad[2][index - 1];
                        tetrad[2][index] = tetrad[1][index - 1];
                        tetrad[3][index] = tetrad[3][index - 1];
                        break;
                    case 3:
                        tetrad[0][index] = tetrad[0][index - 1];
                        tetrad[1][index] = tetrad[3][index - 1];
                        tetrad[2][index] = tetrad[2][index - 1];
                        tetrad[3][index] = tetrad[1][index - 1];
                        break;
                    case 4:
                        tetrad[0][index] = tetrad[3][index - 1];
                        tetrad[1][index] = tetrad[1][index - 1];
                        tetrad[2][index] = tetrad[2][index - 1];
                        tetrad[3][index] = tetrad[0][index - 1];
                        break;
                }
                switch(iAndj[1]) {
                    case 0:
                        tetrad[0][index] = tetrad[0][index];
                        tetrad[1][index] = tetrad[1][index];
                        tetrad[2][index] = tetrad[2][index];
                        tetrad[3][index] = tetrad[3][index];
                        break;
                    case 1:
                        swap = tetrad[0][index];
                        tetrad[0][index] = tetrad[2][index];
                        tetrad[1][index] = tetrad[1][index];
                        tetrad[2][index] = swap;
                        tetrad[3][index] = tetrad[3][index];
                        break;
                    case 2:
                        swap = tetrad[1][index];
                        tetrad[0][index] = tetrad[0][index];
                        tetrad[1][index] = tetrad[2][index];
                        tetrad[2][index] = swap;
                        tetrad[3][index] = tetrad[3][index];
                        break;
                    case 3:
                        swap = tetrad[1][index];
                        tetrad[0][index] = tetrad[0][index];
                        tetrad[1][index] = tetrad[3][index];
                        tetrad[2][index] = tetrad[2][index];
                        tetrad[3][index] = swap;
                        break;
                    case 4:
                        swap = tetrad[0][index];
                        tetrad[0][index] = tetrad[3][index];
                        tetrad[1][index] = tetrad[1][index];
                        tetrad[2][index] = tetrad[2][index];
                        tetrad[3][index] = swap;
                        break;
                }
            }
            data[i] = tetrad[randomIndex()];
        }
        int[][] rflp = new int[data[0].length][data.length];
        for (int index1 = 0; index1 < data[0].length; index1++) {
            for (int index2 = 0; index2 < data.length; index2++) {
                rflp[index1][index2] = data[index2][index1];
            }
        }
        return rflp;
    }

    private static int[] getIandJ(double c) {
        int[] result = new int[2];
        double rnd = Math.random();
        if (rnd <= (1 - c)) {
            result[0] = 0;
        } else {
            rnd = Math.random();
            if (rnd <= 0.25) {
                result[0] = 1;
            }
            if (rnd <= 0.50 && rnd > 0.25) {
                result[0] = 2;
            }
            if (rnd <= 0.75 && rnd > 0.50) {
                result[0] = 3;
            }
            if (rnd > 0.75) {
                result[0] = 4;
            }
        }
        rnd = Math.random();
        if (rnd <= (1 - c)) {
            result[1] = 0;
        } else {
            rnd = Math.random();
            if (rnd <= 0.25) {
                result[1] = 1;
            }
            if (rnd <= 0.50 && rnd > 0.25) {
                result[1] = 2;
            }
            if (rnd <= 0.75 && rnd > 0.50) {
                result[1] = 3;
            }
            if (rnd > 0.75) {
                result[1] = 4;
            }
        }
        return result;
    }

    /**
     * randomIndex(): gives one of the values of 0,1,2 and 3 equally
     * likely,using no chromatid inteference.
     */
    private static int randomIndex() {
        int result = 0;
        double rnd = Math.random();
        if (rnd <= 0.25) {
            result = 0;
        }
        if (rnd <= 0.50 && rnd > 0.25) {
            result = 1;
        }
        if (rnd <= 0.75 && rnd > 0.50) {
            result = 2;
        }
        if (rnd > 0.75) {
            result = 3;
        }
        return result;
    }
}
