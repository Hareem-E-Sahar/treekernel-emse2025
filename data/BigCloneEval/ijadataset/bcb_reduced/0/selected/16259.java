package edu.ucdavis.genomics.metabolomics.util.math;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Matrix {

    /**
     * DOCUMENT ME!
     *
     * @param object DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String[][] transpose(String[][] object) {
        String[][] transpose = new String[object[0].length][object.length];
        for (int i = 0; i < object.length; i++) {
            for (int x = 0; x < transpose.length; x++) {
                transpose[x][i] = object[i][x];
            }
        }
        return transpose;
    }

    /**
     * DOCUMENT ME!
     *
     * @param object DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static double[][] transpose(double[][] object) {
        double[][] transpose = new double[object[0].length][object.length];
        for (int i = 0; i < object.length; i++) {
            for (int x = 0; x < transpose.length; x++) {
                transpose[x][i] = object[i][x];
            }
        }
        return transpose;
    }
}
