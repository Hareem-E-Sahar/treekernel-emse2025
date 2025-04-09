package mil.army.usace.ehlschlaeger.rgik.core;

import java.util.Date;
import java.util.Random;
import mil.army.usace.ehlschlaeger.rgik.util.BubbleSort;

/**
 *  Copyright Charles R. Ehlschlaeger,
 *  work: 309-298-1841, fax: 309-298-3003,
 *	<http://faculty.wiu.edu/CR-Ehlschlaeger2/>
 *  This software is freely usable for research and educational purposes. Contact C. R. Ehlschlaeger
 *  for permission for other purposes.
 *  Use of this software requires appropriate citation in all published and unpublished documentation.
 */
public class NoiseSetLayered extends RGIS {

    private double[] temperatures, temperatureTimes;

    private Sensor[] sensors;

    private double[][] sensorDistances;

    private int[][] solutionEvents;

    private NoiseEvent[] possibleSolutions;

    private double[] solutionError;

    private double solveRunTime;

    public double getSolveRunTimeInSeconds() {
        return solveRunTime;
    }

    public int[][] getSolutionEvents() {
        return solutionEvents;
    }

    public double[] getSolutionError() {
        return solutionError;
    }

    public NoiseEvent[] getPossibleSolutions() {
        return possibleSolutions;
    }

    public double getTemperatureAtTime(double time) {
        double t = temperatures[0] * (temperatureTimes[1] - time) / (temperatureTimes[1] - temperatureTimes[0]) + temperatures[1] * (time - temperatureTimes[0]) / (temperatureTimes[1] - temperatureTimes[0]);
        return t;
    }

    public double getSpeedOfSoundAtTime(double time) {
        double t = getTemperatureAtTime(time);
        return (331.4 + 0.6 * t);
    }

    public NoiseSetLayered(Sensor[] sensors, double[] temperatures, double[] temperatureTimes) {
        super();
        assert sensors != null;
        assert temperatures != null;
        assert temperatureTimes != null;
        assert temperatureTimes.length == temperatures.length;
        this.sensors = sensors;
        this.temperatures = temperatures;
        this.temperatureTimes = temperatureTimes;
    }

    public int solve() {
        Date startTime = new Date();
        sensorDistances = new double[sensors.length][sensors.length];
        for (int s = 0; s < sensors.length; s++) {
            for (int t = s; t < sensors.length; t++) {
                sensorDistances[s][t] = sensors[s].getDistance(sensors[t]);
                sensorDistances[t][s] = sensorDistances[s][t];
            }
        }
        int solutionCount = sensors[0].getNumberEvents() * sensors[1].getNumberEvents() * sensors[2].getNumberEvents();
        solutionError = new double[solutionCount];
        possibleSolutions = new NoiseEvent[solutionCount];
        solutionEvents = new int[solutionCount][];
        int[] order = new int[solutionCount];
        int[] triplet = new int[3];
        for (int i = 0; i < 3; i++) {
            triplet[i] = 0;
        }
        int solutions = 0;
        while (triplet[0] < sensors[0].getNumberEvents()) {
            int numSensors = 3;
            double[] x = new double[numSensors];
            double[] y = new double[numSensors];
            double[] t = new double[numSensors];
            for (int s = 0; s < 3; s++) {
                x[s] = sensors[s].getX();
                y[s] = sensors[s].getY();
                t[s] = sensors[s].getEvent(triplet[s]);
            }
            NoiseEvent ne = new NoiseEvent(x, y, t, getTemperatureAtTime(t[0]));
            boolean possibleNE = ne.possibleNoiseByDistance();
            double solFitness = Double.POSITIVE_INFINITY;
            if (possibleNE) {
                ne.solve();
                solFitness = ne.getSolutionFitness();
                int[] tripletCopy = new int[triplet.length];
                for (int i = 0; i < triplet.length; i++) {
                    tripletCopy[i] = triplet[i];
                }
                solutionEvents[solutions] = tripletCopy;
                possibleSolutions[solutions] = ne;
                solutionError[solutions] = solFitness;
                order[solutions] = solutions;
                solutions++;
            }
            nextPossibleNoise(triplet);
        }
        double[] se = new double[solutions];
        for (int s = 0; s < solutions; s++) {
            se[s] = solutionError[s];
        }
        solutionError = null;
        solutionError = se;
        int[] o = new int[solutions];
        for (int s = 0; s < solutions; s++) {
            o[s] = order[s];
        }
        order = null;
        order = o;
        int[][] sev = new int[solutions][];
        for (int s = 0; s < solutions; s++) {
            sev[s] = solutionEvents[s];
        }
        solutionEvents = null;
        solutionEvents = sev;
        NoiseEvent[] ne = new NoiseEvent[solutions];
        for (int s = 0; s < solutions; s++) {
            ne[s] = possibleSolutions[s];
        }
        possibleSolutions = null;
        possibleSolutions = ne;
        BubbleSort bs = new BubbleSort(false);
        bs.sort(order, solutionError);
        for (int ps = 0; ps < solutions; ps++) {
            for (int numSensors = 4; numSensors <= sensors.length; numSensors++) {
                double[] x = new double[numSensors];
                double[] y = new double[numSensors];
                double[] t = new double[numSensors];
                for (int s = 0; s < solutionEvents[ps].length; s++) {
                    x[s] = sensors[s].getX();
                    y[s] = sensors[s].getY();
                    t[s] = sensors[s].getEvent(solutionEvents[ps][s]);
                }
                double bestSolFitness = Double.POSITIVE_INFINITY;
                int bestSolEvent = -1;
                for (int lastEvent = 0; lastEvent < sensors[numSensors - 1].getNumberEvents(); lastEvent++) {
                    x[numSensors - 1] = sensors[numSensors - 1].getX();
                    y[numSensors - 1] = sensors[numSensors - 1].getY();
                    t[numSensors - 1] = sensors[numSensors - 1].getEvent(lastEvent);
                    NoiseEvent nne = new NoiseEvent(x, y, t, getTemperatureAtTime(t[0]));
                    boolean possibleNE = nne.possibleNoiseByDistance();
                    if (possibleNE) {
                        nne.solve();
                        double newFitness = nne.getSolutionFitness();
                        if (newFitness < bestSolFitness) {
                            bestSolEvent = lastEvent;
                            bestSolFitness = newFitness;
                        }
                    }
                }
                int[] newSE = new int[numSensors];
                for (int i = 0; i < numSensors - 1; i++) {
                    newSE[i] = solutionEvents[ps][i];
                }
                newSE[numSensors - 1] = bestSolEvent;
                solutionEvents[ps] = newSE;
                solutionError[ps] = bestSolFitness;
                if (bestSolEvent == -1) {
                    numSensors = sensors.length;
                }
            }
        }
        bs.sort(order, solutionError);
        double[] orderedSolutionError = new double[solutions];
        NoiseEvent[] orderedPossibleSolutions = new NoiseEvent[solutions];
        int[][] orderedSolutionEvents = new int[solutions][];
        for (int i = 0; i < solutions; i++) {
            orderedPossibleSolutions[i] = possibleSolutions[order[i]];
            orderedSolutionEvents[i] = solutionEvents[order[i]];
            orderedSolutionError[i] = solutionError[order[i]];
        }
        possibleSolutions = orderedPossibleSolutions;
        solutionEvents = orderedSolutionEvents;
        solutionError = orderedSolutionError;
        Date endTime = new Date();
        solveRunTime = (endTime.getTime() - startTime.getTime()) / 1000.0;
        return solutions;
    }

