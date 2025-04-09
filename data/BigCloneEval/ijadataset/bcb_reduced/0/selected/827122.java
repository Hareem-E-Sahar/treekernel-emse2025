package com.nbt;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jnbt.ByteArrayTag;
import org.jnbt.ByteArrayTag.ByteWrapper;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.DoubleTag;
import org.jnbt.FloatTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.LongTag;
import org.jnbt.Mutable;
import org.jnbt.NBTConstants;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTUtils;
import org.jnbt.ShortTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;
import com.nbt.data.SpriteRecord;
import com.nbt.world.NBTChunk;
import com.nbt.world.NBTFileBranch;
import com.nbt.world.NBTFileBranch.TagWrapper;
import com.nbt.world.NBTRegion;
import com.nbt.world.NBTWorld;
import com.tag.FramePreferences;
import com.tag.Hyperlink;
import com.tag.ImageFactory;
import com.tag.SwingWorkerUnlimited;
import com.tag.Thumbnail;
import com.terrain.Block;
import com.terrain.Region;
import com.terrain.Saveable;
import com.terrain.World;
import com.terrain.WorldBlock;
import com.terrain.WorldDirectory;

@SuppressWarnings("serial")
public class TreeFrame extends JFrame {

    public static final String TITLE = "NBT Kit";

    public static final String VERSION = "v1.0.0";

    public static final String KEY_FILE = "file";

    public static final String EXT_DAT = "dat";

    public static final String EXT_DAT_OLD = "dat_old";

    public static final String EXT_MCR = "mcr";

    private JPanel contentPane;

    private JTextField textFile;

    private JButton btnBrowse;

    private NBTTreeTable treeTable;

    private JScrollPane scrollPane;

    protected Action newAction;

    protected Action browseAction;

    protected Action saveAction;

    protected Action saveAsAction;

    protected Action refreshAction;

    protected Action exitAction;

    protected Action cutAction;

    protected Action copyAction;

    protected Action pasteAction;

    protected Action deleteAction;

    protected Action openAction;

    protected Action addByteAction;

    protected Action addShortAction;

    protected Action addIntAction;

    protected Action addLongAction;

    protected Action addFloatAction;

    protected Action addDoubleAction;

    protected Action addByteArrayAction;

    protected Action addStringAction;

    protected Action addListAction;

    protected Action addCompoundAction;

    protected Action helpAction;

    private TileCanvas tileCanvas;

    public TreeFrame() {
        createActions();
        initComponents();
        restoreFile();
        updateActions();
        FramePreferences prefs = new FramePreferences(this, getTitle());
        prefs.restoreAll();
        prefs.install();
    }

