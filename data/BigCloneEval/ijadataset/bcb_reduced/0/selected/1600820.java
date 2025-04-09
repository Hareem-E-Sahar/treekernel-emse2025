package becta.viewer.framework;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import becta.viewer.accessibility.AccessibilityColor;
import becta.viewer.accessibility.Theme;
import becta.viewer.accessibility.ThemeChangeListener;
import becta.viewer.accessibility.ThemeManager;
import becta.viewer.controls.Toolbar;
import becta.viewer.selection.SelectionMode;
import becta.viewer.util.Platform;

/**
 * This is the main class for viewer
 * The framework for the viewer is implemented in this class
 */
public class Viewer implements ThemeChangeListener {

    /**
	 * instance of viewer
	 */
    private static Viewer viewer;

    /**
	 * the desktop pane
	 */
    private JPanel desktop;

    /**
	 * holds the list of frames
	 */
    private ArrayList<becta.viewer.framework.Frame> frames = new ArrayList<becta.viewer.framework.Frame>();

    /**
	 * holds the frame that is currently viewed
	 */
    private becta.viewer.framework.Frame activeFrame;

    /**
	 * menu bar for the viewer
	 */
    private Menubar menubar;

    /**
	 * tool bar for the viewer
	 */
    private Toolbar toolbar;

    /**
	 * instance of resource bundle
	 */
    private ResourceBundle resourceBundle;

    /**
	 * instance of parent container
	 */
    private JFrame parentContainer;

    /**
	 * Listener list
	 */
    private EventListenerList listenerList = new EventListenerList();

    /**
	 * The constructor of the class
	 */
    public Viewer() {
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        resourceBundle = ResourceBundle.getBundle("becta.viewer.framework.resources");
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ThemeManager.addThemeChangeListener(this);
        JFrame mainFrame = new JFrame();
        Insets screenInsets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(mainFrame.getGraphicsConfiguration());
        Rectangle screenSize = mainFrame.getGraphicsConfiguration().getBounds();
        final Rectangle maxBounds = new Rectangle(screenInsets.left + screenSize.x, screenInsets.top + screenSize.y, screenSize.x + screenSize.width - screenInsets.right - screenInsets.left, screenSize.y + screenSize.height - screenInsets.bottom - screenInsets.top);
        mainFrame.setMaximizedBounds(maxBounds);
        mainFrame.setUndecorated(true);
        mainFrame.setTitle(resourceBundle.getString("mainFrame.Title"));
        try {
            BufferedImage icon = ImageIO.read(Viewer.class.getResource("resources/application_icon.png"));
            mainFrame.setIconImage(icon);
        } catch (Exception e) {
            Viewer.logException(e);
        }
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                try {
                    for (becta.viewer.framework.Frame frame : getFrames()) {
                        frame.close();
                    }
                    exit();
                } catch (Exception e) {
                    Viewer.logException(e);
                }
            }