    private void nextPossibleNoise(int[] eventSet) {
        for (int i = eventSet.length - 1; i >= 1; i--) {
            eventSet[i] = eventSet[i] + 1;
            if (eventSet[i] < sensors[i].getNumberEvents()) {
                i = -1;
            } else {
                eventSet[i] = 0;
                if (i == 1) eventSet[0] = eventSet[0] + 1;
            }
        }
    }

    public static void main(String argv[]) {
        System.out.println("NoiseSetLayered.main START");
        for (int n = 2; n < 18; n++) {
            for (int sen = 4; sen <= 5; sen++) {
                double[] xs = { 100., 100., 10000., 10000., 5000., 2000. };
                double[] ys = { 100., 10000., 100., 10000., 5000., 2000. };
                Sensor[] s = new Sensor[sen];
                double[][] ts = new double[sen][n];
                double[] noiseX = new double[n];
                double[] noiseY = new double[n];
                double[] noiseTime = new double[n];
                double[] temps = { 8.0, 12.0 };
                double[] tempTime = { 0.0, 1000.0 };
                Random ran = new Random();
                for (int j = 0; j < n; j++) {
                    double nx = 30000.0 * ran.nextDouble();
                    noiseX[j] = nx;
                    double ny = 30000.0 * ran.nextDouble();
                    noiseY[j] = ny;
                    double nt = ran.nextDouble() * 30.;
                    noiseTime[j] = nt;
                    double temperatureCelcius = temps[0] * (tempTime[1] - nt) / (tempTime[1] - tempTime[0]) + temps[1] * (nt - tempTime[0]) / (tempTime[1] - tempTime[0]);
                    double speedSound = 331.4 + 0.6 * temperatureCelcius;
                    for (int i = 0; i < sen; i++) {
                        ts[i][j] = nt + Math.sqrt((xs[i] - nx) * (xs[i] - nx) + (ys[i] - ny) * (ys[i] - ny)) / speedSound;
                    }
                }
                for (int i = 0; i < sen; i++) {
                    s[i] = new Sensor(xs[i], ys[i], ts[i]);
                }
                NoiseSetLayered ns = new NoiseSetLayered(s, temps, tempTime);
                BubbleSort bs = new BubbleSort(false);
                int[] order = new int[n];
                for (int i = 0; i < n; i++) {
                    order[i] = i;
                }
                bs.sort(order, noiseTime);
                for (int j = 0; j < n; j++) {
                    System.out.println("Event," + noiseX[order[j]] + "," + noiseY[order[j]] + "," + noiseTime[order[j]]);
                }
                ns.solve();
                System.out.println("Time: " + ns.getSolveRunTimeInSeconds() + " seconds for [" + sen + "] sensors and [" + n + "] noise events");
                System.out.println("*********************************************************************");
            }
        }
    }
}
