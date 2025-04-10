package org.spantus.core.threshold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Mindaugas Greibus
 * 
 * @since 0.0.1
 * Created Apr 8, 2009
 *
 */
public abstract class Histogram {

    public enum histogramEnum {

        min, max
    }

    ;

    /**
	 * 
	 * @param list
	 * @return
	 */
    public static List<List<Double>> calculateHistogram(List<Double> list) {
        int numberOfBins = log2(list.size()) + 1;
        Map<histogramEnum, Double> map = getMinAndMax(list);
        return calculateHistogram(list, map.get(histogramEnum.min), map.get(histogramEnum.max), numberOfBins);
    }

    /**
	 * 
	 * @param list
	 * @return
	 */
    public static Map<histogramEnum, Double> getMinAndMax(List<Double> list) {
        Map<histogramEnum, Double> map = new HashMap<histogramEnum, Double>(2);
        Double min = Double.MAX_VALUE;
        Double max = -Double.MAX_VALUE;
        for (Double float1 : list) {
            min = Math.min(min, float1);
            max = Math.max(max, float1);
        }
        map.put(histogramEnum.min, min);
        map.put(histogramEnum.max, max);
        return map;
    }

    /**
	 * 
	 * @param list
	 * @return
	 */
    public static Map<histogramEnum, Double> getMinAndMax(List<Double> list, Map<histogramEnum, Double> map) {
        Map<histogramEnum, Double> curr = getMinAndMax(list);
        curr.put(histogramEnum.max, Math.max(map.get(histogramEnum.max), curr.get(histogramEnum.max)));
        curr.put(histogramEnum.min, Math.min(map.get(histogramEnum.min), curr.get(histogramEnum.min)));
        return curr;
    }

    /**
	 * 
	 * @param list
	 * @param map
	 * @param numberOfBins
	 * @return
	 */
    public static List<List<Double>> calculateHistogram(List<Double> list, Map<histogramEnum, Double> map, int numberOfBins) {
        return calculateHistogram(list, map.get(histogramEnum.min), map.get(histogramEnum.max), numberOfBins);
    }

    /**
	 * 
	 * @param list
	 * @param min
	 * @param max
	 * @param numberOfBins
	 * @return
	 */
    public static List<List<Double>> calculateHistogram(List<Double> list, Double min, Double max, int numberOfBins) {
        Double step = (max - min) / numberOfBins;
        List<List<Double>> histogram = new ArrayList<List<Double>>(numberOfBins + 3);
        for (int i = 0; i < numberOfBins + 3; i++) {
            histogram.add(new LinkedList<Double>());
        }
        for (Double float1 : list) {
            Double indexFloat = (float1 - min) / step;
            int index = indexFloat.intValue();
            safeAdd(histogram, index, float1);
        }
        return histogram;
    }

    /**
	 * 
	 * @param histogram
	 * @return
	 */
    public static Double calculateAvgForFirstBin(List<List<Double>> histogram) {
        int histogramBin = new Double(histogram.size() * .05f).intValue();
        Double avg = null;
        for (Double float1 : histogram.get(histogramBin)) {
            avg = average(avg, float1);
        }
        return avg;
    }

    /**
	 * 
	 * @param histogram
	 * @return
	 */
    public static Double calculateAvg(LinkedList<Double> linkedList) {
        Double avg = null;
        for (Double float1 : linkedList) {
            avg = average(avg, float1);
        }
        return avg;
    }

    /**
	 * 
	 * @param avg
	 * @param float1
	 * @return
	 */
    public static Double average(Double avg, Double float1) {
        Double rtnAvg = avg;
        if (rtnAvg == null) {
            rtnAvg = float1;
        }
        rtnAvg = (rtnAvg + float1) / 2;
        return rtnAvg;
    }

    /**
	 * 
	 * @param list
	 * @param index
	 * @param f
	 */
    public static void safeAdd(List<List<Double>> list, int index, Double f) {
        list.get(index).add(f);
    }

    /**
	 * 
	 * @param d
	 * @return
	 */
    public static int log2(int d) {
        Double l = Math.log(d) / Math.log(2.0);
        return l.intValue();
    }
}
