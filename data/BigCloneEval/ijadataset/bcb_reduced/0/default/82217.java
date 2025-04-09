import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class SimpleTestPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTable albumTable;

    private AlbumTableModel cdTableModel;

    public static void main(String arv[]) {
        JFrame applicationFrame = new JFrame();
        SimpleTestPanel testPanel = new SimpleTestPanel();
        applicationFrame.setContentPane(testPanel);
        applicationFrame.setSize(340, 340);
        applicationFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        applicationFrame.setTitle("Album Database");
        applicationFrame.setLocationRelativeTo(null);
        applicationFrame.setVisible(true);
    }

    /**
	 * @throws HeadlessException
	 */
    public SimpleTestPanel() {
        super(new GridBagLayout());
        JMenuBar menuBar = new JMenuBar();
        GridBagConstraints menuCons = new GridBagConstraints();
        menuCons.fill = GridBagConstraints.BOTH;
        menuCons.insets = new Insets(2, 0, 2, 0);
        menuCons.ipady = 18;
        add(menuBar, menuCons);
        JMenu fileMenu = makeFileMenu();
        menuBar.add(fileMenu);
        JMenu aboutMenu = makeAboutMenu();
        menuBar.add(aboutMenu);
        cdTableModel = new AlbumTableModel(new ArrayList<Album>());
        albumTable = new JTable(cdTableModel);
        GridBagConstraints tableCons = new GridBagConstraints();
        tableCons.fill = GridBagConstraints.BOTH;
        tableCons.weightx = 1.0;
        tableCons.weighty = 1.0;
        tableCons.gridx = 0;
        tableCons.gridy = 1;
        add(new JScrollPane(albumTable), tableCons);
    }

    /**
	 * Create the about menu - add the about menu item for the moment.
	 */
    private JMenu makeAboutMenu() {
        JMenu aboutMenu = new JMenu("About");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog();
                dialog.setTitle("About CD Table");
                dialog.add(new JLabel("Version 0.2 - Written by NAA", SwingConstants.CENTER), BorderLayout.CENTER);
                dialog.setLocationRelativeTo(SimpleTestPanel.this);
                dialog.setVisible(true);
                dialog.setSize(300, 100);
            }
        });
        aboutMenu.add(aboutItem);
        return aboutMenu;
    }

    /**
	 * Create a menu with the title "File" and populate it with some menu items - Save, Load, New Entry and Quit.
	 * @return
	 */
    private JMenu makeFileMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem addEntryItem = new JMenuItem("Add entry");
        addEntryItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addEntries();
            }
        });
        JMenuItem saveItem = new JMenuItem("Save table");
        saveItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveTable();
            }
        });
        JMenuItem loadItem = new JMenuItem("Load table");
        loadItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadTable();
            }
        });
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(addEntryItem);
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);
        return fileMenu;
    }

    /**
	 * Load the table data from a saved file.
	 */
    private void loadTable() {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File loadFile = null;
            BufferedReader in = null;
            try {
                loadFile = fileChooser.getSelectedFile();
                in = new BufferedReader(new FileReader(loadFile));
                ArrayList<Album> newAlbumList = new ArrayList<Album>();
                String nextLine = in.readLine();
                while (nextLine != null) {
                    Album album = Album.makeAlbum(nextLine);
                    if (album != null) {
                        newAlbumList.add(album);
                    }
                    nextLine = in.readLine();
                }
                cdTableModel.setTableData(newAlbumList);
                cdTableModel.fireTableDataChanged();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
	 * Save a table to a flat file. 
	 */
    private void saveTable() {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(saveFile)));
                for (Album album : cdTableModel.getTableData()) {
                    writer.println(album.toString());
                }
                writer.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    private void addEntries() {
        AddEntryDialog addEntryDialog = new AddEntryDialog();
        addEntryDialog.setLocationRelativeTo(this);
        addEntryDialog.setVisible(true);
        List<Album> newAlbumList = addEntryDialog.getNewAlbums();
        for (Album album : newAlbumList) {
            cdTableModel.getTableData().add(album);
        }
        cdTableModel.fireTableDataChanged();
    }
}
