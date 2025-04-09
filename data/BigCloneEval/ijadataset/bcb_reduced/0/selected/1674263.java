package ncg.statistics;

import geovista.common.data.DataSetForApps;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.Frequency;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import com.vividsolutions.jts.algorithm.CentroidArea;
import com.vividsolutions.jts.geom.Coordinate;

public class NCGStatUtils {

    private static final Logger logger = Logger.getLogger(NCGStatUtils.class.getName());

    public static final int CROSS_VALIDATION_LIKELIHOOD = 0;

    public static final int CROSS_VALIDATION_SCORE = 1;

    public static final int BISQUARE_KERNEL = 0;

    public static final int MOVING_WINDOW = 1;

    public static String crossValidationMethodToString(int cvMethod) {
        String crossValidationMethodAsString = null;
        switch(cvMethod) {
            case CROSS_VALIDATION_SCORE:
                crossValidationMethodAsString = "cross validation score";
                break;
            case CROSS_VALIDATION_LIKELIHOOD:
                crossValidationMethodAsString = "cross validation likelihood";
                break;
            default:
                crossValidationMethodAsString = "unsupported cross validation method";
        }
        return crossValidationMethodAsString;
    }

    public static String kernelFunctionTypeToString(int kernelType) {
        String kernelFunctionTypeAsString = null;
        switch(kernelType) {
            case MOVING_WINDOW:
                kernelFunctionTypeAsString = "moving window";
                break;
            case BISQUARE_KERNEL:
                kernelFunctionTypeAsString = "bisquare kernel";
                break;
            default:
                kernelFunctionTypeAsString = "unsupported kernel function type";
        }
        return kernelFunctionTypeAsString;
    }

