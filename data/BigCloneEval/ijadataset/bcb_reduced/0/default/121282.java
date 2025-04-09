import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.ButtonGroup;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultEditorKit;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class BookGUI extends JFrame implements ActionListener, KeyListener, TableModelListener, MouseListener {

    JTable table;

    BookTable booktable;

    JTextField text, textName, textVolumes, textNotes;

    JComboBox textAuthor;

    String file = "", path = "", version = "0.5.4", date = "2006.10.17";

    JRadioButton buttonRead, buttonEnd, buttonBurn;

    JFrame newbook;

    JPopupMenu popup;

    TableSorter sorter;

    public BookGUI() throws Exception {
        new File(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "bml-backup" + File.separator).mkdirs();
        new File(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "bml-setting" + File.separator).mkdirs();
        init();
    }

    private void init() throws Exception {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
        }
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                BookGUI.this.windowClosed();
            }
        });
        setJMenuBar(buildMenuBar());
        booktable = new BookTable();
        sorter = new TableSorter(booktable);
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());
        table.getColumnModel().getColumn(0).setMinWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(50);
        table.getColumnModel().getColumn(6).setMaxWidth(75);
        table.getColumnModel().getColumn(3).setMinWidth(50);
        table.getColumnModel().getColumn(4).setMinWidth(50);
        table.getColumnModel().getColumn(5).setMinWidth(50);
        table.getColumnModel().getColumn(6).setMinWidth(75);
        table.getColumnModel().getColumn(7).setMinWidth(150);
        table.setPreferredScrollableViewportSize(new Dimension(800, 600));
        table.getModel().addTableModelListener(this);
        table.addMouseListener(this);
        JScrollPane scrollPane = new JScrollPane(table);
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        add(scrollPane, c);
        text = new JTextField();
        text.addKeyListener(this);
        text.setPreferredSize(new Dimension(520, 20));
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 1;
        add(text, c);
        title();
        loadLast();
        loadSetting();
        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getSize().width) / 2, 40);
        buildPopupMenu();
    }

    private void title() {
        String s = "Bookman - " + file;
        setTitle(s + (booktable.isModified() ? "*" : ""));
    }

    private void loadLast() throws Exception {
        if (new File(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "setting.bms").exists()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "setting.bms"), "UTF8"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(":", 2);
                if (data[0].equals("last")) {
                    path = data[1].substring(0, data[1].lastIndexOf(File.separatorChar) + 1);
                    file = data[1].substring(data[1].lastIndexOf(File.separatorChar) + 1);
                    booktable.open(path + file);
                }
            }
            br.close();
        }
    }

    private void saveLast() throws Exception {
        new File(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "setting.bms").createNewFile();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "setting.bms"), "UTF8"));
        pw.println("last:" + path + file);
        pw.close();
    }

    private void loadSetting() throws Exception {
        if (new File(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "bml-setting" + File.separator + (file.split("[.]"))[0] + ".bms").exists()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "bml-setting" + File.separator + file.split("[.]")[0] + ".bms"), "UTF8"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(":", 2);
                if (data[0].equals("sort")) {
                    sorter.cancelSorting();
                    String[] list = data[1].split(",");
                    for (String s : list) {
                        String[] dir = s.split(" ");
                        sorter.setSortingStatus(Integer.parseInt(dir[0]), Integer.parseInt(dir[1]));
                    }
                }
            }
            br.close();
        }
    }

    private void saveSetting() throws Exception {
        new File(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "bml-setting" + File.separator + file.split("[.]")[0] + ".bms").createNewFile();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "bml-setting" + File.separator + file.split("[.]")[0] + ".bms"), "UTF8"));
        if (sorter.isSorting()) pw.println("sort:" + sorter.getSortingList());
        pw.close();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(buildFileMenu());
        menuBar.add(buildEditMenu());
        menuBar.add(buildHelpMenu());
        return menuBar;
    }

    private JMenu buildFileMenu() {
        JMenu menu = null;
        JMenuItem item = null;
        menu = new JMenu();
        menu.setText("File");
        menu.setMnemonic('F');
        item = new JMenuItem();
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
        item.setText("New List");
        item.setMnemonic('L');
        item.setActionCommand("CMD_FILE_NEW");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem();
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        item.setText("Open...");
        item.setMnemonic('O');
        item.setActionCommand("CMD_FILE_OPEN");
        item.addActionListener(this);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem();
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        item.setText("Save");
        item.setMnemonic('S');
        item.setActionCommand("CMD_FILE_SAVE");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem();
        item.setText("Save As");
        item.setMnemonic('A');
        item.setActionCommand("CMD_FILE_SAVE_AS");
        item.addActionListener(this);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem();
        item.setText("Export as csv");
        item.setMnemonic('E');
        item.setActionCommand("CMD_FILE_EXPORT_CSV");
        item.addActionListener(this);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem();
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl P"));
        item.setText("Print");
        item.setMnemonic('P');
        item.setActionCommand("CMD_PRINT");
        item.addActionListener(this);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem();
        item.setText("Exit");
        item.setMnemonic('x');
        item.setActionCommand("CMD_EXIT");
        item.addActionListener(this);
        menu.add(item);
        return menu;
    }

    private JMenu buildEditMenu() {
        JMenu menu = null;
        JMenuItem item = null;
        menu = new JMenu();
        menu.setText("Edit");
        menu.setMnemonic('E');
        item = new JMenuItem();
        item.setText("Add New Book");
        item.setMnemonic('N');
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        item.setActionCommand("CMD_NEW");
        item.addActionListener(this);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Copy Cell");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        item.setMnemonic('C');
        item.setActionCommand("CMD_COPY");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem("Copy Row");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl shift C"));
        item.setMnemonic('o');
        item.setActionCommand("CMD_COPYROW");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem("Cut");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
        item.setMnemonic('t');
        item.setActionCommand("CMD_CUT");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem("Paste");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));
        item.setMnemonic('P');
        item.setActionCommand("CMD_PASTE");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem("Clear Cell");
        item.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        item.setMnemonic('E');
        item.setActionCommand("CMD_CLEAR");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem("Delete Book");
        item.setAccelerator(KeyStroke.getKeyStroke("shift DELETE"));
        item.setMnemonic('D');
        item.setActionCommand("CMD_DELETE");
        item.addActionListener(this);
        menu.add(item);
        return menu;
    }

    private JMenu buildHelpMenu() {
        JMenu menu = null;
        JMenuItem item = null;
        menu = new JMenu();
        menu.setText("Help");
        menu.setMnemonic('H');
        item = new JMenuItem();
        item.setText("About Bookman");
        item.setMnemonic('A');
        item.setActionCommand("CMD_HELP_ABOUT");
        item.addActionListener(this);
        menu.add(item);
        return menu;
    }

    private void buildNewBook() {
        newbook = new JFrame("Add New Book");
        newbook.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel l = null;
        ButtonGroup group = null;
        JRadioButton b = null;
        l = new JLabel("Name: ", JLabel.TRAILING);
        c.insets = new Insets(10, 5, 0, 0);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        newbook.add(l, c);
        textName = new JTextField(14);
        l.setLabelFor(textName);
        c.insets = new Insets(10, 0, 0, 5);
        c.weightx = 0.5;
        c.gridwidth = 2;
        c.gridx = 1;
        c.gridy = 0;
        newbook.add(textName, c);
        l = new JLabel("Author: ", JLabel.TRAILING);
        c.insets = new Insets(0, 5, 0, 0);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        newbook.add(l, c);
        textAuthor = new JComboBox(booktable.authors.toArray());
        textAuthor.setEditable(true);
        l.setLabelFor(textAuthor);
        c.insets = new Insets(0, 0, 0, 5);
        c.weightx = 0.5;
        c.gridwidth = 2;
        c.gridx = 1;
        c.gridy = 1;
        newbook.add(textAuthor, c);
        l = new JLabel("Volumes: ", JLabel.TRAILING);
        c.insets = new Insets(0, 5, 0, 0);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 2;
        newbook.add(l, c);
        textVolumes = new JTextField(14);
        l.setLabelFor(textVolumes);
        c.insets = new Insets(0, 0, 0, 5);
        c.weightx = 0.5;
        c.gridwidth = 2;
        c.gridx = 1;
        c.gridy = 2;
        newbook.add(textVolumes, c);
        l = new JLabel("Read: ", JLabel.TRAILING);
        c.insets = new Insets(0, 5, 0, 0);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;
        newbook.add(l, c);
        group = new ButtonGroup();
        buttonRead = new JRadioButton("Yes");
        group.add(buttonRead);
        l.setLabelFor(buttonRead);
        c.insets = new Insets(0, 10, 0, 10);
        c.weightx = 0.1;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 3;
        newbook.add(buttonRead, c);
        b = new JRadioButton("No", true);
        group.add(b);
        c.insets = new Insets(0, 5, 0, 5);
        c.weightx = 0.1;
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 3;
        newbook.add(b, c);
        l = new JLabel("End: ", JLabel.TRAILING);
        c.insets = new Insets(0, 5, 0, 0);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 4;
        newbook.add(l, c);
        group = new ButtonGroup();
        buttonEnd = new JRadioButton("Yes");
        group.add(buttonEnd);
        l.setLabelFor(buttonEnd);
        c.insets = new Insets(0, 10, 0, 10);
        c.weightx = 0.1;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 4;
        newbook.add(buttonEnd, c);
        b = new JRadioButton("No", true);
        group.add(b);
        c.insets = new Insets(0, 5, 0, 5);
        c.weightx = 0.1;
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 4;
        newbook.add(b, c);
        l = new JLabel("Burn: ", JLabel.TRAILING);
        c.insets = new Insets(0, 5, 0, 0);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 5;
        newbook.add(l, c);
        group = new ButtonGroup();
        buttonBurn = new JRadioButton("Yes");
        group.add(buttonBurn);
        l.setLabelFor(buttonBurn);
        c.insets = new Insets(0, 10, 0, 10);
        c.weightx = 0.1;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 5;
        newbook.add(buttonBurn, c);
        b = new JRadioButton("No", true);
        group.add(b);
        c.insets = new Insets(0, 5, 0, 5);
        c.weightx = 0.1;
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 5;
        newbook.add(b, c);
        l = new JLabel("Notes: ", JLabel.TRAILING);
        c.insets = new Insets(0, 5, 10, 0);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 6;
        newbook.add(l, c);
        textNotes = new JTextField(14);
        l.setLabelFor(textNotes);
        c.insets = new Insets(0, 0, 10, 5);
        c.weightx = 0.5;
        c.gridwidth = 2;
        c.gridx = 1;
        c.gridy = 6;
        newbook.add(textNotes, c);
        JButton jb = new JButton("Save");
        c.insets = new Insets(0, 10, 10, 10);
        c.weightx = 0.1;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 7;
        jb.setActionCommand("CMD_NEW_SAVE");
        jb.addActionListener(this);
        newbook.add(jb, c);
        jb = new JButton("Cancel");
        c.insets = new Insets(0, 5, 10, 5);
        c.weightx = 0.1;
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 7;
        jb.setActionCommand("CMD_NEW_CANCEL");
        jb.addActionListener(this);
        newbook.add(jb, c);
        newbook.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        newbook.setLocation((screen.width - newbook.getSize().width) / 2, (screen.height - newbook.getSize().height) / 2);
        newbook.setVisible(true);
    }

    private void buildPopupMenu() {
        JMenuItem item = null;
        popup = new JPopupMenu();
        Action copyCellAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRowCount() > 0) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString()), new StringSelection(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString()));
            }
        };
        item = new JMenuItem("Copy Cell");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        table.getInputMap().put(KeyStroke.getKeyStroke("ctrl C"), "copy");
        table.getActionMap().put("copy", copyCellAction);
        item.setMnemonic('C');
        item.setActionCommand("CMD_COPY");
        item.addActionListener(this);
        popup.add(item);
        Action copyRowAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRowCount() > 0) {
                    String s = "";
                    for (int r : table.getSelectedRows()) {
                        for (int i = 0; i < table.getColumnCount(); i++) {
                            s += table.getValueAt(r, i).toString();
                            if (i + 1 != table.getColumnCount()) s += "\t";
                        }
                        s += "\n";
                    }
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), new StringSelection(s));
                }
            }
        };
        item = new JMenuItem("Copy Row");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl shift C"));
        table.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift C"), "copyrow");
        table.getActionMap().put("copyrow", copyRowAction);
        item.setMnemonic('o');
        item.setActionCommand("CMD_COPYROW");
        item.addActionListener(this);
        popup.add(item);
        Action cutAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRowCount() > 0) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString()), new StringSelection(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString()));
                    table.setValueAt("", table.getSelectedRow(), table.getSelectedColumn());
                }
            }
        };
        item = new JMenuItem("Cut");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
        table.getInputMap().put(KeyStroke.getKeyStroke("ctrl X"), "cut");
        table.getActionMap().put("cut", cutAction);
        item.setMnemonic('t');
        item.setActionCommand("CMD_CUT");
        item.addActionListener(this);
        popup.add(item);
        Action pasteAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRowCount() > 0) {
                    try {
                        table.setValueAt((String) (Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor)), table.getSelectedRow(), table.getSelectedColumn());
                    } catch (Exception ex) {
                    }
                }
            }
        };
        item = new JMenuItem("Paste");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));
        table.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), "paste");
        table.getActionMap().put("paste", pasteAction);
        item.setMnemonic('P');
        item.setActionCommand("CMD_PASTE");
        item.addActionListener(this);
        popup.add(item);
        Action clearAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRowCount() > 0) table.setValueAt("", table.getSelectedRow(), table.getSelectedColumn());
            }
        };
        item = new JMenuItem("Clear Cell");
        item.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        table.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "clear");
        table.getActionMap().put("clear", clearAction);
        item.setMnemonic('E');
        item.setActionCommand("CMD_CLEAR");
        item.addActionListener(this);
        popup.add(item);
        Action deleteAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRowCount() > 0) {
                    int n = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this book, \"" + table.getValueAt(table.getSelectedRow(), 0) + "\"?", "Bookman", JOptionPane.YES_NO_OPTION);
                    if (n == JOptionPane.YES_OPTION) booktable.remove((String) table.getValueAt(table.getSelectedRow(), 0));
                }
            }
        };
        item = new JMenuItem("Delete Book");
        item.setAccelerator(KeyStroke.getKeyStroke("shift DELETE"));
        table.getInputMap().put(KeyStroke.getKeyStroke("shift DELETE"), "delete");
        table.getActionMap().put("delete", deleteAction);
        item.setMnemonic('D');
        item.setActionCommand("CMD_DELETE");
        item.addActionListener(this);
        popup.add(item);
    }

    protected void windowClosed() {
        if (booktable.isModified()) {
            int n = JOptionPane.showConfirmDialog(this, "The list \"" + file + "\" has been modified.\nDo you want to save your changes?", "Bookman", JOptionPane.YES_NO_CANCEL_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                try {
                    booktable.save(path + file);
                } catch (Exception e) {
                }
            }
            if (n == JOptionPane.YES_OPTION || n == JOptionPane.NO_OPTION) {
                setVisible(false);
                try {
                    booktable.save(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "bml-backup" + File.separator + file);
                    saveSetting();
                    saveLast();
                } catch (Exception e) {
                }
                System.exit(0);
            }
        } else {
            setVisible(false);
            try {
                booktable.save(System.getProperty("user.home") + File.separator + ".bookman" + File.separator + "bml-backup" + File.separator + file);
                saveSetting();
                saveLast();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        setVisible(true);
    }

    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("CMD_EXIT")) {
            windowClosed();
        } else if (cmd.equals("CMD_FILE_NEW")) {
            path = "";
            file = JOptionPane.showInputDialog(this, "Please enter the name of the new bookman list:", "Create a New Bookman List", JOptionPane.QUESTION_MESSAGE);
            if (!file.endsWith(".bml")) file += ".bml";
            booktable.newfile();
            title();
        } else if (cmd.equals("CMD_FILE_OPEN")) {
            JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            fc.addChoosableFileFilter(new BookFilter());
            try {
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile().getName();
                    path = fc.getSelectedFile().getParent() + File.separator;
                    booktable.open(path + file);
                    title();
                    loadSetting();
                }
            } catch (Exception e) {
            }
        } else if (cmd.equals("CMD_HELP_ABOUT")) {
            JOptionPane.showMessageDialog(this, "          Bookman " + version + "\n               " + date, "About Bookman", JOptionPane.INFORMATION_MESSAGE);
        } else if (cmd.equals("CMD_PRINT")) {
            try {
                table.print();
            } catch (Exception e) {
            }
        } else if (cmd.equals("CMD_FILE_SAVE")) {
            try {
                booktable.save(path + file);
                title();
                saveSetting();
            } catch (Exception e) {
            }
        } else if (cmd.equals("CMD_FILE_SAVE_AS")) {
            JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            fc.addChoosableFileFilter(new BookFilter());
            try {
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile().getName();
                    path = fc.getSelectedFile().getParent() + File.separator;
                    if (!file.endsWith(".bml")) file += ".bml";
                    booktable.save(path + file);
                    title();
                    saveSetting();
                }
            } catch (Exception e) {
            }
        } else if (cmd.equals("CMD_FILE_EXPORT_CSV")) {
            try {
                booktable.exportcsv(path + file.split("[.]")[0] + ".csv");
            } catch (Exception e) {
            }
        } else if (cmd.equals("CMD_NEW")) {
            buildNewBook();
        } else if (cmd.equals("CMD_NEW_SAVE")) {
            if (booktable.contains(textName.getText())) {
                JOptionPane.showMessageDialog(newbook, "This Book is already in your list, you cannot add it again.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (textName.getText().length() > 0) {
                    booktable.add(textName.getText(), textAuthor.getSelectedItem().toString(), textVolumes.getText(), buttonRead.isSelected(), buttonEnd.isSelected(), buttonBurn.isSelected(), textNotes.getText());
                    newbook.setVisible(false);
                }
            }
        } else if (cmd.equals("CMD_NEW_CANCEL")) {
            newbook.setVisible(false);
        } else if (cmd.equals("CMD_COPY")) {
            if (table.getSelectedRowCount() > 0) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString()), new StringSelection(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString()));
        } else if (cmd.equals("CMD_COPYROW")) {
            if (table.getSelectedRowCount() > 0) {
                String s = "";
                for (int r : table.getSelectedRows()) {
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        s += table.getValueAt(r, i).toString();
                        if (i + 1 != table.getColumnCount()) s += "\t";
                    }
                    s += "\n";
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), new StringSelection(s));
            }
        } else if (cmd.equals("CMD_CUT")) {
            if (table.getSelectedRowCount() > 0) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString()), new StringSelection(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString()));
                table.setValueAt("", table.getSelectedRow(), table.getSelectedColumn());
            }
        } else if (cmd.equals("CMD_PASTE")) {
            if (table.getSelectedRowCount() > 0) {
                try {
                    table.setValueAt((String) (Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor)), table.getSelectedRow(), table.getSelectedColumn());
                } catch (Exception e) {
                }
            }
        } else if (cmd.equals("CMD_CLEAR")) {
            if (table.getSelectedRowCount() > 0) table.setValueAt("", table.getSelectedRow(), table.getSelectedColumn());
        } else if (cmd.equals("CMD_DELETE")) {
            if (table.getSelectedRowCount() > 0) {
                int n = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book, \"" + table.getValueAt(table.getSelectedRow(), 0) + "\"?", "Bookman", JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) booktable.remove((String) table.getValueAt(table.getSelectedRow(), 0));
            }
        } else {
            System.out.println("Command invoked - " + cmd);
        }
    }

    public void keyPressed(KeyEvent e) {
        booktable.find(text.getText());
    }

    public void keyReleased(KeyEvent e) {
        booktable.find(text.getText());
    }

    public void keyTyped(KeyEvent e) {
        booktable.find(text.getText());
    }

    public void tableChanged(TableModelEvent e) {
        title();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            table.addRowSelectionInterval(table.rowAtPoint(e.getPoint()), table.rowAtPoint(e.getPoint()));
            table.setColumnSelectionInterval(table.columnAtPoint(e.getPoint()), table.columnAtPoint(e.getPoint()));
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            table.addRowSelectionInterval(table.rowAtPoint(e.getPoint()), table.rowAtPoint(e.getPoint()));
            table.setColumnSelectionInterval(table.columnAtPoint(e.getPoint()), table.columnAtPoint(e.getPoint()));
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public static void main(String[] args) throws Exception {
        BookGUI frame = new BookGUI();
        frame.pack();
        frame.setVisible(true);
    }

    class BookFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            if (f.getName().indexOf('.') < 0) return false;
            if (f.getName().substring((f.getName().lastIndexOf('.')) + 1).toLowerCase().equals("bml")) return true;
            return false;
        }

        public String getDescription() {
            return "Bookman List Files (.bml)";
        }
    }
}
