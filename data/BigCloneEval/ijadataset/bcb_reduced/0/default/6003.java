import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

public class Gui_Export extends JFrame {

    private static final long serialVersionUID = 1L;

    private ResourceBundle toolTips = ResourceBundle.getBundle("translations.ToolTips");

    private JPanel mainPanel = new JPanel();

    private JPanel topPanel = new JPanel();

    private JPanel buttomPanel = new JPanel();

    private Object[] allHeader = { "Available Streams" };

    private String[][] allData = {};

    private Object[] tableheader = { "Streams to export" };

    private String[][] exportData = {};

    private DefaultTableModel allModel = new DefaultTableModel(allData, allHeader) {

        private static final long serialVersionUID = 1L;

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    };

    private DefaultTableModel exportModel = new DefaultTableModel(exportData, tableheader) {

        private static final long serialVersionUID = 1L;

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    };

    private JTable allTable = new JTable(allModel);

    private JTable exportTable = new JTable(exportModel);

    private JScrollPane allPane = new JScrollPane(allTable);

    private JScrollPane exportPane = new JScrollPane(exportTable);

    private ImageIcon abortIcon = new ImageIcon((URL) getClass().getResource("Icons/abort_small.png"));

    private ImageIcon browseIcon = new ImageIcon((URL) getClass().getResource("Icons/open_small.png"));

    private ImageIcon addIcon = new ImageIcon((URL) getClass().getResource("Icons/addmarked_small.png"));

    private ImageIcon addAllIcon = new ImageIcon((URL) getClass().getResource("Icons/addAll_small.png"));

    private ImageIcon removeIcon = new ImageIcon((URL) getClass().getResource("Icons/deletemarked_small.png"));

    private ImageIcon removeAllIcon = new ImageIcon((URL) getClass().getResource("Icons/deleteAll_small.png"));

    private ImageIcon exportIcon = new ImageIcon((URL) getClass().getResource("Icons/export_small.png"));

    private JButton addButton = new JButton(addIcon);

    private JButton addAllButton = new JButton(addAllIcon);

    private JButton removeButton = new JButton(removeIcon);

    private JButton removeAllButton = new JButton(removeAllIcon);

    private JButton browseButton = new JButton("Browse", browseIcon);

    private JButton abortButton = new JButton("Abort", abortIcon);

    private JButton exportButton = new JButton("Export", exportIcon);

    private JLabel destLabel = new JLabel("Destination :");

    private JTextField exportPathField = new JTextField("myPlayList.pls");

    private JFileChooser dirChooser = new JFileChooser();

    private Control_Stream controlStream = null;

    private ResourceBundle trans = null;

    public Gui_Export(Control_Stream controlStream, ResourceBundle trans) {
        super("Exportiere Stream");
        this.controlStream = controlStream;
        this.trans = trans;
        init();
        setLanguage();
        setToolTips();
        fillWithNames(allModel);
    }

    private void init() {
        mainPanel.setLayout(new BorderLayout());
        topPanel.setLayout(new GridBagLayout());
        buttomPanel.setLayout(new GridBagLayout());
        add(mainPanel);
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(buttomPanel, BorderLayout.SOUTH);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 0.5;
        c.weighty = 1.0;
        c.gridheight = 5;
        c.gridy = 0;
        c.gridx = 0;
        topPanel.add(allPane, c);
        c.weightx = 0.5;
        c.gridy = 0;
        c.gridx = 2;
        topPanel.add(exportPane, c);
        c.insets = new Insets(50, 5, 5, 5);
        c.weighty = 0.0;
        c.weightx = 0.0;
        c.gridheight = 1;
        c.gridy = 0;
        c.gridx = 1;
        topPanel.add(addButton, c);
        c.insets = new Insets(5, 5, 25, 5);
        c.gridy = 1;
        topPanel.add(addAllButton, c);
        c.insets = new Insets(5, 5, 5, 5);
        c.gridy = 2;
        topPanel.add(removeButton, c);
        c.gridy = 3;
        topPanel.add(removeAllButton, c);
        c.weighty = 1.0;
        c.gridy = 4;
        topPanel.add(new Label(""), c);
        c.insets = new Insets(5, 5, 5, 5);
        c.weighty = 0.0;
        c.weightx = 0.0;
        c.gridy = 0;
        c.gridx = 0;
        buttomPanel.add(destLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        buttomPanel.add(exportPathField, c);
        c.gridx = 2;
        c.weightx = 0.0;
        buttomPanel.add(browseButton, c);
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0.0;
        buttomPanel.add(exportButton, c);
        c.gridx = 1;
        c.weightx = 1.0;
        buttomPanel.add(new JLabel(""), c);
        c.gridx = 2;
        c.weightx = 0.0;
        buttomPanel.add(abortButton, c);
        addAllButton.addActionListener(new AddAllListener());
        removeAllButton.addActionListener(new RemoveAllListener());
        addButton.addActionListener(new AddSelectedListener());
        removeButton.addActionListener(new removeSelectedListener());
        abortButton.addActionListener(new AbortListener());
        browseButton.addActionListener(new BrowseListener());
        exportButton.addActionListener(new ExportListener());
        dirChooser.setFileFilter(new PlayListFilter());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension frameDim = new Dimension(600, 500);
        setSize(frameDim);
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
            setTitle(trans.getString("exportStream"));
            browseButton.setText(trans.getString("browse"));
            abortButton.setText(trans.getString("abortButton"));
            exportButton.setText(trans.getString("export"));
            destLabel.setText(trans.getString("destination"));
            exportPathField.setText(trans.getString("myPlayList"));
            allHeader[0] = trans.getString("exportAVHeader");
            tableheader[0] = trans.getString("exportHeader");
            allModel.setColumnIdentifiers(allHeader);
            exportModel.setColumnIdentifiers(tableheader);
        } catch (MissingResourceException e) {
            System.err.println(e);
        }
    }

