package ugliML.learning.quasinewton;

import optim.util.MathUtils;
import Jama.Matrix;

/**
 *
 * @author fhho
 *
 */
public abstract class AbstractQNAlgorithm implements QuasiNewtonAlgorithm {

    private QuasiNewtonProblem problem;

    private int dim;

    public double[] calcMinimum(QuasiNewtonProblem problem) {
        this.problem = problem;
        dim = problem.getDimension();
        Matrix invHes = Matrix.identity(dim, dim);
        Matrix oldX = new Matrix(problem.initValues(), dim);
        Matrix gradAtOldX = new Matrix(problem.getGradientAt(oldX.getColumnPackedCopy()), dim);
        double deltaXNorm = 1000;
        int i = 1;
        while (deltaXNorm > 0.0000001) {
            System.out.print("iteration:" + (i++) + " ");
            Matrix minusdx = invHes.times(gradAtOldX).times(-1.0);
            double alpha = calcAlpha(oldX, minusdx, gradAtOldX);
            System.out.print("alpha:" + alpha + " ");
            Matrix deltaX = minusdx.times(alpha);
            Matrix newX = oldX.plus(deltaX);
            Matrix gradAtNewX = new Matrix(problem.getGradientAt(newX.getColumnPackedCopy()), dim);
            Matrix y = gradAtNewX.minus(gradAtOldX);
            invHes = calcNextInvHessian(deltaX, y, invHes);
            for (int j = 0; j < dim; j++) {
                System.out.print(newX.get(j, 0) + " ");
            }
            System.out.println();
            oldX = newX;
            gradAtOldX = gradAtNewX;
            deltaXNorm = deltaX.normF() / newX.normF();
        }
        return oldX.getColumnPackedCopy();
    }

    /**
	 * Calculates the next estimate of the Hessian matrix (inversed)
	 * This depends on the type of Quasi-Newton
	 * 
	 * @param deltaX
	 * @param y
	 * @param invHes
	 * @return
	 */
    protected abstract Matrix calcNextInvHessian(Matrix deltaX, Matrix y, Matrix invHes);

    /**
	 * 
	 * Uses line-search to find step length satisfying Wolfe Conditions
	 * 
	 * @param x current position
	 * @param gradAtX gradient at x
	 * @param dx direction of descent
	 * @return the alpha that satisfies Wolfe Condition
	 */
    private double calcAlpha(Matrix x, Matrix dx, Matrix gradAtX) {
        double c1 = 0.0001;
        double c2 = 0.9;
        double dPhiAtZero = MathUtils.dotProduct(gradAtX.getColumnPackedCopy(), dx.getColumnPackedCopy());
        double phiAtZero = problem.getFunctionValAt(x.getColumnPackedCopy());
        double oldAlpha = 0;
        double newAlpha = 1;
        double maxAlpha = 2000;
        double incr = 1.1;
        double lowAlpha;
        double highAlpha;
        double phiAtLowAlpha;
        for (int i = 1; ; i++) {
            double phiAtOldAlpha = problem.getFunctionValAt(x.plus(dx.times(oldAlpha)).getColumnPackedCopy());
            double phiAtNewAlpha = problem.getFunctionValAt(x.plus(dx.times(newAlpha)).getColumnPackedCopy());
            if (phiAtNewAlpha > phiAtZero + c1 * newAlpha * dPhiAtZero || (i > 1 && phiAtNewAlpha >= phiAtOldAlpha)) {
                lowAlpha = oldAlpha;
                highAlpha = newAlpha;
                phiAtLowAlpha = phiAtOldAlpha;
                break;
            }
            double dPhiAtNewAlpha = MathUtils.dotProduct(problem.getGradientAt(x.plus(dx.times(newAlpha)).getColumnPackedCopy()), dx.getColumnPackedCopy());
            if (Math.abs(dPhiAtNewAlpha) <= -1.0 * c2 * dPhiAtZero) {
                return newAlpha;
            }
            if (dPhiAtNewAlpha >= 0) {
                lowAlpha = newAlpha;
                highAlpha = maxAlpha;
                phiAtLowAlpha = phiAtNewAlpha;
            }
            oldAlpha = newAlpha;
            newAlpha = oldAlpha * incr;
        }
        for (int i = 1; ; i++) {
            double currAlpha = (lowAlpha + highAlpha) / 2;
            double phiAtCurrAlpha = problem.getFunctionValAt(x.plus(dx.times(currAlpha)).getColumnPackedCopy());
            if (phiAtCurrAlpha > phiAtZero + c1 * currAlpha * dPhiAtZero || phiAtCurrAlpha >= phiAtLowAlpha) {
                highAlpha = currAlpha;
            } else {
                double dPhiAtCurrAlpha = MathUtils.dotProduct(problem.getGradientAt(x.plus(dx.times(currAlpha)).getColumnPackedCopy()), dx.getColumnPackedCopy());
                if (Math.abs(dPhiAtCurrAlpha) <= -1.0 * c2 * dPhiAtZero) {
                    return currAlpha;
                }
                if (dPhiAtCurrAlpha * (highAlpha - lowAlpha) < 0) {
                    highAlpha = lowAlpha;
                }
                lowAlpha = currAlpha;
            }
            if (i == 40) {
                return currAlpha;
            }
        }
    }
}
