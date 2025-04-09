package cluster;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 *
 * @author Grandong
 */
public class SpectralClusteringModel implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
	 * @param sigularValueDecompositionMatrixU
	 * @throws IOException
	 */
    public Dataset[] clusterSpectralClusteringMatrix(SingularValueDecomposition sigularValueDecompositionMatrix) throws IOException {
        List<Instance> instances = new ArrayList<Instance>();
        Matrix sigularValueDecompositionMatrixU = sigularValueDecompositionMatrix.getU();
        double[][] sigularValueDecompositionArrayU = sigularValueDecompositionMatrixU.getArray();
        for (int i = 0; i < sigularValueDecompositionMatrixU.getRowDimension(); i++) {
            double[] tagArray = new double[sigularValueDecompositionMatrixU.getColumnDimension() + 1];
            for (int j = 0; j < sigularValueDecompositionMatrixU.getColumnDimension(); j++) {
                tagArray[j] = sigularValueDecompositionArrayU[i][j];
            }
            instances.add(new DenseInstance(tagArray, "tag_pos_" + i));
        }
        Matrix sigularValueDecompositionMatrixV = sigularValueDecompositionMatrix.getV();
        double[][] sigularValueDecompositionArrayV = sigularValueDecompositionMatrixV.getArray();
        for (int i = 0; i < sigularValueDecompositionMatrixV.getRowDimension(); i++) {
            double[] pageArray = new double[sigularValueDecompositionMatrixV.getColumnDimension() + 1];
            for (int j = 0; j < sigularValueDecompositionMatrixV.getColumnDimension(); j++) {
                pageArray[j] = sigularValueDecompositionArrayV[i][j];
            }
            instances.add(new DenseInstance(pageArray, "page_pos_" + i));
        }
        Dataset data = new DefaultDataset(instances);
        Clusterer kMeans = new KMeans(5);
        Dataset[] clusters = kMeans.cluster(data);
        return clusters;
    }

    /**
	 * @param xyMatrix
	 * @return
	 * @throws IOException
	 */
    public SingularValueDecomposition computeLeftSigularValueDecompositionMatrix(Matrix xyMatrix) throws IOException {
        Matrix userTagFrequencyMatrix = xyMatrix;
        double[][] dimensionMatrix = userTagFrequencyMatrix.getArray();
        int M = dimensionMatrix.length;
        int N = dimensionMatrix[0].length;
        double[][] cosineSimilairity2DArray = new double[M][M];
        double sm1 = 0;
        double sm2 = 0;
        double sm3 = 0;
        for (int i = 0; i < M; i++) {
            for (int j = i + 1; j < M; j++) {
                sm1 = 0;
                sm2 = 0;
                sm3 = 0;
                for (int k = 0; k < N; k++) {
                    sm1 += dimensionMatrix[i][k] * dimensionMatrix[j][k];
                    sm2 += dimensionMatrix[i][k] * dimensionMatrix[i][k];
                    sm3 += dimensionMatrix[j][k] * dimensionMatrix[j][k];
                }
                cosineSimilairity2DArray[i][j] = sm1 / Math.sqrt(sm2) / Math.sqrt(sm3);
                if (new Double(cosineSimilairity2DArray[i][j]).equals(Double.NaN)) {
                    cosineSimilairity2DArray[i][j] = 0d;
                }
            }
        }
        for (int i = 0; i < M; i++) {
            cosineSimilairity2DArray[i][i] = 1;
            for (int j = i + 1; j < M; j++) {
                cosineSimilairity2DArray[j][i] = cosineSimilairity2DArray[i][j];
            }
        }
        Matrix cosineSimilarityMatrix = new Matrix(cosineSimilairity2DArray);
        int columnDimensionSize = cosineSimilarityMatrix.getColumnDimension();
        double[][] sigmaCosineSimilarity2DArray = new double[columnDimensionSize][columnDimensionSize];
        for (int i = 0; i < columnDimensionSize; i++) {
            for (int j = 0; j < columnDimensionSize; j++) {
                sigmaCosineSimilarity2DArray[i][i] = sigmaCosineSimilarity2DArray[i][i] + cosineSimilairity2DArray[i][j];
            }
        }
        Matrix sigmaCosineSimilarityMatrix = new Matrix(sigmaCosineSimilarity2DArray);
        double squaredSigmaCosineSimilarity2DArray[][] = new double[columnDimensionSize][columnDimensionSize];
        for (int i = 0; i < columnDimensionSize; i++) {
            double dij = sigmaCosineSimilarityMatrix.get(i, i);
            squaredSigmaCosineSimilarity2DArray[i][i] = 1 / Math.sqrt(dij);
            if (new Double(squaredSigmaCosineSimilarity2DArray[i][i]).equals(Double.NaN)) {
                cosineSimilairity2DArray[i][i] = 0d;
            }
        }
        Matrix squaredSigmaCosineSimilarityMatrix = new Matrix(squaredSigmaCosineSimilarity2DArray);
        Matrix laplacianMatrix = calculateLaplacianMatrix(cosineSimilarityMatrix, sigmaCosineSimilarityMatrix, squaredSigmaCosineSimilarityMatrix);
        SingularValueDecomposition sigularValueDecompositionMatrix = laplacianMatrix.svd();
        return sigularValueDecompositionMatrix;
    }

    /**
	 * @param cosineSimilarityMatrix
	 * @param sigmaCosineSimilarityMatrix
	 * @param squaredSigmaCosineSimilarityMatrix
	 * @return
	 */
    private static Matrix calculateLaplacianMatrix(Matrix cosineSimilarityMatrix, Matrix sigmaCosineSimilarityMatrix, Matrix squaredSigmaCosineSimilarityMatrix) {
        Matrix minusMatrix = sigmaCosineSimilarityMatrix.minus(cosineSimilarityMatrix);
        Matrix timesMatrix = squaredSigmaCosineSimilarityMatrix.times(minusMatrix);
        Matrix laplacianMatrix = timesMatrix.times(squaredSigmaCosineSimilarityMatrix);
        return laplacianMatrix;
    }

    /**
	 * @param args
	 * @throws IOException
	 */
    public static void main(String[] args) throws IOException {
        SpectralClusteringModel spectralClustering = new SpectralClusteringModel();
        double[][] array = { { 1., 2., 3 }, { 4., 5., 6. }, { 7., 8., 10. } };
        Matrix userTagFrequencyMatrix = new Matrix(array);
        SingularValueDecomposition singularValueDecomposition = spectralClustering.computeLeftSigularValueDecompositionMatrix(userTagFrequencyMatrix);
        singularValueDecomposition.getU().print(5, 4);
    }
}
