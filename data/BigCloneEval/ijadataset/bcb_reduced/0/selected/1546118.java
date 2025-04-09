package net.sf.easylayouts;

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 * A layout manager that arranges items on a row by row basis.  When adding a component
 * to a row, alignment and both vertical and horizontal growth properties can be set.
 * 
 * Do not use a RowLayout to lay out a container that is placed
 * inside of a container laid out with BoxLayout.  It is best to use
 * RowLayout as the outer manager with two rows for this type of use.  BoxLayout
 * does not pass on container growth.
 *
 * If you insert an element in a row with more than one element,
 * and that inserted element is in some container laid out with FlowLayout,
 * the elements in that row will not align correctly.  So, don't use
 * FlowLayout in a row with more than one element.
 *
 * I plan to add several more add methods...at the very least one which just takes
 * a component and no row and then provide some sort of increment method to move
 * the internal row position.
 * 
 * Besides adding more convenience add methods, working on creating
 * speedier height and width distribution algorithms needs to be done.
 */
public class RowLayout implements LayoutManager2, Serializable, PropertyChangeListener {

    /** Growability types: */
    public static final Growability GROW = new Growability();

    public static final Growability NO_GROW = new Growability();

    public static final HorizontalAlignment LEFT = new HorizontalAlignment();

    public static final HorizontalAlignment RIGHT = new HorizontalAlignment();

    public static final VerticalAlignment CENTER = new VerticalAlignment();

    public static final VerticalAlignment BOTTOM = new VerticalAlignment();

    public static final VerticalAlignment TOP = new VerticalAlignment();

    /**
	 * Get the default vertical gap for RowLayouts.
	 */
    public static int getDefaultVerticalGap() {
        return 6;
    }

    /**
	 * Get the default horizontal gap for RowLayouts.
	 */
    public static int getDefaultHorizontalGap() {
        return 6;
    }

    /** The number of coponments in the row with the most 
		components at any given time. */
    private int max_row_size = 0;

    /** Specifies the vertical spacing between rows. */
    private int vgap = getDefaultVerticalGap();

    /** Specifies the spacing between components within a row. */
    private int hgap = getDefaultHorizontalGap();

    /** The container we're laying out. */
    private Container target_container;

    /** List of lists, where each list holds a rows components. */
    private ArrayList row_components = new ArrayList();

    /** List of alignments, specifying each rows horizontal alignment. */
    private HashMap row_alignments = new HashMap();

    /** If not null, we pass debug messages to this stream. */
    private transient PrintStream dbg = null;

    private int min_x_total, pref_x_total, max_x_total;

    private int min_y_total, pref_y_total, max_y_total;

    /** The last active row. */
    private int last_active = 0;

    /** Switch for adding on target or from target. */
    private boolean add_to_target = true;

    /** Disables 'addLayoutComponent'.*/
    private boolean do_not_add = false;

    /** Disables 'removeComponent'. */
    private boolean is_internal_remove = false;

    /** Heuristic used to lazily determine whether to run growing parts of
	    height distribution algorithm. */
    private boolean has_height_growth = false;

    /** Heuristic used to lazily determine whether to run growing parts of
	    width distribution algorithm. */
    private boolean has_width_growth = false;

    /** The background color.*/
    private Color BACKGROUND_COLOR = new Color(204, 204, 204);

    private Dimension preferred_size = null;

    private Dimension maximum_size = null;

    private Dimension minimum_size = null;

    /** constructor
	 * Simple constructor where all layout options are defualt values.
	 *
	 * @param		target	The contaier that we lay out.
	 */
    public RowLayout(Container target) {
        this.target_container = target;
        target.setLayout(this);
        BACKGROUND_COLOR = target.getBackground();
    }

    /** constructor
	 * Simple constructor where all layout options are defualt values.
	 *	
	 * @param		target				The contaier that we lay out.
	 * @param		background_color	The background color of any springs or struts.
	 */
    public RowLayout(Container target, Color background_color) {
        this(target);
        this.BACKGROUND_COLOR = background_color;
    }

    /** constructor
     * Creates a layout manager that will lay out components row
	 * by row.
     *
     * @param		target  			The container that needs to be laid out
     * @param 		vgap 				The vertical gap between rows
	 * @param		hgap				The spacing between components in a row
     * @param		background_color	The background color of any springs or struts.
     */
    public RowLayout(Container target, int vgap, int hgap, Color background_color) {
        this(target, vgap, hgap);
        this.BACKGROUND_COLOR = background_color;
    }

    /** constructor
     * Creates a layout manager that will lay out components row
	 * by row.
     *
     * @param		target  The container that needs to be laid out
     * @param 		vgap 	The vertical gap between rows
	 * @param		hgap	The spacing between components in a row
     *
     */
    public RowLayout(Container target, int vgap, int hgap) {
        this.vgap = vgap;
        this.hgap = hgap;
        this.target_container = target;
        target.setLayout(this);
        BACKGROUND_COLOR = target.getBackground();
    }

