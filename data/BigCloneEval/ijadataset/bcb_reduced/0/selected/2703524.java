package net.emotivecloud.vrmm.vtm.resource;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.LinkedList;

public class ResourceCalculator {

    private String resource;

    private LinkedList<Double> history;

    private double required;

    private double calculated;

    private final int MAX_MEASURES = 1000;

    private final int STARTUP = 10;

    private int decreasing;

    public static void main(String[] args) {
        try {
            ResourceCalculator rc = new ResourceCalculator("cpu", 50);
            BufferedReader in = new BufferedReader(new FileReader("input"));
            PrintStream out = new PrintStream(new FileOutputStream("output"));
            String line;
            while ((line = in.readLine()) != null) {
                String[] colummns = line.split(" ");
                int time = Integer.parseInt(colummns[0]);
                double resource = Double.parseDouble(colummns[1]);
                rc.addValue(resource);
                out.println(time + " " + rc.getMean(10) + " " + rc.getMean(60) + " " + rc.getMean() + " " + rc.getCalculated());
            }
            Runtime.getRuntime().exec("gnuplot plotter");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResourceCalculator(String resource, int required) {
        this.setResource(resource);
        this.required = required;
        this.calculated = required;
        this.decreasing = 0;
        history = new LinkedList<Double>();
    }

    /**
	 * Add a new value to monitorize
	 * @param value
	 */
    public synchronized void addValue(double value) {
        history.addFirst(value);
        while (history.size() > MAX_MEASURES) history.removeLast();
    }

    /**
	 * Return the last measured value.
	 * @return Last measurement.
	 */
    public synchronized double getCurrent() {
        return history.getFirst();
    }

    /**
	 * Gets the estimation of the amount of resources that it would need to execute
	 * @return
	 */
    public synchronized double getCalculated() {
        if (history.size() > STARTUP) {
            double last = history.getFirst();
            double mean1 = this.getMean(5);
            double mean2 = this.getMean(60);
            double mean3 = this.getMean();
            if (last > 1.1 * calculated) {
                calculated = Math.max(mean1, calculated);
                decreasing = 0;
            } else if (last > calculated) {
                calculated = last;
                decreasing = 0;
            } else if (calculated > mean1) {
                decreasing += Math.round(0.5 + calculated / mean1);
            }
            if (decreasing > 200) {
                double aux = Math.max(Math.max(mean1, mean2), mean3);
                if (required > aux) aux = (required + aux) / 2;
                calculated = (calculated + aux) / 2;
                decreasing /= 2;
            }
        } else {
            calculated = Math.max(this.getMean(5), required);
        }
        return calculated;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource.toLowerCase();
    }

    /**
	 * Calculate the mean of the values during the a certain time.
	 * @param during
	 * @return
	 */
    private double getMean(int during) {
        return getMean(0, during);
    }

    /**
	 * Calculate the mean from a certain moment during a time.
	 * @param from
	 * @param during
	 * @return
	 */
    private double getMean(int from, int during) {
        double mean = 0;
        if (history.size() < during) mean = calculated; else {
            for (int i = 0; i < during; i++) mean += history.get(i + from);
            mean = mean / during;
        }
        return mean;
    }

    /**
	 * Calculate the mean of the values during the time that has been executed.
	 * @param during
	 * @return
	 */
    private double getMean() {
        double mean = 0;
        for (double f : history) mean += f;
        return mean / history.size();
    }
}
