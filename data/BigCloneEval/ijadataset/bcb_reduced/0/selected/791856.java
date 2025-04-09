package ufpr.mestrado.ais.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.util.Distance;

public class optAINetDistance extends Distance {

    public optAINetDistance() {
    }

    /**
	 * Returns a matrix with distances between solutions in a
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            The <code>SolutionSet</code>.
	 * @return a matrix with distances.
	 */
    public double[][] distanceMatrix(SolutionSet solutionSet) {
        Solution solutionI, solutionJ;
        double[][] distance = new double[solutionSet.size()][solutionSet.size()];
        try {
            for (int i = 0; i < solutionSet.size(); i++) {
                distance[i][i] = 0.0;
                solutionI = solutionSet.get(i);
                for (int j = i + 1; j < solutionSet.size(); j++) {
                    solutionJ = solutionSet.get(j);
                    distance[i][j] = this.distanceBetweenSolutions(solutionI, solutionJ);
                    distance[j][i] = distance[i][j];
                }
            }
        } catch (Exception e) {
        }
        return distance;
    }
}
