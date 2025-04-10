package applet.favorites;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import libsidplay.Player;
import libsidutils.STIL.STILEntry;
import org.swixml.SwingEngine;
import sidplay.ini.IniConfig;
import applet.PathUtils;
import applet.SidTuneConverter;
import applet.collection.stil.STIL;
import applet.dnd.FileDrop;
import applet.events.IPlayTune;
import applet.events.UIEventFactory;
import applet.events.favorites.IFavoriteTabNames;
import applet.filefilter.TuneFileFilter;
import applet.sidtuneinfo.SidTuneInfoCache;
import applet.ui.JNiceButton;

@SuppressWarnings("serial")
public class PlayList extends JPanel implements IFavorites {

    protected UIEventFactory uiEvents = UIEventFactory.getInstance();

    private SwingEngine swix;

    protected JTable playListTable;

    protected JTextField filterField;

    protected JNiceButton unsort, moveUp, moveDown;

    protected transient RowSorter<TableModel> rowSorter;

    protected final transient FileFilter tuneFilter = new TuneFileFilter();

    protected final Favorites favoritesView;

    protected Player player;

    protected IniConfig config;

    protected int headerColumnToRemove;

    protected File lastDir;

    protected String playListFilename;

    protected Random randomPlayback = new Random();

    public PlayList(Player pl, IniConfig cfg, final Favorites favoritesView) {
        this.favoritesView = favoritesView;
        this.player = pl;
        this.config = cfg;
        try {
            swix = new SwingEngine(this);
            swix.getTaglib().registerTag("nicebutton", JNiceButton.class);
            swix.getTaglib().registerTag("playlisttable", FavoritesTable.class);
            swix.insert(PlayList.class.getResource("PlayList.xml"), this);
            FavoritesModel model = (FavoritesModel) playListTable.getModel();
            model.setConfig(cfg);
            model.setCollections(favoritesView.getHvsc(), favoritesView.getCgsc());
            ((FavoritesCellRenderer) playListTable.getDefaultRenderer(Object.class)).setConfig(cfg);
            ((FavoritesCellRenderer) playListTable.getDefaultRenderer(Object.class)).setPlayer(pl);
            model.fireTableStructureChanged();
            fillComboBoxes();
            setDefaultsAndActions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SwingEngine getSwix() {
        return swix;
    }

    private void setDefaultsAndActions() {
        JTableHeader header = playListTable.getTableHeader();
        header.addMouseListener(new MouseAdapter() {

            private final JPopupMenu headerPopup;

            private JMenuItem removeColumn;

            {
                headerPopup = new JPopupMenu(getSwix().getLocalizer().getString("CUSTOMIZE_COLUMN"));
                removeColumn = new JMenuItem(getSwix().getLocalizer().getString("REMOVE_COLUMN"));
                removeColumn.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        removeColumn();
                    }
                });
                headerPopup.add(removeColumn);
                for (final String element : SidTuneInfoCache.SIDTUNE_INFOS) {
                    JMenuItem menuItem = new JMenuItem(String.format(getSwix().getLocalizer().getString("ADD_COLUMN"), element));
                    menuItem.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                playListTable.getColumn(element);
                            } catch (IllegalArgumentException e1) {
                                FavoritesModel model = (FavoritesModel) playListTable.getModel();
                                model.addColumn(element);
                            }
                        }
                    });
                    headerPopup.add(menuItem);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JTableHeader popupHeader = (JTableHeader) e.getSource();
                    headerColumnToRemove = popupHeader.columnAtPoint(new Point(e.getPoint()));
                    int columnModelIndex = playListTable.convertColumnIndexToModel(headerColumnToRemove);
                    if (columnModelIndex == 0) {
                        removeColumn.setEnabled(false);
                    } else {
                        removeColumn.setEnabled(true);
                    }
                    headerPopup.show((Component) e.getSource(), e.getX(), e.getY());
                }
            }
        });
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        rowSorter = new TableRowSorter<TableModel>(model);
        rowSorter.addRowSorterListener(new RowSorterListener() {

            public void sorterChanged(RowSorterEvent e) {
                setMoveEnabledState(getSelection());
            }
        });
        playListTable.setRowSorter(rowSorter);
        playListTable.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getModifiers() == 0 && KeyEvent.VK_BACK_SPACE == e.getKeyCode() || KeyEvent.VK_DELETE == e.getKeyCode()) {
                    removeSelectedRows();
                } else if (e.getModifiers() == 0 && KeyEvent.VK_ENTER == e.getKeyCode()) {
                    playSelectedRow();
                }
            }
        });
        playListTable.addMouseListener(new MouseAdapter() {

            private JPopupMenu tablePopup;

            protected STILEntry getSTIL(final File file) {
                final String name = config.getHVSCName(file);
                if (null != name) {
                    libsidutils.STIL stil = libsidutils.STIL.getInstance(config.sidplay2().getHvsc());
                    if (stil != null) {
                        return stil.getSTIL(name);
                    }
                }
                return null;
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() == 2) {
                    playSelectedRow();
                } else if (mouseEvent.getButton() == MouseEvent.BUTTON3 && mouseEvent.getClickCount() == 1 && playListTable.getSelectionModel().getMinSelectionIndex() != -1) {
                    tablePopup = new JPopupMenu(getSwix().getLocalizer().getString("TUNE_ACTIONS"));
                    JMenuItem mi = new JMenuItem(getSwix().getLocalizer().getString("SHOW_STIL"));
                    mi.setEnabled(false);
                    tablePopup.add(mi);
                    int[] rows = playListTable.getSelectedRows();
                    if (rows.length == 1) {
                        int row = rowSorter.convertRowIndexToModel(rows[0]);
                        FavoritesModel model = (FavoritesModel) playListTable.getModel();
                        File tuneFile = model.getFile(model.getValueAt(row, 0));
                        final STILEntry se = getSTIL(tuneFile);
                        if (se != null) {
                            mi.setEnabled(true);
                            mi.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent arg0) {
                                    new STIL(se);
                                }
                            });
                        }
                    }
                    JMenuItem fileExportItem = new JMenuItem(getSwix().getLocalizer().getString("EXPORT_TO_DIR"));
                    fileExportItem.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser fc = new JFileChooser(lastDir);
                            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            final Frame containerFrame = JOptionPane.getFrameForComponent(PlayList.this);
                            int result = fc.showOpenDialog(containerFrame);
                            if (result == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
                                lastDir = fc.getSelectedFile();
                                int[] rows = playListTable.getSelectedRows();
                                for (int row1 : rows) {
                                    int row = rowSorter.convertRowIndexToModel(row1);
                                    FavoritesModel model = (FavoritesModel) playListTable.getModel();
                                    File file = model.getFile(model.getValueAt(row, 0));
                                    try {
                                        copy(file.getAbsolutePath(), fc.getSelectedFile().getAbsolutePath());
                                    } catch (IOException e1) {
                                        System.err.println(e1.getMessage());
                                    }
                                }
                            }
                        }
                    });
                    tablePopup.add(fileExportItem);
                    final JMenu moveToTab = new JMenu(getSwix().getLocalizer().getString("MOVE_TO_TAB"));
                    uiEvents.fireEvent(IFavoriteTabNames.class, new IFavoriteTabNames() {

                        public void setFavoriteTabNames(String[] names, String selected) {
                            for (String name1 : names) {
                                if (!name1.equals(selected)) {
                                    final String title = name1;
                                    JMenuItem tabItem = new JMenuItem(title);
                                    tabItem.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent e) {
                                            moveSelectedFavoritesToTab(title, false);
                                        }
                                    });
                                    moveToTab.add(tabItem);
                                }
                            }
                        }
                    });
                    tablePopup.add(moveToTab);
                    if (moveToTab.getMenuComponentCount() == 0) {
                        moveToTab.setEnabled(false);
                    }
                    final JMenu copyToTab = new JMenu(getSwix().getLocalizer().getString("COPY_TO_TAB"));
                    uiEvents.fireEvent(IFavoriteTabNames.class, new IFavoriteTabNames() {

                        public void setFavoriteTabNames(String[] names, String selected) {
                            for (String name1 : names) {
                                if (!name1.equals(selected)) {
                                    final String title = name1;
                                    JMenuItem tabItem = new JMenuItem(title);
                                    tabItem.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent e) {
                                            moveSelectedFavoritesToTab(title, true);
                                        }
                                    });
                                    copyToTab.add(tabItem);
                                }
                            }
                        }
                    });
                    tablePopup.add(copyToTab);
                    if (copyToTab.getMenuComponentCount() == 0) {
                        copyToTab.setEnabled(false);
                    }
                    JMenu convertItem = new JMenu(getSwix().getLocalizer().getString("CONVERT_TO"));
                    JMenuItem psid64 = new JMenuItem(getSwix().getLocalizer().getString("PSID64"));
                    psid64.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            JFileChooser fc = new JFileChooser(lastDir);
                            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            final Frame containerFrame = JOptionPane.getFrameForComponent(PlayList.this);
                            int result = fc.showOpenDialog(containerFrame);
                            if (result == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
                                lastDir = fc.getSelectedFile();
                                convertSelectedTunes();
                            }
                        }
                    });
                    convertItem.add(psid64);
                    tablePopup.add(convertItem);
                    tablePopup.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
                }
            }
        });
        playListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                String[] selection = getSelection();
                setMoveEnabledState(selection);
            }
        });
        new FileDrop(this, new FileDrop.Listener() {

            public void filesDropped(java.io.File[] files, Object source, Point point) {
                addToFavorites(files);
            }
        });
        filterField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (filterField.getText().trim().length() == 0) {
                    ((TableRowSorter<TableModel>) rowSorter).setRowFilter(null);
                } else {
                    if (validatePattern()) {
                        RowFilter<TableModel, Integer> filter = RowFilter.regexFilter(filterField.getText());
                        ((TableRowSorter<TableModel>) rowSorter).setRowFilter(filter);
                    }
                }
            }

            private boolean validatePattern() {
                boolean ok = true;
                filterField.setToolTipText(null);
                filterField.setBackground(Color.white);
                try {
                    Pattern.compile(filterField.getText());
                } catch (PatternSyntaxException e) {
                    filterField.setToolTipText(e.getMessage());
                    filterField.setBackground(Color.red);
                    ok = false;
                }
                return ok;
            }
        });
    }

    public JTable getPlayListTable() {
        return playListTable;
    }

    private void fillComboBoxes() {
    }

    public void addToFavorites(File[] files) {
        for (int i = 0; files != null && i < files.length; i++) {
            final File file = files[i];
            if (file.isDirectory()) {
                addToFavorites(file.listFiles());
            } else {
                if (tuneFilter.accept(file)) {
                    FavoritesModel model = (FavoritesModel) playListTable.getModel();
                    if (!model.contains(file, 0)) {
                        model.addRow(new Object[] { createRelativePath(file) });
                    }
                }
            }
        }
    }

    protected void playSelectedRow() {
        int selectedRow = playListTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        final int selectedModelRow = rowSorter.convertRowIndexToModel(selectedRow);
        if (selectedModelRow != -1) {
            uiEvents.fireEvent(IPlayTune.class, new IPlayTune() {

                @Override
                public boolean switchToVideoTab() {
                    return false;
                }

                @Override
                public File getFile() {
                    FavoritesModel model = (FavoritesModel) playListTable.getModel();
                    return model.getFile(model.getValueAt(selectedModelRow, 0));
                }

                @Override
                public Component getComponent() {
                    return favoritesView;
                }
            });
            favoritesView.setCurrentlyPlayedFavorites(this);
        }
    }

    public void removeSelectedRows() {
        int[] rows = playListTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        int response = JOptionPane.showConfirmDialog(this, String.format(swix.getLocalizer().getString("REMOVE_N_OF_MY_FAVORITES"), rows.length), swix.getLocalizer().getString("REMOVE_FAVORITES"), JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            for (int i = 0; i < rows.length; i++) {
                int row = rowSorter.convertRowIndexToModel(rows[i]);
                FavoritesModel model = (FavoritesModel) playListTable.getModel();
                model.removeRow(row);
                for (int j = i + 1; j < rows.length; j++) {
                    rows[j] = rows[j] - 1;
                }
            }
        }
    }

    public void loadFavorites(String filename) {
        try {
            FavoritesModel model = (FavoritesModel) playListTable.getModel();
            final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "ISO-8859-1"));
            String line;
            model.setRowCount(0);
            while ((line = r.readLine()) != null) {
                model.addRow(new Object[] { line });
            }
            r.close();
            this.playListFilename = filename;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFavorites(String filename) {
        try {
            final PrintStream p = new PrintStream(filename, "ISO-8859-1");
            FavoritesModel model = (FavoritesModel) playListTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                p.println(model.getValueAt(i, 0));
            }
            p.close();
            this.playListFilename = filename;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deselectFavorites() {
        playListTable.getSelectionModel().clearSelection();
        String[] selection = new String[0];
        setMoveEnabledState(selection);
    }

    protected void setMoveEnabledState(String[] selection) {
        List<? extends SortKey> keys = rowSorter.getSortKeys();
        int keySize = keys.size();
        if (keySize == 0 || keys.get(0).getSortOrder() == SortOrder.UNSORTED) {
            moveUp.setEnabled(selection.length == 1);
            moveDown.setEnabled(selection.length == 1);
            unsort.setEnabled(false);
        } else {
            moveUp.setEnabled(false);
            moveDown.setEnabled(false);
            unsort.setEnabled(true);
        }
    }

    public void selectFavorites() {
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        if (model.getRowCount() == 0) {
            return;
        }
        playListTable.getSelectionModel().setSelectionInterval(0, model.getRowCount() - 1);
    }

    public Action doUnsort = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            rowSorter.setSortKeys(new ArrayList<SortKey>());
        }
    };

    public Action doMoveUp = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            FavoritesModel model = (FavoritesModel) playListTable.getModel();
            int row = playListTable.getSelectedRow();
            int start = row;
            int to = row > 0 ? row - 1 : row;
            model.moveRow(start, start, to);
            playListTable.getSelectionModel().setSelectionInterval(to, to);
        }
    };

    public Action doMoveDown = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            FavoritesModel model = (FavoritesModel) playListTable.getModel();
            int row = playListTable.getSelectedRow();
            int start = row;
            int to = row < model.getRowCount() - 1 ? row + 1 : row;
            model.moveRow(start, start, to);
            playListTable.getSelectionModel().setSelectionInterval(to, to);
        }
    };

    protected String createRelativePath(File fileToConvert) {
        boolean converted = false;
        String result = fileToConvert.getAbsolutePath();
        String hvscName = config.getHVSCName(fileToConvert);
        if (hvscName != null) {
            result = FavoritesModel.HVSC_PREFIX + hvscName;
            converted = true;
        }
        String cgscName = config.getCGSCName(fileToConvert);
        if (!converted && cgscName != null) {
            result = FavoritesModel.CGSC_PREFIX + cgscName;
            converted = true;
        }
        if (!converted && fileToConvert.isAbsolute()) {
            String relativePath = PathUtils.getPath(fileToConvert);
            result = relativePath != null ? relativePath : result;
        }
        return result;
    }

    public void copy(String fromFileName, String toFileName) throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);
        if (!fromFile.exists()) {
            throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
        }
        if (!fromFile.isFile()) {
            throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
        }
        if (!fromFile.canRead()) {
            throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);
        }
        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }
        if (toFile.exists()) {
            if (!toFile.canWrite()) {
                throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
            }
            final Frame containerFrame = JOptionPane.getFrameForComponent(PlayList.this);
            int response = JOptionPane.showConfirmDialog(containerFrame, String.format(getSwix().getLocalizer().getString("OVERWRITE_EXISTING_FILE"), toFile.getName()), getSwix().getLocalizer().getString("OVERWRITE_FILE"), JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                throw new IOException(String.format("FileCopy: " + "existing file %s was not overwritten.", toFile.getName()));
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
            }
        }
        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead);
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void removeColumn() {
        int columnModelIndex = playListTable.convertColumnIndexToModel(headerColumnToRemove);
        if (columnModelIndex == 0) {
            return;
        }
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        model.removeColumn(columnModelIndex);
        model.fireTableStructureChanged();
    }

    protected void moveSelectedFavoritesToTab(final String title, final boolean copy) {
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        JTabbedPane pane = favoritesView.getTabbedPane();
        int index = pane.indexOfTab(title);
        IFavorites panel = (IFavorites) pane.getComponentAt(index);
        int[] rows = playListTable.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            int row = rowSorter.convertRowIndexToModel(rows[i]);
            panel.addToFavorites(new File[] { model.getFile(model.getValueAt(row, 0)) });
            if (!copy) {
                model.removeRow(row);
                for (int j = i + 1; j < rows.length; j++) {
                    rows[j] = rows[j] - 1;
                }
            }
        }
    }

    protected void convertSelectedTunes() {
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        final ArrayList<File> files = new ArrayList<File>();
        int[] rows = playListTable.getSelectedRows();
        for (int row1 : rows) {
            int row = rowSorter.convertRowIndexToModel(row1);
            files.add(model.getFile(model.getValueAt(row, 0)));
        }
        SidTuneConverter c = new SidTuneConverter(config);
        c.convertFiles(files.toArray(new File[0]), lastDir);
    }

    public String getFileName() {
        return playListFilename;
    }

    public File getNextFile(File file) {
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        int playedRow = -1;
        int rowCount = playListTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int row = rowSorter.convertRowIndexToModel(i);
            File currFile = model.getFile(model.getValueAt(row, 0));
            if (currFile.equals(file)) {
                playedRow = i;
                break;
            }
        }
        final int nextRow = playedRow + 1;
        if (nextRow == playListTable.getRowCount()) {
            return null;
        }
        int row = rowSorter.convertRowIndexToModel(nextRow);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                playListTable.scrollRectToVisible(playListTable.getCellRect(nextRow, 0, true));
            }
        });
        return model.getFile(model.getValueAt(row, 0));
    }

    public File getNextRandomFile(File file) {
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        int rowCount = playListTable.getRowCount();
        final int randomRow = Math.abs(randomPlayback.nextInt(Integer.MAX_VALUE)) % rowCount;
        int row = rowSorter.convertRowIndexToModel(randomRow);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                playListTable.scrollRectToVisible(playListTable.getCellRect(randomRow, 0, true));
            }
        });
        return model.getFile(model.getValueAt(row, 0));
    }

    public String[] getSelection() {
        int[] rows = playListTable.getSelectedRows();
        if (rows.length == 0) {
            return new String[0];
        }
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        ArrayList<String> filenames = new ArrayList<String>();
        for (int row1 : rows) {
            int row = rowSorter.convertRowIndexToModel(row1);
            filenames.add(model.getFile(model.getValueAt(row, 0)).getAbsolutePath());
        }
        String[] retValue = filenames.toArray(new String[filenames.size()]);
        return retValue;
    }

    public boolean isEmpty() {
        FavoritesModel model = (FavoritesModel) playListTable.getModel();
        return model.getRowCount() == 0;
    }
}
