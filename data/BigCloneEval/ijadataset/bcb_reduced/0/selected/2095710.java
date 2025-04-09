package net.java.dev.joode.util;

/**
 * Very general implementation of the RK4_STEP intergration algorithm.
 * This algorithm allows a state to be intergrated over time. A state is represented
 * by a vector of floating point values. The user must also provide a Differential function.
 * This differential function tells the algorithm what the rate of change of the state variables are
 * for a specified state, and point in time. This informaion is used to move the state forward in time.
 * This is very accurate.
 */
public class RK4 {

    public static float safteyFactor = .9f;

    /**
     * factor the stepsize can increase by maximally.
     * This is used to avoid massive changes in stepsize that can be
     * caused by novel numerical situation s
     * eg 10 (it can increase no more by current * 10
     */
    public static float maxIncreaseFactor = 5;

    /**
     * a lower limit on how much the stepsize can be reduced by.
     * eg .1
     */
    public static float maxDecreaseFactor = .2f;

    public static float minSubStepsize = .00001f;

    /**
     *
     * steps some ODE's by some time using the RK4_STEP algorithm. From time t_start to t_end (these values are passed to the evaluator)
     * zero garbage
     * @param initialStates the initial state of the system at time t_start
     * @param evaluator a function that provided the rates of change of the variables
     * @param t_start the arbitary value of the starting time (which is modified and passed to the differentiator)
     * @param t_end the final time
     * @param passback the intergrated state of the system at time t_end
     */
    public static void intergrate(Vector initialStates, Differential evaluator, float t_start, float t_end, Vector passback) {
        float h = t_end - t_start;
        Real initial = Real.pool.aquire(initialStates.size());
        Real in = Real.pool.aquire(initialStates.size());
        for (int i = 0; i < initialStates.size(); i++) {
            initial.set(i, initialStates.get(i));
        }
        Real k1 = Real.pool.aquire(initialStates.size());
        Real k2 = Real.pool.aquire(initialStates.size());
        Real k3 = Real.pool.aquire(initialStates.size());
        Real k4 = Real.pool.aquire(initialStates.size());
        in.set(initial);
        evaluator.evaluate(in, t_start, t_start, t_end, k1);
        k1.scale(h);
        in.set(k1);
        in.scale(.5f);
        in.add(initial);
        evaluator.evaluate(in, t_start + h / 2, t_start, t_end, k2);
        k2.scale(h);
        in.set(k2);
        in.scale(.5f);
        in.add(initial);
        evaluator.evaluate(in, t_start + h / 2, t_start, t_end, k3);
        k3.scale(h);
        in.set(k3);
        in.add(initial);
        evaluator.evaluate(in, t_end, t_start, t_end, k4);
        k4.scale(h);
        k1.scale(1 / 6f);
        k2.scale(1 / 3f);
        k3.scale(1 / 3f);
        k4.scale(1 / 6f);
        initial.add(k1);
        initial.add(k2);
        initial.add(k3);
        initial.add(k4);
        for (int i = 0; i < initialStates.size(); i++) {
            passback.set(i, initial.get(i));
        }
        Real.pool.release(k1);
        Real.pool.release(k2);
        Real.pool.release(k3);
        Real.pool.release(k4);
        Real.pool.release(initial);
        Real.pool.release(in);
    }

    /**
     * steps ODE's by some time using the RK4 algorithm, with adaptive stepsize. This implemntation
     * ensures that the desired accuracy is maintained.
     * This method is accurate, but is easy converges to doing the minimum step.
     * This is becuase orientation variables require too much accuracy
     * to ensure stability compared to linear variables. To get this routine working correctly
     * different measures of tolerable error will be needed, as well as whther error is
     * absolute or relative for each dimention of the state.
     * The method operates by dividing the error into a linear rate across the time interval.
     * The method then takes a single step at a time and adjust the stepsize to meet the required rate.
     *
     * @param initialState the initial state of the system at time t_start
     * @param evaluator a function that provided the rates of change of the variables
     * @param t_start the arbitary value of the starting time (which is modified and passed to the differentiator)
     * @param t_end the final time
     * @param subStepSize the initial substep size to use (though the implementation will deviate if necissary)
     * @param passback the intergrated state of the system at time t_end
     * @return the last substepsize used
     */
    public static float intergrateAapative(Vector initialState, Differential evaluator, float t_start, float t_end, float accuracy, float subStepSize, Vector passback) {
        float errorRate = accuracy / (t_end - t_start);
        Real initial = Real.pool.aquire(initialState.size());
        Real end1 = Real.pool.aquire(initialState.size());
        Real end2 = Real.pool.aquire(initialState.size());
        Real mid = Real.pool.aquire(initialState.size());
        initial.set(initialState);
        KahanSum t_sub_start = new KahanSum(t_start);
        while (t_sub_start.val() < t_end) {
            float t_sub_end = Math.min(t_end, t_sub_start.val() + subStepSize);
            float t_sub_mid = (t_sub_start.val() + t_sub_end) / 2;
            float h = subStepSize;
            float required_accuracy = errorRate * h;
            RK4.intergrate(initial, evaluator, t_sub_start.val(), t_sub_end, end1);
            RK4.intergrate(initial, evaluator, t_sub_start.val(), t_sub_mid, mid);
            RK4.intergrate(mid, evaluator, t_sub_mid, t_sub_end, end2);
            float error = 0;
            for (int i = 0; i < end1.size(); i++) {
                error = Math.max(Math.abs(end1.get(i) - end2.get(i)), error);
            }
            JOODELog.debug("t_sub_start = ", t_sub_start.val());
            JOODELog.debug("h = ", h);
            JOODELog.debug("error = ", error);
            JOODELog.debug("end1 = ", end1);
            JOODELog.debug("end2 = ", end2);
            if (error == 0) {
                subStepSize *= maxIncreaseFactor;
            } else {
                subStepSize = h * safteyFactor * (float) Math.pow((error / required_accuracy), -1f / 4);
            }
            subStepSize = Math.min(subStepSize, h * maxIncreaseFactor);
            subStepSize = Math.max(subStepSize, h * maxDecreaseFactor);
            subStepSize = Math.max(subStepSize, minSubStepsize);
            if (error > required_accuracy && h != minSubStepsize) {
                assert subStepSize < h;
            } else {
                t_sub_start.add(h);
                initial.set(end2);
            }
        }
        for (int i = 0; i < end2.size(); i++) {
            passback.set(i, initial.get(i));
        }
        Real.pool.release(initial);
        Real.pool.release(end1);
        Real.pool.release(end2);
        Real.pool.release(mid);
        return subStepSize;
    }