    private void createActions() {
        newAction = new NBTAction("New", "New", "New", KeyEvent.VK_N) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', Event.CTRL_MASK));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                updateTreeTable(new CompoundTag(""));
            }
        };
        browseAction = new NBTAction("Browse...", "Open", "Browse...", KeyEvent.VK_O) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = createFileChooser();
                switch(fc.showOpenDialog(TreeFrame.this)) {
                    case JFileChooser.APPROVE_OPTION:
                        File file = fc.getSelectedFile();
                        Preferences prefs = getPreferences();
                        prefs.put(KEY_FILE, file.getAbsolutePath());
                        doImport(file);
                        break;
                }
            }
        };
        saveAction = new NBTAction("Save", "Save", "Save", KeyEvent.VK_S) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', Event.CTRL_MASK));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                String path = textFile.getText();
                File file = new File(path);
                if (file.canWrite()) {
                    doExport(file);
                } else {
                    saveAsAction.actionPerformed(e);
                }
            }
        };
        saveAsAction = new NBTAction("Save As...", "SaveAs", "Save As...", KeyEvent.VK_UNDEFINED) {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = createFileChooser();
                switch(fc.showSaveDialog(TreeFrame.this)) {
                    case JFileChooser.APPROVE_OPTION:
                        File file = fc.getSelectedFile();
                        Preferences prefs = getPreferences();
                        prefs.put(KEY_FILE, file.getAbsolutePath());
                        doExport(file);
                        break;
                }
            }
        };
        refreshAction = new NBTAction("Refresh", "Refresh", "Refresh", KeyEvent.VK_F5) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F5"));
            }

            public void actionPerformed(ActionEvent e) {
                String path = textFile.getText();
                File file = new File(path);
                if (file.canRead()) doImport(file); else showErrorDialog("The file could not be read.");
            }
        };
        exitAction = new NBTAction("Exit", "Exit", KeyEvent.VK_ESCAPE) {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        cutAction = new DefaultEditorKit.CutAction() {

            {
                String name = "Cut";
                putValue(NAME, name);
                putValue(SHORT_DESCRIPTION, name);
                putValue(MNEMONIC_KEY, KeyEvent.VK_X);
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('X', Event.CTRL_MASK));
                ImageFactory factory = new ImageFactory();
                try {
                    putValue(SMALL_ICON, new ImageIcon(factory.readGeneralImage(name, NBTAction.smallIconSize)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    putValue(LARGE_ICON_KEY, new ImageIcon(factory.readGeneralImage(name, NBTAction.largeIconSize)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        copyAction = new DefaultEditorKit.CopyAction() {

            {
                String name = "Copy";
                putValue(NAME, name);
                putValue(SHORT_DESCRIPTION, name);
                putValue(MNEMONIC_KEY, KeyEvent.VK_C);
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', Event.CTRL_MASK));
                ImageFactory factory = new ImageFactory();
                try {
                    putValue(SMALL_ICON, new ImageIcon(factory.readGeneralImage(name, NBTAction.smallIconSize)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    putValue(LARGE_ICON_KEY, new ImageIcon(factory.readGeneralImage(name, NBTAction.largeIconSize)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        pasteAction = new DefaultEditorKit.CutAction() {

            {
                String name = "Paste";
                putValue(NAME, name);
                putValue(SHORT_DESCRIPTION, name);
                putValue(MNEMONIC_KEY, KeyEvent.VK_V);
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('V', Event.CTRL_MASK));
                ImageFactory factory = new ImageFactory();
                try {
                    putValue(SMALL_ICON, new ImageIcon(factory.readGeneralImage(name, NBTAction.smallIconSize)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    putValue(LARGE_ICON_KEY, new ImageIcon(factory.readGeneralImage(name, NBTAction.largeIconSize)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        deleteAction = new NBTAction("Delete", "Delete", "Delete", KeyEvent.VK_DELETE) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
            }

            public void actionPerformed(ActionEvent e) {
                int row = treeTable.getSelectedRow();
                TreePath path = treeTable.getPathForRow(row);
                Object last = path.getLastPathComponent();
                if (last instanceof NBTFileBranch) {
                    NBTFileBranch branch = (NBTFileBranch) last;
                    File file = branch.getFile();
                    String name = file.getName();
                    String message = "Are you sure you want to delete " + name + "?";
                    String title = "Continue?";
                    int option = JOptionPane.showConfirmDialog(TreeFrame.this, message, title, JOptionPane.OK_CANCEL_OPTION);
                    switch(option) {
                        case JOptionPane.CANCEL_OPTION:
                            return;
                    }
                    if (!FileUtils.deleteQuietly(file)) {
                        showErrorDialog(name + " could not be deleted.");
                        return;
                    }
                }
                TreePath parentPath = path.getParentPath();
                Object parentLast = parentPath.getLastPathComponent();
                NBTTreeTableModel model = treeTable.getTreeTableModel();
                int index = model.getIndexOfChild(parentLast, last);
                if (parentLast instanceof Mutable<?>) {
                    Mutable<?> mutable = (Mutable<?>) parentLast;
                    if (last instanceof ByteWrapper) {
                        ByteWrapper wrapper = (ByteWrapper) last;
                        index = wrapper.getIndex();
                    }
                    mutable.remove(index);
                } else {
                    System.err.println(last.getClass());
                    return;
                }
                updateTreeTable();
                treeTable.expandPath(parentPath);
                scrollTo(parentLast);
                treeTable.setRowSelectionInterval(row, row);
            }
        };
        openAction = new NBTAction("Open...", "Open...", KeyEvent.VK_T) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('T', Event.CTRL_MASK));
                final int diamondPickaxe = 278;
                SpriteRecord record = NBTTreeTable.register.getRecord(diamondPickaxe);
                BufferedImage image = record.getImage();
                setSmallIcon(image);
                int width = 24, height = 24;
                Dimension size = new Dimension(width, height);
                Map<RenderingHints.Key, ?> hints = Thumbnail.createRenderingHints(Thumbnail.QUALITY);
                BufferedImage largeImage = Thumbnail.createThumbnail(image, size, hints);
                setLargeIcon(largeImage);
            }

            public void actionPerformed(ActionEvent e) {
                TreePath path = treeTable.getPath();
                if (path == null) return;
                Object last = path.getLastPathComponent();
                if (last instanceof Region) {
                    Region region = (Region) last;
                    createAndShowTileCanvas(new TileCanvas.TileWorld(region));
                    return;
                } else if (last instanceof World) {
                    World world = (World) last;
                    createAndShowTileCanvas(world);
                    return;
                }
                if (last instanceof NBTFileBranch) {
                    NBTFileBranch fileBranch = (NBTFileBranch) last;
                    File file = fileBranch.getFile();
                    try {
                        open(file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        showErrorDialog(ex.getMessage());
                    }
                }
            }

            private void open(File file) throws IOException {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(file);
                    }
                }
            }
        };
        addByteAction = new NBTAction("Add Byte", NBTConstants.TYPE_BYTE, "Add Byte", KeyEvent.VK_1) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('1', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new ByteTag("new byte", (byte) 0));
            }
        };
        addShortAction = new NBTAction("Add Short", NBTConstants.TYPE_SHORT, "Add Short", KeyEvent.VK_2) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('2', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new ShortTag("new short", (short) 0));
            }
        };
        addIntAction = new NBTAction("Add Integer", NBTConstants.TYPE_INT, "Add Integer", KeyEvent.VK_3) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('3', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new IntTag("new int", 0));
            }
        };
        addLongAction = new NBTAction("Add Long", NBTConstants.TYPE_LONG, "Add Long", KeyEvent.VK_4) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('4', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new LongTag("new long", 0));
            }
        };
        addFloatAction = new NBTAction("Add Float", NBTConstants.TYPE_FLOAT, "Add Float", KeyEvent.VK_5) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('5', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new FloatTag("new float", 0));
            }
        };
        addDoubleAction = new NBTAction("Add Double", NBTConstants.TYPE_DOUBLE, "Add Double", KeyEvent.VK_6) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('6', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new DoubleTag("new double", 0));
            }
        };
        addByteArrayAction = new NBTAction("Add Byte Array", NBTConstants.TYPE_BYTE_ARRAY, "Add Byte Array", KeyEvent.VK_7) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('7', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new ByteArrayTag("new byte array"));
            }
        };
        addStringAction = new NBTAction("Add String", NBTConstants.TYPE_STRING, "Add String", KeyEvent.VK_8) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('8', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new StringTag("new string", "..."));
            }
        };
        addListAction = new NBTAction("Add List Tag", NBTConstants.TYPE_LIST, "Add List Tag", KeyEvent.VK_9) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('9', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                Class<? extends Tag> type = queryType();
                if (type != null) addTag(new ListTag("new list", null, type));
            }

            private Class<? extends Tag> queryType() {
                Object[] items = { NBTConstants.TYPE_BYTE, NBTConstants.TYPE_SHORT, NBTConstants.TYPE_INT, NBTConstants.TYPE_LONG, NBTConstants.TYPE_FLOAT, NBTConstants.TYPE_DOUBLE, NBTConstants.TYPE_BYTE_ARRAY, NBTConstants.TYPE_STRING, NBTConstants.TYPE_LIST, NBTConstants.TYPE_COMPOUND };
                JComboBox comboBox = new JComboBox(new DefaultComboBoxModel(items));
                comboBox.setRenderer(new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value instanceof Integer) {
                            Integer i = (Integer) value;
                            Class<? extends Tag> c = NBTUtils.getTypeClass(i);
                            String name = NBTUtils.getTypeName(c);
                            setText(name);
                        }
                        return this;
                    }
                });
                Object[] message = { new JLabel("Please select a type."), comboBox };
                String title = "Title goes here";
                int result = JOptionPane.showOptionDialog(TreeFrame.this, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                switch(result) {
                    case JOptionPane.OK_OPTION:
                        ComboBoxModel model = comboBox.getModel();
                        Object item = model.getSelectedItem();
                        if (item instanceof Integer) {
                            Integer i = (Integer) item;
                            return NBTUtils.getTypeClass(i);
                        }
                }
                return null;
            }
        };
        addCompoundAction = new NBTAction("Add Compound Tag", NBTConstants.TYPE_COMPOUND, "Add Compound Tag", KeyEvent.VK_0) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('0', Event.CTRL_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                addTag(new CompoundTag());
            }
        };
        String name = "About " + TITLE;
        helpAction = new NBTAction(name, "Help", name, KeyEvent.VK_F1) {

            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
            }

            public void actionPerformed(ActionEvent e) {
                Object[] message = { new JLabel(TITLE + " " + VERSION), new JLabel("© Copyright Taggart Spilman 2011.  All rights reserved."), new Hyperlink("<html><a href=\"#\">NamedBinaryTag.com</a></html>", "http://www.namedbinarytag.com"), new Hyperlink("<html><a href=\"#\">Contact</a></html>", "mailto:tagadvance@gmail.com"), new JLabel(" "), new Hyperlink("<html><a href=\"#\">JNBT was written by Graham Edgecombe</a></html>", "http://jnbt.sf.net"), new Hyperlink("<html><a href=\"#\">Available open-source under the BSD license</a></html>", "http://jnbt.sourceforge.net/LICENSE.TXT"), new JLabel(" "), new JLabel("This product includes software developed by"), new Hyperlink("<html><a href=\"#\">The Apache Software Foundation</a>.</html>", "http://www.apache.org"), new JLabel(" "), new JLabel("Default texture pack:"), new Hyperlink("<html><a href=\"#\">SOLID COLOUR. SOLID STYLE.</a></html>", "http://www.minecraftforum.net/topic/72253-solid-colour-solid-style/"), new JLabel("Bundled with the permission of Trigger_Proximity.") };
                String title = "About";
                JOptionPane.showMessageDialog(TreeFrame.this, message, title, JOptionPane.INFORMATION_MESSAGE);
            }
        };
    }

    protected void updateActions() {
        Map<Integer, Action> actionMap = new LinkedHashMap<Integer, Action>();
        actionMap.put(NBTConstants.TYPE_BYTE, addByteAction);
        actionMap.put(NBTConstants.TYPE_SHORT, addShortAction);
        actionMap.put(NBTConstants.TYPE_INT, addIntAction);
        actionMap.put(NBTConstants.TYPE_LONG, addLongAction);
        actionMap.put(NBTConstants.TYPE_FLOAT, addFloatAction);
        actionMap.put(NBTConstants.TYPE_DOUBLE, addDoubleAction);
        actionMap.put(NBTConstants.TYPE_BYTE_ARRAY, addByteArrayAction);
        actionMap.put(NBTConstants.TYPE_STRING, addStringAction);
        actionMap.put(NBTConstants.TYPE_LIST, addListAction);
        actionMap.put(NBTConstants.TYPE_COMPOUND, addCompoundAction);
        for (Action action : actionMap.values()) action.setEnabled(false);
        Action[] actions = { openAction };
        for (Action action : actions) action.setEnabled(false);
        if (treeTable == null) return;
        int row = treeTable.getSelectedRow();
        deleteAction.setEnabled(row > 0);
        if (row == -1) return;
        TreePath path = treeTable.getPathForRow(row);
        Object last = path.getLastPathComponent();
        TreePath parentPath = path.getParentPath();
        Object parentLast = parentPath == null ? null : parentPath.getLastPathComponent();
        if (last instanceof ByteArrayTag || parentLast instanceof ByteArrayTag || last instanceof ByteWrapper || parentLast instanceof ByteWrapper) {
            addByteAction.setEnabled(true);
        } else if (last instanceof ListTag || parentLast instanceof ListTag) {
            if (!(last instanceof ListTag)) last = parentLast;
            ListTag list = (ListTag) last;
            @SuppressWarnings("unchecked") Class<Tag<?>> c = (Class<Tag<?>>) list.getType();
            int type = NBTUtils.getTypeCode(c);
            Action action = actionMap.get(type);
            action.setEnabled(true);
        } else if (last instanceof CompoundTag || parentLast instanceof CompoundTag || last instanceof TagWrapper || parentLast instanceof TagWrapper) {
            for (Action action : actionMap.values()) action.setEnabled(true);
        } else if (last instanceof Region || last instanceof World || last instanceof NBTFileBranch) {
            openAction.setEnabled(true);
        }
    }

    private void initComponents() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        JToolBar toolBar = createToolBar();
        contentPane = new JPanel();
        setContentPane(contentPane);
        JPanel browsePanel = new JPanel();
        Border border = new TitledBorder(null, "Location");
        browsePanel.setBorder(border);
        textFile = new JTextField();
        textFile.setEditable(false);
        btnBrowse = new JButton("Browse");
        btnBrowse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                browseAction.actionPerformed(e);
            }
        });
        scrollPane = new JScrollPane();
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        int unitIncrement = 200;
        verticalScrollBar.setUnitIncrement(unitIncrement);
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addComponent(browsePanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)).addContainerGap()).addComponent(toolBar, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane.createSequentialGroup().addComponent(toolBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(browsePanel).addPreferredGap(ComponentPlacement.RELATED).addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addContainerGap()));
        GroupLayout gl_browsePanel = new GroupLayout(browsePanel);
        gl_browsePanel.setHorizontalGroup(gl_browsePanel.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, gl_browsePanel.createSequentialGroup().addContainerGap().addComponent(textFile, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED).addComponent(btnBrowse).addContainerGap()));
        gl_browsePanel.setVerticalGroup(gl_browsePanel.createParallelGroup(Alignment.LEADING).addGroup(gl_browsePanel.createSequentialGroup().addGroup(gl_browsePanel.createParallelGroup(Alignment.BASELINE).addComponent(textFile, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(btnBrowse)).addContainerGap()));
        browsePanel.setLayout(gl_browsePanel);
        contentPane.setLayout(gl_contentPane);
        int width = 440, height = 400;
        setMinimumSize(new Dimension(width, height));
        pack();
    }

    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        Action[] fileActions = { browseAction, saveAction, refreshAction, exitAction };
        for (Action action : fileActions) menuFile.add(new JMenuItem(action));
        menuBar.add(menuFile);
        JMenu menuEdit = new JMenu("Edit");
        Action[] editActions = { cutAction, copyAction, pasteAction, null, deleteAction, null, addByteAction, addShortAction, addIntAction, addLongAction, addFloatAction, addDoubleAction, addByteArrayAction, addStringAction, addListAction, addCompoundAction };
        for (Action action : editActions) {
            if (action == null) {
                menuEdit.addSeparator();
            } else {
                menuEdit.add(new JMenuItem(action));
            }
        }
        menuBar.add(menuEdit);
        JMenu menuView = new JMenu("View");
        menuView.add(new JMenuItem(openAction));
        menuBar.add(menuView);
        JMenu menuHelp = new JMenu("Help");
        menuHelp.add(new JMenuItem(helpAction));
        menuBar.add(menuHelp);
        return menuBar;
    }

    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        Action[] actions = { browseAction, saveAction, refreshAction, null, deleteAction, null, openAction, null, addByteAction, addShortAction, addIntAction, addLongAction, addFloatAction, addDoubleAction, addByteArrayAction, addStringAction, addListAction, addCompoundAction };
        for (Action action : actions) {
            if (action == null) {
                toolBar.addSeparator();
            } else {
                ToolBarButton button = new ToolBarButton(action);
                toolBar.add(button);
            }
        }
        return toolBar;
    }

    protected JFileChooser createFileChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setFileHidingEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        String description = "named binary tag";
        FileFilter filter = new FileNameExtensionFilter(description, "mcr", "dat", "dat_old");
        fc.setFileFilter(filter);
        Preferences prefs = getPreferences();
        String exportFile = prefs.get(KEY_FILE, null);
        if (exportFile == null) {
            File cwd = new File(".");
            fc.setCurrentDirectory(cwd);
        } else {
            File selectedFile = new File(exportFile);
            fc.setSelectedFile(selectedFile);
        }
        return fc;
    }

    private void restoreFile() {
        Preferences prefs = getPreferences();
        String pathname = prefs.get(KEY_FILE, null);
        if (pathname != null) {
            File file = new File(pathname);
            doImport(file);
        }
    }

    public void doImport(final File file) {
        if (file.isDirectory()) {
            doImportDirectory(file);
        } else if (file.isFile()) {
            doImportFile(file);
        }
    }

    public void doImportFile(File file) {
        String filename = file.getName();
        String extension = FilenameUtils.getExtension(filename);
        if (EXT_DAT.equals(extension) || EXT_DAT_OLD.equals(extension)) {
            doImportDat(file);
        } else if (EXT_MCR.equals(extension)) {
            doImportMCR(file);
        } else {
            showErrorDialog("Unknown file extension.");
        }
    }

    public void doImportDat(final File file) {
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        setCursor(waitCursor);
        SwingWorkerUnlimited.execure(new SwingWorker<CompoundTag, Void>() {

            @Override
            protected CompoundTag doInBackground() throws Exception {
                NBTInputStream ns = null;
                try {
                    ns = new NBTInputStream(new FileInputStream(file));
                    return (CompoundTag) ns.readTag();
                } finally {
                    IOUtils.closeQuietly(ns);
                }
            }

            @Override
            protected void done() {
                CompoundTag tag = null;
                try {
                    tag = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    showErrorDialog(cause.getMessage());
                    return;
                }
                textFile.setText(file.getAbsolutePath());
                updateTreeTable(tag);
                Cursor defaultCursor = Cursor.getDefaultCursor();
                setCursor(defaultCursor);
            }
        });
    }

    public void doImportMCR(final File file) {
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        setCursor(waitCursor);
        SwingWorkerUnlimited.execure(new SwingWorker<Region, Void>() {

            @Override
            protected Region doInBackground() throws Exception {
                NBTRegion region = new NBTRegion(file);
                region.getChunks();
                return region;
            }

            @Override
            protected void done() {
                Region region = null;
                try {
                    region = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    showErrorDialog(cause.getMessage());
                    return;
                }
                textFile.setText(file.getAbsolutePath());
                updateTreeTable(region);
                Cursor defaultCursor = Cursor.getDefaultCursor();
                setCursor(defaultCursor);
            }
        });
    }

    public void doImportDirectory(final File base) {
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        setCursor(waitCursor);
        SwingWorkerUnlimited.execure(new SwingWorker<NBTBranch, Void>() {

            @Override
            protected NBTBranch doInBackground() throws Exception {
                return createBranch(base);
            }

            private NBTBranch createBranch(File file) {
                String[] names = file.list();
                if (ArrayUtils.contains(names, WorldDirectory.DIRECTORY_REGION)) return new NBTWorld(file);
                return new NBTFileBranch(file);
            }

            @Override
            protected void done() {
                NBTBranch branch = null;
                try {
                    branch = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    showErrorDialog(cause.getMessage());
                    return;
                }
                textFile.setText(base.getAbsolutePath());
                updateTreeTable(branch);
                Cursor defaultCursor = Cursor.getDefaultCursor();
                setCursor(defaultCursor);
            }
        });
    }

    private void createAndShowTileCanvas(World world) {
        String title = world.getName();
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.tileCanvas = new TileCanvas(world) {

            @Override
            protected void blockClicked(final Block block) {
                Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                setCursor(waitCursor);
                SwingWorkerUnlimited.execure(new SwingWorker<TreePath, Void>() {

                    @Override
                    protected TreePath doInBackground() throws Exception {
                        WorldBlock b = (WorldBlock) block;
                        NBTChunk chunk = (NBTChunk) b.getChunk();
                        NBTRegion region = (NBTRegion) chunk.getRegion();
                        Tag<?> chunkTag = chunk.getTag();
                        CompoundTag compoundTag = (CompoundTag) chunkTag;
                        Tag<?> level = compoundTag.search("Level");
                        CompoundTag levelTag = (CompoundTag) level;
                        Tag<?> blocks = levelTag.search("Blocks");
                        ByteArrayTag blocksTag = (ByteArrayTag) blocks;
                        int index = b.getIndex();
                        Object child = blocksTag.getChild(index);
                        return treeTable.getPathForNode(region).pathByAddingChild(chunk).pathByAddingChild(level).pathByAddingChild(blocks).pathByAddingChild(child);
                    }

                    @Override
                    protected void done() {
                        Cursor defaultCursor = Cursor.getDefaultCursor();
                        setCursor(defaultCursor);
                        try {
                            TreePath path = get();
                            selectAndScroll(path);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Throwable cause = ExceptionUtils.getRootCause(e);
                            showErrorDialog(cause.getMessage());
                            return;
                        }
                    }
                });
            }
        };
        tileCanvas.restore();
        frame.add(tileCanvas);
        frame.pack();
        frame.setVisible(true);
    }

    private void selectAndScroll(TreePath path) {
        if (path == null) {
            System.err.println("no path found");
            return;
        }
        TreeSelectionModel model = treeTable.getTreeSelectionModel();
        model.setSelectionPath(path);
        treeTable.expandPath(path);
        treeTable.scrollPathToVisible(path);
    }

    public void doExport(final File file) {
        textFile.setText(file.getAbsolutePath());
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        setCursor(waitCursor);
        NBTTreeTableModel model = treeTable.getTreeTableModel();
        final Object root = model.getRoot();
        SwingWorkerUnlimited.execure(new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                if (root instanceof Saveable) {
                    Saveable saveable = (Saveable) root;
                    if (saveable.hasChanged()) saveable.save();
                }
                return null;
            }

            @Override
            protected void done() {
                Cursor defaultCursor = Cursor.getDefaultCursor();
                setCursor(defaultCursor);
                try {
                    get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    showErrorDialog(cause.getMessage());
                    return;
                }
            }
        });
    }

    public void addTag(Tag<?> tag) {
        if (treeTable == null) {
            showErrorDialog("Tree is null");
            return;
        }
        TreePath path = treeTable.getPath();
        add(tag, path);
    }

    private void add(final Tag<?> tag, TreePath path) {
        Object last = path.getLastPathComponent();
        if (!(last instanceof Mutable)) {
            TreePath parentPath = path.getParentPath();
            add(tag, parentPath);
            return;
        }
        Object scroll = tag;
        if (last instanceof Mutable) {
            Mutable mutable = (Mutable) last;
            if (last instanceof ByteArrayTag) {
                mutable.add(null);
            } else if (last instanceof ByteWrapper) {
                ByteWrapper wrapper = (ByteWrapper) last;
                int index = wrapper.getIndex() + 1;
                mutable.add(index, null);
                scroll = index;
            } else if (last instanceof ListTag) {
                mutable.add(tag);
            } else if (last instanceof CompoundTag || last instanceof TagWrapper) {
                mutable.add(tag);
            }
            nodesInserted(last, path);
        }
        scrollTo(scroll);
    }

    private void nodesInserted(Object source, TreePath path) {
        updateTreeTable();
        treeTable.expandPath(path);
    }

    protected void updateTreeTable() {
        NBTTreeTableModel model = treeTable.getTreeTableModel();
        Object root = model.getRoot();
        updateTreeTable(root);
    }

    protected void updateTreeTable(Object root) {
        treeTable = new NBTTreeTable(root) {

            @Override
            public void treeExpanded(Object source, TreePath path) {
                super.treeExpanded(source, path);
                revalidate();
                repaint();
            }
        };
        treeTable.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                updateActions();
                if (tileCanvas != null) tileCanvas.doRepaint();
            }
        });
        scrollPane.setViewportView(treeTable);
        updateActions();
    }

    protected void scrollTo(Object node) {
        TreePath path = treeTable.getPathForNode(node);
        int row = treeTable.getRowForPath(path);
        if (row != -1) {
            treeTable.setRowSelectionInterval(row, row);
            treeTable.scrollPathToVisible(path);
        }
    }

    public void showErrorDialog(String message) {
        String title = "Error";
        JOptionPane.showMessageDialog(TreeFrame.this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public Preferences getPreferences() {
        return Preferences.userNodeForPackage(getClass());
    }
}
