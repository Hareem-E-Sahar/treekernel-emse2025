package org.streets.eis.ext.analysis.internal.buildlogic;

import java.util.List;
import org.streets.database.datadict.Relation;

public class Matrix {

    public static int[][] createMatrix(List<String> list, List<Relation> ers) {
        int[][] adjMatrix = new int[list.size()][list.size()];
        for (int i = 0; i < list.size(); i++) {
            adjMatrix[i][i] = -1;
        }
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                adjMatrix[i][j] = distance(list.get(i), list.get(j), ers);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < i; j++) {
                adjMatrix[i][j] = adjMatrix[j][i];
            }
        }
        return adjMatrix;
    }

    private static int distance(String e1, String e2, List<Relation> ers) {
        for (Relation er : ers) {
            if ((er.getHostTable().equalsIgnoreCase(e1)) && (er.getTargetTable().equalsIgnoreCase(e2))) {
                return 1;
            }
            if ((er.getHostTable().equalsIgnoreCase(e2)) && (er.getTargetTable().equalsIgnoreCase(e1))) {
                return 1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
    }
}
