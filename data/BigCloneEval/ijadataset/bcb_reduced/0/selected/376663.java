package org.jogre.client.awt.AbstractHexBoards;

import org.jogre.client.awt.*;
import java.awt.*;
import java.util.Observer;

/**
 * <p>This graphical component is a base component for hexagonal boards.
 * Various attributes can be set such as the number of cells, the cell
 * size and color.</p>
 *
 * <p>This supports rectangular arrays of hexagons</p>
 *
 * <p>There is a single logical board layout that this component uses.
 * However, there are parameters for allowing the view of the board to
 * be manipulated to provide multiple different views.  The board may be
 * rotated in 90-degree increments; it may be flipped around; it may have
 * half the top and/or bottom hexagons trimmed off.</p>
 *
 * <p>The canonical arrangement of the board of hexagons is this:
 * Hexagons are referred to by a (col, row) coordinate.  Columns run up/down
 * and rows run across.  Therefore, a column # is the distance across the board
 * (similar to an x-coordinate) and a row # is the distance down the board
 * (similar to a y-coordinate).  So (col, row) can be thought of as (x, y).</p>
 *
 * <p>Here is the "One True Way" of laying out hexagons (logically):
 *<pre>
 *  (0, 0)          (2, 0)          (4, 0)
 *          (1, 0)          (3, 0)          (5, 0)
 *  (0, 1)          (2, 1)          (4, 1)
 *          (1, 1)          (3, 1)          (5, 1)
 *  (0, 2)          (2, 2)          (4, 2)
 *          (1, 2)          (3, 2)          (5, 2)
 *</pre>
 *
 * The hexagons are layed out such that their top & bottom lines are horizontal
 * and the side lines are angled.  This is referred to as the "horizontal" configuration.
 * If the board is rotated by 90 or 270 degrees, then the hexagons will have vertical
 * sides and angled tops & bottoms.  This is referred to as the "vertical" configuration.</p>
 *
 * <p>The size of the hexagons is determined by a bounding rectangle that is
 * provided when the board is created.  The Hexagon looks like this:
 *<pre>
 *    +---=======----       * The outer rectangle is the bounding box.  It can
 *    |  /       \  |         be stretched out to make elongated hexagons.
 *    | /         \ |       * The length of the top & bottom sides (shown by
 *    |/           \|         the = characters) is provided.  It is centered
 *    |\           /|         along the top & bottom of the bounding box.
 *    | \         / |       * The center of the left & right sides are where the
 *    |  \       /  |         hexagon touches the bounding box.
 *    ----=======----       * The + in the upper left corner is the "anchor" point
 *                            for a given hexagon.
 *</pre>
 * <p>When creating a hex board, you provide a number of parameters about the
 * board.  These parameters are: </p>
 *
 * <p> * Number of columns and number of rows.</p>
 * <p> * A trimLow parameter, that, when true, will cause the hexagons in row 0
 *       in even columns to be removed from the board.   These are the hexagons
 *       at locations (0,0), (2,0), (4,0), etc...</p>
 * <p> * A trimHigh parameter, that, when true, will cause the hexagons in the
 *       last row in odd columns to be removed from the board.  For the above
 *       board, these are the hexagons at locations (1,2), (3,2), and (5,2).</p>
 * <p> * The bounding box & length of the top side.  These are in pixels and
 *       determine the screen size of the board.</p>
 *
 * <p>After creation, the view of the board can be rotated & flipped by using
 *     the method setOrientation(int rotation, boolean flip).  Rotation is in
 *     units of 90 degrees clockwise.  If flip is true, then the board will be
 *     flipped in the row direction.  (So, setOrientation(0, true) will result
 *     in space (0,2) being in the upper left corner.)</p>
 *
 * <p>This component allows setting of an "inset" and "outset" parameters.
 *     The inset is the distance that the upper left corner of the actual hexagonal
 *     board array is offset from the upper left corner of the component.
 *     The outset is the distance from the lower right corner of the actual hexagonal
 *     board array to the lower right corner of the component.
 *     Changing these allow the board to be placed inside a component and still
 *     allow room for other things around it (such as a border.)  Note that the
 *     default drawing routine draws a 1-pixel border around the hexagons.  Because
 *     these pixels are drawn below and to the right of the coordinate, the outset
 *     must be at least (1,1) in order for these borders to be not chopped off
 *     along the right & bottom of the board.  The default inset is (0,0).  The
 *     default outset is (1,1).</p>
 *
 * <p>This module provides a default paintComponent() method that will draw the
 *     board.  A subclass can override this to do custom painting of the entire
 *     board.</p>
 *
 * <p>The default paintComponent() calls a drawAHex() method to draw each
 *     hexagon of the board.  A subclass can override just this method to
 *     provide a custom drawing of each hex, but let this class paint the board.</p>
 *
 * <p>The default drawAHex() method calls a getHexColor() method to get the
 *     color that it should use to fill in the hex.  A subclass can override
 *     just this method to change the colors of the hex, but still letting
 *     this class paint the hex's.</p>
 *
 * <p>See the sample game "hex" for example of a game that uses this board type.</p>
 *
 * @author  Richard Walter
 * @version Beta 0.3
 */
