package com.dukesoftware.utils.cost;

import com.dukesoftware.utils.math.SimplePoint2d;

public class CostMatrixImpl implements ICostMatrix {

    private final int size;

    private final double[][] matrix;

    public CostMatrixImpl(int size) {
        this.size = size;
        this.matrix = new double[size][size];
    }

    public CostMatrixImpl(SimplePoint2d[] cities) {
        this.size = cities.length;
        matrix = createCostMatrix(cities);
    }

    @Override
    public double get(int r, int c) {
        return matrix[r][c];
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void set(int r, int c, double value) {
        matrix[r][c] = value;
    }

    private static double[][] createCostMatrix(SimplePoint2d[] cities) {
        final int noCity = cities.length;
        double[][] dMat = new double[noCity][noCity];
        for (int i = 0; i < noCity; i++) {
            dMat[i][i] = 0.0;
            for (int j = i + 1; j < noCity; j++) {
                dMat[i][j] = SimplePoint2d.distance(cities[i], cities[j]);
                dMat[j][i] = dMat[i][j];
            }
        }
        return dMat;
    }

    public double calcTotalLength(int[] path) {
        double sum = 0;
        for (int i = 0, size = this.size; i < size; i++) {
            sum += matrix[path[i % size]][path[(i + 1) % size]];
        }
        return sum;
    }
}