    /**
     * steps ODE's by some time using the RK4 algorithm, with adaptive stepsize. This implemntation
     * ensures that the desired accuracy is maintained.
     * Considerable improvement can be made to this method.
     * Currently this method attempts to step in one go, and compares results
     * against two half steps. Then it performs as many equally sized steps as necisarry to meet
     * required error. However, no reevaluation of error is performed, and no limits are placed
     * on how drastic the step size change can be. Dividing the total step into
     * n equally sized steps is also bad, as some parts of the step may need more attention than others.
     *
     * @param initialState the initial state of the system at time t_start
     * @param evaluator a function that provided the rates of change of the variables
     * @param t_start the arbitary value of the starting time (which is modified and passed to the differentiator)
     * @param t_end the final time
     * @param passback the intergrated state of the system at time t_end
     */
    public static void intergrateAapative(Vector initialState, Differential evaluator, float t_start, float t_end, float accuracy, Vector passback) {
        Real initial = Real.pool.aquire(initialState.size());
        Real end1 = Real.pool.aquire(initialState.size());
        Real end2 = Real.pool.aquire(initialState.size());
        Real mid = Real.pool.aquire(initialState.size());
        float t_mid = (t_start + t_end) / 2;
        initial.set(initialState);
        RK4.intergrate(initial, evaluator, t_start, t_end, end1);
        RK4.intergrate(initial, evaluator, t_start, t_mid, mid);
        RK4.intergrate(mid, evaluator, t_mid, t_end, end2);
        float error = 0;
        for (int i = 0; i < end1.size(); i++) {
            error = Math.max(Math.abs(end1.get(i) - end2.get(i)), error);
        }
        if (error < accuracy) {
            for (int i = 0; i < end2.size(); i++) {
                passback.set(i, end2.get(i));
            }
        } else {
            float stepsize = t_end - t_start;
            float maximumStepsize = stepsize * (float) Math.pow((error / accuracy), -1f / 4);
            int numberOfSteps = (int) Math.ceil(stepsize / maximumStepsize);
            float actualStepSize = stepsize / numberOfSteps;
            for (int i = 0; i < passback.size(); i++) {
                passback.set(i, initial.get(i));
            }
            float t_e = 0;
            float t_s = t_start;
            for (int i = 0; i < numberOfSteps; i++) {
                t_e = t_s + actualStepSize;
                RK4.intergrate(passback, evaluator, t_s, t_e, passback);
                t_s += actualStepSize;
                if (i / numberOfSteps == 2) {
                    mid.set(passback);
                }
            }
            if (!Real.epsilonEquals(t_e, t_end, .0001f)) {
                JOODELog.error("error, adaptive step did not correctly step to the end");
            }
        }
        Real.pool.release(initial);
        Real.pool.release(end1);
        Real.pool.release(end2);
        Real.pool.release(mid);
    }

    /**
     * Implementors of this interface provide rates of change for state variables, given the current state and the current time
     */
    public interface Differential {

        /**
         * calulates the gradient of a system of ODEs at a specific state, at a specific point in time
         * @param state the state
         * @param t the current time  t_start<=t<=t_end
         * @parame t_start the start time of the step
         * @param t_end the end time of the step
         * @param passback passback which is filled with gradient information
         */
        public void evaluate(Vector state, float t, float t_start, float t_end, Vector passback);

        /**
         * the size of the vectors this differentiator can work with
         * @return
         */
        public int size();
    }
}