    /** constructor
     * Constructs a RowLayout that 
     * produces debugging messages.
     *
     * @param		target  The container that needs to be laid out.
     * @param		vgap 	The vertical gap between rows.
	 * @param		hgap	The spacing between components in a row.
     * @param 		dbg  	The stream to which debugging messages should be sent,
     *   					null if none.
     */
    public RowLayout(Container target, int vgap, int hgap, PrintStream dbg) {
        this(target, vgap, hgap);
        this.dbg = dbg;
    }

    /**
     * Indicates that a child has changed its layout related information,
     * and thus any cached calculations should be flushed.
     *
     * @param		targ  The affected container.
     *
     */
    public void invalidateLayout(Container targ) {
        checkContainer(targ);
        preferred_size = null;
        maximum_size = null;
        minimum_size = null;
        pref_x_total = 0;
        pref_y_total = 0;
        min_x_total = 0;
        min_y_total = 0;
        max_x_total = 0;
        max_y_total = 0;
    }

    /**
     * Not used by this class.
     *
     * @param name the name of the component
     * @param comp the component
     */
    public void addLayoutComponent(String name, Component comp) {
        throw new RuntimeException("Method not supported");
    }

    /**
     * Remove the given component from its row.
     *
     * @param		comp The component to remove.
     */
    public void removeLayoutComponent(Component comp) {
        add_to_target = false;
        this.removeComponent(comp);
        add_to_target = true;
    }

    /**
     * Insert component with given constraints object.
	 * If constraints is null, assume default attributes
	 * and insert into the last active row.  If constraints
	 * isn't an instance of ORowConstraints throw a runtime
	 * exception.
     *
     * @param		comp		The component to insert.
     * @param		constraints The constraints.
     */
    public void addLayoutComponent(Component comp, Object constraints) {
        if (!do_not_add) {
            if (constraints == null) {
                add_to_target = false;
                add(comp, new RowConstraints(last_active));
                add_to_target = true;
            } else if (constraints instanceof RowConstraints) {
                add_to_target = false;
                add(comp, (RowConstraints) constraints);
                add_to_target = true;
            } else throw new RuntimeException("Invalid constraints object.");
        }
    }

    /**
     * Returns the preferred dimensions for this layout, given the components
     * in the specified target container.
     *
     * @param		target  The container that needs to be laid out.
     * @return 				The dimensions >= 0 && <= Integer.MAX_VALUE.
     * @exception AWTError  If the target isn't the container specified to the
     *                      BoxLayout constructor.
     * @see Container
     * @see #minimumLayoutSize
     * @see #maximumLayoutSize
     */
    public Dimension preferredLayoutSize(Container target) {
        if (preferred_size == null) {
            printMessage("preferredLayoutSize() called");
            checkContainer(target);
            int x_total = 0;
            int y_total = 0;
            for (int i = 0; i < row_components.size(); i++) {
                getRowTotal(i);
                y_total += pref_y_total;
                x_total = Math.max(pref_x_total, x_total);
            }
            Dimension size = new Dimension(x_total, y_total);
            Insets insets = target.getInsets();
            size.width = (int) Math.min((long) size.width + (long) insets.left + (long) insets.right, Integer.MAX_VALUE);
            size.height = (int) Math.min((long) size.height + (long) insets.top + (long) insets.bottom - vgap, Integer.MAX_VALUE);
            pref_x_total = pref_y_total = 0;
            preferred_size = size;
        }
        return preferred_size;
    }

    /**
     * Returns the minimum dimensions needed to lay out the components
     * contained in the specified target container.
     *
     * @param		target  The container that needs to be laid out.
     * @return 				The dimensions >= 0 && <= Integer.MAX_VALUE.
     * @exception AWTError  If the target isn't the container specified to the
     *                      RowLayout constructor.
     * @see #preferredLayoutSize
     * @see #maximumLayoutSize
     */
    public Dimension minimumLayoutSize(Container target) {
        if (minimum_size == null) {
            printMessage("minimumLayoutSize() called");
            checkContainer(target);
            int x_total = 0;
            int y_total = 0;
            for (int i = 0; i < row_components.size(); i++) {
                getRowTotal(i);
                y_total += min_y_total;
                x_total = Math.max(min_x_total, x_total);
            }
            Dimension size = new Dimension(x_total, y_total);
            Insets insets = target.getInsets();
            size.width = (int) Math.min((long) size.width + (long) insets.left + (long) insets.right, Integer.MAX_VALUE);
            size.height = (int) Math.min((long) size.height + (long) insets.top + (long) insets.bottom - vgap, Integer.MAX_VALUE);
            min_y_total = min_x_total = 0;
            minimum_size = size;
        }
        return minimum_size;
    }