    public static double[] standardize(final double[] data) {
        double[] dataTransform = null;
        if (data != null) {
            double dataMean = StatUtils.mean(data);
            double dataStdDev = Math.sqrt(StatUtils.variance(data));
            dataTransform = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                dataTransform[i] = (data[i] - dataMean) / dataStdDev;
            }
        } else {
            dataTransform = new double[0];
        }
        return dataTransform;
    }

    public static double[][] standardize(double[][] data, final boolean rowOrder) {
        double[][] dataZScores = null;
        if (data != null) {
            if (rowOrder == true) {
                data = transpose(data);
            }
            int numVars = data.length;
            dataZScores = new double[numVars][0];
            for (int j = 0; j < data.length; j++) {
                dataZScores[j] = standardize(data[j]);
            }
            if (rowOrder == true) {
                dataZScores = transpose(dataZScores);
            }
        } else {
            dataZScores = new double[0][0];
        }
        return dataZScores;
    }

    public static double[][] transpose(double[][] data) {
        double[][] dataTransposed = new double[0][0];
        if (data != null) {
            int numRows = data.length;
            if (numRows > 0) {
                int numCols = data[0].length;
                dataTransposed = new double[numCols][numRows];
                for (int i = 0; i < numRows; i++) {
                    for (int j = 0; j < numCols; j++) {
                        dataTransposed[j][i] = data[i][j];
                    }
                }
            }
        }
        return dataTransposed;
    }

    public static int getMin(double[] items) {
        int minIndex = -1;
        if (items != null) {
            if (items.length > 0) {
                minIndex = 0;
                for (int i = 1; i < items.length; i++) {
                    if (items[i] < items[minIndex]) {
                        minIndex = i;
                    }
                }
            }
        }
        return minIndex;
    }

    public static int getMin(int[] items) {
        double[] doubleItems = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            doubleItems[i] = items[i];
        }
        return getMin(doubleItems);
    }

    public static int getMax(int[] items) {
        double[] doubleItems = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            doubleItems[i] = items[i];
        }
        return getMax(doubleItems);
    }

    public static int getMax(double[] items) {
        int minIndex = -1;
        if (items != null) {
            if (items.length > 0) {
                minIndex = 0;
                for (int i = 1; i < items.length; i++) {
                    if (items[i] > items[minIndex]) {
                        minIndex = i;
                    }
                }
            }
        }
        return minIndex;
    }

    public static int[] sort(final double[] items, boolean descending) {
        Integer[] itemIndices = new Integer[items.length];
        for (int i = 0; i < items.length; i++) {
            itemIndices[i] = new Integer(i);
        }
        Arrays.sort(itemIndices, new Comparator() {

            @Override
            public int compare(Object index1, Object index2) {
                int intIndex1 = ((Integer) index1).intValue();
                int intIndex2 = ((Integer) index2).intValue();
                if (items[intIndex1] < items[intIndex2]) {
                    return 1;
                } else if (items[intIndex1] > items[intIndex2]) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        int[] sortedIndices = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            sortedIndices[i] = itemIndices[i].intValue();
        }
        if (descending == false) {
            sortedIndices = reverse(sortedIndices);
        }
        return sortedIndices;
    }

    public static int[] reverse(int[] items) {
        int[] itemsReversed = new int[items.length];
        for (int i = 0, j = (items.length - 1); i <= j; i++, j--) {
            itemsReversed[i] = items[j];
            itemsReversed[j] = items[i];
        }
        return itemsReversed;
    }

    public static int[] getUniqueItems(int[] data) {
        int[] uniqueItems = null;
        try {
            Set<Integer> classes = new HashSet<Integer>();
            for (int i = 0; i < data.length; i++) {
                classes.add(data[i]);
            }
            uniqueItems = new int[classes.size()];
            Iterator<Integer> classesIt = classes.iterator();
            int i = 0;
            while (classesIt.hasNext()) {
                uniqueItems[i++] = classesIt.next().intValue();
            }
        } catch (Exception e) {
            logger.severe(e.getCause().toString() + " : " + e.toString() + " : " + e.getMessage());
            e.printStackTrace();
            uniqueItems = new int[0];
        }
        return uniqueItems;
    }

    public static int[] getFrequencies(int[] data) {
        int[] itemFrequencies = null;
        int[] uniqueItems = getUniqueItems(data);
        try {
            Frequency classFrequency = new Frequency();
            for (int i = 0; i < data.length; i++) {
                classFrequency.addValue(data[i]);
            }
            itemFrequencies = new int[uniqueItems.length];
            for (int i = 0; i < uniqueItems.length; i++) {
                itemFrequencies[i] = (int) classFrequency.getCount(uniqueItems[i]);
            }
        } catch (Exception e) {
            logger.severe(e.getCause().toString() + " : " + e.toString() + " : " + e.getMessage());
            e.printStackTrace();
            itemFrequencies = new int[0];
        }
        return itemFrequencies;
    }

    public static Point2D[] computeCentroids(DataSetForApps data) {
        Point2D[] centroids = null;
        switch(data.getSpatialType()) {
            case DataSetForApps.SPATIAL_TYPE_POINT:
                centroids = data.getPoint2DData();
                break;
            case DataSetForApps.SPATIAL_TYPE_POLYGON:
                Shape[] polygons = data.getShapeData();
                int numObservations = data.getNumObservations();
                centroids = new Point2D[numObservations];
                for (int i = 0; i < numObservations; i++) {
                    Shape polygon = polygons[i];
                    List<Coordinate> polygonCoordinatesList = new ArrayList<Coordinate>();
                    PathIterator polygonIterator = polygon.getPathIterator(null);
                    while (!polygonIterator.isDone()) {
                        double[] coordinates = new double[6];
                        if (polygonIterator.currentSegment(coordinates) == PathIterator.WIND_NON_ZERO) {
                            polygonCoordinatesList.add(new Coordinate(coordinates[0], coordinates[1]));
                        }
                        polygonIterator.next();
                    }
                    Coordinate[] polygonCoordinates = new Coordinate[polygonCoordinatesList.size()];
                    polygonCoordinatesList.toArray(polygonCoordinates);
                    CentroidArea polygonCentroid = new CentroidArea();
                    polygonCentroid.add(polygonCoordinates);
                    Coordinate centroid = polygonCentroid.getCentroid();
                    centroids[i] = new Point2D.Double(centroid.x, centroid.y);
                }
                break;
            default:
                logger.warning("unable to compute centroids for unsupported spatial type");
                centroids = new Point2D[0];
        }
        return centroids;
    }

    public static RealMatrix computeDistanceMatrix(Point2D[] vertices) {
        int numVertices = vertices.length;
        RealMatrix distanceMatrix = null;
        try {
            distanceMatrix = MatrixUtils.createRealMatrix(numVertices, numVertices);
            for (int i = 0; i < numVertices; i++) {
                distanceMatrix.setEntry(i, i, 0.0);
                for (int j = (i + 1); j < numVertices; j++) {
                    double distance = vertices[i].distance(vertices[j]);
                    distanceMatrix.setEntry(i, j, distance);
                    distanceMatrix.setEntry(j, i, distance);
                }
            }
        } catch (Exception e) {
            logger.severe(e.getCause().toString() + " : " + e.toString() + " : " + e.getMessage());
            e.printStackTrace();
            distanceMatrix = MatrixUtils.createRealMatrix(0, 0);
        }
        return distanceMatrix;
    }

    public static RealMatrix computeWeightedCovarianceMatrix(double[][] values, double[] means, double[] weights) {
        RealMatrix weightedCovariance = null;
        try {
            int numVars = values.length;
            int numItems = values[0].length;
            weightedCovariance = new Array2DRowRealMatrix(numVars, numVars);
            double sumWeights = (new Sum()).evaluate(weights);
            for (int i = 0; i < numVars; i++) {
                for (int j = i; j < numVars; j++) {
                    double weightedSumSquares = 0.0;
                    for (int k = 0; k < numItems; k++) {
                        double dx1 = (values[i][k] - means[i]);
                        double dx2 = (values[j][k] - means[j]);
                        weightedSumSquares += (weights[k] * dx1 * dx2);
                    }
                    double biasedWeightedCovariance = weightedSumSquares / sumWeights;
                    weightedCovariance.setEntry(i, j, biasedWeightedCovariance);
                    weightedCovariance.setEntry(j, i, biasedWeightedCovariance);
                }
            }
        } catch (Exception e) {
            logger.severe(e.getCause().toString() + " : " + e.toString() + " : " + e.getMessage());
            e.printStackTrace();
            weightedCovariance = new Array2DRowRealMatrix(0, 0);
        }
        return weightedCovariance;
    }

    public static RealVector computeWeightedMean(double[][] values, double[] weights) {
        RealVector weightedMeanVector = null;
        try {
            int numVars = values.length;
            double sumWeights = (new Sum()).evaluate(weights);
            weightedMeanVector = new ArrayRealVector(numVars);
            for (int i = 0; i < numVars; i++) {
                double weightedItemSum = (new Sum()).evaluate(values[i], weights);
                weightedMeanVector.setEntry(i, weightedItemSum / sumWeights);
            }
        } catch (Exception e) {
            logger.severe(e.getCause().toString() + " : " + e.toString() + " : " + e.getMessage());
            e.printStackTrace();
            weightedMeanVector = new ArrayRealVector();
        }
        return weightedMeanVector;
    }

    public static double computeMahalanobisDistance2(RealVector x, RealMatrix covInv, RealVector mean) {
        double mhd2 = -1.0;
        try {
            RealVector meanDiff = x.subtract(mean);
            mhd2 = covInv.preMultiply(meanDiff).dotProduct(meanDiff);
        } catch (Exception e) {
            logger.severe(e.getCause().toString() + " : " + e.toString() + " : " + e.getMessage());
            e.printStackTrace();
            mhd2 = -1.0;
        }
        return mhd2;
    }

    public static double[] movingWindow(double[] distances, double bandwidth) {
        double[] weights = null;
        try {
            weights = new double[distances.length];
            for (int i = 0; i < distances.length; i++) {
                if (distances[i] < bandwidth) {
                    weights[i] = 1.0;
                } else {
                    weights[i] = 0.0;
                }
            }
        } catch (Exception e) {
            logger.severe(e.getCause().toString() + " : " + e.toString() + " : " + e.getMessage());
            e.printStackTrace();
            weights = new double[0];
        }
        return weights;
    }

    public static double[] bisquareKernel(double[] distances, double bandwidth) {
        double[] weights = null;
        try {
            weights = new double[distances.length];
            for (int i = 0; i < distances.length; i++) {
                if (distances[i] < bandwidth) {
                    weights[i] = Math.pow((1.0 - Math.pow((distances[i] / bandwidth), 2.0)), 2.0);
                } else {
                    weights[i] = 0.0;
                }
            }
        } catch (Exception e) {
            logger.severe(e.getCause().toString() + " : " + e.toString() + " : " + e.getMessage());
            e.printStackTrace();
            weights = new double[0];
        }
        return weights;
    }
}
