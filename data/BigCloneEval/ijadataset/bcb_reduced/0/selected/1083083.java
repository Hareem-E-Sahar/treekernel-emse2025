package org.databene.benerator.distribution.sequence;

import org.databene.benerator.GeneratorContext;
import org.databene.benerator.NonNullGenerator;
import org.databene.benerator.distribution.Distribution;
import org.databene.benerator.distribution.SequenceManager;
import org.databene.benerator.primitive.number.AbstractNonNullNumberGenerator;

/**
 * Long Generator that implements a 'randomWalk' Long Sequence.<br/>
 * <br/>
 * Created: 13.06.2006 07:36:45
 * @since 0.1
 * @author Volker Bergmann
 */
public class RandomWalkLongGenerator extends AbstractNonNullNumberGenerator<Long> {

    long minIncrement;

    long maxIncrement;

    Distribution incrementDistribution;

    private long initial;

    private long next;

    private NonNullGenerator<Long> incrementGenerator;

    public RandomWalkLongGenerator() {
        this(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public RandomWalkLongGenerator(long min, long max) {
        this(min, max, 1, 2);
    }

    public RandomWalkLongGenerator(long min, long max, long minIncrement, long maxIncrement) {
        this(min, max, 1, min, minIncrement, maxIncrement, SequenceManager.RANDOM_SEQUENCE);
    }

    public RandomWalkLongGenerator(long min, long max, long granularity, long initial, long minIncrement, long maxIncrement) {
        this(min, max, granularity, initial, minIncrement, maxIncrement, SequenceManager.RANDOM_SEQUENCE);
    }

    public RandomWalkLongGenerator(long min, long max, long granularity, long initial, long minIncrement, long maxIncrement, Distribution incrementDistribution) {
        super(Long.class, min, max, granularity);
        this.minIncrement = minIncrement;
        this.maxIncrement = maxIncrement;
        this.incrementDistribution = incrementDistribution;
        this.initial = initial;
    }

    public long getNext() {
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    @Override
    public void init(GeneratorContext context) {
        incrementGenerator = incrementDistribution.createNumberGenerator(Long.class, minIncrement, maxIncrement, granularity, false);
        if (minIncrement < 0 && maxIncrement <= 0) initial = max; else if (minIncrement >= 0 && maxIncrement > 0) initial = min; else initial = (min + max) / 2;
        next = initial;
        incrementGenerator.init(context);
        super.init(context);
    }

    @Override
    public synchronized Long generate() {
        assertInitialized();
        long value = next;
        next += incrementGenerator.generate();
        if (next > max) next = max; else if (next < min) next = min;
        return value;
    }

    @Override
    public synchronized void reset() {
        super.reset();
        next = initial;
    }

    @Override
    public synchronized void close() {
        super.close();
        next = initial;
    }
}
