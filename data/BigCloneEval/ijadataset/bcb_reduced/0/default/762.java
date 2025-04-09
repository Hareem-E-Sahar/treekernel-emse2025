import info.clearthought.layout.TableLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * @author Markus Plessing
 */
public class DBImport extends JFrame {

    private static DBImport dbimport;

    private String[] splittedHead;

    private String[][] splittedBody;

    private JFrame frame = null;

    private JTable dbtable = new JTable();

    private JTable dbtable2;

    private JList list;

    private String tablename;

    private JTable savetable;

    private JFrame resultFrame;

    private DefaultTableModel savemodel;

    private Chainlist chainlist = new Chainlist();

    private JScrollPane scrollpane = new JScrollPane(dbtable);

    private int tableCount = 0, mergeRow = -1;

    private Node work;

    private boolean createPopup = true;

    private JPopupMenu popup = new JPopupMenu();

    private final String driver = "org.postgresql.Driver";

    private String dburl = "jdbc:postgresql://[192.168.0.21]:5432/dumpDB", dbuser = "postgres", passwd = "x15A9b", inputFile = "/home/mp/kunden.txt";

    private JTextField jTFdburl = new JTextField("", 36);

    private JTextField jTFdbuser = new JTextField("", 36);

    private JPasswordField jTFpasswd = new JPasswordField("", 36);

    private JTextField jTFimportFile = new JTextField("", 36);

    private final JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

    private final JFileChooser fc2 = new JFileChooser(new File(System.getProperty("user.dir")));

    private final AbstractAction Esc = new AbstractAction("Beenden") {

        {
            putValue(Action.SHORT_DESCRIPTION, "Beenden des DB Importdialogs");
            putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
        }

        public void actionPerformed(ActionEvent e) {
            if (dbimport != null && frame == null) {
                dbimport.processWindowEvent(new WindowEvent(dbimport, WindowEvent.WINDOW_CLOSING));
            } else if (frame != null) {
                dbimport.processWindowEvent(new WindowEvent(dbimport, WindowEvent.WINDOW_CLOSING));
                frame.setVisible(false);
                frame.dispose();
                frame = null;
            }
        }
    };

    private final AbstractAction save = new AbstractAction("Ok") {

        {
            putValue(Action.SHORT_DESCRIPTION, "Speichern und Starten");
            putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
        }

        public void actionPerformed(ActionEvent e) {
            frame.dispose();
            frame = null;
            dburl = jTFdburl.getText();
            dbuser = jTFdbuser.getText();
            passwd = jTFpasswd.getPassword().toString();
            inputFile = jTFimportFile.getText();
            dbimport.createAndShowGUI();
        }
    };

    private final AbstractAction fileChooser = new AbstractAction("Durchsuchen ...") {

        {
            putValue(Action.SHORT_DESCRIPTION, "Pfad zur Importdatei suchen.");
        }

        public void actionPerformed(ActionEvent e) {
            fc.showOpenDialog(dbimport);
            File selFile = fc.getSelectedFile();
            if (selFile != null) {
                jTFimportFile.setText(selFile.toString());
            }
        }
    };

    private final AbstractAction insert = new AbstractAction("") {

        {
            putValue(Action.SMALL_ICON, IconUtils.getNavigationIcon("Back", 16));
            putValue(Action.SHORT_DESCRIPTION, "Ausgewählten Wert selektiertem Feld zuweisen.");
        }

        public void actionPerformed(ActionEvent e) {
            if (dbtable.getSelectedRow() >= 0 && dbtable2.getSelectedRow() >= 0) {
                dbtable.setValueAt(dbtable2.getValueAt(dbtable2.getSelectedRow(), 0), dbtable.getSelectedRow(), 3);
                dbtable.setValueAt(dbtable2.getValueAt(dbtable2.getSelectedRow(), 1), dbtable.getSelectedRow(), 2);
            }
        }
    };

