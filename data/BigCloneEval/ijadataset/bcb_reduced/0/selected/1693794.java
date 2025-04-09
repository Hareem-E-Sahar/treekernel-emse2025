package org.systemsbiology.dbsearch;

import java.util.ArrayList;
import java.util.Collections;

public class PeakSearch {

    public static int peakBinarySearch(double lowMass, double highMass, ArrayList peakList) {
        int low = 0;
        int high = peakList.size() - 1;
        int middle = -1;
        Peak tmpPeak = null;
        while (low <= high) {
            middle = (low + high) / 2;
            tmpPeak = (Peak) peakList.get(middle);
            if (lowMass > tmpPeak.mass) low = middle + 1; else if (highMass < tmpPeak.mass) high = middle - 1; else if (tmpPeak.mass >= lowMass && tmpPeak.mass <= highMass) {
                return middle;
            }
        }
        return -1;
    }

    public static Peak selectPeak(double lowMass, double highMass, ArrayList peakList) {
        int peakIndex = peakBinarySearch(lowMass, highMass, peakList);
        if (peakIndex != -1) {
            Peak maxPeak = (Peak) peakList.get(peakIndex);
            int tmpIndex = peakIndex - 1;
            Peak tmpPeak = null;
            while (tmpIndex >= 0) {
                tmpPeak = (Peak) peakList.get(tmpIndex);
                if (tmpPeak.mass >= lowMass && tmpPeak.mass <= highMass) {
                    if (maxPeak.intensity < tmpPeak.intensity) maxPeak = tmpPeak;
                } else if (lowMass > tmpPeak.mass) break;
                tmpIndex--;
            }
            tmpIndex = peakIndex + 1;
            while (tmpIndex < peakList.size()) {
                tmpPeak = (Peak) peakList.get(tmpIndex);
                if (tmpPeak.mass >= lowMass && tmpPeak.mass <= highMass) {
                    if (maxPeak.intensity < tmpPeak.intensity) maxPeak = tmpPeak;
                } else if (highMass < tmpPeak.mass) break;
                tmpIndex++;
            }
            return maxPeak;
        } else return null;
    }
}