    /**
     * Returns the maximum dimensions the target container can use
     * to lay out the components it contains.
     *
     * @param		target  The container that needs to be laid out.
     * @return 		The dimenions >= 0 && <= Integer.MAX_VALUE.
     * @exception 	AWTError  If the target isn't the container specified to the
     *                        RowLayout constructor.
     * @see #preferredLayoutSize
     * @see #minimumLayoutSize
     */
    public Dimension maximumLayoutSize(Container target) {
        if (maximum_size == null) {
            printMessage("maximumLayoutSize() called");
            checkContainer(target);
            int x_total = 0;
            int y_total = 0;
            for (int i = 0; i < row_components.size(); i++) {
                getRowTotal(i);
                y_total += max_y_total;
                x_total = Math.max(max_x_total, x_total);
            }
            Dimension size = new Dimension(x_total, y_total);
            Insets insets = target.getInsets();
            size.width = (int) Math.min((long) size.width + (long) insets.left + (long) insets.right, Integer.MAX_VALUE);
            size.height = (int) Math.min((long) size.height + (long) insets.top + (long) insets.bottom - vgap, Integer.MAX_VALUE);
            max_y_total = max_x_total = 0;
            maximum_size = size;
        }
        return maximum_size;
    }

    /**
     * Returns the alignment along the X axis for the container.
     * If the box is horizontal, the default
     * alignment will be returned. Otherwise, the alignment needed
     * to place the children along the X axis will be returned.
     *
     * @param		target  The container.
     * @return 		The alignment >= 0.0f && <= 1.0f.
     * @exception 	AWTError  If the target isn't the container specified to the
     *                        RowLayout constructor.
     */
    public float getLayoutAlignmentX(Container target) {
        printMessage("getLayoutAligmentX() called");
        checkContainer(target);
        return 0;
    }

    /**
     * Returns the alignment along the Y axis for the container.
     * If the box is vertical, the default
     * alignment will be returned. Otherwise, the alignment needed
     * to place the children along the Y axis will be returned.
     *
     * @param		target	The container.
     * @return 		The alignment >= 0.0f && <= 1.0f.
     * @exception 	AWTError  If the target isn't the container specified to the
     *              		  RowLayout constructor.
     */
    public float getLayoutAlignmentY(Container target) {
        printMessage("getLayoutAlignmentY() called");
        checkContainer(target);
        return 0;
    }

    /**
     * Called by the AWT <!-- XXX CHECK! --> when the specified container
     * needs to be laid out.
     *
     * @param		target	The container to lay out.
     *
     * @exception AWTError  If the target isn't the container specified to the
     *                      RowLayout constructor
     */
    public void layoutContainer(Container target) {
        checkContainer(target);
        int y_offset = 0;
        int max_height = 0;
        Dimension alloc = target.getSize();
        Insets in = target.getInsets();
        alloc.width -= in.left + in.right;
        alloc.height -= in.top + in.bottom;
        int[][] heights = distributeHeights(alloc.height);
        for (int i = 0; i < row_components.size(); i++) {
            ArrayList current_row = (ArrayList) row_components.get(i);
            int row = ((RowComponent) current_row.get(0)).getRowID();
            int num_children = current_row.size();
            max_height = 0;
            for (int j = 0; j < current_row.size(); j++) {
                max_height = Math.max(max_height, heights[i][j]);
            }
            int x_size = 0;
            int[] sizes = new int[num_children];
            int x_total = distributeWidths(current_row, alloc.width, sizes);
            x_total = alloc.width - x_total - (num_children - 1) * hgap;
            HorizontalAlignment r_align = (HorizontalAlignment) row_alignments.get(new Integer(row));
            if (r_align != null && r_align == RIGHT) {
                x_size += x_total;
            }
            for (int k = 0; k < num_children; k++) {
                RowComponent row_comp = (RowComponent) current_row.get(k);
                Component c = row_comp.getComponent();
                Dimension pref_d = c.getPreferredSize();
                Dimension min_d = c.getMinimumSize();
                Dimension max_d = c.getMaximumSize();
                int y = 0;
                if (i == 0) {
                    y = (int) Math.min((long) in.top + y_offset, Integer.MAX_VALUE);
                } else {
                    y = (int) Math.min((long) in.top + vgap + y_offset, Integer.MAX_VALUE);
                }
                if (row_comp.getVerticalAlignment() == BOTTOM && max_height != heights[i][k]) {
                    y += max_height - heights[i][k];
                }
                if (row_comp.getVerticalAlignment() == CENTER && max_height != heights[i][k]) {
                    y += (max_height - heights[i][k]) / 2;
                }
                int x = in.left + x_size;
                if (k > 0) {
                    c.setBounds((int) Math.min((long) in.left + (long) hgap + x_size, Integer.MAX_VALUE), y, sizes[k], heights[i][k]);
                    x_size += sizes[k] + hgap;
                } else {
                    c.setBounds((int) Math.min((long) in.left + x_size, Integer.MAX_VALUE), y, sizes[k], heights[i][k]);
                    x_size += sizes[k];
                }
            }
            if (dbg != null) {
                for (int l = 0; l < num_children; l++) {
                    RowComponent r_comp = (RowComponent) current_row.get(l);
                    Component c = r_comp.getComponent();
                    printMessage(c.toString());
                }
            }
            if (i != 0) y_offset += max_height + vgap; else y_offset += max_height;
        }
    }

