package exp.util;

import java.lang.Long;
import ec.*;
import ec.coevolve.*;
import ec.simple.*;
import ec.util.*;
import ec.gp.*;

/**
 *
 * @author T.S.Yo
 * @version 1.0 
 */
public class MyExpEvaluator2 extends Evaluator {

    public int ntp;

    public int nsol;

    protected float[][] interaction;

    public int STPflag;

    public static final String TPOINTS = "SelectTestPoints";

    public static final int TPOINT_FIXED_RANDOM = 1;

    public static final int TPOINT_NEW_RANDOM = 2;

    public static final int TPOINT_ALL = 3;

    public static final int TPOINT_COEVOLVUTION = 4;

    public int SCOflag;

    public static final int SCORE_AVERAGE = 1;

    public static final int SCORE_WEIGHTED = 2;

    public static final int SCORE_MOO = 3;

    public static final int SCORE_SIMILARITY = 4;

    public static final int SCORE_AVE_INFO = 5;

    public static final int SCORE_WEI_INFO = 6;

    public Individual[] iniTP;

    public float[] finalEval;

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        MyExpParam exppar = new MyExpParam();
        STPflag = exppar.readSTPflag(state, base);
        SCOflag = exppar.readSCOflag(state, base);
        Parameter tempSubpop = new Parameter(ec.Initializer.P_POP).push(ec.Population.P_SIZE);
        int numSubpopulations = state.parameters.getInt(tempSubpop, null, 0);
        if (numSubpopulations != 2) state.output.fatal("Parameter incorrect, number of subpopulations has to be 2.", tempSubpop);
        ntp = state.parameters.getInt(tempSubpop.pop().push("subpop.0").push("size"), null, 0);
        if (ntp < 0) state.output.fatal("Parameter not found, or it has an incorrect value.", tempSubpop.push("0").push("size"));
        nsol = state.parameters.getInt(tempSubpop.pop().push("subpop.1").push("size"), null, 0);
        if (nsol < 0) state.output.fatal("Parameter not found, or it has an incorrect value.", tempSubpop.push("1").push("size"));
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
        ntp = state.population.subpops[0].individuals.length;
        nsol = state.population.subpops[1].individuals.length;
        interaction = new float[ntp][nsol];
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
        switch(SCOflag) {
            case SCORE_AVERAGE:
                evalAveScore(state, population.subpops[0].individuals, population.subpops[1].individuals);
                break;
            case SCORE_WEIGHTED:
                evalWeiScore(state, population.subpops[0].individuals, population.subpops[1].individuals);
                break;
            case SCORE_MOO:
                evalMOOScore(state, population.subpops[0].individuals, population.subpops[1].individuals);
                break;
            case SCORE_SIMILARITY:
                evalDisScore(state, population.subpops[0].individuals, population.subpops[1].individuals);
                break;
            case SCORE_AVE_INFO:
                evalAveInfo(state, population.subpops[0].individuals, population.subpops[1].individuals);
                break;
            case SCORE_WEI_INFO:
                evalWeiInfo(state, population.subpops[0].individuals, population.subpops[1].individuals);
                break;
            default:
                System.out.println("Evaluation method not specified, use Ave-Score by default");
                evalAveScore(state, population.subpops[0].individuals, population.subpops[1].individuals);
        }
    }

    public void afterEvaluation(final EvolutionState state, final Population population, final GroupedProblemForm prob) {
    }

    public void evalAveScore(final EvolutionState state, final Individual[] testPoints, final Individual[] solutions) {
        float[][] tp_sol = mtxMargin(interaction);
        float[] tSize = new float[nsol];
        for (int j = 0; j < nsol; j++) {
            tSize[j] = (float) ((GPIndividual) solutions[j]).size();
        }
        float[] nTSize = normalizeMax(tSize);
        for (int i = 0; i < ntp; i++) {
            ((SimpleFitness) (testPoints[i].fitness)).setFitness(state, (1 - tp_sol[0][i] / (float) nsol), false);
        }
        for (int j = 0; j < nsol; j++) {
            ((SimpleFitness) (solutions[j].fitness)).setFitness(state, (tp_sol[1][j] / (float) ntp), false);
        }
    }

    public void evalWeiScore(final EvolutionState state, final Individual[] testPoints, final Individual[] solutions) {
        float[][] wt_tp_sol = mtxMargin(interaction);
        for (int i = 0; i < ntp; i++) {
            if (wt_tp_sol[0][i] == 0) wt_tp_sol[0][i] = 0F; else wt_tp_sol[0][i] = 1 / wt_tp_sol[0][i];
        }
        for (int j = 0; j < nsol; j++) {
            if (wt_tp_sol[1][j] == 0) wt_tp_sol[1][j] = 0F; else wt_tp_sol[1][j] = 1 / wt_tp_sol[1][j];
        }
        float[] tSize = new float[nsol];
        for (int j = 0; j < nsol; j++) {
            tSize[j] = (float) ((GPIndividual) solutions[j]).size();
        }
        float[] nTSize = normalizeMax(tSize);
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
        for (int i = 0; i < ntp; i++) {
            ((SimpleFitness) (testPoints[i].fitness)).setFitness(state, (1 - tpFitness[i]), false);
        }
        for (int j = 0; j < nsol; j++) {
            ((SimpleFitness) (solutions[j].fitness)).setFitness(state, solFitness[j], false);
        }
    }

    public void evalMOOScore(final EvolutionState state, final Individual[] testPoints, final Individual[] solutions) {
        float[][] domTP = calTPDominance();
        float[][] domSol = calSolDominance();
        float[] domScoreTP = mtxMargin(domTP)[0];
        float[] domScoreSol = mtxMargin(domSol)[0];
        float[] ndscoreTP = normalizeMax(domScoreTP);
        float[] ndscoreSol = normalizeMax(domScoreSol);
        float[] tSize = new float[nsol];
        for (int j = 0; j < nsol; j++) {
            tSize[j] = (float) ((GPIndividual) solutions[j]).size();
        }
        float[] nTSize = normalizeMax(tSize);
        for (int i = 0; i < ntp; i++) {
            ((SimpleFitness) (testPoints[i].fitness)).setFitness(state, (ndscoreTP[i]), false);
        }
        for (int j = 0; j < nsol; j++) {
            ((SimpleFitness) (solutions[j].fitness)).setFitness(state, (ndscoreSol[j]), false);
        }
    }

    public void evalDisScore(final EvolutionState state, final Individual[] testPoints, final Individual[] solutions) {
    }

    public void evalAveInfo(final EvolutionState state, final Individual[] testPoints, final Individual[] solutions) {
        float weiAS = 0.7F;
        float[][] tp_sol = mtxMargin(interaction);
        float[][][] distTP = calTPDistinction();
        float[][][] distSol = calSolDistinction();
        float[] tSize = new float[nsol];
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
            tSize[j] = (float) ((GPIndividual) solutions[j]).size();
        }
        float[] ndscoreTP = normalizeMax(dscoreTP);
        float[] ndscoreSol = normalizeMax(dscoreSol);
        float[] nTSize = normalizeMax(tSize);
        for (int i = 0; i < ntp; i++) {
            ((SimpleFitness) (testPoints[i].fitness)).setFitness(state, (1 - weiAS) * ndscoreTP[i] + weiAS * (1 - tp_sol[0][i] / (float) nsol), false);
        }
        for (int j = 0; j < nsol; j++) {
            ((SimpleFitness) (solutions[j].fitness)).setFitness(state, 0.0F * ndscoreSol[j] + 1.0F * (tp_sol[1][j] / (float) ntp), false);
        }
    }

    public void evalWeiInfo(final EvolutionState state, final Individual[] testPoints, final Individual[] solutions) {
        float weiAS = 0.9F;
        float[][] tp_sol = mtxMargin(interaction);
        float[][][] distTP = calTPDistinction();
        float[][][] distSol = calSolDistinction();
        float[] tSize = new float[nsol];
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
            tSize[j] = (float) ((GPIndividual) solutions[j]).size();
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
        float[] nTSize = normalizeMax(tSize);
        for (int i = 0; i < ntp; i++) {
            ((SimpleFitness) (testPoints[i].fitness)).setFitness(state, (1 - weiAS) * ndscoreTP[i] + weiAS * (1 - tp_sol[0][i] / (float) nsol), false);
        }
        for (int j = 0; j < nsol; j++) {
            ((SimpleFitness) (solutions[j].fitness)).setFitness(state, 0.0F * ndscoreSol[j] + 1.0F * (tp_sol[1][j] / (float) ntp), false);
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

    public float[][] calTPDominance() {
        float[][] dom = new float[ntp][ntp];
        for (int i = 0; i < ntp; i++) {
            for (int j = (i + 1); j < ntp; j++) {
                dom[i][j] = (float) checkDominance(interaction[i], interaction[j]);
                if (dom[i][j] == 1) {
                    dom[j][i] = -1;
                    dom[i][j] = 0;
                }
            }
        }
        return (dom);
    }

    public float[][] calSolDominance() {
        float[][] dom = new float[nsol][nsol];
        float[][] int2 = new float[nsol][ntp];
        for (int i = 0; i < nsol; i++) {
            for (int j = 0; j < ntp; j++) {
                int2[i][j] = interaction[j][i];
            }
        }
        for (int i = 0; i < nsol; i++) {
            for (int j = (i + 1); j < nsol; j++) {
                dom[i][j] = (float) checkDominance(int2[i], int2[j]);
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
        float maxA = -1000000F;
        float minA = 1000000F;
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
