package games.strategy.engine.random;

import games.strategy.util.IntegerMap;
import java.io.Serializable;

public class RandomStatsDetails implements Serializable {

    private final IntegerMap<Integer> m_data;

    private double m_average;

    private int m_total;

    private double m_median;

    private double m_stdDeviation;

    private double m_variance;

    public RandomStatsDetails(final IntegerMap<Integer> data) {
        m_data = data;
        if (data.totalValues() != 0) {
            int sumTotal = 0;
            int total = 0;
            for (int i = 1; i <= 6; i++) {
                sumTotal += i * m_data.getInt(Integer.valueOf(i));
                total += m_data.getInt(Integer.valueOf(i));
            }
            m_total = total;
            m_average = ((double) sumTotal) / ((double) data.totalValues());
            if (total % 2 != 0) {
                m_median = calcMedian((total / 2) + 1);
            } else {
                double tmp1 = 0;
                double tmp2 = 0;
                tmp1 = calcMedian((total / 2));
                tmp2 = calcMedian((total / 2) + 1);
                m_median = (tmp1 + tmp2) / 2;
            }
            double variance = 0;
            for (int i = 1; i <= 6; i++) {
                variance += (m_data.getInt(new Integer(i)) - (total / 6)) * (m_data.getInt(new Integer(i)) - (total / 6));
            }
            m_variance = variance / (total - 1);
            m_stdDeviation = Math.sqrt(m_variance);
        } else {
            m_total = 0;
            m_median = 0;
            m_average = 0;
            m_stdDeviation = 0;
            m_variance = 0;
        }
    }

    public double getAverage() {
        return m_average;
    }

    public IntegerMap<Integer> getData() {
        return m_data;
    }

    public int getTotal() {
        return m_total;
    }

    public double getMedian() {
        return m_median;
    }

    public double getVariance() {
        return m_variance;
    }

    public double getStdDeviation() {
        return m_stdDeviation;
    }

    private int calcMedian(final int centerPoint) {
        int sum = 0;
        int i = 1;
        for (i = 1; i <= 6; i++) {
            sum += m_data.getInt(new Integer(i));
            if (sum >= centerPoint) {
                return i;
            }
        }
        return i;
    }
}