    /** 
	 * Cycle through a row and distribute the total width of cotnainer
	 * amongst the components in that row.  
	 * This algorithm is rather expensive.
	 *
	 * @param		current_row	The row of components.
	 * @param		total_width	The total amount of space to allocate within.
	 * @param		ret_sizes	The array to place each component's allocated 
	 *							space in.
	 *
	 * @return		int			The total space alloacted.
	 */
    private int distributeWidths(ArrayList current_row, int total_width, int[] ret_sizes) {
        printMessage("entering distributeWidths");
        int num_springs = 0;
        for (int acc = 0; acc < current_row.size(); acc++) {
            if (((RowComponent) current_row.get(acc)).isSpring()) num_springs++;
        }
        int num_components = current_row.size();
        int extra_space = 0;
        int num_allocated = 0;
        int num_grow_left = 0;
        int x_total = 0;
        for (int index = 0; index < num_components; index++) {
            RowComponent r_comp = (RowComponent) current_row.get(index);
            if (!r_comp.isSpring()) {
                Component cur_comp = r_comp.getComponent();
                Dimension pref_size = cur_comp.getPreferredSize();
                ret_sizes[index] = pref_size.width;
                x_total += pref_size.width;
                Growability grow = r_comp.getHorizontalGrow();
                if (grow == GROW) {
                    num_grow_left++;
                }
            }
        }
        if (has_width_growth) {
            int max_extra_alloc = 0;
            if (num_grow_left > 0) {
                max_extra_alloc = (total_width - x_total - ((num_components - num_springs - 1) * hgap)) / num_grow_left;
            }
            num_allocated = num_components - num_springs - num_grow_left;
            for (int l = 0; l < num_components; l++) {
                RowComponent r_comp = (RowComponent) current_row.get(l);
                Component cur_comp = r_comp.getComponent();
                Growability grow = r_comp.getHorizontalGrow();
                if (grow == GROW && !r_comp.isSpring()) {
                    cur_comp.setSize(max_extra_alloc + ret_sizes[l], cur_comp.getPreferredSize().height);
                    x_total += (max_extra_alloc + ret_sizes[l]) - cur_comp.getPreferredSize().width;
                    ret_sizes[l] += max_extra_alloc;
                    num_allocated++;
                }
            }
            if (num_components - num_allocated != 0) {
                max_extra_alloc = (total_width - x_total - (num_components - 1) * hgap) / num_springs;
                for (int m = 0; m < num_components; m++) {
                    RowComponent r_comp = (RowComponent) current_row.get(m);
                    Component cur_comp = r_comp.getComponent();
                    Growability grow = r_comp.getHorizontalGrow();
                    if (r_comp.isSpring()) {
                        cur_comp.setSize(max_extra_alloc, cur_comp.getPreferredSize().height);
                        x_total += max_extra_alloc;
                        ret_sizes[m] = max_extra_alloc;
                        num_allocated++;
                    }
                }
            }
        }
        return x_total;
    }

    /** 
	 * Cycle through a row and distribute the total height of container
	 * amongst the rows.  
	 * This algorithm is rather expensive.
	 *
	 * @param		total_height	The total vertical space in the container.
	 *
	 * @return		int[][]			Array of array of heights.
	 */
    private int[][] distributeHeights(int total_height) {
        int total = row_components.size();
        int y_total = 0;
        int num_grow_rows = 0;
        if (total <= 0) return null;
        int[][] height_list = new int[row_components.size()][max_row_size];
        int[] row_max_height_array = new int[total];
        int num_allocated = 0;
        for (int index = 0; index < total; index++) {
            ArrayList current_row = (ArrayList) row_components.get(index);
            int[] row_heights = height_list[index];
            int max_height = 0;
            boolean wants_grow = false;
            for (int row_idx = 0; row_idx < current_row.size(); row_idx++) {
                RowComponent r_comp = (RowComponent) current_row.get(row_idx);
                Component cur_comp = r_comp.getComponent();
                Dimension pref_size = cur_comp.getPreferredSize();
                row_heights[row_idx] = pref_size.height;
                if (row_heights[row_idx] > max_height) max_height = row_heights[row_idx];
                Growability grow = r_comp.getVerticalGrow();
                if (grow == GROW) {
                    wants_grow = true;
                }
            }
            row_max_height_array[index] = max_height;
            if (index != (total - 1)) y_total += max_height + vgap; else y_total += max_height;
            if (wants_grow) num_grow_rows++;
            if (current_row.size() < max_row_size) row_heights[current_row.size()] = -1;
        }
        if (has_height_growth) {
            if (num_grow_rows > 0) {
                int max_extra_alloc = (total_height - y_total) / num_grow_rows;
                for (int index = 0; index < total; index++) {
                    ArrayList current_row = (ArrayList) row_components.get(index);
                    int[] row_heights = height_list[index];
                    int num_components = current_row.size();
                    int row_max_height = -1;
                    for (int i = 0; i < num_components; i++) {
                        RowComponent r_comp = (RowComponent) current_row.get(i);
                        Component cur_comp = r_comp.getComponent();
                        Growability grow = r_comp.getVerticalGrow();
                        int pref_height = cur_comp.getPreferredSize().height;
                        if (grow == GROW) {
                            row_heights[i] = max_extra_alloc + row_max_height_array[index];
                        }
                    }
                }
            }
        }
        return height_list;
    }

