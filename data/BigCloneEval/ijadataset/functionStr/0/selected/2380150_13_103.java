public class Test {    public String ConstructXMLTree(double[][] dDistanceMatrix) throws Exception {
        ArrayList alDist = new ArrayList();
        String[] XMLTree = new String[dDistanceMatrix.length];
        int[] DataIds = new int[dDistanceMatrix.length];
        for (int i = 0; i < dDistanceMatrix.length; i++) DataIds[i] = i;
        int min_i = -1, min_j = -1;
        int k = 0;
        while (dDistanceMatrix.length > 1) {
            double[] r = new double[dDistanceMatrix.length];
            for (int i = 0; i < dDistanceMatrix.length; i++) {
                for (int j = 0; j < dDistanceMatrix.length; j++) {
                    if (i != j) r[i] += dDistanceMatrix[i][j];
                }
            }
            double[][] M = new double[dDistanceMatrix.length][dDistanceMatrix.length];
            for (int i = 0; i < dDistanceMatrix.length; i++) {
                for (int j = 0; j < dDistanceMatrix.length; j++) {
                    if (i != j) {
                        M[i][j] = dDistanceMatrix[i][j] - (r[i] + r[j]) / (dDistanceMatrix.length - 2);
                    }
                }
            }
            double min_dist = Double.MAX_VALUE;
            min_i = -1;
            min_j = -1;
            for (int i = 0; i < dDistanceMatrix.length; i++) {
                for (int j = i + 1; j < dDistanceMatrix.length; j++) {
                    if (min_dist > M[i][j]) {
                        min_i = i;
                        min_j = j;
                        min_dist = M[i][j];
                    }
                }
            }
            if (XMLTree[min_i] == null && XMLTree[min_j] == null) {
                XMLTree[min_i] = "<node id=\"-1\" step=\"" + k + "\" dist=\"" + dDistanceMatrix[min_i][min_j] + "\">";
                XMLTree[min_i] += "<node id=\"" + DataIds[min_i] + "\"/>";
                XMLTree[min_i] += "<node id=\"" + DataIds[min_j] + "\"/>";
                XMLTree[min_i] += "</node>";
            } else if (XMLTree[min_i] != null && XMLTree[min_j] != null) {
                XMLTree[min_i] = "<node id=\"-1\" step=\"" + k + "\" dist=\"" + dDistanceMatrix[min_i][min_j] + "\">" + XMLTree[min_i] + XMLTree[min_j] + "</node>";
            } else if (XMLTree[min_i] != null) {
                XMLTree[min_i] = "<node id=\"-1\" step=\"" + k + "\" dist=\"" + dDistanceMatrix[min_i][min_j] + "\">" + XMLTree[min_i];
                XMLTree[min_i] += "<node id=\"" + DataIds[min_j] + "\"/>";
                XMLTree[min_i] += "</node>";
            } else {
                XMLTree[min_i] = "<node id=\"-1\" step=\"" + k + "\" dist=\"" + dDistanceMatrix[min_i][min_j] + "\">" + XMLTree[min_j];
                XMLTree[min_i] += "<node id=\"" + DataIds[min_i] + "\"/>";
                XMLTree[min_i] += "</node>";
            }
            for (int i = min_j + 1; i < XMLTree.length; i++) {
                XMLTree[i - 1] = XMLTree[i];
                DataIds[i - 1] = DataIds[i];
            }
            double[][] dTempMatrix = new double[dDistanceMatrix.length - 1][dDistanceMatrix.length - 1];
            for (int i = 0; i < dDistanceMatrix.length; i++) {
                for (int j = i + 1; j < dDistanceMatrix.length; j++) {
                    if (i == min_i) {
                        if (j < min_j) {
                            dTempMatrix[min_i][j] = (dDistanceMatrix[min(min_i, j)][max(min_i, j)] + dDistanceMatrix[min(min_j, j)][max(min_j, j)] - dDistanceMatrix[min(min_j, min_i)][max(min_j, min_i)]) / 2;
                            dTempMatrix[j][min_i] = (dDistanceMatrix[min(min_i, j)][max(min_i, j)] + dDistanceMatrix[min(min_j, j)][max(min_j, j)] - dDistanceMatrix[min(min_j, min_i)][max(min_j, min_i)]) / 2;
                        } else if (j > min_j) {
                            dTempMatrix[min_i][j - 1] = (dDistanceMatrix[min(min_i, j)][max(min_i, j)] + dDistanceMatrix[min(min_j, j)][max(min_j, j)] - dDistanceMatrix[min(min_j, min_i)][max(min_j, min_i)]) / 2;
                            dTempMatrix[j - 1][min_i] = (dDistanceMatrix[min(min_i, j)][max(min_i, j)] + dDistanceMatrix[min(min_j, j)][max(min_j, j)] - dDistanceMatrix[min(min_j, min_i)][max(min_j, min_i)]) / 2;
                        }
                    } else if (i != min_j) {
                        if (j < min_j) {
                            if (i < min_j) {
                                dTempMatrix[i][j] = dDistanceMatrix[i][j];
                                dTempMatrix[j][i] = dDistanceMatrix[i][j];
                            } else {
                                dTempMatrix[i - 1][j] = dDistanceMatrix[i][j];
                                dTempMatrix[j][i - 1] = dDistanceMatrix[i][j];
                            }
                        } else if (j > min_j) {
                            if (i < min_j) {
                                dTempMatrix[i][j - 1] = dDistanceMatrix[i][j];
                                dTempMatrix[j - 1][i] = dDistanceMatrix[i][j];
                            } else {
                                dTempMatrix[i - 1][j - 1] = dDistanceMatrix[i][j];
                                dTempMatrix[j - 1][i - 1] = dDistanceMatrix[i][j];
                            }
                        }
                    }
                }
            }
            dDistanceMatrix = dTempMatrix;
            k++;
        }
        return XMLTree[min_i];
    }
}