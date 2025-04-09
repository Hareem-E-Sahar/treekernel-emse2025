package edu.colorado.emml.util.latex;

import java.io.IOException;
import javax.swing.table.DefaultTableModel;
import edu.colorado.emml.util.latex.CSVUtil;

/**
 * Author: Sam Reid
 * May 9, 2007, 4:40:44 AM
 */
public class SwingUtil {

    public static DefaultTableModel transpose(DefaultTableModel orig) {
        Object[][] all = new Object[orig.getRowCount() + 1][orig.getColumnCount()];
        for (int i = 0; i < all.length; i++) {
            for (int k = 0; k < all[0].length; k++) {
                if (i == 0) {
                    all[i][k] = orig.getColumnName(k);
                } else {
                    all[i][k] = orig.getValueAt(i - 1, k);
                }
            }
        }
        Object[][] transpose = new Object[all[0].length][all.length];
        for (int row = 0; row < all.length; row++) {
            for (int col = 0; col < all[0].length; col++) {
                transpose[col][row] = all[row][col];
            }
        }
        Object[][] data = new Object[transpose.length - 1][transpose[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int k = 0; k < data[0].length; k++) {
                data[i][k] = transpose[i + 1][k];
            }
        }
        return new DefaultTableModel(data, transpose[0]);
    }

    public static void main(String[] args) throws IOException {
        DefaultTableModel defaultTableModel = new DefaultTableModel(new Object[][] { { "00", "01" }, { "10", "11" }, { "20", "21" } }, new Object[] { "colA", "colB" });
        new CSVUtil().compileAndView("before", defaultTableModel);
        new CSVUtil().compileAndView("after", transpose(defaultTableModel));
    }
}
