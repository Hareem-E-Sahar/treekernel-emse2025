import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class ClassBrowser extends JFrame implements WindowListener, ActionListener {

    public static void main(String[] args) {
        ClassBrowser _ClassBrowser = new ClassBrowser();
        _ClassBrowser.pack();
        _ClassBrowser.show();
    }

    JMenuBar menuBar = new JMenuBar();

    JMenu menuFile = new JMenu("File");

    JMenuItem menuItemOpen = new JMenuItem("Open");

    JSeparator menuSeparator = new JSeparator();

    JMenuItem menuItemExit = new JMenuItem("Exit");

    JMenu menuHelp = new JMenu("Help");

    JMenuItem menuItemAbout = new JMenuItem("About");

    JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel border = new JPanel(new BorderLayout(5, 5));

    public ClassBrowser() {
        super("Class Browser");
        this.setResizable(true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setBackground(SystemColor.control);
        this.setForeground(SystemColor.controlText);
        this.addWindowListener(this);
        menuItemOpen.addActionListener(this);
        menuItemExit.addActionListener(this);
        menuItemAbout.addActionListener(this);
        menuFile.add(menuItemOpen);
        menuFile.add(menuSeparator);
        menuFile.add(menuItemExit);
        menuBar.add(menuFile);
        menuHelp.add(menuItemAbout);
        menuBar.add(menuHelp);
        ClassFileTableModel cft = new ClassFileTableModel();
        TableSorter sorter = new TableSorter(cft);
        JTable table = new JTable(sorter);
        scrollPane.setViewportView(table);
        border.add("North", menuBar);
        border.add("Center", scrollPane);
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(200);
        tcm.getColumn(0).setMinWidth(150);
        tcm.getColumn(1).setPreferredWidth(75);
        tcm.getColumn(1).setMinWidth(75);
        tcm.getColumn(2).setPreferredWidth(60);
        tcm.getColumn(2).setMinWidth(60);
        tcm.getColumn(3).setPreferredWidth(60);
        tcm.getColumn(3).setMinWidth(60);
        tcm.getColumn(4).setPreferredWidth(200);
        tcm.getColumn(4).setMinWidth(150);
        tcm.getColumn(5).setPreferredWidth(200);
        tcm.getColumn(5).setMinWidth(100);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        getContentPane().add("Center", border);
    }

    /**
	 * Updates the JTable with the new archive information.
	 *
	 * @param   files an array of files, single file or directory
	 */
    public void updateTable(File[] files) {
        ReadJar rj = new ReadJar();
        Object[][] classFileList = rj.getClassFiles(files);
        ClassFileTableModel cft = new ClassFileTableModel();
        cft.data = classFileList;
        TableSorter sorter = new TableSorter(cft);
        JTable table = new JTable(sorter);
        scrollPane.setViewportView(table);
        sorter.addMouseListenerToHeaderInTable(table);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(200);
        tcm.getColumn(0).setMinWidth(150);
        tcm.getColumn(1).setPreferredWidth(75);
        tcm.getColumn(1).setMinWidth(75);
        tcm.getColumn(2).setPreferredWidth(60);
        tcm.getColumn(2).setMinWidth(60);
        tcm.getColumn(3).setPreferredWidth(60);
        tcm.getColumn(3).setMinWidth(60);
        tcm.getColumn(4).setPreferredWidth(200);
        tcm.getColumn(4).setMinWidth(150);
        tcm.getColumn(5).setPreferredWidth(200);
        tcm.getColumn(5).setMinWidth(100);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        getContentPane().add("Center", border);
    }

    public void windowActivated(WindowEvent evt) {
    }

    public void windowClosed(WindowEvent evt) {
    }

    public void windowClosing(WindowEvent evt) {
        if (evt.getSource() == this) {
            System.exit(0);
        }
    }

    public void windowDeactivated(WindowEvent evt) {
    }

    public void windowDeiconified(WindowEvent evt) {
    }

    public void windowIconified(WindowEvent evt) {
    }

    public void windowOpened(WindowEvent evt) {
    }

    /**
	 * Handles the events from the menu.
	 *
	 * @param   evt the event that cause this method to be called 
	 */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == menuItemOpen) {
            File[] files = doFilesOpen();
            if (files != null) {
                updateTable(files);
            }
        }
        if (evt.getSource() == menuItemExit) {
            System.exit(0);
        }
        if (evt.getSource() == menuItemAbout) {
        }
    }

    /**
	 * Open a fileOpen dialog so the user can select an archive file.
	 *
	 * @return the selected archive file
	 */
    public File doFileOpen() {
        FileDialog fd = new FileDialog(_getFrame(this), "Select Archive or Directory", FileDialog.LOAD);
        fd.show();
        if (fd.getFile() == null) return null;
        File theFile = new File(fd.getDirectory(), fd.getFile());
        System.out.println("theFile: " + theFile);
        return theFile;
    }

    /**
	 * Creates an open file dialog which allows for the selection of multiple files 
	 * or a directory of files.
	 *
	 * @return an array of files a single file or directory
	 */
    public File[] doFilesOpen() {
        JFileChooser chooser = new JFileChooser();
        ArchiveFilter filter = new ArchiveFilter();
        filter.addExtension("jar");
        filter.addExtension("zip");
        filter.setDescription("Jar & Zip Files");
        File root = new File("/fuegotech3.1.3");
        chooser.setCurrentDirectory(root);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(_getFrame(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFiles();
        } else {
            return null;
        }
    }

    public static Frame _getFrame(Component comp) {
        Component c = comp;
        try {
            while (!(c instanceof Frame)) c = c.getParent();
            return (Frame) c;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
