package org.databene.benerator.distribution.sequence;

import org.databene.benerator.GeneratorContext;
import org.databene.benerator.primitive.number.AbstractNonNullNumberGenerator;

/**
 * Double Generator that implements a 'randomWalk' Double Sequence.<br/>
 * <br/>
 * Created: 13.06.2006 07:36:45
 * @author Volker Bergmann
 */
public class RandomWalkDoubleGenerator extends AbstractNonNullNumberGenerator<Double> {

    private double next;

    private RandomDoubleGenerator incrementGenerator;

    public RandomWalkDoubleGenerator() {
        this(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public RandomWalkDoubleGenerator(double min, double max) {
        this(min, max, 1, 1);
    }

    public RandomWalkDoubleGenerator(double min, double max, double minIncrement, double maxIncrement) {
        super(Double.class, min, max, 1.);
        incrementGenerator = new RandomDoubleGenerator(minIncrement, maxIncrement);
    }

    public RandomWalkDoubleGenerator(double min, double max, double granularity, double minIncrement, double maxIncrement) {
        super(Double.class, min, max, granularity);
        incrementGenerator = new RandomDoubleGenerator(minIncrement, maxIncrement, granularity);
    }

    public void setGranularity(double granularity) {
        super.setGranularity(granularity);
        incrementGenerator.setGranularity(granularity);
    }

    public double getNext() {
        return next;
    }

    public void setNext(double next) {
        this.next = next;
    }

    @Override
    public void init(GeneratorContext context) {
        assertNotInitialized();
        resetMembers();
        super.init(context);
    }

    @Override
    public synchronized Double generate() {
        assertInitialized();
        double value = next;
        next += incrementGenerator.generate();
        if (next > max) next = max; else if (next < min) next = min;
        return value;
    }

    @Override
    public synchronized void reset() {
        resetMembers();
    }

    private void resetMembers() {
        double minIncrement = incrementGenerator.getMin();
        double maxIncrement = incrementGenerator.getMax();
        if (minIncrement < 0 && maxIncrement <= 0) next = max; else if (minIncrement >= 0 && maxIncrement > 0) next = min; else next = (min + max) / 2;
    }
}