    /**
	 * set Tooltips
	 */
    private void setToolTips() {
        addButton.setToolTipText(toolTips.getString("Export.addButton"));
        addAllButton.setToolTipText(toolTips.getString("Export.addAllButton"));
        removeButton.setToolTipText(toolTips.getString("Export.removeButton"));
        removeAllButton.setToolTipText(toolTips.getString("Export.removeAllButton"));
        browseButton.setToolTipText(toolTips.getString("Export.browseButton"));
        abortButton.setToolTipText(toolTips.getString("Export.abortButton"));
        exportButton.setToolTipText(toolTips.getString("Export.exportButton"));
    }

    private void fillWithNames(DefaultTableModel model) {
        Vector<Stream> streamVector = controlStream.getStreamVector();
        for (int i = 0; i < streamVector.capacity(); i++) model.addRow(((Stream) streamVector.get(i)).getNameAsObject());
    }

    private void removeAllNames(DefaultTableModel model) {
        int rowCount = model.getRowCount();
        for (int i = rowCount; i > 0; i--) model.removeRow(i - 1);
    }

    private void moveSelectedNames(DefaultTableModel removeModel, DefaultTableModel addModel, JTable removeTable) {
        for (int i = removeTable.getSelectedRowCount(); i > 0; i--) {
            Object[] a = new Object[1];
            a[0] = removeModel.getValueAt(removeTable.getSelectedRows()[i - 1], 0);
            addModel.addRow(a);
            removeModel.removeRow(removeTable.getSelectedRows()[i - 1]);
        }
    }

    private Gui_Export getMe() {
        return this;
    }

    class AddSelectedListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            moveSelectedNames(allModel, exportModel, allTable);
        }
    }

    class removeSelectedListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            moveSelectedNames(exportModel, allModel, exportTable);
        }
    }

    class AddAllListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (allModel.getRowCount() > 0) {
                removeAllNames(allModel);
                removeAllNames(exportModel);
                fillWithNames(exportModel);
            }
        }
    }

    class RemoveAllListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (exportModel.getRowCount() > 0) {
                removeAllNames(exportModel);
                removeAllNames(allModel);
                fillWithNames(allModel);
            }
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
                if (dirChooser.getSelectedFile().toString().toLowerCase().endsWith(".pls")) exportPathField.setText(dirChooser.getSelectedFile().toString()); else if (dirChooser.getSelectedFile().toString().toLowerCase().endsWith(".m3u")) exportPathField.setText(dirChooser.getSelectedFile().toString()); else exportPathField.setText(dirChooser.getSelectedFile().toString() + ".pls");
            }
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

    class ExportListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int streamCount = exportModel.getRowCount();
            if (streamCount > 0) {
                String path = exportPathField.getText();
                String[][] exportData = new String[streamCount][3];
                String writePlayList = "";
                for (int i = 0; i < streamCount; i++) {
                    Object name = exportModel.getValueAt(i, 0);
                    Stream stream = controlStream.getStreamInVectorByName(name.toString());
                    exportData[i][0] = name.toString();
                    exportData[i][1] = stream.getAddress();
                    exportData[i][2] = stream.getWebsite();
                }
                if (exportPathField.getText().toLowerCase().endsWith(".pls")) writePlayList = new Control_PlayList().getPLSData(exportData);
                if (exportPathField.getText().toLowerCase().endsWith(".m3u")) writePlayList = new Control_PlayList().getM3UData(exportData);
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(path));
                    bw.write(writePlayList);
                    bw.close();
                } catch (FileNotFoundException f) {
                    System.err.println(f);
                } catch (IOException g) {
                    System.err.println(g);
                }
                JOptionPane.showMessageDialog(getMe(), trans.getString("succExport"));
                dispose();
            } else JOptionPane.showMessageDialog(getMe(), trans.getString("lowExportCount"));
        }
    }
}