public abstract class AbstractHexBoardComponent extends JogreComponent implements Observer {

    /** Constant to show a cell point is off the board (not selected). */
    public static final Point OFF_SCREEN_POINT = new Point(-1, -1);

    /** Size of the bounding box around the hexagons (in pixels). */
    protected Dimension cellBoundingDimension;

    /** The length of the controlling line of the hex (in pixels).  */
    protected int controlLineLength;

    /** The distance from the upper left corner of the component to the upper left
		corner of the hex array. */
    protected Dimension inset = new Dimension(0, 0);

    /** The distance from the lower right corner of the hex array to the lower
	    right corner of the component. */
    protected Dimension outset = new Dimension(1, 1);

    /** Number of logical rows in the board. */
    protected int numOfLogicalRows;

    /** Number of logical columns in the board. */
    protected int numOfLogicalCols;

    /** Flag that indicates if the logical top hex's are trimmed. */
    protected boolean logicalTrimLow;

    /** Flag that indicates if the logical bottom hex's are trimmed. */
    protected boolean logicalTrimHigh;

    /** Current orientation, both rotation & flip. */
    protected int rot = 0;

    protected boolean userFlip = false;

    /** Colour of a cell. */
    protected Color cellColour;

    /** Colour of a cell's outline. */
    protected Color outlineColour;

    /** Colour to outline the highlighted cell with. */
    protected Color highlightColour;

    /** Stores the value of a mouse down. **/
    protected Point pressedPoint = OFF_SCREEN_POINT;

    /** Stores the value of where the point is being dragged. */
    protected Point dragPoint = OFF_SCREEN_POINT;

    /** Stores the value of which point should be highlighted on the board. */
    protected Point highlightPoint = OFF_SCREEN_POINT;

    /** A representative hexagon used while drawing the board using the default
	    methods here. */
    protected Polygon baseHexPoly;

    /** Horizontal & Vertical hexagons.
	   baseHexPoly is set to one of these depending on the orientation of the board. */
    protected Polygon HBaseHexPoly, VBaseHexPoly;

    /** The distance from the upper/left corner of the bounding box to the start
	    of the controlling line. */
    protected int boundInset;

    /** The width of the "superRect" that encloses two hexes.  It is used
	    to convert between board & screen coordinates. */
    protected int superWidth;

    /** Half of the height & width of the bounding box.
	    These are computed once and then saved rather than dividing by 2 alot. */
    protected int halfHeight;

    protected int halfWidth;

    private int i_hh;

    private int w_hh;

    /** Flag that indicates the orientation of each individual hexagon.
	    False => Hexagons have their controlling line running horizontally.
		True  => Hexagons have their controlling line running vertically.
	*/
    protected boolean verticalOrientation = false;

    private boolean physicalTrimLow;

    private boolean physicalTrimHigh;

    private boolean invertCols, invertRows;

    private int oddRowOffset, evenRowOffset;

