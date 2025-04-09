package travelingsalesman;

import java.awt.Color;
import java.awt.Component;
import java.util.Random;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Vicente J. Ferrer Dalmau
 */
public class RoutesMatrix {

    private int[][] theMatrix;

    private int cities;

    private int MAXDISTANCE = 100;

    /** Creates a new instance of RoutesMatrix
     */
    public RoutesMatrix(int cities) {
        theMatrix = new int[cities][cities];
        this.cities = cities;
        Random generator = new Random();
        for (int i = 0; i < cities; i++) {
            for (int j = i; j < cities; j++) {
                if (i == j) theMatrix[i][j] = 0; else {
                    theMatrix[i][j] = generator.nextInt(MAXDISTANCE);
                    theMatrix[j][i] = theMatrix[i][j];
                }
            }
        }
    }

    /**
     * returns the number of cities
     */
    public int getCitiesCount() {
        return cities;
    }

    /**
     * gets the cost of going from city "a" to city "b"
     */
    public int getCost(int a, int b) {
        return theMatrix[a][b];
    }

    /**
     * sets the cost of going from city "a" to city "b"
     */
    public void setCost(int a, int b, int cost) {
        theMatrix[a][b] = cost;
    }

    /**
     * gets the array of costs as an Object[][] array.
     */
    public Object[][] getCosts() {
        Object[][] array = new Object[cities][cities + 1];
        for (int i = 0; i < cities; i++) {
            for (int j = 0; j < cities; j++) {
                if (j == 0) {
                    array[i][0] = i;
                    array[i][j + 1] = theMatrix[i][j];
                } else array[i][j + 1] = theMatrix[i][j];
            }
        }
        return array;
    }

    /**
     * gets the cities in an Object[] array.
     */
    public Object[] getCities() {
        Object[] array = new Object[cities + 1];
        for (int i = 0; i < cities; i++) {
            if (i == 0) {
                array[i] = " ";
                array[i + 1] = 0;
            } else array[i + 1] = i;
        }
        return array;
    }

    /**
     * gets the maximum distance between two cities.
     */
    public int getMaxDistance() {
        return MAXDISTANCE;
    }

    public String toString() {
        String str = new String();
        for (int i = 0; i < cities; i++) {
            for (int j = 0; j < cities; j++) {
                if (j == cities - 1) str += theMatrix[i][j] + "\n"; else str += theMatrix[i][j] + ", ";
            }
        }
        return str;
    }

    public class MyRender extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            this.setOpaque(true);
            this.setToolTipText("");
            if (column == 0) {
                this.setBackground(Color.LIGHT_GRAY);
                this.setHorizontalAlignment(JTextField.CENTER);
            } else if (column - 1 == row) {
                this.setBackground(Color.LIGHT_GRAY);
            } else {
                this.setBackground(Color.WHITE);
                this.setToolTipText("De la ciudad " + row + " a la " + (column - 1));
            }
            return this;
        }
    }

    /** 
     * Shows the content of the matrix in a JTable object.
     */
    public void drawJTable(JTable j) {
        DefaultTableModel dtm = new DefaultTableModel() {

            public boolean isCellEditable(int row, int column) {
                if (column != 0 && (column - 1 != row)) return true; else return false;
            }
        };
        dtm.setDataVector(this.getCosts(), this.getCities());
        j.setModel(dtm);
        j.setDefaultRenderer(Object.class, new MyRender());
    }
}