    private final AbstractAction create = new AbstractAction("") {

        {
            putValue(Action.SMALL_ICON, IconUtils.getNavigationIcon("Forward", 16));
            putValue(Action.SHORT_DESCRIPTION, "Instanz der ausgewählten Tabelle erstellen.");
        }

        public void actionPerformed(ActionEvent e) {
            if (list.getSelectedIndex() >= 0) {
                if (scrollpane != null && dbtable != null) {
                    scrollpane.remove(dbtable);
                    dbimport.remove(scrollpane);
                    scrollpane = null;
                    dbtable = null;
                }
                String sel = list.getModel().getElementAt(list.getSelectedIndex()).toString();
                tablename = sel;
                scrollpane = createTableView(sel);
                dbimport.getContentPane().add(scrollpane, "5,1, l,c");
                dbimport.getContentPane().validate();
            }
        }
    };

    private final AbstractAction put = new AbstractAction("") {

        {
            putValue(Action.SMALL_ICON, IconUtils.getNavigationIcon("Down", 16));
            putValue(Action.SHORT_DESCRIPTION, "Im Bearbeitungsmodus befindliche Tabelle speichern");
        }

        public void actionPerformed(ActionEvent e) {
            String[][] elements = null;
            if (tablename != null) {
                int rowCount = dbtable.getRowCount();
                int columnCount = dbtable.getColumnCount();
                elements = new String[rowCount][columnCount];
                for (int i = 0; i < rowCount; i++) {
                    for (int a = 0; a < columnCount; a++) {
                        elements[i][a] = dbtable.getValueAt(i, a).toString();
                    }
                }
                chainlist.insertAfter(chainlist.getLastNode(), tablename, elements);
                savemodel.addRow(new Object[] { tablename });
                dbimport.getContentPane().remove(scrollpane);
                scrollpane.remove(dbtable);
                dbtable = null;
                scrollpane = null;
                dbtable = new JTable();
                scrollpane = new JScrollPane(dbtable);
                dbimport.getContentPane().add(scrollpane, "5,1,l,c");
                dbimport.getContentPane().validate();
                dbimport.getContentPane().repaint();
                tablename = null;
            }
        }
    };