    /**
	 * Constructor for a hex array.
	 *
	 * @param numOfCols           Number of columns in the board.
	 * @param numOfRows           Number of rows in the board.
	 * @param cellDim             The bounding box for each cell.
	 * @param controlLineLength   The length of the control line of each hexagon.
	 * @param trimLow             If true, trim the top row of hex's.
	 * @param trimHigh            If true, trim the bottom row of hex's.
	 */
    public AbstractHexBoardComponent(int numOfCols, int numOfRows, Dimension cellDim, int controlLineLength, boolean trimLow, boolean trimHigh) {
        super();
        this.numOfLogicalRows = numOfRows;
        this.numOfLogicalCols = numOfCols;
        this.cellBoundingDimension = new Dimension(cellDim);
        this.controlLineLength = controlLineLength;
        this.logicalTrimLow = trimLow;
        this.logicalTrimHigh = trimHigh;
        this.boundInset = (cellBoundingDimension.width - controlLineLength) / 2;
        this.superWidth = cellBoundingDimension.width + controlLineLength;
        this.halfHeight = cellBoundingDimension.height / 2;
        this.halfWidth = cellBoundingDimension.width / 2;
        this.i_hh = this.boundInset * this.halfHeight;
        this.w_hh = cellBoundingDimension.width * this.halfHeight;
        createBaseHexPolys();
        setPhysicalParamsFromLogical();
        setPreferredSize(getBoardComponentDim());
        setColours(new Color(178, 178, 178), new Color(0, 0, 0), new Color(255, 255, 0));
    }

    /**
	 * This will create the polygons that describe a hexagon for the
	 * board.  The (0,0) point of the polygon lies at the basePoint
	 * for the hexagon at space (0,0) of the board.
	 */
    private void createBaseHexPolys() {
        int[] xPoints = { 0, 0, 0, 0, 0, 0 };
        int[] yPoints = { 0, 0, 0, 0, 0, 0 };
        int p2 = cellBoundingDimension.width - boundInset;
        xPoints[0] = xPoints[4] = boundInset;
        xPoints[1] = xPoints[3] = p2;
        xPoints[2] = cellBoundingDimension.width;
        yPoints[2] = yPoints[5] = halfHeight;
        yPoints[3] = yPoints[4] = cellBoundingDimension.height;
        HBaseHexPoly = new Polygon(xPoints, yPoints, 6);
        VBaseHexPoly = new Polygon(yPoints, xPoints, 6);
    }

    /**
	 * Given the current values of the board, this will return the size of
	 * the entire board in pixels.  This includes the inset & outset borders
	 * around the array.
	 *
	 * @return a dimension of the whole board
	 */
    public Dimension getBoardComponentDim() {
        Dimension hexArraySize = calcHexArrayDimension();
        if (verticalOrientation) {
            invertDimension(hexArraySize);
        }
        hexArraySize.setSize(hexArraySize.width + inset.width + outset.width, hexArraySize.height + inset.height + outset.height);
        return hexArraySize;
    }

    private Dimension calcHexArrayDimension() {
        int heightHalves = 2 * numOfLogicalRows + 1;
        heightHalves -= (logicalTrimLow ? 1 : 0);
        heightHalves -= (logicalTrimHigh ? 1 : 0);
        int height = ((cellBoundingDimension.height * heightHalves) + 1) / 2;
        int numWholeWidths = (numOfLogicalCols + 1) / 2;
        int numSmallWidth = numOfLogicalCols - numWholeWidths;
        int width = (numWholeWidths * cellBoundingDimension.width) + (numSmallWidth * controlLineLength);
        return new Dimension(width, height);
    }

    /**
	 * Give a new inset dimension to the component.  The inset is the
	 * distance from the upper left corner of the component to the upper
	 * left corner of the hex array.
	 *
	 * @param newInset   The new inset to use with the component.
	 */
    public void setInset(Dimension newInset) {
        this.inset = new Dimension(newInset);
    }

