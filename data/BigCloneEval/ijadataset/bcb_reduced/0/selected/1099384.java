package vademecum.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GridUtils {

    private static final Log log = LogFactory.getLog(GridUtils.class);

    /**
	 * Returns a Double Array of one Grid Column
	 * Note: for another return type @see get1DColumnArray().
	 *
	 * @param grid the grid
	 * @param var the column
	 * @return Double[] the Double Array
	 */
    public static Double[] get1DColArray(IDataGrid grid, int var) {
        Double[] lrn = new Double[grid.getNumRows()];
        for (int i = 0; i < lrn.length; i++) {
            Class colClass = grid.getColumn(var).getType();
            if (colClass.equals(Double.class)) {
                lrn[i] = ((Double) grid.getPoint(i, var)).doubleValue();
            } else if (colClass.equals(Integer.class)) {
                lrn[i] = ((Integer) grid.getPoint(i, var)).doubleValue();
            }
        }
        return lrn;
    }

    /**
	 * Returns a primitive double array of one Grid Column.
	 * Note: for another return type @see get1DColArray().
	 *
	 * @param grid the grid
	 * @param col the column or variable of interest
	 * @return double[] primitive double array
	 */
    public static double[] get1DColumnArray(IDataGrid grid, int var) {
        double[] lrn = new double[grid.getNumRows()];
        for (int i = 0; i < lrn.length; i++) {
            Class colClass = grid.getColumn(var).getType();
            if (colClass.equals(Double.class)) {
                Object value = grid.getPoint(i, var);
                if (value.equals(IDataGrid.NaN)) value = Double.NaN;
                lrn[i] = (Double) value;
            } else if (colClass.equals(Integer.class)) {
                lrn[i] = ((Integer) grid.getPoint(i, var)).doubleValue();
            }
        }
        return lrn;
    }

    /**
	 * Return one Row of the Grid
	 * @param grid the grid
	 * @param row the row of interest
	 * @return primitive double array
	 *
	 * change 20070303 : double[] to Double[]
	 */
    public static Double[] get1DRowArray(IDataGrid grid, int row) {
        Double[] lrn = new Double[grid.getNumCols()];
        for (int i = 0; i < lrn.length; i++) {
            lrn[i] = ((Double) grid.getPoint(row, i)).doubleValue();
        }
        return lrn;
    }

    /**
	 * Transform datagrid to double[][]
	 * first dimension  : key / number
	 * second dimension : data-value
	 */
    public static double[][] get1DArray(IDataGrid grid, int var) {
        double[][] lrn = new double[grid.getNumRows()][2];
        for (int i = 0; i < lrn.length; i++) {
            lrn[i][0] = i + 1;
            Class colClass = grid.getColumn(var).getType();
            if (colClass.equals(Double.class)) {
                lrn[i][1] = ((Double) grid.getPoint(i, var)).doubleValue();
            } else if (colClass.equals(Integer.class)) {
                lrn[i][1] = ((Integer) grid.getPoint(i, var)).doubleValue();
            }
        }
        return lrn;
    }

    /**
	 * Converting Double Array to Grid
	 * @param da
	 * @return
	 */
    public static IDataGrid singleArrayToGrid(double[] single) {
        IDataGrid grid = new DataGrid();
        int rows = single.length;
        int cols = 1;
        IColumn col;
        for (int j = 0; j < cols; j++) {
            col = new Column();
            col.setType(Double.class);
            col.setLabel(Integer.toString(j));
            grid.addColumn(col);
        }
        IDataRow row;
        try {
            for (int i = 0; i < rows; i++) {
                row = new DataRow();
                row.setColumns(grid.getColumns());
                for (int j = 0; j < cols; j++) {
                    row.setPoint(j, Double.valueOf(single[i]));
                }
                grid.addRow(row);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (TypeMismatchException e) {
            e.printStackTrace();
        }
        return grid;
    }

    /**
	 * Adding a new Column(array) to the grid 
	 * @param input
	 * @param array
	 * @param label
	 * @return
	 */
    public static IDataGrid addArray(IDataGrid input, double[] array, String label) {
        IDataGrid output = new DataGrid();
        output.setColumns(input.getColumns());
        IColumn column = new Column();
        column.setType(Double.class);
        column.setLabel(label);
        output.addColumn(column);
        Vector<IColumn> columnTypeVector = new Vector<IColumn>();
        for (int i = 0; i < output.getNumCols(); i++) {
            IColumn _column = new Column();
            column.setType(output.getColumn(i).getType());
            columnTypeVector.add(_column);
        }
        for (int index = 0; index < input.getNumRows(); index++) {
            IDataRow row = new DataRow();
            row.setColumns(columnTypeVector);
            row.setKey(input.getRow(index).getKey());
            for (int i = 0; i < input.getNumCols(); i++) {
                try {
                    row.setPoint(i, input.getRow(index).getPoint(i));
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }
            try {
                row.setPoint(row.getColumns().size() - 1, array[index]);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            }
            try {
                output.addRow(row);
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    /**
	 * Converting 1D double Array to a Grid
	 * @param da a one dimensional double array
	 * @return a grid
	 */
    public static IDataGrid arrayToGrid(double[] da) {
        IDataGrid grid = new DataGrid();
        int rows = da.length;
        int cols = 1;
        IColumn col;
        for (int j = 0; j < cols; j++) {
            col = new Column();
            col.setType(Double.class);
            col.setLabel(Integer.toString(j));
            grid.addColumn(col);
        }
        IDataRow row;
        try {
            for (int i = 0; i < rows; i++) {
                row = new DataRow();
                row.setColumns(grid.getColumns());
                for (int j = 0; j < cols; j++) {
                    row.setPoint(j, Double.valueOf(da[i]));
                }
                grid.addRow(row);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (TypeMismatchException e) {
            e.printStackTrace();
        }
        return grid;
    }

    /**
	 * Getting 2 Dimensional double - Array
	 * First Dimension : Rows
	 * Second Dimension : Variable1 on 0, Variable2 on 1
	 * @param grid
	 * @param var1
	 * @param var2
	 * @return
	 */
    public static double[][] get2DArray(IDataGrid grid, int var1, int var2) {
        double[][] lrn = new double[grid.getNumRows()][2];
        for (int i = 0; i < lrn.length; i++) {
            try {
                lrn[i][0] = ((Double) grid.getPoint(i, var1)).doubleValue();
            } catch (Exception e) {
                lrn[i][0] = ((Integer) grid.getPoint(i, var1)).doubleValue();
            }
            try {
                lrn[i][1] = ((Double) grid.getPoint(i, var2)).doubleValue();
            } catch (Exception e) {
                System.out.println("#-#-#-#-#" + grid.getPoint(i, var2).getClass());
                lrn[i][1] = ((Integer) grid.getPoint(i, var2)).doubleValue();
            }
        }
        return lrn;
    }

    /**
	 * Getting SORTED 2 Dimensional double - Array
	 * It's e.g. needed for qq-plot
	 * First Dimension : Rows
	 * Second Dimension : Variable1 on 0, Variable2 on 1
	 * @param grid
	 * @param var1
	 * @param var2
	 * @return
	 */
    public static double[][] getSorted2DArray(IDataGrid grid, int var1, int var2) {
        double[][] lrn = new double[grid.getNumRows()][2];
        double[] X = new double[grid.getNumRows()];
        double[] Y = new double[grid.getNumRows()];
        for (int i = 0; i < lrn.length; i++) {
            try {
                X[i] = ((Double) grid.getPoint(i, var1)).doubleValue();
            } catch (Exception e) {
                X[i] = ((Integer) grid.getPoint(i, var1)).doubleValue();
            }
            try {
                Y[i] = ((Double) grid.getPoint(i, var2)).doubleValue();
            } catch (Exception e) {
                System.out.println(grid.getPoint(i, var2).getClass());
                Y[i] = ((Integer) grid.getPoint(i, var2)).doubleValue();
            }
        }
        Arrays.sort(X);
        Arrays.sort(Y);
        for (int i = 0; i < lrn.length; i++) {
            lrn[i][0] = X[i];
            lrn[i][1] = Y[i];
        }
        return lrn;
    }

    /**
	 * Converting Double Array to Grid
	 * @param da
	 * @return
	 */
    public static IDataGrid doubleArrayToGrid(double[][] da) {
        IDataGrid grid = new DataGrid();
        int rows = da.length;
        int cols = da[0].length;
        IColumn col;
        for (int j = 0; j < cols; j++) {
            col = new Column();
            col.setType(Double.class);
            col.setLabel(Integer.toString(j));
            grid.addColumn(col);
        }
        IDataRow row;
        try {
            for (int i = 0; i < rows; i++) {
                row = new DataRow();
                row.setColumns(grid.getColumns());
                for (int j = 0; j < cols; j++) {
                    row.setPoint(j, Double.valueOf(da[i][j]));
                }
                grid.addRow(row);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (TypeMismatchException e) {
            e.printStackTrace();
        }
        return grid;
    }

    /**
	 * Getting 3 Dimensional double - Array
	 * @param grid - IDataGrid
	 * @param var[] - Array of ColumnsNo, e.g. Variables 1,3,4
	 */
    public static Vector<double[]> get3DArray(IDataGrid grid, int[] var) {
        Vector<double[]> v = new Vector<double[]>();
        int n = grid.getNumRows();
        System.out.println("var[0]=" + var[0]);
        System.out.println("#rows = " + n);
        for (int dim = 0; dim < 3; dim++) {
            double[] d = new double[n];
            for (int i = 0; i < n; i++) {
                d[i] = ((Double) grid.getPoint(i, var[dim])).doubleValue();
            }
            v.add(d);
        }
        return v;
    }

    /**
	 * Getting n-Dimensional Double -Array
	 * First Dim : Every Row
	 * Second Dim : EVERY Column
	 * @param grid
	 * @return
	 */
    public static double[][] grid2Array(IDataGrid grid) {
        Vector<Integer> doubleCols = new Vector<Integer>();
        for (int i = 0; i < grid.getNumCols(); i++) {
            if (grid.getPoint(0, i) instanceof Double) {
                doubleCols.add(i);
            }
        }
        double[][] lrn = new double[grid.getNumRows()][doubleCols.size()];
        for (int i = 0; i < lrn.length; i++) {
            for (int j = 0; j < lrn[0].length; j++) {
                lrn[i][j] = ((Double) grid.getPoint(i, doubleCols.get(j))).doubleValue();
            }
        }
        return lrn;
    }

    /**
	 * Adds a Class Column to given DataGrid
	 * with numClasses (1..numClasses) to it.
	 */
    public static IDataGrid addRandomClasses(IDataGrid input, int numClasses) {
        IDataGrid output = new DataGrid();
        output.setColumns(input.getColumns());
        IColumn ccolumn = new Column();
        ccolumn.setType(IClusterNumber.class);
        ccolumn.setLabel("Cluster");
        output.addColumn(ccolumn);
        int rows = input.getNumRows();
        int[] clustering = new int[rows];
        for (int i = 0; i < rows; i++) {
            clustering[i] = (i % numClasses) + 1;
        }
        for (int index = 0; index < rows; index++) {
            Vector<IColumn> columnTypeVector = new Vector<IColumn>();
            for (int i = 0; i < output.getNumCols(); i++) {
                IColumn column = new Column();
                column.setType(output.getColumn(i).getType());
                columnTypeVector.add(column);
            }
            IDataRow row = new DataRow();
            row.setColumns(columnTypeVector);
            for (int i = 0; i < input.getNumCols(); i++) {
                try {
                    row.setPoint(i, input.getRow(index).getPoint(i));
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }
            IClusterNumber num = new ClusterNumber(clustering[index]);
            try {
                row.setPoint(row.getColumns().size() - 1, num);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            }
            try {
                output.addRow(row);
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    public static String getColumnName(IDataGrid grid, int index) {
        IColumn col = grid.getColumn(index);
        return col.getLabel();
    }

    /**
	 * Inverting Rows with Columns
	 * @param grid
	 * @return
	 */
    public static IDataGrid transpose(IDataGrid grid) {
        IDataGrid gridT;
        double[][] a = GridUtils.grid2Array(grid);
        int rows = a.length;
        int cols = a[0].length;
        if (rows == cols) {
            for (int i = 0; i < a.length; i++) {
                for (int j = i + 1; j < a[0].length; j++) {
                    double tmp = a[i][j];
                    a[i][j] = a[j][i];
                    a[j][i] = tmp;
                }
            }
            gridT = GridUtils.doubleArrayToGrid(a);
        } else {
            double[][] tmp = new double[cols][rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    tmp[j][i] = a[i][j];
                }
            }
            gridT = GridUtils.doubleArrayToGrid(tmp);
        }
        return gridT;
    }

    public static Vector<double[][]> getFragmentedGrid(int numpts, IDataGrid grid, int var1, int var2) {
        System.out.println("getFragmented...");
        Vector<double[][]> v = new Vector<double[][]>();
        int numr = grid.getNumRows();
        System.out.println("numrows:" + numr);
        int rcnt = 0;
        DataGrid dg;
        for (int pt = 0; pt < numpts; pt++) {
            dg = new DataGrid();
            for (int i = 0; i < numr / numpts; i++) {
                IDataRow row = grid.getRow(rcnt++);
                System.out.println("row : " + row);
                try {
                    dg.addRow(row);
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(dg);
            v.add(GridUtils.get2DArray(dg, var1, var2));
        }
        return v;
    }

    public static IDataGrid getSubGridWithColumns(IDataGrid grid, int column) {
        IDataGrid output = new DataGrid();
        return output;
    }

    /**
	 * The following methods are usefull to get the different clusters
	 * in a DataGrid:
	 *
	 * hasClusterColumns(IDataGrid grid) returns true if the grid has a
	 * Column of type IClusterNumber.
	 *
	 * getClusterColumnPos(IDataGrid grid) returns an ArrayList of Integer
	 * with the Columnnumber()s of the Clustercolumn(s).
	 *
	 * getClusterColumn(IDataGrid grid, int clusterCol returns you an ArrayList
	 * of Integers with the values of the specified Cluster Column in it.
	 *
	 * With getClusterNumbers(IDataGrid grid, int clusterCol) you get
	 * an Array of Integers with all ClusterNumbers of the specified
	 * Clustercolumn in it (or an empty Array if there is no Column of Type
	 * IClusterNumber). If there are more then one Clustercolumn you can
	 * specifiy the number of the Clustercolumn you want to use (0 for the
	 * first, 1 for the second Clustercolumn and so on). Just type 0 for
	 * clusterCol if there is only one Clustercloumn.
	 *
	 * With getCluster(IDataGrid grid, int clusterCol, int clusterNr) you
	 * get a DataGrid with the rows wich have the ClusterNumber clusterNr.
	 * The field clusterCol is the same as in getClusterNumbers.
	 *
	 * @author Torben Ruehl.
	 **/
    public static boolean hasClusterColumns(IDataGrid grid) {
        for (IColumn column : grid.getColumns()) {
            if (column.getType() == IClusterNumber.class) {
                log.debug("DataGrid contains a ClusterColumn");
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Integer> getClusterColumnPos(IDataGrid grid) {
        ArrayList<Integer> erg = new ArrayList<Integer>();
        Integer i = new Integer(0);
        for (IColumn column : grid.getColumns()) {
            if (column.getType() == IClusterNumber.class) {
                log.debug("Clustercolumn added");
                erg.add(i);
            }
            i++;
        }
        if (erg.size() == 0) {
            return null;
        }
        return erg;
    }

    public static ArrayList<Integer> getClusterColumn(IDataGrid grid, int columnNr) {
        ArrayList<Integer> erg = new ArrayList<Integer>();
        ArrayList<Integer> clusterCols = getClusterColumnPos(grid);
        if (clusterCols == null) {
            log.debug("@@@@@@@@@@@@@@@@@@@@@ Return null @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            return null;
        }
        if ((clusterCols.size() > 0) && (columnNr < clusterCols.size())) {
            for (int i = 0; i < grid.getNumRows(); i++) {
                IClusterNumber cn = (IClusterNumber) grid.getPoint(i, clusterCols.get(columnNr));
                Integer j = cn.get();
                erg.add(j);
            }
        }
        if (erg.size() == 0) {
            log.debug("@@@@@@@@@@@@@@@@@@@@@ Return null @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            return null;
        }
        return erg;
    }

    /**
	 * Extracts the clusterinformation from the column of type IClusterNumber with the highest columnnumber in an IDataGrid.
	 * @param grid An IDataGrid Object.
	 * @return an ArrayList of Integer with clusterinformation or null if there is no clusterinformation in the grid.
	 */
    public static ArrayList<Integer> getLastClusterColumn(IDataGrid grid) {
        ArrayList<Integer> colPos = GridUtils.getClusterColumnPos(grid);
        log.debug(colPos);
        return GridUtils.getClusterColumn(grid, colPos.size() - 1);
    }

    public static ArrayList<Integer> getClusterNumbers(IDataGrid grid, int columnNr) {
        ArrayList<Integer> erg = new ArrayList<Integer>();
        ArrayList<Integer> clusterCols = getClusterColumnPos(grid);
        if (clusterCols == null) {
            return null;
        }
        int i = 0;
        if ((clusterCols.size() > 0) && (columnNr < clusterCols.size())) {
            IClusterNumber cn = (IClusterNumber) grid.getPoint(i, clusterCols.get(columnNr));
            Integer j = cn.get();
            if (!erg.contains(j)) {
                erg.add(j);
            }
        }
        if (erg.size() == 0) {
            return null;
        }
        Collections.sort(erg);
        return erg;
    }

    public static ArrayList<Integer> getClusterNumbers2(IDataGrid grid, int clColNo) {
        ArrayList<Integer> erg = new ArrayList<Integer>();
        ArrayList<Integer> clusterIds = getClusterColumn(grid, clColNo);
        int cl = 0;
        for (int i = 0; i < clusterIds.size(); i++) {
            cl = clusterIds.get(i);
            if (!erg.contains(cl)) {
                erg.add(cl);
            }
        }
        Collections.sort(erg);
        return erg;
    }

    public static IDataGrid getCluster(IDataGrid grid, int columnNr, int clusterNr) {
        IDataGrid erg = new DataGrid();
        erg.setColumns(grid.getColumns());
        ArrayList<Integer> clusterCols = getClusterColumnPos(grid);
        if (clusterCols == null) {
            return null;
        }
        ArrayList<Integer> clusterNumbers = getClusterNumbers(grid, columnNr);
        if ((clusterCols.size() > 0) && (columnNr < clusterCols.size()) && (clusterNr <= clusterNumbers.get(clusterNumbers.size() - 1))) {
            for (int i = 0; i < grid.getNumRows(); i++) {
                IClusterNumber cn = (ClusterNumber) grid.getPoint(i, clusterCols.get(columnNr));
                Integer j = cn.get();
                if (j == clusterNr) {
                    try {
                        erg.addRow(grid.getRow(i));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (TypeMismatchException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (erg.getNumRows() == 0) {
            return null;
        }
        return erg;
    }

    public static IDataGrid getCluster2(IDataGrid grid, int columnNr, int clusterNr) {
        IDataGrid erg = new DataGrid();
        erg.setColumns(grid.getColumns());
        ArrayList<Integer> clusterCols = getClusterColumnPos(grid);
        if (clusterCols == null) {
            return null;
        }
        ArrayList<Integer> clusterNumbers = getClusterNumbers2(grid, columnNr);
        if ((clusterCols.size() > 0) && (columnNr < clusterCols.size()) && (clusterNr <= clusterNumbers.get(clusterNumbers.size() - 1))) {
            for (int i = 0; i < grid.getNumRows(); i++) {
                IClusterNumber cn = (ClusterNumber) grid.getPoint(i, clusterCols.get(columnNr));
                Integer j = cn.get();
                if (j == clusterNr) {
                    try {
                        erg.addRow(grid.getRow(i));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (TypeMismatchException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (erg.getNumRows() == 0) {
            return null;
        }
        return erg;
    }

    public static IDataGrid removeRowWithNaN(IDataGrid grid) {
        IDataGrid erg = new DataGrid();
        erg.setColumns(grid.getColumns());
        for (int i = 0; i < grid.getNumRows(); i++) {
            boolean hasNaN = false;
            for (int j = 0; j < grid.getNumCols(); j++) {
                if (grid.isNan(i, j)) {
                    hasNaN = true;
                }
            }
            if (!hasNaN) {
                try {
                    erg.addRow(grid.getRow(i));
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }
        }
        return erg;
    }

    public static IDataGrid retinaToGrid(IRetina retina) {
        IDataGrid erg = new DataGrid();
        for (int i = 0; i < retina.getDim(); i++) {
            erg.addColumn(new Column(Double.class));
        }
        for (int row = 0; row < retina.getNumRows(); row++) {
            for (int column = 0; column < retina.getNumCols(); column++) {
                IDataRow dataRow = new DataRow();
                for (int i = 0; i < retina.getDim(); i++) {
                    dataRow.addColumn(i, new Column(Double.class));
                    try {
                        dataRow.setPoint(i, ((Vector<Double>) retina.getPoint(row, column)).get(i));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (TypeMismatchException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    erg.addRow(dataRow);
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }
        }
        return erg;
    }

    /**
	 * return a single column from the grid
	 * @param grid
	 * @param column
	 * @return
	 */
    public static Vector<Object> getColumn(IDataGrid grid, int column) {
        Vector<Object> erg = new Vector<Object>();
        for (int i = 0; i < grid.getNumRows(); i++) {
            erg.add(grid.getPoint(i, column));
        }
        return erg;
    }

    /**
	 * Shuffle entries of the IDataGrid
	 *
	 * @param grid
	 * @return
	 * @throws TypeMismatchException
	 * @throws
	 */
    public static IDataGrid shuffle(IDataGrid grid) {
        if (grid == null) return null;
        log.debug("Shuffle DataGrid with " + grid.getNumRows() + " rows, " + grid.getNumCols() + " columns");
        IDataGrid out = grid.copy();
        Random rnd = new Random();
        for (int i = 0; i < grid.getNumRows(); i++) {
            int row1 = rnd.nextInt(grid.getNumRows());
            int row2 = rnd.nextInt(grid.getNumRows());
            IDataRow dummy = out.getRow(row1);
            try {
                out.setRow(row1, out.getRow(row2));
                out.setRow(row2, dummy);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            }
        }
        return out;
    }

    /**
	 * create numSplit Samples of the given data
	 *
	 * @param data
	 * @param numSplits
	 * @return Array of numSplits IDataGrid's
	 * @throws Exception
	 */
    public static IDataGrid[] equalSplit(IDataGrid data, int numSplits) throws Exception {
        if (data == null) return null;
        if (numSplits < 1 || numSplits > data.getNumRows()) throw new Exception("Invalid number of Splits. numSplits must be 0<numSplits<=data.getNumOfRows()");
        double weight = (data.getNumRows() / (double) numSplits) / (double) data.getNumRows();
        double splitWeights[] = new double[numSplits];
        for (int i = 0; i < numSplits; i++) {
            splitWeights[i] = weight;
            log.debug("#" + i + " got weight " + weight);
        }
        log.debug("splitWeights: #" + numSplits + ", weight:" + weight);
        return GridUtils.split(data, splitWeights);
    }

    /**
	 * create numSplit Samples of the given data, maybe shuffled before splitted
	 *
	 * @param data
	 * @param numSplits
	 * @param shuffle
	 * @return
	 * @throws Exception
	 */
    public static IDataGrid[] equalSplit(IDataGrid data, int numSplits, boolean shuffle) throws Exception {
        if (shuffle) {
            data = GridUtils.shuffle(data);
        }
        return GridUtils.equalSplit(data, numSplits);
    }

    /**
	 * Split the given datagrid into numSplits partitions and
	 *
	 * @param numSplits
	 * @param splitWeights array of double weights. The sum of weights must be 1 !.
	 * @throws Exception if sum of splitweight != 1
	 */
    public static IDataGrid[] split(IDataGrid data, double[] splitWeights) throws Exception {
        if (data == null) return null;
        int numSplits = splitWeights.length;
        IDataGrid[] samples = new IDataGrid[numSplits];
        int[] sizes = new int[splitWeights.length];
        int[] offsets = new int[splitWeights.length];
        int i = 0, offset = 0;
        double sum = 0;
        log.debug("splitting dataset with " + data.getNumRows() + " rows into " + splitWeights.length + " samples");
        for (i = 0; i < sizes.length; i++) {
            sum += splitWeights[i];
            offsets[i] = offset;
            sizes[i] = (int) Math.round(data.getNumRows() * splitWeights[i]);
            log.debug("Partition #" + i + " with size " + (i < sizes.length - 1 ? sizes[i] : data.getNumRows() - sizes[i] * i) + ", offset " + offsets[i]);
            offset += sizes[i];
            Vector<IColumn> cols = (Vector<IColumn>) data.getColumns().clone();
            samples[i] = new DataGrid();
            samples[i].setColumns(cols);
        }
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(2);
        log.debug("sum of weights: " + fmt.format(sum));
        if (!fmt.format(sum).equals(fmt.format(1.0))) throw new Exception("Cannot Split dataset, Sum of weight != " + fmt.format(1.0) + " Sum==" + fmt.format(sum));
        int j = 0;
        log.debug("fill new sample #" + j + " at row 0");
        for (i = 0; i < data.getNumRows(); i++) {
            if (offsets[j] + sizes[j] <= i && j < sizes.length - 1) {
                log.debug("sizes.length:" + sizes.length);
                j++;
                log.debug("fill new sample #" + j + " at row " + i);
            }
            try {
                samples[j].addRow(data.getRow(i));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            }
        }
        return samples;
    }

    /**
	 * Split the given datagrid into numSplits partitions and
	 *
	 *
	 * @param numSplits
	 * @param splitWeights
	 * @throws Exception
	 */
    public static IDataGrid[] split(IDataGrid data, double[] splitWeights, boolean shuffle) throws Exception {
        if (data == null) return null;
        if (shuffle) {
            data = GridUtils.shuffle(data);
        }
        return GridUtils.split(data, splitWeights);
    }

    /**
	 * merges newData to the data grid
	 * @throws TypeMismatchException if columntypes of the grids provided do not match
	 *
	 */
    public static IDataGrid merge(IDataGrid data, IDataGrid newData) throws TypeMismatchException {
        Vector<IColumn> dataCols = data.getColumns();
        Vector<IColumn> newDataCols = newData.getColumns();
        int index = 0;
        for (IColumn col : dataCols) {
            if (!col.getType().equals(newDataCols.get(index++).getType())) {
                throw new TypeMismatchException("Column #" + index + " don't match (" + col.getType().getName() + "!=" + newDataCols.get(index).getType().getName() + ")");
            }
        }
        for (int i = 0; i < newData.getNumRows(); i++) {
            IDataRow row = newData.getRow(i);
            data.addRow(row);
        }
        return data;
    }

    /**
	 * Checks if the ClassType of a column in an IDataGrid is of Type columnClass
	 * and writes its index into a Vector of Type Integer
	 * @param data the IDataGrid to be checked
	 * @param columnClass the type of the IColumn wich is searched
	 * @return a Vector<Integer> wich holds the indexes of the columns of type columnClass
	 */
    public static Vector<Integer> checkColumns(IDataGrid data, Class columnClass) {
        Vector<Integer> erg = new Vector<Integer>();
        int index = 0;
        for (IColumn column : data.getColumns()) {
            if (column.getType() == columnClass) erg.add(index);
            index++;
        }
        return erg;
    }

    public static IDataGrid generate(int columns, int rows) {
        DataGrid data = new DataGrid();
        for (int i = 0; i < columns; i++) {
            Column col = new Column();
            col.setType(Double.class);
            col.setLabel("GenCol" + i);
            col.setComment("Generated by vademecum.data.GridUtils.generate()");
            data.addColumn(col);
        }
        try {
            Random r = new Random();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    data.setPoint(i, j, r.nextDouble());
                }
            }
        } catch (TypeMismatchException e) {
            log.error(e.getMessage());
            return null;
        }
        return data;
    }
}
