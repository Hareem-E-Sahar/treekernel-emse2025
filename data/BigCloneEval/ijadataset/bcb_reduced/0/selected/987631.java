package gui.javagui;

import gui.GuiInterface;
import gui.javagui.preferences.PreferencesDialog;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import config.ConfigInterface;
import controller.ControllerInterface;

public class JavaGui implements GuiInterface, ActionInterface {

    private ControllerInterface controller;

    private ConfigInterface config;

    private GuiActions guiActions;

    private static JFrame frame;

    private SyncTable syncTable;

    private JSplitPane splitPane;

    private JTabbedPane tabbedPane;

    public JavaGui(ControllerInterface controller) {
        this.controller = controller;
        this.config = controller.getConfig();
        setLookAndFeelType(config.getVariableString("", "GuiType"));
        init();
    }

    public static JFrame getFrame() {
        return frame;
    }

    private void setLookAndFeelType(String lookAndFeelType) {
        try {
            if (lookAndFeelType.equals("JavaGuiNative")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else if (lookAndFeelType.equals("JavaGui")) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        frame = new JFrame("SyncIT");
        frame.addWindowListener(windowListener);
        setIconImages(frame);
        guiActions = new GuiActions((ActionInterface) this);
        MainMenuBar mainMenuBar = new MainMenuBar(guiActions);
        JToolBar toolBar = new ControlToolBar(guiActions);
        syncTable = new SyncTable(config, (ActionInterface) this);
        StatusBar statusBar = new StatusBar();
        TabGeneralPanel tabGeneralPanel = new TabGeneralPanel();
        TabLoggerPanel tabLoggerPanel = new TabLoggerPanel(controller.getLog());
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", new ImageIcon("res/general.png"), tabGeneralPanel, "General tab");
        tabbedPane.addTab("Logger", new ImageIcon("res/logger.png"), tabLoggerPanel, "Logger tab");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_G);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_L);
        try {
            tabbedPane.setSelectedIndex(config.getVariableInteger("gui", "JavaActiveTab"));
        } catch (IndexOutOfBoundsException e) {
        }
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, syncTable, tabbedPane);
        splitPane.setOneTouchExpandable(false);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerLocation(config.getVariableInteger("gui", "JavaSplitPanePosition"));
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        frame.setJMenuBar(mainMenuBar);
        container.add(toolBar, BorderLayout.NORTH);
        container.add(splitPane, BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.SOUTH);
        frame.setSize(config.getVariableInteger("", "GuiWidth"), config.getVariableInteger("", "GuiHeight"));
        frame.setResizable(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int screenResX = (int) dim.getWidth();
        int screenResY = (int) dim.getHeight();
        int guiPosX = config.getVariableInteger("", "GuiPosX");
        int guiPosY = config.getVariableInteger("", "GuiPosY");
        if ((guiPosX > (screenResX - 50)) || (guiPosY > (screenResY - 50))) {
            config.setVariable("", "GuiWidth", 0);
            config.setVariable("", "GuiHeight", 0);
            frame.setLocation(0, 0);
        } else {
            frame.setLocation(guiPosX, guiPosY);
        }
        frame.setMinimumSize(new Dimension(50, 50));
        frame.setVisible(true);
    }

    /**
	 * Sets the icon images to the application.
	 */
    private void setIconImages(JFrame frame) {
        String[] sizes = { "16", "32", "64", "128" };
        ArrayList<Image> imageList = new ArrayList<Image>();
        Image image;
        for (String size : sizes) {
            image = Toolkit.getDefaultToolkit().getImage("res/SyncIT_" + size + ".png");
            imageList.add(image);
        }
        frame.setIconImages(imageList);
    }