    /**
	 * Give a new outset dimension to the component.  The outset is the
	 * distance from the lower right corner of the hex array to the lower
	 * right corner of the component.
	 *
	 * Outsets can be used to place the array within a larger component, or
	 * to increase the size of the component to allow space for drawing outlines
	 * of hexagons.  The default outset is (1, 1) so that all hexagons can
	 * be outlined with a width 1 stroke and still show up.
	 *
	 * @param newOutset   The new outset to use with the component.
	 */
    public void setOutset(Dimension newOutset) {
        this.outset = new Dimension(newOutset);
    }

    /**
	 * Draws the board.
	 * This method calls the drawAHex() method to draw each hex.
	 *
	 * To customize the board drawing, the subclass can override
	 * either this entire method, or just the drawAHex() method.
	 *
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
    public void paintComponent(Graphics g) {
        for (int r = 0; r < numOfLogicalRows; r++) {
            for (int c = 0; c < numOfLogicalCols; c++) {
                if (existsOnBoard(c, r)) {
                    drawAHex(g, c, r, getScreenAnchorFor(c, r));
                }
            }
        }
        if (existsOnBoard(highlightPoint)) {
            Point highlightAnchor = getScreenAnchorFor(highlightPoint);
            this.baseHexPoly.translate(highlightAnchor.x, highlightAnchor.y);
            g.setColor(this.highlightColour);
            g.drawPolygon(this.baseHexPoly);
            this.baseHexPoly.translate(-highlightAnchor.x, -highlightAnchor.y);
        }
    }

    /**
	 * Draws a Hexagon on the board.
	 * This calls the getHexColor() method to get the color to
	 * be used to fill in each hex.
	 *
	 * To customize the board drawing, the subclass can override this
	 * method to draw board spaces however it would like to.  Or, the
	 * subclass can override the getHexColor() method to just change
	 * the color used to draw the hex.
	 *
	 * @param g            The grahics context to draw on.
	 * @param col          The column of the hex to draw.
	 * @param row          The row of the hex to draw.
	 * @param anchorPoint  The anchor point, in pixel coordinates, of where to
	 *                     draw the hex at.
	 */
    public void drawAHex(Graphics g, int col, int row, Point anchorPoint) {
        this.baseHexPoly.translate(anchorPoint.x, anchorPoint.y);
        g.setColor(getHexColor(col, row));
        g.fillPolygon(this.baseHexPoly);
        g.setColor(this.outlineColour);
        g.drawPolygon(this.baseHexPoly);
        this.baseHexPoly.translate(-anchorPoint.x, -anchorPoint.y);
    }

    /**
	 * This method returns the color that should be used to fill in the space
	 * at the given logical col, row.
	 *
	 * This standard method just returns the basic cellColour provided.
	 *
	 * To customize the board drawing, a subclass can override this method
	 * and use the col, row to determine what color the hex should be filled
	 * with.
	 *
	 * @param (col, row)   The logical position of the hex on the board, whose
	 *                     color is wanted.
	 * @return the color that the given hex should be drawn with.
	 */
    public Color getHexColor(int col, int row) {
        return this.cellColour;
    }

    /**
	 * Sets the orientation of the visual view of the board to the
	 * given parameters.
	 *
	 * @param rotation    Number of 90-degree clockwise rotations to rotate
	 *                    the board.
	 * @param flip        If true, then the board has rows flipped.
	 * @return a value that indicates if the visual board size has changed
	 *         as a result of this orientation change.
	 */
    public boolean setOrientation(int rotation, boolean flip) {
        this.rot = (rotation & 0x03);
        this.userFlip = flip;
        return setPhysicalParamsFromLogical();
    }

    private boolean[] vertOrientTable = { false, true, false, true };

    private boolean[] invertColsTable = { false, false, true, true };

    private boolean[][] invertRowsTable = { { false, true, true, false }, { true, false, false, true } };

    private int[][][][] oddRowTable = { { { { 0, 1, 1, 1 }, { 0, 1, 1, 0 } }, { { 1, 0, 1, 1 }, { 1, 0, 0, 1 } } }, { { { 0, 1, 1, 0 }, { 0, 1, 1, 0 } }, { { 1, 0, 0, 1 }, { 1, 0, 0, 1 } } }, { { { 0, 2, 1, 1 }, { 0, 2, 2, 0 } }, { { 2, 0, 1, 1 }, { 2, 0, 0, 2 } } }, { { { 0, 2, 1, 0 }, { 0, 2, 2, 0 } }, { { 2, 0, 0, 1 }, { 2, 0, 0, 2 } } } };

