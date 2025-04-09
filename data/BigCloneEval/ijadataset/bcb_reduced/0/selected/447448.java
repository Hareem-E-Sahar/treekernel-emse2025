package mil.army.usace.ehlschlaeger.rgik.core;

import java.io.Serializable;
import java.util.Random;

/**
 * Defines a normalized monotonically increasing function over the cells of a
 * lattice. Cells are ordered in English book-reading order (left to right and
 * top to bottom,) and in this order the value of each cell must be at least as
 * great as the previous cell. The first cell (northwest corner) can have any
 * value >= 0.0, but the southeast corner must be == 1.0.
 * <p>
 * One use for CDFs is to allow a single random number to select a cell from a
 * lattice where the probability of a cell being selected is equal to the value
 * in that cell. <code>createNormalized()</code> creates a CDF from just such a
 * table.
 * <p>
 * Copyright <a href="http://faculty.wiu.edu/CR-Ehlschlaeger2/">Charles R.
 * Ehlschlaeger</a>, work: 309-298-1841, fax: 309-298-3003, This software is
 * freely usable for research and educational purposes. Contact C. R.
 * Ehlschlaeger for permission for other purposes. Use of this software requires
 * appropriate citation in all published and unpublished documentation.
 */
public class CumulativeDistributionFunction extends GISLattice implements Serializable {

    private int lastCell;

    /**
	 * Creates a new CDF that is an exact copy of given lattice.
	 * Lattice must conform to the rules for a CDF.
	 * 
	 * @param data lattice to copy
	 */
    public CumulativeDistributionFunction(GISLattice data) {
        super(data);
        double largest = data.getCellValue(0, 0);
        for (int r = 0; r < data.getNumberRows(); r++) {
            for (int c = 0; c < data.getNumberColumns(); c++) {
                if (data.isNoData(r, c)) {
                    throw new IllegalArgumentException("all grid cells must have values.");
                }
                double thisValue = data.getCellValue(r, c);
                if (thisValue < largest) {
                    String msg = "All grid cells must have values" + "that are >= to the adjacent grid cell due west of it. (Cells on the west edge must be >= to the" + "easternmost cell of the row north of the cell.";
                    throw new IllegalArgumentException(msg);
                }
                if (thisValue > 1.0) {
                    throw new IllegalArgumentException("grid cells cannot have values > 1.0");
                }
                setCellValue(r, c, thisValue);
                if (thisValue > largest) {
                    largest = thisValue;
                }
            }
        }
        if (largest < 1.0) {
            throw new IllegalArgumentException("Southeast corner must == 1.0.");
        }
        lastCell = getNumberRows() * getNumberColumns() - 1;
    }

    /**
     * Create a sized but blank CDF. Note that this CDF violates the rules, and
     * must be initialized before it can be used.
     * 
     * @param grid dimensions of new CDF
     * @param cellValue initial value for all cells
     */
    public CumulativeDistributionFunction(GISGrid grid, double cellValue) {
        super(grid, cellValue);
        lastCell = getNumberRows() * getNumberColumns() - 1;
    }

    /**
     * Create new CDF by summing and normalizing a lattice. No-data cells are
     * considered zero.  All values must >= zero.
     * 
     * @param lattice values to sum and normalize
     * @return new initialized CDF, or null if sum of contents of lattice is zero
     */
    public static CumulativeDistributionFunction createNormalized(GISLattice lattice) {
        double sum = 0.0;
        for (int r = 0; r < lattice.getNumberRows(); r++) {
            for (int c = 0; c < lattice.getNumberColumns(); c++) {
                if (lattice.isNoData(r, c) == false) {
                    double v = lattice.getCellValue(r, c);
                    if (v < 0) {
                        String msg = String.format("All values must be >= 0. row %d/col %d is %d.", r, c, v);
                        throw new IllegalArgumentException(msg);
                    }
                    sum += v;
                }
            }
        }
        if (sum == 0) return null;
        CumulativeDistributionFunction cdf = new CumulativeDistributionFunction(lattice, 0);
        double thisSum = 0.0;
        for (int r = 0; r < lattice.getNumberRows(); r++) {
            for (int c = 0; c < lattice.getNumberColumns(); c++) {
                if (lattice.isNoData(r, c) == false) {
                    thisSum += lattice.getCellValue(r, c);
                }
                cdf.setCellValue(r, c, thisSum / sum);
            }
        }
        double se = cdf.getCellValue(lattice.getNumberRows() - 1, lattice.getNumberColumns() - 1);
        if (!(Math.abs(se - 1.0) < 0.000001)) throw new DataException("Southeast corner must == 1.0, but is " + se);
        return cdf;
    }

    /**
     * Find the linear index of the cell with the given value. Call
     * <code>getGridCellRow</code> and <code>getGridCellColumn</code> for the
     * row and column values of this index.
     * 
     * @param value
     *            number to find
     * @return linear index (in book-reading order) of cell
     */
    public int getGridCellID(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("value must be in the range [0,1].");
        }
        int minCell = -1;
        int maxCell = lastCell;
        while (maxCell - 1 > minCell) {
            int checkCell = (maxCell + minCell) / 2;
            int row = getGridCellRow(checkCell);
            int col = getGridCellColumn(checkCell, row);
            double cellValue = getCellValue(row, col);
            if (value < cellValue) {
                maxCell = checkCell;
            } else {
                minCell = checkCell;
            }
        }
        return maxCell;
    }

    /**
	 * Compute row number for index returned by <code>getGridCellID</code>.
	 * 
	 * @param id linear index of cell
	 * @return row number of cell
	 */
    public int getGridCellRow(int id) {
        int row = id / getNumberColumns();
        return row;
    }

    /**
     * Compute column number for index returned by <code>getGridCellID</code>.
     * 
     * @param id linear index of cell
     * @return column number of cell
     */
    public int getGridCellColumn(int id) {
        int row = getGridCellRow(id);
        return getGridCellColumn(id, row);
    }

    /**
	 * Slightly faster version of <code>getGridCellColumn(id)</code>
	 * if you already have the row.
	 */
    public int getGridCellColumn(int id, int row) {
        int col = id - row * getNumberColumns();
        return col;
    }

    /**
	 * Move a point to a random location with probability given by our contents.
	 *  
	 * @param point GISPoint to adjust
	 * @param random source of random numbers
	 */
    public void locateRandomly(GISPoint point, Random random) {
        double value = random.nextDouble();
        int cdfID = getGridCellID(value);
        int row = getGridCellRow(cdfID);
        int col = getGridCellColumn(cdfID, row);
        double newEasting = ((random.nextDouble() - .5) * getEWResolution() + getCellCenterEasting(row, col));
        double newNorthing = ((random.nextDouble() - .5) * getNSResolution() + getCellCenterNorthing(row, col));
        point.setEasting(newEasting);
        point.setNorthing(newNorthing);
    }
}
