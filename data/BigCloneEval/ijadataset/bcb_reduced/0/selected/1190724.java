package ru.sitekeeper.cpn.gui;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import ru.sitekeeper.cpn.ClassInfoReader;
import ru.sitekeeper.cpn.EnrichedClassEntry;

/**
 * 
 * @author $Author: alx27 $
 * @version $Id: FoundClassesDialog.java,v 1.1 2008/08/03 08:04:47 alx27 Exp $
 *
 */
public class FoundClassesDialog extends JDialog {

    private static final int DEFAULT_WIDTH = 760;

    private static final int DEFAULT_HEIGHT = 260;

    private static final String PREF_SIZE_X = "sizeX";

    private static final String PREF_SIZE_Y = "sizeY";

    private final Preferences prefs = Preferences.userNodeForPackage(FoundClassesDialog.class);

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JScrollPane jScrollPane = null;

    private JTable jTable = null;

    private DefaultTableModel model = null;

    /**
	 * @param owner
	 */
    public FoundClassesDialog(Frame owner) {
        super(owner);
        initialize();
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setTitle("Classes found:");
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setIconImages(ru.sitekeeper.cpn.gui.Utils.getIcons());
        this.setSize(prefs.getInt(PREF_SIZE_X, DEFAULT_WIDTH), prefs.getInt(PREF_SIZE_Y, DEFAULT_HEIGHT));
        setupActions();
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {
                doSaveWindowPrefs();
            }

            @Override
            public void windowClosed(final WindowEvent e) {
                doSaveWindowPrefs();
            }
        });
    }

    private void doSaveWindowPrefs() {
        final Rectangle r = getBounds();
        prefs.putInt(PREF_SIZE_X, r.width);
        prefs.putInt(PREF_SIZE_Y, r.height);
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTable());
        }
        return jScrollPane;
    }

    /**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
    private JTable getJTable() {
        if (jTable == null) {
            jTable = new JTable();
        }
        return jTable;
    }

    public static void showDialog(Frame owner, ActionFindClass cf) {
        FoundClassesDialog dlg = new FoundClassesDialog(owner);
        dlg.setClassFinder(cf);
        dlg.adjustColumns();
        dlg.validate();
        Utils.alignCenter(owner, dlg);
        dlg.setVisible(true);
    }

    private void setClassFinder(ActionFindClass cf) {
        model = new ReadOnlyTableModel();
        for (EnrichedClassEntry sce : cf.getFoundClasses()) {
            String name = sce.getClassname();
            model.addRow(new Object[] { name, sce.getFile().getAbsolutePath(), sce.getSize(), ClassInfoReader.getHumanReadable(sce.getVersion()) });
        }
        jTable.setModel(model);
        jTable.setRowSorter(new TableRowSorter<DefaultTableModel>(model));
    }

    private void adjustColumns() {
        for (int i = 0; i < model.getColumnCount(); i++) {
            int maxSize = 0;
            for (int j = 0; j < model.getRowCount(); j++) {
                TableCellRenderer rend = jTable.getCellRenderer(j, i);
                Component c = rend.getTableCellRendererComponent(jTable, jTable.getValueAt(j, i), false, false, j, i);
                maxSize = Math.max(maxSize, c.getPreferredSize().width);
            }
            jTable.getColumnModel().getColumn(i).setPreferredWidth(maxSize + 20);
        }
        jTable.doLayout();
        jTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                    doHandleDoubleClick();
                }
            }
        });
    }

    private void doHandleDoubleClick() {
        if (jTable.getSelectedRowCount() == 1) {
            int row = jTable.getSelectedRow();
            String path = (String) model.getValueAt(row, 1);
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open((new File(path)).getParentFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupActions() {
        Utils.bindActionToShortcut(this, new ActionClose(this), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }
}