            @Override
            public void windowActivated(WindowEvent e) {
                if (Platform.isLinux && parentContainer.getHeight() < (maxBounds.getHeight() / 2)) {
                    parentContainer.setSize(maxBounds.getSize());
                }
            }
        });
        parentContainer = mainFrame;
        menubar = new Menubar();
        toolbar = new Toolbar();
        Canvas.addSelectionModeListener(toolbar);
        desktop = new JPanel();
        desktop.setLayout(new BorderLayout());
        desktop.setBackground(AccessibilityColor.window);
        parentContainer.setJMenuBar(menubar);
        JPanel dummy = new JPanel();
        dummy.setPreferredSize(new Dimension(0, 0));
        parentContainer.add(dummy, BorderLayout.NORTH);
        parentContainer.add(desktop, BorderLayout.CENTER);
        parentContainer.add(toolbar, BorderLayout.SOUTH);
        mainFrame.pack();
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.addFileChangeListener(menubar);
        this.addFileChangeListener(toolbar);
        addStateChangeListener(menubar);
        addStateChangeListener(toolbar);
    }

    /**
     * Adds an <code>FileChangeListener</code> to the button.
     * @param l the <code>FileChangeListener</code> to be added
     */
    public void addFileChangeListener(FileChangeListener l) {
        listenerList.add(FileChangeListener.class, l);
    }

    /**
     * Adds an <code>StateChangeListener</code> to the button.
     * @param l the <code>StateChangeListener</code> to be added
     */
    public void addStateChangeListener(StateChangeListener l) {
        listenerList.add(StateChangeListener.class, l);
    }

    /**
	 * Gets the desktop of this viewer
	 * @return the component into which all internal frames will be placed
	 */
    public final JPanel getDesktop() {
        return desktop;
    }

    /**
	 * Gets the toolbar
	 * @return the toolbar of this viewer
	 */
    public final Toolbar getToolbar() {
        return toolbar;
    }

    /**
	 * Gets the menubar
	 * @return the menubar of this viewer
	 */
    public final Menubar getMenubar() {
        return menubar;
    }

    /**
	 * Method to get parent container
	 * @return parentContainer
	 */
    public final Frame getContainer() {
        return parentContainer;
    }

    /**
	 * Get the list of frames that are opened
	 * @return ArrayList of Frame
	 */
    public final List<becta.viewer.framework.Frame> getFrames() {
        return frames;
    }

    /**
	 * Gets the currently viewed frame
	 * @return activeFrame
	 */
    public final becta.viewer.framework.Frame getActiveFrame() {
        return activeFrame;
    }

    /**
	 * Sets active theme
	 * Used to switch between opened files
	 * @param frame
	 */
    public void setActiveFrame(becta.viewer.framework.Frame frame) {
        if (frame != activeFrame) {
            if (activeFrame != null) activeFrame.getCanvas().getDocument().getCurrentPage().dispose(false);
            desktop.removeAll();
            desktop.repaint();
            activeFrame = frame;
            desktop.add(activeFrame, BorderLayout.CENTER);
            desktop.validate();
            fireFileChange(activeFrame);
            frame.load(false);
            frame.loadComplete();
        }
    }

    /**
	 * Gets the instance of viewer
	 * @return instance of Viewer
	 */
    public static Viewer getViewer() {
        return viewer;
    }

    /**
	 * Log details of exception
	 * @param ex Exception
	 */
    public static void logException(Exception ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        ex.printStackTrace(printWriter);
        printWriter.flush();
        stringWriter.flush();
        logException(stringWriter.toString());
    }

    /**
	 * Log an error message
	 * @param msg Message string
	 */
    public static void logException(String msg) {
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String currentTime = df.format(now);
        try {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + File.separator + "log.txt", true);
            fileWriter.write(currentTime + " " + msg + System.getProperty("line.separator"));
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
        }
    }

    /**
	 * Shows confirmation message dialog
	 * @param owner
	 * @param title
	 * @param message
	 * @param type
	 * @return
	 */
    public static int showConfirmationDialog(Frame owner, String title, String message, int optionType) {
        if (title == null) title = Viewer.getViewer().resourceBundle.getString("mainFrame.Title");
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, optionType, null, null, null);
        pane.getAccessibleContext().setAccessibleDescription(message);
        pane.selectInitialValue();
        JDialog dlg = pane.createDialog(owner, title);
        dlg.setVisible(true);
        dlg.dispose();
        Object selectedValue = pane.getValue();
        if (selectedValue == null) return JOptionPane.CLOSED_OPTION;
        if (selectedValue instanceof Integer) return ((Integer) selectedValue).intValue();
        return JOptionPane.CLOSED_OPTION;
    }

    public static void showMessageDialog(Component owner, String title, String message, int messageType) {
        if (title == null) title = Viewer.getViewer().resourceBundle.getString("mainFrame.Title");
        JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.DEFAULT_OPTION, null, null, null);
        pane.getAccessibleContext().setAccessibleDescription(message);
        pane.selectInitialValue();
        JDialog dlg = pane.createDialog(owner, title);
        dlg.setVisible(true);
        dlg.dispose();
    }

    /**
	 * Main class
	 * @param args
	 */
    public static void main(String[] args) {
        try {
            ThemeManager.init();
            if (!Platform.isOSX || System.getProperty("apple.laf.useScreenMenuBar").equals(Boolean.FALSE)) {
                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            InputMap inputMap = (InputMap) UIManager.get("Button.focusInputMap");
            inputMap.put(KeyStroke.getKeyStroke("ENTER"), "pressed");
            inputMap.put(KeyStroke.getKeyStroke("released ENTER"), "released");
            viewer = new Viewer();
        } catch (Exception ex) {
            logException(ex);
            showMessageDialog(null, null, ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Minimise the application
	 */
    public final void minimise() {
        parentContainer.setState(Frame.ICONIFIED);
    }

    /**
	 * Opens a file
	 * @param filePath file to be opened
	 */
    public final void openFile(String filePath) {
        File workingPath = null;
        try {
            workingPath = createWorkingPath();
            becta.viewer.framework.Frame frame = new becta.viewer.framework.Frame(workingPath.getAbsolutePath(), filePath);
            if (activeFrame != null) {
                activeFrame.getCanvas().getDocument().getCurrentPage().dispose(false);
                desktop.removeAll();
                desktop.repaint();
            }
            activeFrame = frame;
            frames.add(frame);
            desktop.add(activeFrame, BorderLayout.CENTER);
            activeFrame.load(true);
            desktop.validate();
            if (activeFrame.getCanvas().getDocument().hasFlashContent()) {
                Viewer.showMessageDialog(this.getContainer(), null, resourceBundle.getString("FlashContentMsg"), JOptionPane.INFORMATION_MESSAGE);
            }
            fireFileChange(activeFrame);
            activeFrame.loadComplete();
        } catch (Exception ex) {
            logException(ex);
            Viewer.showMessageDialog(this.getContainer(), null, resourceBundle.getString("FileLoadErrorMsg"), JOptionPane.ERROR_MESSAGE);
            if (workingPath != null) deleteDirectory(workingPath);
        }
    }

    private static File createWorkingPath() throws Exception {
        File file = File.createTempFile("iwb", null);
        file.delete();
        file.mkdir();
        return file;
    }

    /**
	 * Save a copy of file
	 * @param filePath name of file to be created
	 */
    public final void saveCopy(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                if (showConfirmationDialog(getContainer(), resourceBundle.getString("WarningMsgTitle"), resourceBundle.getString("FileAlreadyExistMsg"), JOptionPane.YES_NO_OPTION) != 0) {
                    return;
                }
            }
            File dir = createWorkingPath();
            activeFrame.save(dir.getAbsolutePath(), filePath);
            deleteDirectory(dir);
        } catch (Exception ex) {
            logException(ex);
            JOptionPane.showMessageDialog(this.getContainer(), resourceBundle.getString("FileSaveErrorMsg"));
        }
    }

    public static boolean deleteDirectory(File path) {
        try {
            if (path.exists()) {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
            return (path.delete());
        } catch (Exception e) {
            return false;
        }
    }

    /**
	 * Close file
	 */
    public final void closeFile() {
        activeFrame.close();
        frames.remove(activeFrame);
        desktop.removeAll();
        desktop.repaint();
        if (frames.size() > 0) {
            activeFrame = frames.get(0);
            desktop.add(activeFrame, BorderLayout.CENTER);
            desktop.validate();
            fireFileChange(activeFrame);
            activeFrame.load(false);
            activeFrame.loadComplete();
        } else {
            activeFrame = null;
            fireFileChange(null);
        }
    }

    /**
	 * Shows about dialog box
	 */
    public final void showAbout() {
        AboutDialog.showDialog(parentContainer);
    }

    /**	
	 * Closes the application
	 */
    public final void exit() {
        boolean unChanged = true;
        for (becta.viewer.framework.Frame frame : getFrames()) {
            if (frame.getCanvas().getDocument().isDirty()) {
                unChanged = false;
                break;
            }
        }
        if (unChanged || Viewer.showConfirmationDialog(viewer.getContainer(), resourceBundle.getString("ExitViewerMsgTitle"), resourceBundle.getString("ExitViewerMsg"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }

    /**
	 * Handles undo/redo operation
	 */
    public final void undoRedo() {
    }

    /**
	 * Toggles selection tool
	 */
    public final void selectTool() {
        toolbar.showAnnotationDialog(false);
        activeFrame.setSelectionMode(SelectionMode.REGULAR);
    }

    /**
	 * Select annotation mode
	 */
    public final void selectPen() {
        activeFrame.setSelectionMode(SelectionMode.DRAWING);
    }

    /**
	 * shows change pen dialog
	 */
    public final void changePen() {
        toolbar.showAnnotationDialog(true);
    }

    /**
	 * shows colour mapping menu
	 */
    public final void colourMapping() {
        toolbar.showAccessibilityMenu();
    }

    /**
	 * resets file
	 */
    public final void resetFile() {
        activeFrame.resetFile();
        toolbar.resetPageNo();
    }

    /**
	 * Reset viewer
	 */
    public void resetViewer() {
        toolbar.reset();
    }

    /**
	 * clear annotations
	 */
    public final void clearAnnotations() {
        if (activeFrame != null) {
            activeFrame.clearAnnotations();
        }
    }

    /**
	 * handles zooming
	 */
    public final void zoom(boolean isZoomIn) {
        if (activeFrame != null) {
            ZoomManager zoomManager = activeFrame.getCanvas().getZoomManager();
            double currentZoomFactor = zoomManager.getCurrentScale() - 1;
            double newZoomFactor = isZoomIn ? currentZoomFactor + 0.2 : currentZoomFactor - 0.2;
            zoomManager.scaleTo(newZoomFactor + 1);
            fireZoomChange(activeFrame);
        }
    }

    public final void resetZoom() {
        if (activeFrame != null) {
            ZoomManager zoomManager = activeFrame.getCanvas().getZoomManager();
            zoomManager.scaleTo(1);
            fireZoomChange(activeFrame);
        }
    }

    /**
	 * hide/show revealer
	 * @return Boolean value indicating whether revealer is shown or not
	 */
    public final void toggleRevealer() {
        if (activeFrame != null) {
            activeFrame.getScrollPane().showRevealer(!activeFrame.getScrollPane().getRevealer().isVisible());
        }
    }

    /**
	 * Updates menutext and toolbar icon based on state of revealer
	 * @param isVisible
	 */
    public final void toggleRevealerIcon(boolean isVisible) {
        menubar.updateRevealerMenu(isVisible);
        toolbar.updateRevealerIcon(isVisible);
    }

    /**
	 * Handles themeChanged event
	 */
    public void themeChanged(Theme theme) {
        menubar.updateForThemeChanged(theme.getFont("ViewerControlFont"), theme.getFont("MenuItemFont"));
        toolbar.updateForThemeChanged(theme.getFont("ViewerControlFont"));
        parentContainer.repaint();
        if (toolbar.isFloating()) {
            toolbar.repaint();
        }
        if (toolbar.isAnnotationDialogShowing()) {
            toolbar.repaintAnnotationDialog();
        }
        if (activeFrame != null) {
            if (activeFrame.getScrollPane().getRevealer() != null) activeFrame.getScrollPane().getRevealer().repaint();
            activeFrame.getCanvas().refreshCanvasContent(true);
        }
    }

    public void toolbarStateChanged() {
        for (becta.viewer.framework.Frame frame : frames) {
            frame.getScrollPane().floatChanged();
        }
    }

    /**
	 * Gets the number of open files
	 * @return
	 */
    public int getOpenFileCount() {
        return frames.size();
    }

    /**
     * Fires file change event
     * @param doc
     */
    private void fireFileChange(becta.viewer.framework.Frame frame) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FileChangeListener.class) {
                ((FileChangeListener) listeners[i + 1]).fileChanged(frame);
            }
        }
    }

    /**
     * Fires file change event
     * @param doc
     */
    private void firePageChange(becta.viewer.framework.Frame frame, int number) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FileChangeListener.class) {
                ((FileChangeListener) listeners[i + 1]).pageChanged(frame, number);
            }
        }
    }

    /**
     * Fires Zoom change event
     * @param doc
     */
    private void fireZoomChange(becta.viewer.framework.Frame frame) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == StateChangeListener.class) {
                ((StateChangeListener) listeners[i + 1]).zoomStateChanged(frame);
            }
        }
    }

    public void pageChanged(becta.viewer.framework.Frame frame, int number) {
        firePageChange(frame, number);
    }

    /**
     * Mute media in current page
     * @param enable
     */
    public void setMute(boolean enable) {
        if (activeFrame != null) activeFrame.setMute(enable);
    }

    /**
	 * Set volume for media
	 */
    public void setVolume(int volume) {
        if (activeFrame != null) activeFrame.setVolume(volume);
    }

    /**
     * Play media in current page
     */
    public void resumeMedia() {
        activeFrame.setMute(toolbar.isMute());
        activeFrame.setVolume(toolbar.getVolume());
        activeFrame.resumeMedia();
    }

    /**
     * Pause media in current page
     */
    public void pauseMedia() {
        activeFrame.pauseMedia();
    }
}
