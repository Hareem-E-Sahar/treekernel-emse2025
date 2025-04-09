package de.gebea.fsrcp.internal.accelerometer.analyser.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.osgi.service.prefs.Preferences;
import de.gebea.fsrcp.accelerometer.streamer.AccelerationVector;
import de.gebea.fsrcp.internal.accelerometer.analyser.Gesture;
import de.gebea.fsrcp.internal.accelerometer.analyser.IPreprocessor;

public class OverallPreprocessor implements IPreprocessor {

    private static final double THRESHOLD = 0.3;

    private Map<Integer, double[]> centers;

    private Map<Integer, Double> distribution;

    public void load(final Preferences node) {
        final String centersString = node.get("centers", "");
        final String distString = node.get("distribution", "");
        String[] entrys = centersString.split(">");
        centers = new HashMap<Integer, double[]>(entrys.length);
        if (centersString.length() != 0) {
            for (final String entryString : entrys) {
                final String[] entry = entryString.split(":");
                final String[] values = entry[1].split(";");
                final double[] a = new double[values.length];
                for (int i = 0; i < a.length; i++) {
                    a[i] = Double.parseDouble(values[i]);
                }
                centers.put(Integer.parseInt(entry[0]), a);
            }
        }
        entrys = distString.split(">");
        distribution = new HashMap<Integer, Double>(entrys.length);
        if (distString.length() != 0) {
            for (final String entryString : entrys) {
                final String[] entry = entryString.split(":");
                distribution.put(Integer.parseInt(entry[0]), Double.parseDouble(entry[1]));
            }
        }
    }

    public boolean prefilter(final List<Integer> gestureCenters) {
        final Map<Integer, Double> tmpDistr = calcDistribution(gestureCenters);
        for (final Map.Entry<Integer, Double> entry : distribution.entrySet()) {
            final Double x = tmpDistr.get(entry.getKey());
            final double diff = entry.getValue() > x ? entry.getValue() - x : x - entry.getValue();
            if (diff > THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> process(final Gesture gesture) {
        return assignToCenters(gesture);
    }

    public void save(final Preferences node) {
        StringBuffer sb = new StringBuffer();
        for (final Map.Entry<Integer, double[]> center : centers.entrySet()) {
            if (sb.length() > 0) {
                sb.append(">");
            }
            sb.append(center.getKey()).append(":");
            boolean first = true;
            for (final double value : center.getValue()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(";");
                }
                sb.append(value);
            }
        }
        node.put("centers", sb.toString());
        sb = new StringBuffer();
        for (final Entry<Integer, Double> dist : distribution.entrySet()) {
            if (sb.length() > 0) {
                sb.append(">");
            }
            sb.append(dist.getKey()).append(":").append(dist.getValue());
        }
        node.put("distribution", sb.toString());
    }

    @Override
    public String toString() {
        if (centers == null) {
            return "";
        }
        final StringBuffer sb = new StringBuffer("Centers: \n");
        for (int i = 0; i < centers.size(); i++) {
            sb.append(i).append(". : ");
            sb.append(Arrays.toString(centers.get(i))).append("\n");
        }
        sb.append("Distribution: \n");
        for (int i = 0; i < distribution.size(); i++) {
            sb.append(i).append(". : ");
            sb.append(distribution.get(i)).append("\n");
        }
        return sb.toString();
    }

    public List<List<Integer>> train(final List<Gesture> trainingGestures) {
        assert trainingGestures != null;
        assert !trainingGestures.isEmpty();
        final Gesture collectGesture = new Gesture(trainingGestures.size() * trainingGestures.get(0).size());
        for (final Gesture gesture : trainingGestures) {
            collectGesture.addAll(gesture);
        }
        initCenters(collectGesture);
        List<Integer> assignedCenters = null;
        List<Integer> assignedCenters2 = null;
        do {
            assignedCenters2 = assignedCenters;
            assignedCenters = assignToCenters(collectGesture);
            final Map<Integer, double[]> newCenters = new HashMap<Integer, double[]>(14);
            for (int i = 0; i < assignedCenters.size(); i++) {
                final Integer center = assignedCenters.get(i);
                final AccelerationVector vector = collectGesture.get(i);
                double[] newCenter = newCenters.get(center);
                if (newCenter == null) {
                    newCenter = new double[] { 0, 0, 0, 0 };
                }
                newCenter[0] += vector.getX();
                newCenter[1] += vector.getY();
                newCenter[2] += vector.getZ();
                newCenter[3]++;
                newCenters.put(center, newCenter);
            }
            for (final Map.Entry<Integer, double[]> entry : newCenters.entrySet()) {
                final double[] value = entry.getValue();
                centers.put(entry.getKey(), new double[] { value[0] / value[3], value[1] / value[3], value[2] / value[3] });
            }
        } while (!assignedCenters.equals(assignedCenters2));
        distribution = calcDistribution(assignedCenters);
        final List<List<Integer>> ret = new ArrayList<List<Integer>>(trainingGestures.size());
        for (final Gesture gesture : trainingGestures) {
            ret.add(assignToCenters(gesture));
        }
        return ret;
    }

    private List<Integer> assignToCenters(final Gesture gesture) {
        final List<Integer> assignedCenters = new ArrayList<Integer>(gesture.size());
        double x, y, z, dx, dy, dz;
        double[] centerV;
        for (final AccelerationVector vector : gesture) {
            double d2, d = Double.MAX_VALUE;
            x = vector.getX();
            y = vector.getY();
            z = vector.getZ();
            int assignedCenter = -1;
            for (final Map.Entry<Integer, double[]> center : centers.entrySet()) {
                centerV = center.getValue();
                dx = centerV[0] - x;
                dy = centerV[1] - y;
                dz = centerV[2] - z;
                d2 = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
                if (d2 < d) {
                    d = d2;
                    assignedCenter = center.getKey();
                }
            }
            if (assignedCenter != -1) {
                assignedCenters.add(assignedCenter);
            }
        }
        return assignedCenters;
    }

    private Map<Integer, Double> calcDistribution(final List<Integer> centers) {
        final int[] sums = new int[14];
        Arrays.fill(sums, 0);
        for (final Integer center : centers) {
            sums[center]++;
        }
        final Map<Integer, Double> distribution = new HashMap<Integer, Double>(14);
        for (int i = 0; i < sums.length; i++) {
            distribution.put(i, 1d * sums[i] / centers.size());
        }
        return distribution;
    }

    private void initCenters(final Gesture collectGesture) {
        final int min = collectGesture.getMinValue();
        final int max = collectGesture.getMaxValue();
        final int r = (min + max) / 2;
        final double u = Math.cos(Math.PI / 4) * r;
        centers = new HashMap<Integer, double[]>(14);
        centers.put(0, new double[] { r, 0, 0 });
        centers.put(1, new double[] { u, u, 0 });
        centers.put(2, new double[] { 0, r, 0 });
        centers.put(3, new double[] { -u, u, 0 });
        centers.put(4, new double[] { -r, 0, 0 });
        centers.put(5, new double[] { -u, -u, 0 });
        centers.put(6, new double[] { 0, -r, 0 });
        centers.put(7, new double[] { u, -u, 0 });
        centers.put(8, new double[] { 0, 0, r });
        centers.put(9, new double[] { -u, 0, u });
        centers.put(10, new double[] { -u, 0, -u });
        centers.put(11, new double[] { 0, 0, -r });
        centers.put(12, new double[] { u, 0, -u });
        centers.put(13, new double[] { u, 0, u });
    }
}