    /**
	 * Check if the container asking to be laid out is the
	 * container owned by this instance.  If it isn't, throw
	 * an exception.
	 */
    private void checkContainer(Container target) {
        if (this.target_container != target) {
            printMessage("target isn't mine!");
            throw new AWTError("RowLayout can't be shared");
        }
    }

    /**
	 * Calculate the total min, max, and preferred x an y space
	 * for the given row.
	 *
	 * @param		row		The row number to calculate values for.
	 */
    private void getRowTotal(int row) {
        ArrayList current_row = (ArrayList) row_components.get(row);
        int n = current_row.size();
        min_y_total = pref_y_total = max_y_total = 0;
        min_x_total = pref_x_total = max_x_total = 0;
        for (int i = 0; i < n; i++) {
            RowComponent r_comp = (RowComponent) current_row.get(i);
            Component c = r_comp.getComponent();
            Dimension min = c.getMinimumSize();
            min_x_total += min.width;
            Dimension typ = c.getPreferredSize();
            pref_x_total += typ.width;
            Dimension max = c.getMaximumSize();
            max_x_total += max.width;
            min_y_total = Math.max(min.height, min_y_total);
            pref_y_total = Math.max(typ.height, pref_y_total);
            max_y_total = Math.max(max.height, max_y_total);
        }
        min_x_total += (n - 1) * hgap;
        pref_x_total += (n - 1) * hgap;
        max_x_total += (n - 1) * hgap;
        min_y_total += vgap;
        pref_y_total += vgap;
        max_y_total += vgap;
    }

    /**
	 * Add component to the specified 0 based row with the Object constraints.
	 *
	 * @param		comp		The component to add.
	 * @param		constraints	Constraints placed on comp in row.
	 */
    public void add(Component comp, RowConstraints constraints) {
        add(comp, constraints.getRow(), constraints.getVerticalGrow(), constraints.getHorizontalGrow(), constraints.getVerticalAlignment());
    }

    /**
	 * Add component to the specified 0 based row with the Object constraints.
	 *
	 * @param		comp		The component to add.
	 * @param		rowid		The row to add to.
	 * @param		constraints	Constraints placed on comp in row.
	 */
    public void add(Component comp, int rowid, RowConstraints constraints) {
        add(comp, rowid, constraints.getVerticalGrow(), constraints.getHorizontalGrow(), constraints.getVerticalAlignment());
    }

    /**
	 * Add component to the specified 0 based row with default constraints.
	 *
	 * @param		comp	The component to add.
	 * @param		rowid	The row to add the component to.
	 */
    public void add(Component comp, int rowid) {
        this.do_not_add = true;
        comp.addPropertyChangeListener(this);
        preferred_size = null;
        maximum_size = null;
        minimum_size = null;
        if (add_to_target) target_container.add(comp);
        RowComponent new_comp = new RowComponent(comp, rowid);
        binarySearchInsert(rowid, new_comp);
        this.do_not_add = false;
    }

    /**
	 * Add component to the last row added to with default constraints.
	 *
	 * @param		comp	The component to add.
	 */
    public void add(Component comp) {
        add(comp, last_active);
    }

    /**
	 * Add component to row with given characteristics
	 *
	 * @param		comp	The component to add to the row.
	 * @param		rowid	The row to add to.
	 * @param		vGrow	The vertical growth properties of the component.
	 * @param		hGrow	The horizontal alignment for the component.
	 * @param		vAlign	The vertical alignment for the component.
	 */
    public void add(Component comp, int rowid, Growability vGrow, Growability hGrow, VerticalAlignment vAlign) {
        add(comp, rowid, vGrow, hGrow, vAlign, false);
    }

    /**
	 * Add component to the last row added to with given characteristics
	 *
	 * @param		comp	The component to add to the row.
	 * @param		vGrow	The vertical growth properties of the component.
	 * @param		hGrow	The horizontal alignment for the component.
	 * @param		vAlign	The vertical alignment for the component.
	 */
    public void add(Component comp, Growability vGrow, Growability hGrow, VerticalAlignment vAlign) {
        add(comp, last_active, vGrow, hGrow, vAlign);
    }

