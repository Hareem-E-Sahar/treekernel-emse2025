import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.awt.Dimension;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

public class Gui_Import extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel = new JPanel();

    private Object[] importHeader = { "import", "Stream Name", "Address", "Website" };

    private String[][] allData = {};

    private DefaultTableModel importModel = new DefaultTableModel(allData, importHeader) {

        private static final long serialVersionUID = 1L;

        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) return Boolean.class;
            return String.class;
        }
    };

    private JTable importTable = new JTable(importModel);

    private JScrollPane importPane = new JScrollPane(importTable);

    private ImageIcon abortIcon = new ImageIcon((URL) getClass().getResource("Icons/abort_small.png"));

    private ImageIcon browseIcon = new ImageIcon((URL) getClass().getResource("Icons/open_small.png"));

    private ImageIcon importIcon = new ImageIcon((URL) getClass().getResource("Icons/import_small.png"));

    private ImageIcon loadIcon = new ImageIcon((URL) getClass().getResource("Icons/load_small.png"));

    private JButton browseButton = new JButton(browseIcon);

    private JButton abortButton = new JButton("Abort", abortIcon);

    private JButton importButton = new JButton("Import", importIcon);

    private JButton loadButton = new JButton("Load", loadIcon);

    private JButton selectAllButton = new JButton("Select All");

    private JButton unselectAllButton = new JButton("Unselect All");

    private JLabel destLabel = new JLabel("Source :");

    private JTextField importPathField = new JTextField("myImport.pls");

    private JFileChooser dirChooser = new JFileChooser();

    private String[][] importableStreams = null;

    private Gui_Stripper mainGui = null;

    private ResourceBundle trans = null;

    public Gui_Import(Gui_Stripper gui) {
        super("importiere Stream");
        this.mainGui = gui;
        this.trans = mainGui.getTrans();
        setLanguage();
        init();
    }

    public Gui_Import(Gui_Stripper gui, String url) {
        this(gui);
        importPathField.setText(url);
        load();
    }

    private void init() {
        importTable.getColumn(importHeader[0]).setMaxWidth(50);
        mainPanel.setLayout(new GridBagLayout());
        add(mainPanel);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridy = 0;
        c.gridx = 0;
        mainPanel.add(destLabel, c);
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 1;
        c.weightx = 1.0;
        c.gridwidth = 3;
        mainPanel.add(importPathField, c);
        c.insets = new Insets(5, 0, 5, 5);
        c.gridx = 4;
        c.weightx = 0.0;
        c.gridwidth = 1;
        mainPanel.add(browseButton, c);
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 5;
        mainPanel.add(loadButton, c);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 6;
        mainPanel.add(importPane, c);
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.weighty = 0.0;
        c.weightx = 0.0;
        c.gridy = 2;
        c.gridx = 0;
        mainPanel.add(importButton, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.CENTER;
        mainPanel.add(selectAllButton, c);
        c.gridx = 2;
        mainPanel.add(unselectAllButton, c);
        c.gridx = 4;
        c.weightx = 0.0;
        c.gridwidth = 2;
        mainPanel.add(abortButton, c);
        browseButton.addActionListener(new BrowseListener());
        abortButton.addActionListener(new AbortListener());
        loadButton.addActionListener(new LoadListener());
        dirChooser.setFileFilter(new PlayListFilter());
        importButton.addActionListener(new ImportListener());
        selectAllButton.addActionListener(new SelectAllListener());
        unselectAllButton.addActionListener(new UnselectAllListener());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        Dimension frameDim = getSize();
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenDim.width - frameDim.width) / 2;
        int y = (screenDim.height - frameDim.height) / 2;
        setLocation(x, y);
        setVisible(true);
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        getRootPane().registerKeyboardAction(new AbortListener(), escStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void setLanguage() {
        try {
            setTitle(trans.getString("importStream"));
            abortButton.setText(trans.getString("abortButton"));
            importButton.setText(trans.getString("import"));
            loadButton.setText(trans.getString("load"));
            selectAllButton.setText(trans.getString("selectAll"));
            unselectAllButton.setText(trans.getString("unselectAll"));
            destLabel.setText(trans.getString("source"));
            importPathField.setText(trans.getString("myPlayList"));
            importHeader[0] = trans.getString("import");
            importHeader[1] = trans.getString("streamname");
            importHeader[2] = trans.getString("address");
            importHeader[3] = trans.getString("website");
            importModel.setColumnIdentifiers(importHeader);
        } catch (MissingResourceException e) {
            System.err.println(e);
        }
    }

    private void fillListTable() {
        if (importableStreams != null) {
            for (int i = 0; i < importableStreams.length; i++) {
                Object[] tmp = { new Boolean(false), importableStreams[i][1], importableStreams[i][0], importableStreams[i][2] };
                importModel.addRow(tmp);
            }
        }
    }

    private Gui_Import getMe() {
        return this;
    }

    private void removeAllFromTable() {
        int rowCount = importModel.getRowCount();
        for (int i = rowCount; i > 0; i--) importModel.removeRow(i - 1);
    }

    private void load() {
        String pathPlayList = importPathField.getText().toLowerCase().trim();
        if (pathPlayList.endsWith(".pls") || pathPlayList.endsWith(".m3u")) {
            removeAllFromTable();
            importableStreams = new Control_PlayList().anlyseFile(importPathField.getText());
            fillListTable();
            selectAll();
        }
    }

    public void selectAll() {
        for (int i = 0; i < importTable.getRowCount(); i++) {
            importTable.setValueAt(true, i, 0);
        }
    }

    public void unselectAll() {
        for (int i = 0; i < importTable.getRowCount(); i++) {
            importTable.setValueAt(false, i, 0);
        }
    }

    class SelectAllListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            selectAll();
        }
    }

    class UnselectAllListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            unselectAll();
        }
    }

    class LoadListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            load();
        }
    }

    class AbortListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    class BrowseListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dirChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int i = dirChooser.showOpenDialog(getMe());
            if (i == JFileChooser.APPROVE_OPTION) {
                importPathField.setText(dirChooser.getSelectedFile().toString());
            }
            load();
            selectAll();
        }
    }

    class PlayListFilter extends FileFilter {

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".pls") || f.getName().toLowerCase().endsWith(".m3u");
        }

        public String getDescription() {
            return ".pls .m3u";
        }
    }

    class ImportListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int importStreamCount = importModel.getRowCount();
            Vector<Integer> toImportLines = new Vector<Integer>(0, 1);
            Boolean errorAcc = false;
            Boolean ignoreError = false;
            Gui_AddStream addstream = new Gui_AddStream(mainGui, false);
            for (int i = 0; i < importStreamCount; i++) {
                if (errorAcc == false && (Boolean) importModel.getValueAt(i, 0) == true) {
                    String name = importModel.getValueAt(i, 1).toString();
                    if (addstream.testExitingName(name)) {
                        if (ignoreError != true) {
                            int j = JOptionPane.showConfirmDialog(getMe(), trans.getString("FollowNameExist") + name + trans.getString("ignoreAndImport"), trans.getString("existContinue"), JOptionPane.YES_NO_OPTION);
                            if (j == 0) {
                                System.out.println("ignore this stream: " + name);
                                ignoreError = true;
                            } else {
                                errorAcc = true;
                                break;
                            }
                        }
                    } else toImportLines.add(i);
                }
            }
            if (!errorAcc) {
                for (int i = 0; i < toImportLines.capacity(); i++) {
                    String address = importModel.getValueAt(toImportLines.get(i), 2).toString();
                    if (address == null || address.trim().equals("")) {
                        String name = importModel.getValueAt(toImportLines.get(i), 1).toString();
                        JOptionPane.showMessageDialog(getMe(), trans.getString("enterValidAdress") + name);
                        errorAcc = true;
                        break;
                    }
                }
                if (!errorAcc) {
                    for (int i = 0; i < toImportLines.capacity(); i++) {
                        String stream[] = new String[3];
                        stream[0] = importModel.getValueAt(toImportLines.get(i), 1).toString();
                        stream[1] = importModel.getValueAt(toImportLines.get(i), 2).toString();
                        stream[2] = importModel.getValueAt(toImportLines.get(i), 3).toString();
                        addstream.addNewStreamAll(stream);
                    }
                    JOptionPane.showMessageDialog(getMe(), toImportLines.capacity() + " " + trans.getString("addedStreams"));
                    dispose();
                }
            }
        }
    }
}
