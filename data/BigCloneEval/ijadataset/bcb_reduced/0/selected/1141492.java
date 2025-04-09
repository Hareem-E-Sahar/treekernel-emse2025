package bpui.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * The row header of a table.
 * It is a panel that has to be put on the side of the table.
 * It uses push buttons as the row header buttons. 
 * @author SHZ Jul 31, 2010
 */
public class TableRowHeader extends Composite {

    /**
	 * external function to build the text to write on each button 
	 * @author SHZ Aug 3, 2010
	 */
    public interface IRowHeaderTextListener {

        public String textForRow(int rowIndex);
    }

    /**
	 * To get called when a row header button has been clicked 
	 * @author SHZ Aug 3, 2010
	 */
    public interface IRowHeaderSelectionListener {

        public void widgetSelected(SelectionEvent ev, int rowIndex);
    }

    /**
	 * The table of which this is the row header
	 */
    private Table table;

    /**
	 * The number of rows visible in the grid and therefore the number of buttons
	 * to show in the row header panel 
	 */
    private int buttonNumber;

    /**
	 * the listener used to get the texts to write on each row header button
	 */
    private IRowHeaderTextListener rowHeaderTextListener;

    /**
	 * The row header buttons created until now, in the order in which they are displayed
	 */
    private List<Button> lstButton = new ArrayList<Button>();

    /**
	 * When someone clicks on one of the row header buttons, 
	 */
    private List<IRowHeaderSelectionListener> lstSelectionListener = new ArrayList<TableRowHeader.IRowHeaderSelectionListener>();

    /**
	 * Since there has to be some space at the top of the row header (by the column header)
	 * and perhaps on the bottom of the header (if there is an horizontal scrollbar in the table)
	 */
    private Composite buttonPanel;

    public TableRowHeader(Composite parent, IRowHeaderTextListener rowHeaderTextListener) {
        super(parent, SWT.NONE);
        buttonPanel = new Composite(this, SWT.NONE);
        this.rowHeaderTextListener = rowHeaderTextListener;
    }

    /**
	 * Builds the row header panel and its content.
	 * To be called when the structure of the table is complete, included its columns 
	 * (the rows can be added later)  
	 * @param table the table to build the row header of
	 */
    public void init(final Table table) {
        this.table = table;
        addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent ev) {
                showHeader();
            }
        });
        table.addControlListener(new ControlAdapter() {

            public void controlResized(ControlEvent ev) {
                rebuildPanel(table);
            }
        });
        for (TableColumn col : table.getColumns()) {
            col.addControlListener(new ControlAdapter() {

                public void controlResized(ControlEvent ev) {
                    rebuildPanel(table);
                }
            });
        }
        table.getVerticalBar().addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent ev) {
                showHeader();
            }
        });
    }

    /**
	 * At least in Gnome the top visible row of the table is often displayed only partially
	 * (the top piece remains outside the table).
	 * This calculates the height of the missing piece, so that we can place the first row header button 
	 * exactly at the same height of the first row.
	 * To do it, it locates the limit between the first and the second row (and therefore the 
	 * height of the visible part of the first row).
	 * To locate this limit, it uses the bisection algorithm (to make it fast).
	 * @param topItem the top visible row in the table 
	 * @return
	 */
    private int getOffset(TableItem topItem) {
        int rowHeight = table.getItemHeight();
        int from = 0;
        int to = rowHeight + 1;
        int test;
        for (int i = 0; i <= rowHeight; i++) {
            test = from + (to - from) / 2;
            if (table.getItem(new Point(0, test)) == topItem) {
                if (table.getItem(new Point(0, test + 1)) != topItem) return rowHeight - test - 1; else from = test;
            } else {
                if (table.getItem(new Point(0, test - 1)) == topItem) return rowHeight - test - 1; else to = test;
            }
        }
        return 0;
    }

    /**
	 * displays the row header buttons with the correct text (extracted using rowHeaderTextListener) 
	 */
    private void showHeader() {
        int topIndex = table.getTopIndex();
        int offset = getOffset(table.getItem(topIndex));
        int rowHeight = table.getItemHeight();
        int panelWidth = getSize().x;
        for (int i = 0; i < buttonNumber; i++) {
            Button button = lstButton.get(i);
            button.setSize(new Point(panelWidth, rowHeight + 1));
            button.setText(rowHeaderTextListener.textForRow(i + topIndex));
            button.setLocation(new Point(0, rowHeight * i - offset));
            button.setData(topIndex + i);
        }
        table.setFocus();
    }

    /**
	 * Calculates the height of the horizontal scrollbar in the table.
	 * Returns zero if there is no visible horizontal scrollbar.
	 * Used to avoid to show row header buttons on the side of the horizontal scrollbar
	 * @param table
	 * @return
	 */
    private int calcHorizontalScrollHeight(final Table table) {
        int horizontalScrollbarHeight;
        if (table.getSize().y > table.getClientArea().height + 5) horizontalScrollbarHeight = table.getHorizontalBar().getSize().y; else horizontalScrollbarHeight = 0;
        return horizontalScrollbarHeight;
    }

    /**
	 * to register a listener for the row header buttons.
	 * Can be used to select the related table row when one of the row header buttons is clicked 
	 * @param listener
	 */
    public void addRowHeaderSelectionListener(IRowHeaderSelectionListener listener) {
        lstSelectionListener.add(listener);
    }

    /**
	 * when the table is resized we need to add buttons to the panel if they are less than 
	 * the visible rows.
	 * @param rowHeight the height of each row
	 * @param tableHeight the height of the table
	 * @param panelWidth the width of the panel (that becomes the width of each button)
	 * @param headerHeight the height of the table column header
	 */
    private void rebuildButtons(int rowHeight, int tableHeight, int panelWidth, int headerHeight) {
        buttonNumber = table.getItemCount();
        int buttonsToAdd = buttonNumber - lstButton.size();
        if (buttonsToAdd > 0) {
            for (int i = 0; i < buttonsToAdd; i++) {
                Button button = new Button(buttonPanel, SWT.PUSH);
                button.addSelectionListener(new SelectionAdapter() {

                    public void widgetSelected(SelectionEvent ev) {
                        for (IRowHeaderSelectionListener listener : lstSelectionListener) listener.widgetSelected(ev, (Integer) ((Button) ev.widget).getData());
                    }
                });
                button.setSize(new Point(panelWidth, rowHeight + 1));
                lstButton.add(button);
            }
        }
    }

    /**
	 * rebuilds or updates the panel GUI. Adds row header buttons if there are not enough
	 * calculates the height of the panel (depending by the presence of the horizontal scrollbar in the table)
	 * @param table
	 */
    private void rebuildPanel(final Table table) {
        int rowHeight = table.getItemHeight();
        int tableHeight = table.getSize().y;
        int panelWidth = getSize().x;
        int headerHeight = table.getHeaderHeight();
        buttonPanel.setLocation(new Point(0, headerHeight));
        int horizontalScrollbarHeight = calcHorizontalScrollHeight(table);
        buttonPanel.setSize(new Point(panelWidth, tableHeight - headerHeight - horizontalScrollbarHeight));
        rebuildButtons(rowHeight, tableHeight, panelWidth, headerHeight);
    }
}