    /**
	 * Add component to row with given characteristics
	 *
	 * @param		comp		The component to add to the row.
	 * @param		rowid		The row to add to.
	 * @param		vGrow		The vertical growth properties of the component.
	 * @param		hGrow		The horizontal alignment for the component.
	 * @param		isSpring	True if component is spring, false otherwise.
	 */
    public void add(Component comp, int rowid, Growability vGrow, Growability hGrow, boolean isSpring) {
        add(comp, rowid, vGrow, hGrow, BOTTOM, isSpring);
    }

    /**
	 * Add component to the given row with given characteristics
	 *
	 * @param		comp		The component to add to the row.
	 * @param		rowid		The row to add to.
	 * @param		vGrow		The vertical growth properties of the component.
	 * @param		hGrow		The horizontal alignment for the component.
	 * @param		vAlign		The vertical alignment for the component.
	 * @param		isSpring	True if component is spring, false otherwise.
	 */
    public void add(Component comp, int rowid, Growability vGrow, Growability hGrow, VerticalAlignment vAlign, boolean isSpring) {
        this.do_not_add = true;
        comp.addPropertyChangeListener(this);
        preferred_size = null;
        maximum_size = null;
        minimum_size = null;
        if (vGrow == GROW) has_height_growth = true;
        if (hGrow == GROW) has_width_growth = true;
        if (add_to_target) target_container.add(comp);
        RowComponent new_comp = new RowComponent(comp, rowid, vGrow, hGrow, vAlign, isSpring);
        binarySearchInsert(rowid, new_comp);
        this.do_not_add = false;
    }

    /**
	 * Add component to row with given characteristics.
	 *
	 * @param		comp	The component to add to the row.
	 * @param		rowid	The row to add to.
	 * @param		vGrow	The vertical growth properties of the component.
	 * @param		hGrow	The horizontal growth properties of the component.
	 */
    public void add(Component comp, int rowid, Growability vGrow, Growability hGrow) {
        add(comp, rowid, vGrow, hGrow, BOTTOM, false);
    }

    /**
	 * Add component to the last row added to with given characteristics
	 *
	 * @param		comp	The component to add to the row.
	 * @param		vGrow	The vertical growth properties of the component.
	 * @param		hGrow	The horizontal growth properties of the component.
	 */
    public void add(Component comp, Growability vGrow, Growability hGrow) {
        add(comp, last_active, vGrow, hGrow, BOTTOM, false);
    }

    /**
	 * Add component to the given row with given characteristics
	 *
	 * @param		comp	The component to add to the row.
	 * @param		rowid	The row to add to.
	 * @param		vGrow	The verical growth properties of the component.
	 */
    public void add(Component comp, int rowid, Growability vGrow) {
        add(comp, rowid, vGrow, NO_GROW, BOTTOM, false);
    }

    /**
	 * Add a non growable panel that will appear as a spacer to the current row.
	 *
	 * @param		width	The width of the strut.
	 */
    public void addStrut(int width) {
        addStrut(width, last_active);
    }

    /**
	 * Add a non growable panel that will appear as a spacer.
	 *
	 * @param		width	The width of the strut.
	 * @param		row		The row to place the strut in.
	 */
    public void addStrut(int width, int row) {
        JPanel strut = new JPanel();
        strut.setBackground(BACKGROUND_COLOR);
        Dimension d = strut.getPreferredSize();
        strut.setPreferredSize(new Dimension(width, d.height));
        add(strut, row);
    }

    /**
	 * Create and place a spring into the current row.
	 */
    public void addSpring() {
        addSpring(last_active);
    }

    /**
	 * Create and place a spring into the given row.
	 *
	 * @param		row	The row to place the spring in.
	 */
    public void addSpring(int row) {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        add(panel, row, RowLayout.NO_GROW, RowLayout.GROW, true);
    }

    /**
	 * Add an empty row with zero height to the last row.
	 * The empty row is implicity the last row from when method called.
	 */
    public void addEmptyRow() {
        addEmptyRow(0);
    }

    /**
	 * Move the last active row to the next row so that insertion can begin 
	 * there.  Only needs to be called when rowids are not being explicitly 
	 * given with adds.
	 */
    public void nextRow() {
        last_active++;
    }

