package exp.util;

import ec.*;
import ec.coevolve.*;
import ec.simple.*;
import ec.util.*;

/**
 *
 * @author T.S.Yo
 * @version 1.0 
 */
public class MyExpEvaluator3 extends Evaluator {

    public int ntp;

    public int nsol;

    protected float[][] interaction;

    public int STPflag;

    public int SCOflag;

    public Individual[] iniTP;

    public float[] finalEval;

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        Parameter tempSubpop = new Parameter(ec.Initializer.P_POP).push(ec.Population.P_SIZE);
        int numSubpopulations = state.parameters.getInt(tempSubpop, null, 0);
        if (numSubpopulations != 2) state.output.fatal("Parameter incorrect, number of subpopulations has to be 2.", tempSubpop);
        ntp = state.parameters.getInt(tempSubpop.pop().push("subpop.0").push("size"), null, 0);
        if (ntp < 0) state.output.fatal("Parameter not found, or it has an incorrect value.", tempSubpop.push("0").push("size"));
        nsol = state.parameters.getInt(tempSubpop.pop().push("subpop.1").push("size"), null, 0);
        if (nsol < 0) state.output.fatal("Parameter not found, or it has an incorrect value.", tempSubpop.push("1").push("size"));
        STPflag = state.parameters.getInt(base.push("eval1"), null, 0);
        SCOflag = state.parameters.getInt(base.push("eval2"), null, 0);
        System.out.println("ntp: " + ntp + "    nsol: " + nsol);
        System.out.println("EvaMethod 1: " + STPflag + "    EvaMethod 2: " + SCOflag);
        interaction = new float[ntp][nsol];
    }

    public boolean runComplete(final EvolutionState state) {
        return false;
    }

    public void evaluatePopulation(final EvolutionState state) {
        ((GroupedProblemForm) p_problem).preprocessPopulation(state, state.population);
        performEvaluation(state, state.population, (GroupedProblemForm) p_problem, SCOflag);
        ((GroupedProblemForm) p_problem).postprocessPopulation(state, state.population);
    }

    public void beforeEvaluation(final EvolutionState state, final Population population, final GroupedProblemForm prob) {
    }

    public void performEvaluation(final EvolutionState state, final Population population, final GroupedProblemForm prob, final int SCOflag) {
        Individual[] pair = new Individual[2];
        boolean[] updates = new boolean[2];
        updates[0] = true;
        updates[1] = true;
        for (int i = 0; i < ntp; i++) {
            pair[0] = population.subpops[0].individuals[i];
            for (int j = 0; j < nsol; j++) {
                pair[1] = population.subpops[1].individuals[j];
                prob.evaluate(state, pair, updates, false, 0);
                interaction[i][j] = ((SimpleFitness) (pair[1].fitness)).fitness();
            }
        }
        float[] tpFit1 = new float[ntp];
        float[] solFit1 = new float[nsol];
        float[] tpFit2 = new float[ntp];
        float[] solFit2 = new float[nsol];
        float[] tpFit = new float[ntp];
        float[] solFit = new float[nsol];
        switch(STPflag) {
            case 1:
                evalAveScore(tpFit1, solFit1);
                break;
            case 2:
                evalWeiScore(tpFit1, solFit1);
                break;
            default:
                evalAveScore(tpFit1, solFit1);
        }
        switch(SCOflag) {
            case 5:
                evalAveInfo(tpFit2, solFit2);
                break;
            case 6:
                evalWeiInfo(tpFit2, solFit2);
                break;
            default:
                evalAveInfo(tpFit2, solFit2);
        }
        float[][] tpMOS = new float[2][];
        float[][] solMOS = new float[2][];
        tpMOS[0] = tpFit1;
        tpMOS[1] = tpFit2;
        solMOS[0] = solFit1;
        solMOS[1] = solFit2;
        advMOEval(transposeMatrix(tpMOS), tpFit);
        advMOEval(transposeMatrix(solMOS), solFit);
        for (int i = 0; i < ntp; i++) {
            ((SimpleFitness) (population.subpops[0].individuals[i].fitness)).setFitness(state, tpFit[i], false);
        }
        for (int j = 0; j < nsol; j++) {
            ((SimpleFitness) (population.subpops[1].individuals[j].fitness)).setFitness(state, solFit[j], false);
        }
    }

    public void afterEvaluation(final EvolutionState state, final Population population, final GroupedProblemForm prob) {
    }

    public void evalAveScore(final float[] tpFit, final float[] solFit) {
        float[][] tp_sol = mtxMargin(interaction);
        float[] nASt = normalizeMax(tp_sol[0]);
        float[] nASs = normalizeMax(tp_sol[1]);
        for (int i = 0; i < ntp; i++) {
            tpFit[i] = 1 - nASt[i];
        }
        for (int j = 0; j < nsol; j++) {
            solFit[j] = nASs[j];
        }
    }

    public void evalWeiScore(final float[] tpFit, final float[] solFit) {
        float[][] wt_tp_sol = mtxMargin(interaction);
        for (int i = 0; i < ntp; i++) {
            if (wt_tp_sol[0][i] == 0) wt_tp_sol[0][i] = 0F; else wt_tp_sol[0][i] = 1 / wt_tp_sol[0][i];
        }
        for (int j = 0; j < nsol; j++) {
            if (wt_tp_sol[1][j] == 0) wt_tp_sol[1][j] = 0F; else wt_tp_sol[1][j] = 1 / wt_tp_sol[1][j];
        }
        wt_tp_sol[0] = normalizeSumToOne(wt_tp_sol[0]);
        wt_tp_sol[1] = normalizeSumToOne(wt_tp_sol[1]);
        float[] tpFitness = new float[ntp];
        float[] solFitness = new float[nsol];
        for (int i = 0; i < ntp; i++) {
            for (int j = 0; j < nsol; j++) {
                tpFitness[i] += interaction[i][j] * wt_tp_sol[1][j];
            }
        }
        for (int j = 0; j < nsol; j++) {
            for (int i = 0; i < ntp; i++) {
                solFitness[j] += interaction[i][j] * wt_tp_sol[0][i];
            }
        }
        float[] nASt = normalizeMax(tpFitness);
        float[] nASs = normalizeMax(solFitness);
        for (int i = 0; i < ntp; i++) {
            tpFit[i] = 1 - nASt[i];
        }
        for (int j = 0; j < nsol; j++) {
            solFit[j] = nASs[j];
        }
    }

    public void evalMOOScore(final float[] tpFit, final float[] solFit) {
        float[][] domTP = calDominance(interaction);
        float[][] domSol = calDominance(transposeMatrix(interaction));
        float[] domScoreTP = mtxMargin(domTP)[0];
        float[] domScoreSol = mtxMargin(domSol)[0];
        float[] ndscoreTP = normalizeMax(domScoreTP);
        float[] ndscoreSol = normalizeMax(domScoreSol);
        for (int i = 0; i < ntp; i++) {
            tpFit[i] = ndscoreTP[i];
        }
        for (int j = 0; j < nsol; j++) {
            solFit[j] = ndscoreSol[j];
        }
    }

    public void evalSimScore(final float[] tpFit, final float[] solFit) {
    }

    public void evalAveInfo(final float[] tpFit, final float[] solFit) {
        float weiAS = 0.7F;
        float[][] tp_sol = mtxMargin(interaction);
        float[][][] distTP = calTPDistinction();
        float[][][] distSol = calSolDistinction();
        float[] dscoreTP = new float[ntp];
        float[] dscoreSol = new float[nsol];
        for (int i = 0; i < ntp; i++) {
            for (int p = 0; p < nsol; p++) {
                for (int q = 0; q < nsol; q++) {
                    dscoreTP[i] += distTP[i][p][q];
                }
            }
        }
        for (int j = 0; j < nsol; j++) {
            for (int p = 0; p < ntp; p++) {
                for (int q = 0; q < ntp; q++) {
                    dscoreSol[j] += distSol[j][p][q];
                }
            }
        }
        float[] ndscoreTP = normalizeMax(dscoreTP);
        float[] ndscoreSol = normalizeMax(dscoreSol);
        for (int i = 0; i < ntp; i++) {
            tpFit[i] = (1 - weiAS) * ndscoreTP[i] + weiAS * (1 - tp_sol[0][i] / (float) nsol);
        }
        for (int j = 0; j < nsol; j++) {
            solFit[j] = (1 - weiAS) * ndscoreSol[j] + weiAS * (tp_sol[1][j] / (float) ntp);
        }
    }

    public void evalWeiInfo(final float[] tpFit, final float[] solFit) {
        float weiAS = 0.7F;
        float[][] tp_sol = mtxMargin(interaction);
        float[][][] distTP = calTPDistinction();
        float[][][] distSol = calSolDistinction();
        float[][] weiTP = new float[nsol][nsol];
        float[][] weiSol = new float[ntp][ntp];
        for (int i = 0; i < ntp; i++) {
            for (int p = 0; p < nsol; p++) {
                for (int q = 0; q < nsol; q++) {
                    weiTP[p][q] += distTP[i][p][q];
                }
            }
        }
        for (int j = 0; j < nsol; j++) {
            for (int p = 0; p < ntp; p++) {
                for (int q = 0; q < ntp; q++) {
                    weiSol[p][q] += distSol[j][p][q];
                }
            }
        }
        float[] dscoreTP = new float[ntp];
        float[] dscoreSol = new float[nsol];
        for (int i = 0; i < ntp; i++) {
            for (int p = 0; p < nsol; p++) {
                for (int q = 0; q < nsol; q++) {
                    if (weiTP[p][q] == 0) weiTP[p][q] = 1;
                    dscoreTP[i] += distTP[i][p][q] / weiTP[p][q];
                }
            }
        }
        for (int j = 0; j < nsol; j++) {
            for (int p = 0; p < ntp; p++) {
                for (int q = 0; q < ntp; q++) {
                    if (weiSol[p][q] == 0) weiSol[p][q] = 1;
                    dscoreSol[j] += distSol[j][p][q] / weiSol[p][q];
                }
            }
        }
        float[] ndscoreTP = normalizeMax(dscoreTP);
        float[] ndscoreSol = normalizeMax(dscoreSol);
        for (int i = 0; i < ntp; i++) {
            tpFit[i] = (1 - weiAS) * ndscoreTP[i] + weiAS * (1 - tp_sol[0][i] / (float) nsol);
        }
        for (int j = 0; j < nsol; j++) {
            solFit[j] = (1 - weiAS) * ndscoreSol[j] + weiAS * (tp_sol[1][j] / (float) ntp);
        }
    }

    public void advMOEval(final float[][] rawScore, final float[] MOScore) {
        float[][] dom = calDominance(rawScore);
        float[] domScore = mtxMargin(dom)[0];
        float[] ndScore = normalizeMax(domScore);
        for (int i = 0; i < MOScore.length; i++) {
            MOScore[i] = ndScore[i];
        }
    }

    public void printInteraction() {
        for (int j = 0; j < nsol; j++) {
            System.out.print("sol " + j + ": ");
            for (int i = 0; i < ntp; i++) {
                System.out.print(interaction[i][j] + ", ");
            }
            System.out.print("\n");
        }
    }

    public void printArray(float[] a) {
        int n = a.length;
        for (int j = 0; j < n; j++) {
            System.out.print(a[j] + ", ");
        }
        System.out.print("\n");
    }

    public float[][] transposeMatrix(float[][] a) {
        int n = a.length;
        int m = a[0].length;
        float[][] b = new float[m][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                b[j][i] = a[i][j];
            }
        }
        return (b);
    }

    public float zeroCount(float[] a) {
        int n = a.length;
        int nz = 0;
        for (int j = 0; j < n; j++) {
            nz += (a[j] == 0 ? 1 : 0);
        }
        return ((float) nz / (float) n);
    }

    public float[][] mtxMargin(final float[][] mtx) {
        int nrow = mtx.length;
        int ncol = mtx[0].length;
        float[] margCol = new float[ncol];
        float[] margRow = new float[nrow];
        for (int j = 0; j < ncol; j++) {
            for (int i = 0; i < nrow; i++) {
                margCol[j] += mtx[i][j];
            }
        }
        for (int i = 0; i < nrow; i++) {
            for (int j = 0; j < ncol; j++) {
                margRow[i] += mtx[i][j];
            }
        }
        float[][] margin = new float[2][];
        margin[0] = margRow;
        margin[1] = margCol;
        return (margin);
    }

    public float[][] calDominance(final float[][] rawScore) {
        int nInd = rawScore.length;
        int nObj = rawScore[0].length;
        float[][] dom = new float[nInd][nInd];
        for (int i = 0; i < nInd; i++) {
            for (int j = (i + 1); j < nInd; j++) {
                dom[i][j] = (float) checkDominance(rawScore[i], rawScore[j]);
                if (dom[i][j] == 1) {
                    dom[j][i] = -1;
                    dom[i][j] = 0;
                }
            }
        }
        return (dom);
    }

    public int checkDominance(final float[] a1, final float[] a2) {
        boolean A1DomA2 = ((a1[0] - a2[0]) > 0);
        boolean A2DomA1 = ((a1[0] - a2[0]) < 0);
        for (int i = 1; i < a1.length; i++) {
            if (A1DomA2) {
                if ((a1[i] - a2[i]) < 0) {
                    A1DomA2 = false;
                    break;
                }
            } else if (A2DomA1) {
                if ((a1[i] - a2[i]) > 0) {
                    A2DomA1 = false;
                    break;
                }
            } else {
                A1DomA2 = ((a1[i] - a2[i]) > 0);
                A2DomA1 = ((a1[i] - a2[i]) < 0);
            }
        }
        if (A1DomA2) return (1); else if (A2DomA1) return (-1); else return (0);
    }

    public float[][][] calTPDistinction() {
        float[][][] dist = new float[ntp][nsol][nsol];
        for (int i = 0; i < ntp; i++) {
            for (int j = 2; j < nsol; j++) {
                for (int k = 0; k < nsol; k++) {
                    dist[i][j][k] = ((interaction[i][j] > interaction[i][k]) ? 1 : 0);
                }
            }
        }
        return (dist);
    }

    public float[][][] calSolDistinction() {
        float[][][] dist = new float[nsol][ntp][ntp];
        for (int i = 0; i < nsol; i++) {
            for (int j = 2; j < ntp; j++) {
                for (int k = 0; k < ntp; k++) {
                    dist[i][j][k] = ((interaction[j][i] > interaction[k][i]) ? 1 : 0);
                }
            }
        }
        return (dist);
    }

    public float sumArray(final float[] a1) {
        float sum = 0;
        for (int i = 0; i < a1.length; i++) {
            sum += a1[i];
        }
        return (sum);
    }

    public float[] normalizeMax(final float[] a1) {
        float maxA = -1.0e15F;
        float minA = 1.0e15F;
        float[] a2 = new float[a1.length];
        for (int i = 0; i < a1.length; i++) {
            maxA = Math.max(maxA, a1[i]);
            minA = Math.min(minA, a1[i]);
        }
        float distA = (((maxA - minA) == 0) ? 1 : (maxA - minA));
        for (int i = 0; i < a2.length; i++) {
            a2[i] = (a1[i] - minA) / distA;
        }
        return (a2);
    }

    public float[] normalizeSumToOne(final float[] a1) {
        float sum = sumArray(a1);
        float[] a2 = new float[a1.length];
        for (int i = 0; i < a2.length; i++) {
            a2[i] = a1[i] / sum;
        }
        return (a2);
    }
}