    private int[][][][] evenRowTable = { { { { 0, 0, 1, 0 }, { 0, 0, 0, 0 } }, { { 0, 0, 0, 1 }, { 0, 0, 0, 0 } } }, { { { 0, 0, 1, -1 }, { 0, 0, 0, 0 } }, { { 0, 0, -1, 1 }, { 0, 0, 0, 0 } } }, { { { 0, 1, 1, 0 }, { 0, 1, 1, 0 } }, { { 1, 0, 0, 1 }, { 1, 0, 0, 1 } } }, { { { 0, 1, 1, -1 }, { 0, 1, 1, 0 } }, { { 1, 0, -1, 1 }, { 1, 0, 0, 1 } } } };

    private boolean[][][][] physicalTrimLowTable = { { { { false, true, false, true }, { false, true, true, false } }, { { true, false, true, false }, { true, false, false, true } } }, { { { true, true, false, false }, { true, true, true, true } }, { { true, true, false, false }, { true, true, true, true } } }, { { { false, false, true, true }, { false, false, false, false } }, { { false, false, true, true }, { false, false, false, false } } }, { { { true, false, true, false }, { true, false, false, true } }, { { false, true, false, true }, { false, true, true, false } } } };

    private boolean[][][][] physicalTrimHighTable = { { { { false, true, false, true }, { false, true, true, false } }, { { true, false, true, false }, { true, false, false, true } } }, { { { false, false, true, true }, { false, false, false, false } }, { { false, false, true, true }, { false, false, false, false } } }, { { { true, true, false, false }, { true, true, true, true } }, { { true, true, false, false }, { true, true, true, true } } }, { { { true, false, true, false }, { true, false, false, true } }, { { false, true, false, true }, { false, true, true, false } } } };

    private boolean setPhysicalParamsFromLogical() {
        boolean oldVertOrient = verticalOrientation;
        int columnIndex = (numOfLogicalCols & 0x01);
        int userFlipIndex = (userFlip ? 1 : 0);
        int logicalTrimIndex = (logicalTrimLow ? 1 : 0) + (logicalTrimHigh ? 2 : 0);
        verticalOrientation = vertOrientTable[rot];
        physicalTrimLow = physicalTrimLowTable[logicalTrimIndex][userFlipIndex][columnIndex][rot];
        physicalTrimHigh = physicalTrimHighTable[logicalTrimIndex][userFlipIndex][columnIndex][rot];
        invertCols = invertColsTable[rot];
        invertRows = invertRowsTable[userFlipIndex][rot];
        oddRowOffset = oddRowTable[logicalTrimIndex][userFlipIndex][columnIndex][rot];
        evenRowOffset = evenRowTable[logicalTrimIndex][userFlipIndex][columnIndex][rot];
        baseHexPoly = (verticalOrientation ? VBaseHexPoly : HBaseHexPoly);
        return (verticalOrientation != oldVertOrient);
    }

    private Point logicalToPhysical(int logicalCol, int logicalRow) {
        if (existsOnBoard(logicalCol, logicalRow)) {
            int physicalCol = invertCols ? numOfLogicalCols - logicalCol - 1 : logicalCol;
            int rowOffset = ((logicalCol & 0x01) == 0) ? evenRowOffset : oddRowOffset;
            int physicalRow = invertRows ? (numOfLogicalRows - logicalRow) - rowOffset : logicalRow + rowOffset;
            return new Point(physicalCol, physicalRow);
        } else {
            return new Point(OFF_SCREEN_POINT);
        }
    }

