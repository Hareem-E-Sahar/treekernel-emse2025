package org.matsim.population.algorithms;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.population.LegImpl;
import org.matsim.core.utils.misc.Time;

/**
 * Summarizes numbers of departures, arrivals and en-routes by activity type.
 *
 * @author meisterk
 *
 */
public class PersonAnalyseTimesByActivityType extends AbstractPersonAlgorithm {

    public enum Activities {

        h(0), w(1), l(2), s(3), e(4), all(5);

        private final int position;

        private Activities(final int position) {
            this.position = position;
        }

        public int getPosition() {
            return this.position;
        }
    }

    private static final int ALL_POS = Activities.valueOf("all").getPosition();

    private final int timeBinSize;

    private final int[][] numDeps;

    private final int[][] numArrs;

    private final int[][] numTraveling;

    public PersonAnalyseTimesByActivityType(final int timeBinSize) {
        super();
        this.timeBinSize = timeBinSize;
        this.numDeps = new int[Activities.values().length][0 * 3600 / timeBinSize];
        this.numArrs = new int[Activities.values().length][0 * 3600 / timeBinSize];
        this.numTraveling = new int[Activities.values().length][0 * 3600 / timeBinSize];
    }

    @Override
    public void run(final Person person) {
        this.analyseDepartures(person);
        this.analyseArrivals(person);
        this.analyseTraveling(person);
    }

    public int[][] getNumDeps() {
        return this.numDeps.clone();
    }

    private void analyseDepartures(final Person person) {
        Plan plan = person.getPlans().get(0);
        for (PlanElement pe : plan.getPlanElements()) {
            if (pe instanceof Activity) {
                Activity act = (Activity) pe;
                String actType = act.getType().substring(0, 1);
                double depTime = act.getEndTime();
                if (depTime != Time.UNDEFINED_TIME) {
                    int actIndex = Activities.valueOf(actType).getPosition();
                    int timeIndex = ((int) depTime) / this.timeBinSize;
                    int oldLength = this.numDeps[actIndex].length;
                    if (timeIndex >= oldLength) {
                        this.numDeps[actIndex] = (int[]) PersonAnalyseTimesByActivityType.resizeArray(this.numDeps[actIndex], timeIndex + 1);
                        for (int ii = oldLength; ii < this.numDeps[actIndex].length; ii++) {
                            this.numDeps[actIndex][ii] = 0;
                        }
                        System.out.println("new length of " + actType + ": " + this.numDeps[actIndex].length);
                    }
                    this.numDeps[actIndex][timeIndex]++;
                    if (this.numDeps[actIndex].length > this.numDeps[ALL_POS].length) {
                        oldLength = this.numDeps[ALL_POS].length;
                        this.numDeps[ALL_POS] = (int[]) PersonAnalyseTimesByActivityType.resizeArray(this.numDeps[ALL_POS], timeIndex + 1);
                        for (int ii = oldLength; ii < this.numDeps[ALL_POS].length; ii++) {
                            this.numDeps[ALL_POS][ii] = 0;
                        }
                        System.out.println("new length of " + actType + ": " + this.numDeps[actIndex].length);
                    }
                    this.numDeps[ALL_POS][timeIndex]++;
                }
            }
        }
    }

    private void analyseArrivals(final Person person) {
        Plan plan = person.getPlans().get(0);
        for (PlanElement pe : plan.getPlanElements()) {
            if (pe instanceof Activity) {
                Activity act = (Activity) pe;
                String actType = act.getType().substring(0, 1);
                double arrTime = act.getStartTime();
                if (arrTime != 0.0) {
                    int actIndex = Activities.valueOf(actType).getPosition();
                    int timeIndex = ((int) arrTime) / this.timeBinSize;
                    int oldLength = this.numArrs[actIndex].length;
                    if (timeIndex >= oldLength) {
                        this.numArrs[actIndex] = (int[]) PersonAnalyseTimesByActivityType.resizeArray(this.numArrs[actIndex], timeIndex + 1);
                        for (int ii = oldLength; ii < this.numArrs[actIndex].length; ii++) {
                            this.numArrs[actIndex][ii] = 0;
                        }
                        System.out.println("new length of " + actType + ": " + this.numArrs[actIndex].length);
                    }
                    this.numArrs[actIndex][timeIndex]++;
                    if (this.numArrs[actIndex].length > this.numArrs[ALL_POS].length) {
                        oldLength = this.numArrs[ALL_POS].length;
                        this.numArrs[ALL_POS] = (int[]) PersonAnalyseTimesByActivityType.resizeArray(this.numArrs[ALL_POS], timeIndex + 1);
                        for (int ii = oldLength; ii < this.numArrs[ALL_POS].length; ii++) {
                            this.numArrs[ALL_POS][ii] = 0;
                        }
                        System.out.println("new length of " + actType + ": " + this.numArrs[actIndex].length);
                    }
                    this.numArrs[ALL_POS][timeIndex]++;
                }
            }
        }
    }

    private void analyseTraveling(final Person person) {
        Plan plan = person.getPlans().get(0);
        double depTime = -1.0, arrTime = -1.0;
        int actIndex = -1;
        String actType = null;
        int oldLength;
        for (Object o : plan.getPlanElements()) {
            if (o instanceof Activity) {
                if (depTime != -1.0) {
                    actType = ((Activity) o).getType().substring(0, 1);
                    actIndex = Activities.valueOf(actType).getPosition();
                    int startTimeBinIndex = ((int) depTime) / this.timeBinSize;
                    int endTimeBinIndex = ((int) arrTime) / this.timeBinSize;
                    if (this.numTraveling[actIndex].length <= endTimeBinIndex) {
                        oldLength = this.numTraveling[actIndex].length;
                        this.numTraveling[actIndex] = (int[]) PersonAnalyseTimesByActivityType.resizeArray(this.numTraveling[actIndex], endTimeBinIndex + 1);
                        for (int ii = oldLength; ii < this.numTraveling[actIndex].length; ii++) {
                            this.numTraveling[actIndex][ii] = 0;
                        }
                        System.out.println("new length of " + actType + ": " + this.numTraveling[actIndex].length);
                    }
                    if (this.numTraveling[actIndex].length > this.numTraveling[ALL_POS].length) {
                        oldLength = this.numTraveling[ALL_POS].length;
                        this.numTraveling[ALL_POS] = (int[]) PersonAnalyseTimesByActivityType.resizeArray(this.numTraveling[ALL_POS], endTimeBinIndex + 1);
                        for (int ii = oldLength; ii < this.numTraveling[ALL_POS].length; ii++) {
                            this.numTraveling[ALL_POS][ii] = 0;
                        }
                        System.out.println("new length of all: " + this.numTraveling[ALL_POS].length);
                    }
                    for (int ii = startTimeBinIndex; ii <= endTimeBinIndex; ii++) {
                        this.numTraveling[actIndex][ii]++;
                        this.numTraveling[this.ALL_POS][ii]++;
                    }
                }
            } else if (o instanceof Leg) {
                depTime = ((Leg) o).getDepartureTime();
                if (o instanceof LegImpl && ((LegImpl) o).getArrivalTime() != Time.UNDEFINED_TIME) {
                    arrTime = ((LegImpl) o).getArrivalTime();
                } else {
                    arrTime = depTime + ((Leg) o).getTravelTime();
                }
            }
        }
    }

    private static Object resizeArray(final Object oldArray, final int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        return newArray;
    }

    public int[][] getNumArrs() {
        return this.numArrs.clone();
    }

    public int[][] getNumTraveling() {
        return this.numTraveling.clone();
    }
}
