package org.jquantlib.methods.finitedifferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jquantlib.lang.exceptions.LibraryException;
import org.jquantlib.math.matrixutilities.Array;

/**
 * @author Srinivas Hasti
 *
 */
public class FiniteDifferenceModel<S extends Operator, T extends MixedScheme<S>> {

    private final T evolver;

    private final List<Double> stoppingTimes;

    private final Class<? extends Operator> classS;

    private final Class<? extends MixedScheme> classT;

    public FiniteDifferenceModel(final Class<? extends Operator> classS, final Class<? extends MixedScheme> classT, final S L, final List<BoundaryCondition<S>> bcs, final List<Double> stoppingTimes) {
        this.classS = classS;
        this.classT = classT;
        this.evolver = getEvolver(L, bcs);
        final Set<Double> times = new HashSet<Double>(stoppingTimes);
        this.stoppingTimes = new ArrayList<Double>(times);
        Collections.sort(stoppingTimes);
    }

    public FiniteDifferenceModel(final Class<? extends Operator> classS, final Class<? extends MixedScheme> classT, final S L, final List<BoundaryCondition<S>> bcs) {
        this(classS, classT, L, bcs, new ArrayList<Double>());
    }

    public FiniteDifferenceModel(final Class<? extends Operator> classS, final Class<? extends MixedScheme> classT, final T evolver, final List<Double> stoppingTimes) {
        this.classS = classS;
        this.classT = classT;
        this.evolver = evolver;
        final Set<Double> times = new HashSet<Double>(stoppingTimes);
        this.stoppingTimes = new ArrayList<Double>(times);
        Collections.sort(stoppingTimes);
    }

    public T getEvolver() {
        return evolver;
    }

    public Array rollback(final Array a, final double from, final double to, final int steps) {
        return rollbackImpl(a, from, to, steps, null);
    }

    public Array rollback(final Array a, final double from, final double to, final int steps, final StepCondition<Array> condition) {
        return rollbackImpl(a, from, to, steps, condition);
    }

    private Array rollbackImpl(Array a, final double from, final double to, final int steps, final StepCondition<Array> condition) {
        if (from <= to) throw new IllegalStateException("trying to roll back from " + from + " to " + to);
        final double dt = (from - to) / steps;
        double t = from;
        evolver.setStep(dt);
        for (int i = 0; i < steps; ++i, t -= dt) {
            double now = t;
            final double next = t - dt;
            boolean hit = false;
            for (int j = stoppingTimes.size() - 1; j >= 0; --j) if (next <= stoppingTimes.get(j) && stoppingTimes.get(j) < now) {
                hit = true;
                evolver.setStep(now - stoppingTimes.get(j));
                a = evolver.step(a, now);
                if (condition != null) {
                    condition.applyTo(a, stoppingTimes.get(j));
                }
                now = stoppingTimes.get(j);
            }
            if (hit) {
                if (now > next) {
                    evolver.setStep(now - next);
                    a = evolver.step(a, now);
                    if (condition != null) {
                        condition.applyTo(a, next);
                    }
                }
                evolver.setStep(dt);
            } else {
                a = evolver.step(a, now);
                if (condition != null) {
                    condition.applyTo(a, next);
                }
            }
        }
        return a;
    }

    protected T getEvolver(final S l, final List<BoundaryCondition<S>> bcs) {
        try {
            return (T) classT.getConstructor(Operator.class, List.class).newInstance(l, bcs);
        } catch (final Exception e) {
            throw new LibraryException(e);
        }
    }
}
