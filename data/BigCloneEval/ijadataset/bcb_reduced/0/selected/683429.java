package fseditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.KeyEventDispatcher;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileSystemView;
import util.swing.FileTransferHandler;
import util.swing.ListenersAdapter;
import util.swing.SwingUtils;

/**
 * Note: When we enable drag for contentList, key events do no reach the key listeners 
 * of contentList. Having the container (SwingFsEditor) implementing KeyEventDispatcher 
 * (dispatchKeyEvent doing nothing and returning false), fixes this problem.
 * 
 * Note: Draging should not be enabled by a MouseListener but by a DragGestureRecognizer.
 * 
 * Note: TransferHandler only manages COPY actions.
 * 
 * @author Damien Dudouit
 */
public class SwingFsEditor extends JPanel implements KeyEventDispatcher {

    private static final long serialVersionUID = 1L;

    private JLabel pathLabel = new JLabel();

    private JList contentList = new JList();

    private File currentDir;

    private JMenu menuEdit;

    private Listeners listeners = new Listeners();

    public SwingFsEditor(File baseDir) {
        setLayout(new BorderLayout(0, 0));
        pathLabel.setTransferHandler(new TransferHandler("text"));
        new DragSource().createDefaultDragGestureRecognizer(pathLabel, DnDConstants.ACTION_COPY, listeners);
        contentList.setCellRenderer(new FsListRenderer());
        contentList.addKeyListener(listeners);
        contentList.addMouseListener(listeners);
        contentList.setDragEnabled(true);
        contentList.setTransferHandler(new FileTransferHandler());
        SwingUtils.addCutCopyPasteBindings(contentList);
        add(pathLabel, BorderLayout.NORTH);
        add(new JScrollPane(contentList), BorderLayout.CENTER);
        menuEdit = new JMenu("Edit");
        SwingUtils.addJMenuItem(menuEdit, "Parent", listeners);
        setCurrentDir(baseDir);
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File dir) {
        this.currentDir = dir;
        pathLabel.setText(dir.getPath());
        File[] content = dir.listFiles();
        contentList.setListData(content);
        if (content.length > 0) {
            contentList.setSelectedIndex(0);
        }
    }

    public boolean setParentAsBaseDir() {
        File parent = currentDir.getParentFile();
        if (parent != null) {
            setCurrentDir(parent);
            return true;
        }
        return false;
    }

    public boolean setSelectionAsBaseDir() {
        File file = getSelectedFile();
        if (file != null && file.isDirectory()) {
            setCurrentDir(file);
            return true;
        }
        return false;
    }

    public File getSelectedFile() {
        return (File) contentList.getSelectedValue();
    }

    public JMenu getMenu() {
        return menuEdit;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println(event);
        return false;
    }

    private class Listeners extends ListenersAdapter {

        public void actionPerformed(ActionEvent event) {
            String actionName = getSourceName(event);
            if ("Parent".equals(actionName)) {
                setParentAsBaseDir();
            } else if ("Child".equals(actionName)) {
                setSelectionAsBaseDir();
            }
        }

        public void keyReleased(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                setParentAsBaseDir();
            } else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                setSelectionAsBaseDir();
            } else if (event.getKeyChar() >= 'a' && event.getKeyChar() <= 'z') {
            }
        }

        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                File file = getSelectedFile();
                if (file.isDirectory()) {
                    setSelectionAsBaseDir();
                } else if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }

        public void dragGestureRecognized(DragGestureEvent event) {
            JComponent c = (JComponent) event.getComponent();
            TransferHandler handler = c.getTransferHandler();
            if (c == pathLabel) {
                handler.exportAsDrag(c, event.getTriggerEvent(), event.getDragAction());
            }
        }
    }
}

class FsListRenderer extends JLabel implements ListCellRenderer {

    private static FileSystemView fsView = FileSystemView.getFileSystemView();

    public FsListRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        File file = (File) value;
        setText(file.getName() + " (" + fsView.getSystemTypeDescription(file) + ")");
        Icon icon = fsView.getSystemIcon(file);
        setIcon(icon);
        if (isSelected) {
            setBackground(Color.lightGray);
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}