    private final AbstractAction get = new AbstractAction("") {

        {
            putValue(Action.SMALL_ICON, IconUtils.getNavigationIcon("Up", 16));
            putValue(Action.SHORT_DESCRIPTION, "Selektierten Eintrag bearbeiten");
        }

        public void actionPerformed(ActionEvent evt) {
            if (savetable.getSelectedRow() >= 0) {
                work = chainlist.getNodeAt(savetable.getSelectedRow() + 1);
                tablename = savetable.getValueAt(savetable.getSelectedRow(), 0).toString();
                savemodel.removeRow(savetable.getSelectedRow());
                dbimport.getContentPane().remove(scrollpane);
                scrollpane.remove(dbtable);
                dbtable = null;
                scrollpane = null;
                DefaultTableModel defmodel = new DefaultTableModel();
                defmodel.setDataVector(work.elements, new Object[] { "Feld", "type", "Wert", "Pos." });
                dbtable = new JTable(defmodel) {

                    public boolean isCellEditable(int rowIndex, int vColIndex) {
                        return false;
                    }

                    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                        if (c instanceof JComponent) {
                            JComponent jc = (JComponent) c;
                            jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
                        }
                        return c;
                    }
                };
                dbtable.getTableHeader().setReorderingAllowed(false);
                int[] width = { 150, 0, 100, 0 };
                for (int a = 0; a < dbtable.getColumnCount(); a++) {
                    TableColumn col = dbtable.getColumnModel().getColumn(a);
                    TableColumn head = dbtable.getTableHeader().getColumnModel().getColumn(a);
                    col.setMaxWidth(width[a]);
                    col.setMinWidth(width[a]);
                    head.setMaxWidth(width[a]);
                    head.setMinWidth(width[a]);
                }
                dbtable.addMouseListener(new MouseAdapter() {

                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            Point p = e.getPoint();
                            int row = dbtable.rowAtPoint(p);
                            String retVal = JOptionPane.showInputDialog(dbimport, "Wert eingeben : ", "Manuelle Werteingabe", JOptionPane.QUESTION_MESSAGE);
                            if (retVal == null) {
                                retVal = "";
                            }
                            dbtable.setValueAt(retVal, row, 2);
                        }
                    }
                });
                scrollpane = new JScrollPane(dbtable);
                dbimport.getContentPane().add(scrollpane, "5,1,l,c");
                chainlist.delete(work);
                dbimport.getContentPane().validate();
                dbimport.getContentPane().repaint();
            }
        }
    };

    private final AbstractAction del = new AbstractAction("") {

        {
            putValue(Action.SMALL_ICON, IconUtils.getNavigationIcon("Delete", 16));
            putValue(Action.SHORT_DESCRIPTION, "Selektiertes Element löschen");
        }

        public void actionPerformed(ActionEvent evt) {
            if (savetable.getSelectedRow() >= 0) {
                chainlist.delete(chainlist.getNodeAt(savetable.getSelectedRow() + 1));
                savemodel.removeRow(savetable.getSelectedRow());
            }
        }
    };

    private final AbstractAction out = new AbstractAction("Import") {

        {
            putValue(Action.SHORT_DESCRIPTION, "Daten importieren");
        }

        public void actionPerformed(ActionEvent evt) {
            showResultFrame();
        }
    };

    private final String getTableNames = "select tablename from pg_tables where schemaname='public' order by tablename ASC";

    private final String getColumnNames = "select attname, atttypid from pg_class c join pg_attribute a on c.oid = a.attrelid  where c.relname = ? and a.attnum > 1 and a.atttypid > 0";

    private JTextArea resultArea = new JTextArea("", 25, 50);

    /**Constructor for DBImport*/
    public DBImport() {
        dbimport = this;
        showStartFrame();
    }

    private void createAndShowGUI() {
        this.setTitle("DatenbankImport");
        this.setIconImage(IconUtils.getCustomIcon("Data").getImage());
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        double[][] sizes = { { 20, 130, 10, 50, 10, 250, 10, 50, 10, 170, 20 }, { 20, 450, 30, 20, 100, 10, 20, 10, 20 } };
        this.setJMenuBar(setupMainMenu());
        this.setSize(new Dimension(710, 520));
        Dimension window = this.getSize();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.getContentPane().setLayout(new TableLayout(sizes));
        JScrollPane pane1 = createDatabaseView();
        JScrollPane pane3 = createFileHeaderView(inputFile);
        JButton button = new JButton(insert);
        JButton button2 = new JButton(create);
        JButton button3 = new JButton(put);
        JButton button4 = new JButton(get);
        JButton button5 = new JButton(del);
        JButton button6 = new JButton(out);
        savemodel = new DefaultTableModel();
        savemodel.setDataVector(null, new Object[] { "Tabletype" });
        savetable = new JTable(savemodel) {

            public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
            }

            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
                }
                return c;
            }
        };
        JScrollPane scrollpane2 = new JScrollPane(savetable);
        JPanel saveeditPanel = new JPanel();
        saveeditPanel.add(button3);
        saveeditPanel.add(button4);
        this.getContentPane().add(pane1, "1,1, l,c");
        this.getContentPane().add(button2, "3,1,l,c");
        this.getContentPane().add(scrollpane, "5,1,l,c");
        this.getContentPane().add(button, "7,1, c,c");
        this.getContentPane().add(pane3, "9,1, l,c");
        this.getContentPane().add(saveeditPanel, "5,2");
        this.getContentPane().add(scrollpane2, "5,4,c,c");
        this.getContentPane().add(button5, "7,4, c,c");
        this.getContentPane().add(button6, "5,6, c,c");
        this.setLocation(screen.width / 2 - window.width / 2, screen.height / 2 - window.height / 2);
        this.pack();
        this.show();
    }

    private JScrollPane createDatabaseView() {
        JScrollPane dbpane = null;
        try {
            Class.forName(driver).newInstance();
            Connection con = java.sql.DriverManager.getConnection(dburl, dbuser, passwd);
            int i = 0;
            String[] tableNames = null;
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(getTableNames);
            while (rs.next()) {
                tableCount++;
            }
            tableNames = new String[tableCount];
            rs.beforeFirst();
            while (rs.next()) {
                tableNames[i++] = rs.getString("tablename");
            }
            list = new JList(tableNames);
            dbpane = new JScrollPane(list);
            rs.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            new ErrorHandler(ex);
            processWindowEvent(new WindowEvent(dbimport, WindowEvent.WINDOW_CLOSING));
        }
        list.setPreferredSize(new Dimension(123, 393));
        dbpane.setPreferredSize(new Dimension(130, 400));
        return dbpane;
    }

    private JScrollPane createTableView(String table) {
        JScrollPane dbpane = null;
        int i = 0;
        try {
            Class.forName(driver).newInstance();
            Connection con = java.sql.DriverManager.getConnection(dburl, dbuser, passwd);
            ResultSet rs = null;
            int sumCnt = 0;
            PreparedStatement pstmt = con.prepareStatement(getColumnNames);
            pstmt.setString(1, table);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                sumCnt++;
            }
            Object[][] tableRows = new Object[sumCnt][4];
            rs.beforeFirst();
            while (rs.next()) {
                tableRows[i][0] = "" + table + "." + rs.getString("attname");
                if (rs.getInt("atttypid") > 23) {
                    tableRows[i][1] = "" + 1;
                } else {
                    tableRows[i][1] = "" + 0;
                }
                tableRows[i][2] = "";
                tableRows[i++][3] = "";
            }
            DefaultTableModel model = new DefaultTableModel();
            model.setDataVector(tableRows, new Object[] { "Feld", "type", "Wert", "Pos." });
            dbtable = new JTable(model) {

                public boolean isCellEditable(int rowIndex, int vColIndex) {
                    return false;
                }

                public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                    Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                    if (c instanceof JComponent) {
                        JComponent jc = (JComponent) c;
                        jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
                    }
                    return c;
                }
            };
            dbtable.getTableHeader().setReorderingAllowed(false);
            int[] width = { 150, 0, 100, 0 };
            for (int a = 0; a < dbtable.getColumnCount(); a++) {
                TableColumn col = dbtable.getColumnModel().getColumn(a);
                TableColumn head = dbtable.getTableHeader().getColumnModel().getColumn(a);
                col.setMaxWidth(width[a]);
                col.setMinWidth(width[a]);
                head.setMaxWidth(width[a]);
                head.setMinWidth(width[a]);
            }
            dbtable.addMouseListener(new MouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        Point p = e.getPoint();
                        int row = dbtable.rowAtPoint(p);
                        String retVal = JOptionPane.showInputDialog(dbimport, "Wert eingeben : ", "Manuelle Werteingabe", JOptionPane.QUESTION_MESSAGE);
                        if (retVal == null) {
                            retVal = "";
                        }
                        dbtable.setValueAt(retVal, row, 2);
                        dbtable.setValueAt("", row, 3);
                    }
                }
            });
            dbpane = new JScrollPane(dbtable);
            rs.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            new ErrorHandler(ex);
        }
        return dbpane;
    }

    /**
     * setupMainMenu : create the main Menubar and the Popup for merge
     * @return JMenuBar the Menubar for the Frame
     * */
    private JMenuBar setupMainMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu jMFile = new JMenu("Datei");
        JMenuItem jMFileIImport = new JMenuItem("Importeinstellungen");
        jMFileIImport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showStartFrame();
            }
        });
        JMenuItem jMFileIExit = new JMenuItem(Esc);
        jMFile.add(jMFileIImport);
        jMFile.addSeparator();
        jMFile.add(jMFileIExit);
        if (createPopup) {
            final JMenuItem jMPopupIGetMerge = new JMenuItem("Verbinden mit ...");
            final JMenuItem jMPopupIDoMerge = new JMenuItem("... hiermit verbinden.");
            jMPopupIGetMerge.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    mergeRow = dbtable2.getSelectedRow();
                    jMPopupIGetMerge.setEnabled(false);
                    jMPopupIDoMerge.setEnabled(true);
                }
            });
            jMPopupIDoMerge.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    int domergeRow = dbtable2.getSelectedRow();
                    String mergeVal = dbtable2.getValueAt(mergeRow, 1).toString() + " & " + dbtable2.getValueAt(domergeRow, 1);
                    String mergeInt = dbtable2.getValueAt(mergeRow, 0).toString() + " & " + dbtable2.getValueAt(domergeRow, 0);
                    dbtable2.setValueAt(mergeVal, domergeRow, 1);
                    dbtable2.setValueAt(mergeInt, domergeRow, 0);
                    DefaultTableModel defmodel = (DefaultTableModel) dbtable2.getModel();
                    defmodel.removeRow(mergeRow);
                    mergeRow = -1;
                    jMPopupIGetMerge.setEnabled(true);
                    jMPopupIDoMerge.setEnabled(false);
                }
            });
            jMPopupIDoMerge.setEnabled(false);
            popup.add(jMPopupIGetMerge);
            popup.add(jMPopupIDoMerge);
            createPopup = false;
        }
        menuBar.add(jMFile);
        return menuBar;
    }

    private void showStartFrame() {
        frame = new JFrame() {

            protected void processWindowEvent(WindowEvent e) {
                super.processWindowEvent(e);
                if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    this.setVisible(false);
                    this.dispose();
                    frame = null;
                }
            }
        };
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        double sizes[][] = { { 20, 150, 200, 130, 20 }, { 20, 30, 30, 30, 30, 30, 30, 20 } };
        JButton ok = new JButton(save);
        JButton esc = new JButton(Esc);
        frame.setTitle("Einstellungen zum Datenbankimport");
        frame.setIconImage(IconUtils.getCustomIcon("Data").getImage());
        JPanel panel1 = new JPanel(new TableLayout(sizes));
        JLabel jLbdbname = new JLabel("Datenbankurl : ");
        JLabel jLbdbuser = new JLabel("Datenbankuser : ");
        JLabel jLbdbpass = new JLabel("Datenbankpasswort : ");
        JLabel jLbimportFile = new JLabel("ImportDatei : ");
        jTFpasswd.setEchoChar('*');
        jTFdburl.setText(dburl);
        jTFdbuser.setText(dbuser);
        jTFpasswd.setText(passwd);
        jTFimportFile.setText(inputFile);
        JButton jBimportFile = new JButton(fileChooser);
        panel1.add(jLbdbname, "1,1, l,c");
        panel1.add(jTFdburl, "2,1,3,1, l,c");
        panel1.add(jLbdbuser, "1,2, l,c");
        panel1.add(jTFdbuser, "2,2,3,2, l,c");
        panel1.add(jLbdbpass, "1,3, l,c");
        panel1.add(jTFpasswd, "2,3,3,3, l,c");
        panel1.add(jLbimportFile, "1,4, l,c");
        panel1.add(jTFimportFile, "2,4, l,c");
        panel1.add(jBimportFile, "3,4, l,c");
        panel1.add(ok, "2,5, r,c");
        panel1.add(esc, "3,5, l,c");
        frame.getContentPane().add(panel1);
        frame.pack();
        Dimension window = frame.getSize();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screen.width / 2 - window.width / 2, screen.height / 2 - window.height / 2);
        frame.show();
    }

    private JScrollPane createFileHeaderView(String filepath) {
        JScrollPane dbpane = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "ISO-8859-1"));
            String str;
            str = in.readLine();
            splittedHead = str.split("#");
            Object[][] tableRows = new Object[splittedHead.length][2];
            DefaultTableModel model = new DefaultTableModel();
            for (int i = 0; i < splittedHead.length; i++) {
                tableRows[i][1] = splittedHead[i];
                tableRows[i][0] = "" + i;
            }
            model.setDataVector(tableRows, new Object[] { "Pos.", "Filedata" });
            dbtable2 = new JTable(model) {

                public boolean isCellEditable(int rowIndex, int vColIndex) {
                    return false;
                }

                public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                    Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                    if (c instanceof JComponent) {
                        JComponent jc = (JComponent) c;
                        jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
                    }
                    return c;
                }
            };
            dbtable2.addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
                        Point p = e.getPoint();
                        popup.show(dbtable2, p.x, p.y);
                        popup.setVisible(true);
                    } else {
                        popup.setVisible(false);
                    }
                }
            });
            dbtable2.getTableHeader().setReorderingAllowed(false);
            int[] width = { 0, 183 };
            for (int a = 0; a < dbtable2.getColumnCount(); a++) {
                TableColumn col = dbtable2.getColumnModel().getColumn(a);
                TableColumn head = dbtable2.getTableHeader().getColumnModel().getColumn(a);
                col.setMaxWidth(width[a]);
                col.setMinWidth(width[a]);
                head.setMaxWidth(width[a]);
                head.setMinWidth(width[a]);
            }
            dbpane = new JScrollPane(dbtable2);
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            new ErrorHandler(ex);
            processWindowEvent(new WindowEvent(dbimport, WindowEvent.WINDOW_CLOSING));
        }
        readFileBody(inputFile);
        return dbpane;
    }

    private void readFileBody(String filepath) {
        int lines = 0, a = 0, i = 0;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "ISO-8859-1"));
            String str;
            while ((str = in.readLine()) != null) {
                lines++;
            }
            splittedBody = new String[lines][splittedHead.length + 1];
            in.close();
            in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "ISO-8859-1"));
            while ((str = in.readLine()) != null) {
                String[] splitted = str.split("#");
                for (i = 0; i < splitted.length; i++) {
                    splittedBody[a][i] = splitted[i];
                }
                a++;
            }
            in.close();
            in = null;
        } catch (Exception ex) {
            ex.printStackTrace();
            new ErrorHandler(ex);
        }
    }

    private void showResultFrame() {
        resultFrame = new JFrame() {

            protected void processWindowEvent(WindowEvent e) {
                super.processWindowEvent(e);
                if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    this.setVisible(false);
                    this.dispose();
                    resultFrame = null;
                }
            }
        };
        resultFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Datei");
        resultFrame.setTitle("Importausgabe");
        JScrollPane pane = new JScrollPane(resultArea);
        resultArea.setText("");
        JMenuItem item1 = new JMenuItem("Alles markieren");
        JMenuItem item2 = new JMenuItem("Speichern unter");
        JMenuItem item3 = new JMenuItem("Schliessen");
        JMenuItem item4 = new JMenuItem("Alles kopieren");
        menu.add(item1);
        menu.add(item4);
        menu.add(item2);
        menu.addSeparator();
        menu.add(item3);
        menuBar.add(menu);
        item1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                resultArea.setSelectionStart(0);
                resultArea.setSelectionEnd(resultArea.getText().length());
            }
        });
        item2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (fc2.showSaveDialog(resultFrame) != JFileChooser.CANCEL_OPTION) {
                    File file = fc2.getSelectedFile();
                    if (file != null) {
                        try {
                            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                            writer.write(resultArea.getText());
                            writer.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            new ErrorHandler(ex);
                        }
                    }
                }
            }
        });
        item3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                resultFrame.dispose();
            }
        });
        item4.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                resultArea.setSelectionStart(0);
                resultArea.setSelectionEnd(resultArea.getText().length());
                StringSelection ss = new StringSelection(resultArea.getSelectedText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            }
        });
        resultFrame.setJMenuBar(menuBar);
        resultFrame.getContentPane().add(pane);
        resultFrame.pack();
        Dimension size = resultFrame.getSize();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        resultFrame.setLocation(screen.width / 2 - size.width / 2, screen.height / 2 - size.height / 2);
        resultFrame.show();
        createOutput();
    }

    private void createOutput() {
        StringBuffer buff = new StringBuffer();
        StringBuffer values = new StringBuffer();
        StringBuffer vars = new StringBuffer();
        work = chainlist.head;
        while (work.next != chainlist.end) {
            work = work.next;
            int Cnt = 1;
            while (Cnt < splittedBody.length) {
                values.delete(0, values.length());
                vars.delete(0, vars.length());
                for (int i = 0; i < work.elements.length; i++) {
                    vars.append(work.elements[i][0].replaceFirst(work.nodeName, " ").replace('.', ' ').replaceAll("-", "\\\\-").trim());
                    if (work.elements[i][1] == "0") {
                        if (work.elements[i][2].equals("")) {
                            values.append("0");
                        } else if (work.elements[i][2].equals("<COUNT>")) {
                            values.append(Cnt);
                        } else if (work.elements[i][3].equals("")) {
                            values.append(work.elements[i][2]);
                        } else {
                            values.append(splittedBody[Cnt][Integer.parseInt(work.elements[i][3])]);
                        }
                    } else {
                        if (work.elements[i][2].equals("")) {
                            values.append("''");
                        } else if (work.elements[i][2].equals("<COUNT>")) {
                            values.append((Cnt - 1));
                        } else if (work.elements[i][3].equals("")) {
                            values.append("'" + work.elements[i][2].replaceAll("'", "\\\\'").replaceAll("-", "\\\\-") + "'".replace(',', '.'));
                        } else {
                            if (work.elements[i][3].indexOf("&") > 0) {
                                String[] split = work.elements[i][3].split("&");
                                int[] vals = new int[split.length];
                                for (int x = 0; x < vals.length; x++) {
                                    vals[x] = Integer.parseInt(split[x].trim());
                                }
                                values.append("'");
                                for (int y = 0; y < vals.length; y++) {
                                    values.append(splittedBody[Cnt][vals[y]].replaceAll("'", "\\\\'").replace(',', '.'));
                                    if (y < vals.length - 1) {
                                        values.append(" ");
                                    }
                                }
                                values.append("'");
                            } else {
                                values.append("'" + splittedBody[Cnt][Integer.parseInt(work.elements[i][3])].replaceAll("'", "\\\\'") + "'".replace(',', '.'));
                            }
                        }
                    }
                    if (i < work.elements.length - 1) {
                        vars.append(",");
                        values.append(",");
                    }
                }
                buff.append("INSERT INTO " + work.nodeName + " (" + vars.toString() + ") VALUES (" + values.toString() + ");\n");
                Cnt++;
            }
        }
        resultArea.setText(buff.toString());
        buff.delete(0, buff.length());
        buff = null;
        work = null;
        vars = null;
        values = null;
    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            this.setVisible(false);
            this.dispose();
            if (dbimport != null) {
                dbimport = null;
                chainlist.clearList();
                chainlist = null;
                dbtable = null;
                dbtable2 = null;
                savetable = null;
                splittedHead = null;
                splittedBody = null;
            }
            if (frame != null) {
                frame.dispose();
                frame = null;
            }
            if (resultFrame != null) {
                resultFrame.dispose();
                resultFrame = null;
            }
        }
    }

    /**Main Method of DBImport
     * @param args String[] not used yet
     */
    public static void main(String[] args) {
        new DBImport();
    }
}