    /** 
	 * Add an empty with the given height to the last row.
	 * The empty row is implicity the last row from when method called.
	 *
	 * @param		height	The height of the empty row.
	 */
    public void addEmptyRow(int height) {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, height));
        if (last_active != 0) add(panel, ++last_active); else if (row_components.size() == 0) add(panel, last_active++); else add(panel, ++last_active);
    }

    private void binarySearchInsert(int rowid, RowComponent new_comp) {
        if (rowid > last_active) last_active = rowid;
        int num_rows = row_components.size();
        int front = 0;
        int end = num_rows - 1;
        if (num_rows <= 0) {
            printMessage("inserting first row " + rowid);
            ArrayList new_list = new ArrayList();
            new_list.add(new_comp);
            if (new_list.size() > max_row_size) max_row_size = new_list.size();
            row_components.add(new_list);
            row_alignments.put(new Integer(rowid), LEFT);
            return;
        }
        while (front < end) {
            int mid = (front + end) / 2;
            RowComponent r_comp = (RowComponent) ((ArrayList) row_components.get(mid)).get(0);
            if (r_comp.getRowID() == rowid) {
                end = mid;
                front = mid;
            } else if (rowid < r_comp.getRowID()) {
                end = mid - 1;
            } else if (rowid > r_comp.getRowID()) {
                front = mid + 1;
            }
        }
        RowComponent r_comp = (RowComponent) ((ArrayList) row_components.get(front)).get(0);
        if (rowid == r_comp.getRowID()) {
            printMessage("inserting to existing row " + rowid);
            ((ArrayList) row_components.get(front)).add(new_comp);
            if (((ArrayList) row_components.get(front)).size() > max_row_size) max_row_size = ((ArrayList) row_components.get(front)).size();
        } else {
            insertNewRow(rowid, front, new_comp);
        }
    }

    private void insertNewRow(int rowid, int mid, RowComponent new_comp) {
        RowComponent r_comp = (RowComponent) ((ArrayList) row_components.get(mid)).get(0);
        int rowID = r_comp.getRowID();
        if (rowid > rowID) {
            printMessage("inserting new row after " + mid);
            ++mid;
        } else if (rowid < rowID) {
            printMessage("inserting new row before " + mid);
        }
        ArrayList new_list = new ArrayList();
        new_list.add(new_comp);
        row_components.add(mid, new_list);
        if (new_list.size() > max_row_size) max_row_size = new_list.size();
        printMessage("INSERTING: " + rowid);
        row_alignments.put(new Integer(rowid), LEFT);
    }

    /**
	 * Remove all elements from the given rowand then re-layout container.
	 * 
	 * @param		row		The row to remove.
	 */
    public void removeRow(int row) {
        printMessage("removing row " + row);
        if (row < row_components.size()) {
            ArrayList remove_list = (ArrayList) row_components.get(row);
            for (int i = 0; i < remove_list.size(); i++) {
                Component c = ((RowComponent) remove_list.get(i)).getComponent();
                c.removePropertyChangeListener(this);
                target_container.remove(c);
            }
            remove_list.clear();
            row_components.remove(row);
            row_alignments.remove(new Integer(row));
        }
        preferred_size = null;
        maximum_size = null;
        minimum_size = null;
        max_row_size = 0;
        for (int i = 0; i < row_components.size(); i++) {
            ArrayList current_row = (ArrayList) row_components.get(i);
            if (current_row.size() > max_row_size) max_row_size = current_row.size();
        }
        layoutContainer(target_container);
        target_container.repaint();
    }

    /**
	 * Remove the given component from whatever row it is in. 
	 * Re-layout the container. 
	 *
	 * @param		component	The compnent to remove.
	 */
    public void removeComponent(Component component) {
        if (!is_internal_remove) {
            boolean finished = false;
            for (int i = 0; i < row_components.size(); i++) {
                ArrayList inner_list = (ArrayList) row_components.get(i);
                for (int j = 0; j < inner_list.size(); j++) {
                    if (((RowComponent) inner_list.get(j)).equals(component)) {
                        printMessage("layout manager removing component " + "from row " + i + " index " + j);
                        Component c = ((RowComponent) inner_list.get(j)).getComponent();
                        c.removePropertyChangeListener(this);
                        if (add_to_target) {
                            is_internal_remove = true;
                            target_container.remove(c);
                            is_internal_remove = false;
                        }
                        inner_list.remove(j);
                        if (inner_list.size() == 0) {
                            row_components.remove(i);
                            row_alignments.remove(new Integer(i));
                        }
                        finished = true;
                        break;
                    }
                }
                if (finished) break;
            }
            preferred_size = null;
            maximum_size = null;
            minimum_size = null;
            max_row_size = 0;
            for (int i = 0; i < row_components.size(); i++) {
                ArrayList current_row = (ArrayList) row_components.get(i);
                if (current_row.size() > max_row_size) max_row_size = current_row.size();
            }
            layoutContainer(target_container);
            target_container.repaint();
        }
    }

    /**
	 * Remove all components. 
	 */
    public void removeAll() {
        for (int i = 0; i < row_components.size(); i++) {
            ArrayList row = (ArrayList) row_components.get(i);
            for (int j = 0; j < row.size(); j++) {
                RowComponent row_comp = (RowComponent) row.get(j);
                row_comp.getComponent().removePropertyChangeListener(this);
            }
        }
        preferred_size = null;
        maximum_size = null;
        minimum_size = null;
        max_row_size = 0;
        target_container.removeAll();
        row_components.clear();
        row_alignments.clear();
        layoutContainer(target_container);
        target_container.repaint();
    }

    /**
	 * Set the background color for internal panels.
	 *
	 * @param		color	The background color.
	 */
    public void setBackgroundColor(Color color) {
        BACKGROUND_COLOR = color;
    }

    public Color getBackgroundColor() {
        return BACKGROUND_COLOR;
    }

    /** 
	 * Set the horizontal gap between row components. 
	 *
	 * @param		hgap	The new horizontal gap.
	 */
    public void setHorizontalGap(int hgap) {
        this.hgap = hgap;
        preferred_size = null;
        maximum_size = null;
        minimum_size = null;
    }

    /** 
	 * Set the vertical gap between rows. 
	 *
	 * @param		vgap	The new vertical gap.
	 */
    public void setVerticalGap(int vgap) {
        this.vgap = vgap;
        preferred_size = null;
        maximum_size = null;
        minimum_size = null;
    }

    /**
	 * Get the horizontal gap.
	 *
	 * @return		int	The horizontal gap.
	 */
    public int getHorizontalGap() {
        return hgap;
    }

    /**
	 * Get the vertical gap.
	 *
	 * @return		int	The vertical gap.
	 */
    public int getVerticalGap() {
        return vgap;
    }

    /**
	 * Set the alignment for a given row
	 *
	 * @param		rowid		The row number to set the alignment for.
	 * @param		alignemnt	The alignment.
	 */
    public boolean setRowAlignment(int rowid, HorizontalAlignment alignment) {
        if (row_alignments.containsKey(new Integer(rowid))) {
            printMessage("setting alignment");
            row_alignments.put(new Integer(rowid), alignment);
            return true;
        } else return false;
    }

    /**
	 * Listen for changes in component's preferred, minimum and maximum sizes.
	 * When any of these change, the corresponding xLayoutSize() function
	 * needs to recalculate.
	 *
	 * @see		PropertyChangeListener#propertyChange
	 */
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("preferredSize")) {
            preferred_size = null;
        } else if (e.getPropertyName().equals("maximumSize")) {
            maximum_size = null;
        } else if (e.getPropertyName().equals("minimumSize")) {
            minimum_size = null;
        }
    }

    /**
	 * Class represents row components, specifying all relative
	 * layout data for that component.
	 */
    private class RowComponent {

        private Growability vGrow;

        private Growability hGrow;

        private VerticalAlignment vAlign;

        private int rowid;

        private Component comp;

        private boolean is_spring = false;

        public RowComponent(Component comp, int rowid) {
            this.comp = comp;
            this.rowid = rowid;
            vGrow = NO_GROW;
            hGrow = NO_GROW;
            vAlign = BOTTOM;
            this.is_spring = false;
        }

        public RowComponent(Component comp, int rowid, Growability vGrow, Growability hGrow, VerticalAlignment vAlign, boolean is_spring) {
            this.comp = comp;
            this.rowid = rowid;
            this.vGrow = vGrow;
            this.hGrow = hGrow;
            this.vAlign = vAlign;
            this.is_spring = is_spring;
        }

        public Growability getVerticalGrow() {
            return vGrow;
        }

        public Growability getHorizontalGrow() {
            return hGrow;
        }

        public VerticalAlignment getVerticalAlignment() {
            return vAlign;
        }

        public int getRowID() {
            return rowid;
        }

        public Component getComponent() {
            return comp;
        }

        public boolean equals(Component component) {
            if (comp == component) return true; else return false;
        }

        public boolean isSpring() {
            return is_spring;
        }
    }

    private void printMessage(String message) {
        if (dbg != null) dbg.println(message);
    }
}

/**
 * Class used to for better type checking in the add methods on RowLayout.
 * This class is NOT meant to be instantiated by any other classes in the 
 * package, even though it legally can be.  If you try to pass a self-created
 * reference to RowLayout, the results are unpredictable (probably bad).  
 */
class Growability {

    public Growability() {
    }
}

/**
 * Class used to for better type checking in the add methods on RowLayout.
 * This class is NOT meant to be instantiated by any other classes in the 
 * package, even though it legally can be.  If you try to pass a self-created
 * reference to RowLayout, the results are unpredictable (probably bad).  
 */
class VerticalAlignment {

    public VerticalAlignment() {
    }
}

/**
 * Class used to for better type checking in the add methods on RowLayout.
 * This class is NOT meant to be instantiated by any other classes in the 
 * package, even though it legally can be.  If you try to pass a self-created
 * reference to RowLayout, the results are unpredictable (probably bad).  
 */
class HorizontalAlignment {

    public HorizontalAlignment() {
    }
}
