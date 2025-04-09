package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.exactmass;

import java.util.ArrayList;
import java.util.TreeSet;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.MassDetector;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.util.DataPointSorter;
import net.sf.mzmine.util.SortingDirection;
import net.sf.mzmine.util.SortingProperty;

public class ExactMassDetector implements MassDetector {

    /**
     * @see net.sf.mzmine.modules.peakpicking.threestep.massdetection.MassDetector#getMassValues(net.sf.mzmine.data.Scan)
     */
    public DataPoint[] getMassValues(Scan scan, ParameterSet parameters) {
        double noiseLevel = parameters.getParameter(ExactMassDetectorParameters.noiseLevel).getValue();
        TreeSet<ExactMzDataPoint> mzPeaks = new TreeSet<ExactMzDataPoint>(new DataPointSorter(SortingProperty.MZ, SortingDirection.Ascending));
        TreeSet<ExactMzDataPoint> candidatePeaks = new TreeSet<ExactMzDataPoint>(new DataPointSorter(SortingProperty.Intensity, SortingDirection.Descending));
        getLocalMaxima(scan, candidatePeaks, noiseLevel);
        while (candidatePeaks.size() > 0) {
            ExactMzDataPoint currentCandidate = candidatePeaks.first();
            double exactMz = calculateExactMass(currentCandidate);
            currentCandidate.setMZ(exactMz);
            mzPeaks.add(currentCandidate);
            candidatePeaks.remove(currentCandidate);
        }
        return mzPeaks.toArray(new ExactMzDataPoint[0]);
    }

    /**
     * This method gets all possible MzPeaks using local maximum criteria from
     * the current scan and return a tree set of MzPeaks sorted by intensity in
     * descending order.
     * 
     * @param scan
     * @return
     */
    private void getLocalMaxima(Scan scan, TreeSet<ExactMzDataPoint> candidatePeaks, double noiseLevel) {
        DataPoint[] scanDataPoints = scan.getDataPoints();
        if (scanDataPoints.length == 0) return;
        DataPoint localMaximum = scanDataPoints[0];
        ArrayList<DataPoint> rangeDataPoints = new ArrayList<DataPoint>();
        boolean ascending = true;
        for (int i = 0; i < scanDataPoints.length - 1; i++) {
            boolean nextIsBigger = scanDataPoints[i + 1].getIntensity() > scanDataPoints[i].getIntensity();
            boolean nextIsZero = scanDataPoints[i + 1].getIntensity() == 0;
            boolean currentIsZero = scanDataPoints[i].getIntensity() == 0;
            if (currentIsZero) {
                continue;
            }
            rangeDataPoints.add(scanDataPoints[i]);
            if (ascending && (!nextIsBigger)) {
                localMaximum = scanDataPoints[i];
                ascending = false;
                continue;
            }
            if ((!ascending) && (nextIsBigger || nextIsZero)) {
                if (localMaximum.getIntensity() > noiseLevel) {
                    DataPoint[] rawDataPoints = rangeDataPoints.toArray(new DataPoint[0]);
                    candidatePeaks.add(new ExactMzDataPoint(localMaximum, rawDataPoints));
                }
                ascending = true;
                rangeDataPoints.clear();
            }
        }
    }

    /**
     * This method calculates the exact mass of a peak using the FWHM concept
     * and linear equation (y = mx + b).
     * 
     * @param ExactMassDataPoint
     * @return double
     */
    private double calculateExactMass(ExactMzDataPoint currentCandidate) {
        double xRight = -1, xLeft = -1;
        double halfIntensity = currentCandidate.getIntensity() / 2;
        DataPoint[] rangeDataPoints = currentCandidate.getRawDataPoints();
        for (int i = 0; i < rangeDataPoints.length - 1; i++) {
            if ((rangeDataPoints[i].getIntensity() <= halfIntensity) && (rangeDataPoints[i].getMZ() < currentCandidate.getMZ()) && (rangeDataPoints[i + 1].getIntensity() >= halfIntensity)) {
                double leftY1 = rangeDataPoints[i].getIntensity();
                double leftX1 = rangeDataPoints[i].getMZ();
                double leftY2 = rangeDataPoints[i + 1].getIntensity();
                double leftX2 = rangeDataPoints[i + 1].getMZ();
                double mLeft = (leftY1 - leftY2) / (leftX1 - leftX2);
                xLeft = leftX1 + (((halfIntensity) - leftY1) / mLeft);
                continue;
            }
            if ((rangeDataPoints[i].getIntensity() >= halfIntensity) && (rangeDataPoints[i].getMZ() > currentCandidate.getMZ()) && (rangeDataPoints[i + 1].getIntensity() <= halfIntensity)) {
                double rightY1 = rangeDataPoints[i].getIntensity();
                double rightX1 = rangeDataPoints[i].getMZ();
                double rightY2 = rangeDataPoints[i + 1].getIntensity();
                double rightX2 = rangeDataPoints[i + 1].getMZ();
                double mRight = (rightY1 - rightY2) / (rightX1 - rightX2);
                xRight = rightX1 + (((halfIntensity) - rightY1) / mRight);
                break;
            }
        }
        if ((xRight == -1) || (xLeft == -1)) return currentCandidate.getMZ();
        double exactMass = (xLeft + xRight) / 2;
        return exactMass;
    }

    public String getName() {
        return "Exact mass";
    }

    @Override
    public Class<? extends ParameterSet> getParameterSetClass() {
        return ExactMassDetectorParameters.class;
    }
}
