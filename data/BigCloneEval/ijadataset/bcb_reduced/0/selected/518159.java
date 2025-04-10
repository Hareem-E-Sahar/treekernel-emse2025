package de.hattrickorganizer.tools.updater;

import gui.HOColorName;
import gui.HOIconName;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import plugins.IDebugWindow;
import plugins.IOfficialPlugin;
import de.hattrickorganizer.database.DBZugriff;
import de.hattrickorganizer.gui.pluginWrapper.GUIPluginWrapper;
import de.hattrickorganizer.gui.theme.ThemeManager;
import de.hattrickorganizer.model.HOVerwaltung;

/**
 * Abstract class with contains methods for all UpdaterDialogs
 *
 * @author Thorsten Dietz
 *
 * @since 1.35
 */
abstract class UpdaterDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = -991600939074866793L;

    protected JTable table;

    protected String ACT_SHOW_INFO = "ShowInfo";

    protected String HOPLUGINS_DIRECTORY = System.getProperty("user.dir") + File.separator + "hoplugins";

    protected String PROP_APPLY = HOVerwaltung.instance().getLanguageString("Uebernehmen");

    protected String PROP_FILE_NOT_FOUND = HOVerwaltung.instance().getLanguageString("DateiNichtGefunden");

    protected String PROP_HOMEPAGE = HOVerwaltung.instance().getLanguageString("Homepage");

    protected String PROP_NAME = HOVerwaltung.instance().getLanguageString("Name");

    protected String PROP_NEW_START = HOVerwaltung.instance().getLanguageString("NeustartErforderlich");

    protected String okButtonLabel;

    protected String[] columnNames;

    protected Object[] object;

    protected boolean defaultSelected;

    private String ACT_CANCEL = "CANCEL";

    private String ACT_FIND = "FIND";

    private String ACT_SET_ALL = "SET_ALL";

    private String ACT_SET_NONE = "SET_NONE";

    protected UpdaterDialog(Object data, String title) {
        super(GUIPluginWrapper.instance().getOwner4Dialog(), title);
        int dialogWidth = 500;
        int dialogHeight = 400;
        int with = (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth();
        int height = (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight();
        setLocation((with - dialogWidth) / 2, (height - dialogHeight) / 2);
        setSize(dialogWidth, dialogHeight);
    }

    public void actionPerformed(ActionEvent e) {
        String comand = e.getActionCommand();
        UpdaterDialog dialog = (UpdaterDialog) ((JButton) e.getSource()).getTopLevelAncestor();
        if (comand.equals(ACT_CANCEL)) {
            dialog.dispose();
        }
        if (comand.equals(ACT_SET_NONE)) {
            dialog.setAll(false);
        }
        if (comand.equals(ACT_SET_ALL)) {
            dialog.setAll(true);
        }
        if (comand.equals(ACT_FIND)) {
            dialog.action();
        }
        if (comand.equals(ACT_SHOW_INFO)) {
            ((RefreshDialog) dialog).showInfo(((JButton) e.getSource()).getName());
        }
    }

    protected abstract TableModel getModel(boolean selected, String[] columnNames2);

    protected abstract void action();

    protected JCheckBox getCheckbox(boolean isSelected, boolean isEnabled) {
        JCheckBox tmp = new JCheckBox();
        tmp.setOpaque(false);
        tmp.setEnabled(isEnabled);
        tmp.setSelected(isSelected);
        tmp.setHorizontalAlignment(SwingConstants.CENTER);
        return tmp;
    }

    protected JLabel getLabel(boolean isEnabled, String txt) {
        JLabel tmp = new JLabel(txt);
        tmp.setEnabled(isEnabled);
        return tmp;
    }

    protected boolean isUnquenchable(File file, File[] files) {
        if (files == null) {
            return false;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists() && file.getName().equals(files[i].getName())) {
                return true;
            }
        }
        return false;
    }

    protected void clearDirectory(String path, File[] unquenchablesFiles) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    clearDirectory(files[i].getAbsolutePath(), unquenchablesFiles);
                }
                if (!isUnquenchable(files[i], unquenchablesFiles)) {
                    files[i].delete();
                }
            }
        }
    }

    protected JPanel createButtons() {
        JPanel buttonPanel = GUIPluginWrapper.instance().createImagePanel();
        ((FlowLayout) buttonPanel.getLayout()).setAlignment(FlowLayout.RIGHT);
        JButton okButton = new JButton(okButtonLabel);
        okButton.setActionCommand(ACT_FIND);
        okButton.addActionListener(this);
        JButton cancelButton = new JButton(HOVerwaltung.instance().getLanguageString("Abbrechen"));
        cancelButton.setActionCommand(ACT_CANCEL);
        cancelButton.addActionListener(this);
        JButton selectAllButton = new JButton(ThemeManager.getIcon(HOIconName.CHECKBOXSELECTED));
        selectAllButton.setBackground(ThemeManager.getColor(HOColorName.BUTTON_BG));
        selectAllButton.setPreferredSize(new Dimension(23, 23));
        selectAllButton.setActionCommand(ACT_SET_ALL);
        selectAllButton.addActionListener(this);
        JButton selectNoneButton = new JButton(ThemeManager.getIcon(HOIconName.CHECKBOXNOTSELECTED));
        selectNoneButton.setBackground(selectAllButton.getBackground());
        selectNoneButton.setPreferredSize(new Dimension(23, 23));
        selectNoneButton.setActionCommand(ACT_SET_NONE);
        selectNoneButton.addActionListener(this);
        buttonPanel.add(selectAllButton);
        buttonPanel.add(selectNoneButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    protected JScrollPane createTable() {
        table = new JTable(getModel(defaultSelected, columnNames));
        table.setDefaultRenderer(Object.class, new UpdaterCellRenderer());
        table.getTableHeader().setReorderingAllowed(false);
        if (table.getColumnCount() == 5) {
            table.getColumn(HOVerwaltung.instance().getLanguageString("Notizen")).setCellEditor(new TableEditor());
        }
        table.getColumn(columnNames[0]).setCellEditor(new TableEditor());
        JScrollPane scroll = new JScrollPane(table);
        return scroll;
    }

    protected void deletePlugin(Object plugin, boolean withTables) {
        File[] unquenchableFiles = new File[0];
        String pluginName = plugin.getClass().getName();
        pluginName = pluginName.substring(pluginName.indexOf(".") + 1);
        if (withTables) {
            deletePluginTables(pluginName);
        }
        File classFile = new File(HOPLUGINS_DIRECTORY + File.separator + pluginName + ".class");
        if (classFile.exists()) {
            classFile.delete();
            if (plugin instanceof IOfficialPlugin) {
                unquenchableFiles = ((IOfficialPlugin) plugin).getUnquenchableFiles();
            }
            clearDirectory(HOPLUGINS_DIRECTORY + File.separator + pluginName, unquenchableFiles);
            classFile = new File(HOPLUGINS_DIRECTORY + File.separator + pluginName);
            classFile.delete();
        }
    }

    protected void deletePluginTables(String pluginname) {
        try {
            ArrayList<String> droptables = new ArrayList<String>();
            Object[] tables = DBZugriff.instance().getAdapter().getDBInfo().getAllTablesNames();
            for (int i = 0; i < tables.length; i++) {
                if (tables[i].toString().toUpperCase(java.util.Locale.ENGLISH).startsWith(pluginname.toUpperCase(java.util.Locale.ENGLISH))) {
                    droptables.add(tables[i].toString());
                }
            }
            for (int i = 0; i < droptables.size(); i++) {
                DBZugriff.instance().getAdapter().executeUpdate("DROP TABLE " + droptables.get(i));
            }
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    protected void handleException(Exception e, String txt) {
        showException(e, txt);
    }

    protected void show(String key) {
        JOptionPane.showMessageDialog(null, key);
    }

    protected void showException(Exception ex, String itxt) {
        IDebugWindow debugWindow = GUIPluginWrapper.instance().createDebugWindow(new Point(100, 200), new Dimension(700, 400));
        debugWindow.setVisible(true);
        debugWindow.append(itxt);
        debugWindow.append(ex);
    }

    private void setAll(boolean value) {
        for (int i = 0; i < table.getRowCount(); i++) {
            JCheckBox tmp = (JCheckBox) table.getValueAt(i, 0);
            if (tmp.isEnabled()) {
                tmp.setSelected(value);
            }
        }
        table.repaint();
    }
}