    private Point physicalToLogical(int physicalCol, int physicalRow) {
        int logicalCol = invertCols ? numOfLogicalCols - physicalCol - 1 : physicalCol;
        int rowOffset = ((logicalCol & 0x01) == 0) ? evenRowOffset : oddRowOffset;
        int logicalRow = invertRows ? (numOfLogicalRows - physicalRow) - rowOffset : physicalRow - rowOffset;
        if (existsOnBoard(logicalCol, logicalRow)) {
            return new Point(logicalCol, logicalRow);
        } else {
            return new Point(OFF_SCREEN_POINT);
        }
    }

    /**
	 * This method returns a screen coordinate (in the top-left) from a board
	 * point.
	 *
	 * @param boardX    0 < boardX < numOfColumns
	 * @param boardY    0 < boardY < numOfRows
	 * @return          Screen coordinates for the anchor point of the hex.
	 */
    public Point getScreenAnchorFor(int boardX, int boardY) {
        Point anchorPoint = getHorizontalScreenAnchorFor(logicalToPhysical(boardX, boardY));
        if (verticalOrientation) {
            invertPoint(anchorPoint);
        }
        anchorPoint.translate(inset.width, inset.height);
        return anchorPoint;
    }

    private Point getHorizontalScreenAnchorFor(Point physicalBoardPoint) {
        int anchorX, anchorY;
        boolean physicalColOdd = ((physicalBoardPoint.x & 1) != 0);
        int extCols = physicalBoardPoint.x / 2;
        anchorX = (extCols * superWidth);
        anchorX += (physicalColOdd ? (controlLineLength + boundInset) : 0);
        int heightHalves = 2 * physicalBoardPoint.y;
        heightHalves -= (physicalTrimLow ? 1 : 0);
        heightHalves += (physicalColOdd ? 1 : 0);
        anchorY = ((cellBoundingDimension.height * heightHalves) + 1) / 2;
        return new Point(anchorX, anchorY);
    }

    /**
	 * This method returns a screen coordinate (in the top-left) from a board
	 * point.
	 *
	 * @param boardPoint   Point on board (col, row) coordinates.
	 * @return             Screen coordinates for the anchor point of the hex.
	 */
    public Point getScreenAnchorFor(Point boardPoint) {
        return getScreenAnchorFor(boardPoint.x, boardPoint.y);
    }

    /**
	 * This method returns a screen coordinate in the center of a hex
	 * from a board point.
	 *
	 * @param boardX    0 < boardX < numOfColumns
	 * @param boardY    0 < boardY < numOfRows
	 * @return          Screen coordinates for the center point of the hex.
	 */
    public Point getScreenCenterFor(int boardX, int boardY) {
        Point thePoint = getScreenAnchorFor(boardX, boardY);
        thePoint.translate(halfWidth, halfHeight);
        return thePoint;
    }

    /**
	 * This method returns a screen coordinate in the center of a hex
	 * from a board point.
	 *
	 * @param boardPoint   Point on board (col, row) coordinates.
	 * @return             Screen coordinates for the anchor point of the hex.
	 */
    public Point getScreenCenterFor(Point boardPoint) {
        return getScreenCenterFor(boardPoint.x, boardPoint.y);
    }

    /**
	 * Determine if the hex at the given (col, row) exists on the board.
	 * Because of trimLow & trimHigh, some hex's within the array
	 * should not be drawn.
	 *
	 * @param (col, row)    The location to check for existance.
	 * @return true => hex is on the board.
	 */
    public boolean existsOnBoard(int col, int row) {
        if ((col < 0) || (col >= numOfLogicalCols) || (row < 0) || (row >= numOfLogicalRows)) {
            return false;
        }
        boolean colEven = ((col & 1) == 0);
        if ((logicalTrimLow & colEven && (row == 0)) || (logicalTrimHigh & !colEven && (row == (numOfLogicalRows - 1)))) {
            return false;
        }
        return true;
    }

    /**
	 * Determine if the hex at the given logical board point exists
	 * on the board.
	 *
	 * @param boardPoint    The location to check for existance.
	 * @return true => hex is on the board.
	 */
    public boolean existsOnBoard(Point boardPoint) {
        return existsOnBoard(boardPoint.x, boardPoint.y);
    }

