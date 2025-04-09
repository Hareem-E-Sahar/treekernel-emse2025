package ufpr.mestrado.ais.util;

import jmetal.util.Distance;
import jmetal.util.JMException;
import ufpr.mestrado.ais.base.VISSolution;

/**
 * @author bertol
 * 
 */
public class VISDistance extends Distance {

    /**
	 * 
	 */
    public VISDistance() {
    }

    /**
	 * Returns a matrix with distances between solutions in a
	 * <code>AISSolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            The <code>AISSolutionSet</code>.
	 * @return a matrix with distances.
	 */
    public double[][] distanceMatrix(final AISNonDominatedSolutionList visSolutionSet) {
        VISSolution solutionI, solutionJ;
        final double[][] limits_ = visSolutionSet.getObjectiveBounds();
        double[][] distance = new double[visSolutionSet.size()][visSolutionSet.size()];
        for (int i = 0; i < visSolutionSet.size(); i++) {
            distance[i][i] = 0.0;
            solutionI = (VISSolution) visSolutionSet.get(i);
            for (int j = i + 1; j < visSolutionSet.size(); j++) {
                solutionJ = (VISSolution) visSolutionSet.get(j);
                distance[i][j] = distanceBetweenObjectives(solutionI, solutionJ, limits_);
                distance[j][i] = distance[i][j];
            }
        }
        return distance;
    }

    /**
	 * Returns the distance between two solutions in the search space.
	 * 
	 * @param solutionI
	 *            The first <code>VISSolution</code>.
	 * @param solutionJ
	 *            The second <code>VISSolution</code>.
	 * @return the distance between solutions.
	 * @throws JMException
	 */
    public double distanceBetweenObjectives(final VISSolution solutionI, final VISSolution solutionJ, final double[][] objectivesBounds) {
        double diff;
        double distance = 0.0;
        for (int nObj = 0; nObj < solutionI.numberOfObjectives(); nObj++) {
            double diffObj_ = objectivesBounds[nObj][1] - objectivesBounds[nObj][0];
            diff = (solutionI.getObjective(nObj) - objectivesBounds[nObj][0]) / diffObj_ - (solutionJ.getObjective(nObj) - objectivesBounds[nObj][0]) / diffObj_;
            distance += Math.pow(diff, 2.0);
        }
        return Math.sqrt(distance);
    }
}
