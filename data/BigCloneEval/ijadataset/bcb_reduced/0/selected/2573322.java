package org.gwtopenmaps.georeport.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gwtopenmaps.georeport.client.transfert.Site;
import org.gwtopenmaps.mapfish.client.widget.geostat.*;

public class RadiusNormalizer {

    private List<Site> m_vSite;

    private List<Double> m_vValues;

    private ValueToRadiusConverter m_converter;

    private double m_dAverageValue;

    private double m_dMedianValue;

    private double m_dValueToRadiusRatio;

    private RadiusFilter m_radiusFilter;

    private int m_idealMinRadius = 20;

    private int m_idealMaxRadius = 60;

    public RadiusNormalizer(List<Site> sites, Indicator indicator, ValueToRadiusConverter converter) {
        m_vSite = sites;
        m_converter = converter;
        initValueList();
        if (m_vValues.size() > 0) {
            initAverageValue();
            initMedianValue();
            initValueToRadiusRatio();
        } else {
            m_dValueToRadiusRatio = 1;
            m_radiusFilter = new HighValuesRadiusFilter();
        }
    }

    private void initValueList() {
        m_vValues = new ArrayList<Double>();
        for (Site site : m_vSite) {
            if (site.getValue() > 0) m_vValues.add(site.getValue());
        }
        Collections.sort(m_vValues);
    }

    private void initAverageValue() {
        m_dAverageValue = 0;
        for (Double value : m_vValues) {
            m_dAverageValue += value;
        }
        m_dAverageValue /= m_vValues.size();
    }

    private void initMedianValue() {
        if (isNumValuesEven()) {
            double medianLow = m_vValues.get(m_vValues.size() / 2 - 1);
            double medianHigh = m_vValues.get(m_vValues.size() / 2);
            m_dMedianValue = (medianLow + medianHigh) / 2;
        } else {
            m_dMedianValue = m_vValues.get((m_vValues.size() - 1) / 2);
        }
    }

    private boolean isNumValuesEven() {
        int i = m_vValues.size() % 2;
        if (i == 0) return true; else return false;
    }

    /**
	 * In order to display sites as circles that are not too big nor too small,
	 * we need to calculate a ratio to apply to the displayed values.
	 */
    private void initValueToRadiusRatio() {
        m_dValueToRadiusRatio = 1;
        double dMin = m_converter.convertToRadius(getMinValue(m_vValues));
        double dMax = m_converter.convertToRadius(getMaxValue(m_vValues));
        if (m_dAverageValue < m_dMedianValue) {
            enlargePoints(dMin, dMax);
            shrinkPoints(dMin, dMax);
            m_radiusFilter = new HighValuesRadiusFilter();
        } else {
            shrinkPoints(dMin, dMax);
            enlargePoints(dMin, dMax);
            m_radiusFilter = new LowValuesRadiusFilter();
        }
    }

    /**
	 * Shrink size to avoid too big points
	 */
    private void shrinkPoints(double dMin, double dMax) {
        while (dMax > m_idealMaxRadius) {
            dMin /= 2;
            dMax /= 2;
            m_dValueToRadiusRatio /= 2;
        }
    }

    /**
	 * Enlarge the points in order to get a better picture
	 */
    private void enlargePoints(double dMin, double dMax) {
        while (dMin < m_idealMinRadius) {
            dMin *= 2;
            dMax *= 2;
            m_dValueToRadiusRatio *= 2;
        }
    }

    private double getMinValue(List<Double> vValues) {
        double dMin = vValues.get(0);
        for (Double value : vValues) {
            if (value < dMin) dMin = value;
        }
        return dMin;
    }

    private double getMaxValue(List<Double> vValues) {
        double dMax = vValues.get(0);
        for (Double value : vValues) {
            if (value > dMax) dMax = value;
        }
        return dMax;
    }

    public double normalizeRadius(double radius) {
        double normalizedRadius = radius * m_dValueToRadiusRatio;
        return m_radiusFilter.filterRadius(normalizedRadius);
    }

    private class HighValuesRadiusFilter implements RadiusFilter {

        private double m_maxRadius = 80;

        public double filterRadius(double radius) {
            if (radius > m_maxRadius) radius = m_maxRadius;
            return radius;
        }
    }

    private class LowValuesRadiusFilter implements RadiusFilter {

        private double m_minRadius = 10;

        public double filterRadius(double radius) {
            if (radius < m_minRadius) radius = m_minRadius;
            return radius;
        }
    }
}