    /**
	 * Action to be performed when program terminates via the JavaGui.
	 */
    private void closingWindow() {
        int guiWidth = config.getVariableInteger("", "GuiWidth");
        if (guiWidth != frame.getWidth()) {
            config.setVariable("", "GuiWidth", frame.getWidth());
        }
        int guiHeight = config.getVariableInteger("", "GuiHeight");
        if (guiHeight != frame.getHeight()) {
            config.setVariable("", "GuiHeight", frame.getHeight());
        }
        int guiPosX = config.getVariableInteger("", "GuiPosX");
        if (guiPosX != frame.getX()) {
            config.setVariable("", "GuiPosX", frame.getX());
        }
        int guiPosY = config.getVariableInteger("", "GuiPosY");
        if (guiPosY != frame.getY()) {
            config.setVariable("", "GuiPosY", frame.getY());
        }
        int splitPanePos = config.getVariableInteger("gui", "JavaSplitPanePosition");
        if (splitPanePos != splitPane.getDividerLocation()) {
            config.setVariable("gui", "JavaSplitPanePosition", splitPane.getDividerLocation());
        }
        String columnWidths = config.getVariableString("gui", "JavaColumnWidths");
        int[] newColumnWidths = syncTable.getColumnWidths();
        String newColumnWidthsStr = "";
        for (int i = 0; i < newColumnWidths.length; i++) {
            newColumnWidthsStr += newColumnWidths[i];
            if (i < newColumnWidths.length - 1) {
                newColumnWidthsStr += ",";
            }
        }
        if (!columnWidths.equals(newColumnWidthsStr)) {
            config.setVariable("gui", "JavaColumnWidths", newColumnWidthsStr);
        }
        String columnPositions = config.getVariableString("gui", "JavaColumnPositions");
        int[] newColumnPositions = syncTable.getColumnPositions();
        String newColumnPositionsStr = "";
        for (int i = 0; i < newColumnPositions.length; i++) {
            newColumnPositionsStr += newColumnPositions[i];
            if (i < newColumnPositions.length - 1) {
                newColumnPositionsStr += ",";
            }
        }
        if (!columnPositions.equals(newColumnPositionsStr)) {
            config.setVariable("gui", "JavaColumnPositions", newColumnPositionsStr);
        }
        String rowSortOrder = config.getVariableString("gui", "JavaRowSortOrder");
        List<? extends RowSorter.SortKey> sortKeys = syncTable.getSortKeys();
        String newRowSortOrderStr = "";
        for (int i = 0; i < sortKeys.size(); i++) {
            RowSorter.SortKey sortKey = sortKeys.get(i);
            newRowSortOrderStr += sortKey.getColumn() + ";" + sortKey.getSortOrder().name();
            if (i < sortKeys.size() - 1) {
                newRowSortOrderStr += ",";
            }
        }
        if (!rowSortOrder.equals(newRowSortOrderStr)) {
            config.setVariable("gui", "JavaRowSortOrder", newRowSortOrderStr);
        }
        int activeTab = config.getVariableInteger("gui", "JavaActiveTab");
        if (activeTab != tabbedPane.getSelectedIndex()) {
            config.setVariable("gui", "JavaActiveTab", tabbedPane.getSelectedIndex());
        }
        controller.terminate();
        System.exit(0);
    }

    WindowAdapter windowListener = new WindowAdapter() {

        public void windowClosing(WindowEvent e) {
            closingWindow();
        }
    };

    public void addAction() {
        int index = config.addListElement("client");
        syncTable.addRow(index);
        enableDisableComponents();
    }

    public void removeAction() {
        int[] rows = syncTable.getSelectedRows();
        for (int i = (rows.length - 1); i >= 0; i--) {
            config.deleteListElement("client", rows[i]);
            syncTable.removeRow(rows[i]);
        }
    }

    public void startAction() {
        controller.getLog().logInfo(new Date(), "Testing");
        controller.getLog().logError(new Date(), "Error!!!");
        controller.getLog().logWarning(new Date(), "Warning");
    }

    public void pauseAction() {
    }

    public void stopAction() {
    }

    public void moveUpAction() {
        int row = syncTable.getLeadSelectionRow();
        if (row != -1 && row > 0) {
            config.moveListElement("client", row, row - 1);
            syncTable.moveUp(row);
        }
    }

    public void moveDownAction() {
        int row = syncTable.getLeadSelectionRow();
        int listSize = config.getListSize("client");
        if (row != -1 && row < (listSize - 1)) {
            config.moveListElement("client", row, row + 1);
            syncTable.moveDown(row);
        }
    }

    public void preferencesAction() {
        new PreferencesDialog(config);
    }

    public void exitAction() {
        closingWindow();
    }

    public void helpAction() {
    }

    public void webpageAction() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(controller.getUrlLinks().getWebpageUri());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(getFrame(), "Can't find the default web browser\nto open the web page " + controller.getUrlLinks().getWebpageUrlStr() + ".", "Cannot open web page", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(getFrame(), "Operation not supported on this platform.\nPlease go to " + controller.getUrlLinks().getWebpageUrlStr(), "Cannot open web page", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void whatIsMyIPAction() {
        new DialogWhatIsMyIP(controller);
    }

    public void checkForUpdatesAction() {
    }

    public void aboutAction() {
        new DialogAbout(controller.getVersion());
    }

    public void enableDisableComponents() {
        int row = syncTable.getLeadSelectionRow();
        if (row == -1) {
            guiActions.enableRemoveAction(false);
            guiActions.enableMoveUpAction(false);
            guiActions.enableMoveDownAction(false);
        } else {
            guiActions.enableRemoveAction(true);
            if (row <= 0) {
                guiActions.enableMoveUpAction(false);
                guiActions.enableMoveDownAction(true);
            } else if (row >= syncTable.getRowCount() - 1) {
                guiActions.enableMoveUpAction(true);
                guiActions.enableMoveDownAction(false);
            } else {
                guiActions.enableMoveUpAction(true);
                guiActions.enableMoveDownAction(true);
            }
        }
    }
}
