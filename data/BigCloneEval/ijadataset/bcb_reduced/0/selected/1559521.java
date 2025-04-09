package org.opu.db_vdumper.ui.component.sql;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLWarning;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.EditorKit;
import jsyntaxpane.DefaultSyntaxKit;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import org.opu.db_vdumper.actions.db.DbAction;
import org.opu.db_vdumper.ui.TextResource;
import org.opu.db_vdumper.ui.i18n.SqlEditPaneI18N;
import org.opu.db_vdumper.ui.i18n.SqlEditPaneI18NImpl;
import org.opu.db_vdumper.util.Logger;

/**
 *
 * @author yura
 */
public class SqlEditPane extends JPanel implements TextResource {

    static {
        DefaultSyntaxKit.getContentTypes();
    }

    private JScrollPane scrollPane;

    private JEditorPane syntaxPane;

    private SqlEditPaneI18N i18n;

    /** Main tab container */
    private TabWindow tabWindow;

    /** cut, copy, paste, ... */
    private JToolBar editToolBar;

    /** run, commit, rollback, autocommit */
    private JToolBar sqlToolBar;

    public SqlEditPane(TabWindow tabWindow) {
        this(new SqlEditPaneI18NImpl(), tabWindow);
    }

    public SqlEditPane(SqlEditPaneI18N i18n, TabWindow tabWindow) {
        super(new BorderLayout());
        this.i18n = i18n;
        this.tabWindow = tabWindow;
        scrollPane = new JScrollPane();
        syntaxPane = new JEditorPane();
        editToolBar = new JToolBarImpl();
        sqlToolBar = new JToolBarImpl();
        sqlToolBar.setOrientation(JToolBar.VERTICAL);
        syntaxPane.setFont(new Font("Monospaced", 0, 14));
        syntaxPane.setCaretColor(new Color(153, 204, 255));
        syntaxPane.setBackground(Color.WHITE);
        scrollPane.setViewportView(syntaxPane);
        scrollPane.getViewport().setBackground(Color.WHITE);
        editToolBar.setRollover(true);
        editToolBar.setFocusable(false);
        String lang = "text/sql";
        syntaxPane.setContentType(lang);
        editToolBar.removeAll();
        EditorKit kit = syntaxPane.getEditorKit();
        if (kit instanceof DefaultSyntaxKit) {
            DefaultSyntaxKit defaultSyntaxKit = (DefaultSyntaxKit) kit;
            defaultSyntaxKit.addToolBarActions(syntaxPane, editToolBar);
        }
        editToolBar.validate();
        try {
            syntaxPane.read(new StringReader(""), lang);
        } catch (IOException ex) {
            Logger.getInstance().info(SqlEditPane.class, "Can't reload text");
            Logger.getInstance().debug(SqlEditPane.class, ex);
        }
        syntaxPane.requestFocusInWindow();
        init();
    }

    public void reinit() {
        init();
    }

    private void init() {
        add(scrollPane);
        add(editToolBar, BorderLayout.NORTH);
        add(sqlToolBar, BorderLayout.WEST);
    }

    public void setDbActions(Collection<DbAction> actions) {
        sqlToolBar.removeAll();
        for (DbAction action : actions) {
            sqlToolBar.add(action);
        }
    }

    @Override
    public String getText() {
        return syntaxPane.getText();
    }

    @Override
    public void setResult(String query, String[] headers, Object data, SQLWarning warning) {
        JComponent comp;
        try {
            String[][] arr = (String[][]) data;
            JTable table = new TabelWithToolType(arr, headers);
            JScrollPane tableScroll = new JScrollPane(table);
            tableScroll.setRowHeaderView(getNumbers(arr[0].length, arr.length, table));
            comp = tableScroll;
        } catch (Exception ex) {
            comp = new Panel(String.valueOf(data));
        }
        JTabbedPane tw = new JTabbedPane(JTabbedPane.BOTTOM);
        tw.addTab(i18n.getResult(), comp);
        StringBuilder sb = new StringBuilder().append(i18n.getQuery()).append(":\n").append(query).append("\n").append("\n");
        if (warning != null) {
            while (warning != null) {
                sb.append(warning.toString()).append("\n");
                warning = warning.getNextWarning();
            }
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        tw.addTab(i18n.getLog(), new JScrollPane(ta));
        View tabView = new View(i18n.getQuery(), null, tw);
        tabView.setToolTipText(getText());
        tabWindow.addTab(tabView);
    }

    @Override
    public void setError(Exception ex) {
    }

    private JComponent getNumbers(int row, int col, JTable table) {
        DefaultTableModel rowheadmodel = new DefaultTableModel(row, col);
        JTable rowHeaderTable = new JTable(rowheadmodel);
        rowheadmodel.setColumnCount(1);
        rowheadmodel.setColumnIdentifiers(new Object[] { "" });
        rowHeaderTable.setEnabled(false);
        rowHeaderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        rowHeaderTable.getColumnModel().getColumn(0).setPreferredWidth(row);
        RowHeaderRenderer rh = new RowHeaderRenderer(table);
        rowHeaderTable.setDefaultRenderer(rowHeaderTable.getColumnClass(0), rh);
        int width = rowHeaderTable.getColumnModel().getColumn(0).getPreferredWidth();
        rowHeaderTable.setPreferredScrollableViewportSize(new Dimension(width, 0));
        return rowHeaderTable;
    }

    class Panel extends JComponent {

        public static final int RECT_WIDTH = 250;

        public static final int RECT_HEIGHT = 50;

        private String string;

        public Panel(String string) {
            this.string = string;
            setToolTipText(string);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            Dimension s = getSize();
            int x = (s.width - RECT_WIDTH) / 2;
            int y = (s.height - RECT_HEIGHT) / 2;
            g2.setColor(Color.WHITE);
            g2.fill(new Double(5, 5, s.width - 10, s.height - 10, 10, 10));
            g2.setStroke(new BasicStroke(3.0f, 0, 0, 2.0f, new float[] { 10.0f }, 0.0f));
            g2.setColor(Color.LIGHT_GRAY);
            g2.fill(new Double(x, y, RECT_WIDTH, RECT_HEIGHT, 10, 10));
            int width = g2.getFontMetrics().stringWidth(string);
            int height = g2.getFontMetrics().getAscent();
            int sx = x + (RECT_WIDTH - width) / 2;
            int sy = y + (RECT_HEIGHT) / 2;
            g2.setColor(Color.WHITE);
            g2.drawString(string, sx, sy);
        }
    }
}
