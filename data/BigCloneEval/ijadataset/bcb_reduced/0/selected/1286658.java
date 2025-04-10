package jam.data.func;

import java.text.NumberFormat;

/**
 * A polynomial histogram calibration function of up to 4th order, where energy
 * is a polynomial in the channel minus some offset.
 */
public class PolynomialOffsetFunction extends AbstractCalibrationFunction {

    /**
	 * Creates a new <code>CalibrationFunction</code> object of the specified
	 * order of polynomial in (x-x0).
	 * 
	 * @param numberTerms
	 *            number of terms (including the constant term)
	 * @throws IllegalArgumentException
	 *             thrown if invalid <code>type</code> passed to constructor
	 */
    public PolynomialOffsetFunction(final int numberTerms) {
        super("Polynomial Offset", numberTerms);
        if (numberTerms < MAX_TERMS) {
            title = "E = a0+a1*(ch-x0)+a2*(ch-x0)^2+ ...";
            coeff = new double[numberTerms];
            labels = new String[numberTerms];
            labels[0] = "x0";
            for (int i = 0; i < numberTerms - 1; i++) {
                labels[i + 1] = "a(" + i + ")";
            }
        } else {
            throw new IllegalArgumentException("Number of terms greater than MAX_NUMBER_TERMS [PolynomialOffsetFunction]: " + numberTerms);
        }
    }

    /**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param channel
	 *            value at which to get calibration
	 * @return calibration value of the channel
	 */
    @Override
    public double getValue(final double channel) {
        double chanMult;
        double value = 0.0;
        chanMult = 1.0;
        for (int i = 0; i < coeff.length - 1; i++) {
            value = value + coeff[i + 1] * chanMult;
            chanMult = chanMult * (channel - coeff[0]);
        }
        return value;
    }

    @Override
    public double getChannel(final double energy) {
        return ((energy - coeff[0]) / coeff[1]);
    }

    /**
	 * do a fit of x y values
	 */
    @Override
    public void fit() {
    }

    @Override
    protected String updateFormula(final NumberFormat numFormat) {
        return "Polynomial Offset fit not yet implemented";
    }
}
