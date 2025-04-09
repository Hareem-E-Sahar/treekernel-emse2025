package eyetrackercalibrator.math;

import eyetrackercalibrator.framemanaging.InformationDatabase;

/**
 *
 * @author eeglab
 */
public class EstimateTrialMarking {

    private double lowGroup = 0d;

    private double middleGroup = 0d;

    private double highGroup = 0d;

    /**
     * 
     * @param informationDatabase Cannot be null
     */
    public EstimateTrialMarking(InformationDatabase informationDatabase) {
        this.informationDatabase = informationDatabase;
    }

    private InformationDatabase informationDatabase;

    /**
     * Return 2 mean value of two groups.  The estimation user k-Mean with 2 
     * kernal.  The first one starts at the smallest value in the database from
     * the given range (start to end inclusive).  The second one starts at the
     * average between the largest value and the smallest value.
     * @param start Starting position in the information database
     * @param end Ending position in the information database
     * @return return the center of two resulting kernal.  The result is sorted
     * in ascending order (low, high)
     */
    public double[] estimateGroup(int start, int end) {
        double temp;
        lowGroup = Math.abs(getDiff(start));
        highGroup = lowGroup;
        double oldLowGroup = lowGroup;
        double oldHighGroup = highGroup;
        for (int i = start + 1; i <= end; i++) {
            temp = Math.abs(getDiff(i));
            lowGroup = Math.min(lowGroup, temp);
            highGroup = Math.max(highGroup, temp);
        }
        highGroup = (lowGroup + highGroup) / 2;
        double changes;
        int round = 0;
        do {
            oldLowGroup = lowGroup;
            oldHighGroup = highGroup;
            double dLow, dHigh, dMid = 0;
            double sumLow = 0d;
            double sumHign = 0d;
            double totalLow = 0d;
            double totalMid = 0d;
            double totalHigh = 0d;
            for (int i = start; i <= end; i++) {
                temp = Math.abs(getDiff(i));
                dLow = Math.abs(temp - lowGroup);
                dHigh = Math.abs(temp - highGroup);
                if (dLow <= dHigh) {
                    sumLow += temp;
                    totalLow++;
                } else {
                    sumHign += temp;
                    totalHigh++;
                }
            }
            lowGroup = sumLow / totalLow;
            highGroup = sumHign / totalHigh;
            dLow = oldLowGroup - lowGroup;
            dHigh = oldHighGroup - highGroup;
            changes = Math.max(dLow * dLow, Math.max(dMid * dMid, dHigh * dHigh));
            round++;
        } while (changes > 0.001 && round < 10000);
        double[] result = new double[3];
        result[0] = lowGroup;
        result[2] = highGroup;
        return result;
    }

    /**
     * Return v(pos) - v(pos-1) 
     * @param pos
     * @return 0 if v(pos) or v(pos - 1) is not available. Otherwise returning
     * v(pos) - v(pos-1)
     */
    public double getDiff(int pos) {
        Double c = informationDatabase.getInfo(pos);
        Double p = informationDatabase.getInfo(pos - 1);
        if (c != null && p != null) {
            return c - p;
        } else {
            return 0d;
        }
    }

    /**
     * @param pos
     * @return true when v(pos) is closer to high group than low group mean
     */
    public boolean isHighGroup(int pos) {
        double v = Math.abs(getDiff(pos));
        return (Math.abs(v - lowGroup) > Math.abs(v - highGroup));
    }
}