    /**
	 * This method returns a board coordinate from a screen point.
	 *
	 * @param screenX   X coordinate in pixels.
	 * @param screenY   Y coordinate in pixels.
	 * @return          Return board coordinates
	 */
    public Point getBoardCoords(int screenX, int screenY) {
        screenX -= (inset.width + 1);
        screenY -= (inset.height + 1);
        if (verticalOrientation) {
            return getHorizBoardCoords(screenY, screenX);
        } else {
            return getHorizBoardCoords(screenX, screenY);
        }
    }

    private Point invertPoint(Point p) {
        p.setLocation(p.y, p.x);
        return p;
    }

    private Dimension invertDimension(Dimension d) {
        d.setSize(d.height, d.width);
        return d;
    }

    private Point getHorizBoardCoords(int screenX, int screenY) {
        screenY += (physicalTrimLow ? halfHeight : 0);
        int col = (screenX / superWidth) * 2;
        int row = (screenY / cellBoundingDimension.height);
        int XinCell = screenX % superWidth;
        int YinCell = screenY % cellBoundingDimension.height;
        if (XinCell >= cellBoundingDimension.width) {
            if (YinCell >= halfHeight) {
                return physicalToLogical(col + 1, row);
            } else {
                return physicalToLogical(col + 1, row - 1);
            }
        }
        int f1 = boundInset * YinCell;
        int f2 = halfHeight * XinCell;
        if ((f1 + f2) <= i_hh) {
            return physicalToLogical(col - 1, row - 1);
        }
        if ((f1 - f2) >= i_hh) {
            return physicalToLogical(col - 1, row);
        }
        if ((f1 - f2) <= (i_hh - w_hh)) {
            return physicalToLogical(col + 1, row - 1);
        }
        if ((f1 + f2) >= (i_hh + w_hh)) {
            return physicalToLogical(col + 1, row);
        }
        return physicalToLogical(col, row);
    }

    /**
	 * Return the size of each hex in pixels.
	 *
	 * @return  bounding dimensions of the hex.
	 */
    public Dimension getHexSize() {
        return this.cellBoundingDimension;
    }

    /**
	 * Return the point where a user has pressed on the board.
	 *
	 * @return   Point where user has pressed.
	 */
    public Point getPressedPoint() {
        return this.pressedPoint;
    }

    /**
	 * Return the drag point of a mouse.
	 *
	 * @return  Point where user has dragged.
	 */
    public Point getDragPoint() {
        return this.dragPoint;
    }

    /**
	 * Return the highlighted point.
	 *
	 * @param highlightPoint    Point on board that is highlighted
	 */
    public Point getHighlightPoint() {
        return this.highlightPoint;
    }

    /**
	 * Sets pressed point in the board.
	 *
	 * @param pressedPoint  Point on board where pressed.
	 */
    public void setPressedPoint(Point pressedPoint) {
        this.pressedPoint = pressedPoint;
    }

    /**
	 * Sets the drag point.
	 *
	 * @param dragPoint    Point on board where dragged.
	 */
    public void setDragPoint(Point dragPoint) {
        this.dragPoint = dragPoint;
    }

    /**
	 * Sets the highlighted point.
	 *
	 * @param highlightPoint    Point on board to be highlighted
	 */
    public void setHighlightPoint(Point newHighlightPoint) {
        if (!newHighlightPoint.equals(this.highlightPoint)) {
            this.highlightPoint = newHighlightPoint;
            repaint();
        }
    }

    /**
	 * Reset the dragPoint and pressedPoint variables.
	 */
    public void resetPoints() {
        this.dragPoint = OFF_SCREEN_POINT;
        this.pressedPoint = OFF_SCREEN_POINT;
        setHighlightPoint(OFF_SCREEN_POINT);
    }

    /**
	 * Set up colours for this board.
	 *
 	 * @param cellColour             Colour for the cells.
 	 * @param outlineColour          Colour to outline the cells.
	 * @param highlightColour        Colour to highlight the cell.
	 */
    public final void setColours(Color cellColour, Color outlineColour, Color highlightColour) {
        this.cellColour = cellColour;
        this.outlineColour = outlineColour;
        this.highlightColour = highlightColour;
    }
}
