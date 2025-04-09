package org.jquantlib.model.volatility;

import java.util.Iterator;
import org.jquantlib.lang.exceptions.LibraryException;
import org.jquantlib.math.IntervalPrice;
import org.jquantlib.time.Date;
import org.jquantlib.time.TimeSeries;

/**
 * This template factors out common functionality found in classes which rely on the difference between the previous day's close
 * price and today's open price.
 *
 * @author Anand Mani
 * @author Richard Gomes
 */
public class GarmanKlassOpenClose<T extends GarmanKlassAbstract> implements LocalVolatilityEstimator<IntervalPrice> {

    private final double f;

    private final double a;

    private T delegate;

    private final Class<? extends GarmanKlassAbstract> classT;

    @SuppressWarnings("unchecked")
    public GarmanKlassOpenClose(final Class<? extends GarmanKlassAbstract> classT, final double y, final double marketOpenFraction, final double a) {
        this.classT = classT;
        this.delegate = null;
        try {
            delegate = (T) classT.getConstructor(double.class).newInstance(y);
        } catch (final Exception e) {
            throw new LibraryException(e);
        }
        this.f = marketOpenFraction;
        this.a = a;
    }

    @Override
    public TimeSeries<Double> calculate(final TimeSeries<IntervalPrice> quotes) {
        final TimeSeries<Double> retval = new TimeSeries<Double>(Double.class);
        final Iterator<Date> it = quotes.navigableKeySet().iterator();
        Date date = it.next();
        IntervalPrice prev = quotes.get(date);
        while (it.hasNext()) {
            date = it.next();
            final IntervalPrice curr = quotes.get(date);
            final double c0 = Math.log(prev.close());
            final double o1 = Math.log(curr.open());
            final double sigma2 = this.a * (o1 - c0) * (o1 - c0) / this.f + (1 - this.a) * delegate.calculatePoint(curr) / (1 - this.f);
            retval.put(date, Math.sqrt(sigma2 / delegate.getYearFraction()));
            prev = curr;
        }
        return retval;
    }
}
