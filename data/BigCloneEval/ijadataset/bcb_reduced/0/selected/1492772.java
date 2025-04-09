package org.digitall.lib.components.basic;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import org.digitall.lib.components.Advisor;
import org.digitall.lib.icons.IconTypes;
import org.digitall.lib.image.LibIMG;

public class ExtendedInternalFrame extends BasicInternalFrame {

    private GeneralButtons generalButtons;

    private BasicInternalFrameNorthPane northPane;

    private BasicInfoLabel lblInfo;

    private Component centralPanel;

    private ImageIcon icon;

    private int iconWidth = 22;

    private int iconHeight = 22;

    private JPanel centerPanel = new JPanel();

    private ExtendedInternalFrame parentIFrame;

    final Toolkit toolkit = Toolkit.getDefaultToolkit();

    final Clipboard clipboard = toolkit.getDefaultToolkit().getSystemClipboard();

    private int securityLevel = 0;

    private Class centralPanelClass;

    public ExtendedInternalFrame() {
        this("Ventana Sin Nombre");
    }

    public ExtendedInternalFrame(String _title) {
        this(_title, new ImageIcon());
    }

    public ExtendedInternalFrame(String _title, Component _centralPanel) {
        this(_title, new ImageIcon());
        setCentralPanel(_centralPanel);
    }

    public ExtendedInternalFrame(String _title, ImageIcon _icon) {
        super(_title, _icon);
        icon = _icon;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ExtendedInternalFrame(String _title, ImageIcon _icon, Class _class, boolean _asButton, BasicDesktop _desktop) {
        super(_title, _icon, _desktop);
        centralPanelClass = _class;
        icon = _icon;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_asButton) {
            setClosable(false);
            setIcon(true);
        }
    }

    private void jbInit() throws Exception {
        this.setDefaultCloseOperation(BasicInternalFrame.DO_NOTHING_ON_CLOSE);
        this.setBorder(BorderFactory.createLineBorder(BasicConfig.INTERNALFRAME_BORDER_COLOR, 2));
        this.addInternalFrameListener(new InternalFrameAdapter() {

            public void internalFrameClosing(InternalFrameEvent e) {
                close(true);
            }
        });
        createKeyBindings();
        this.setLayout(new BorderLayout());
        lblInfo = new BasicInfoLabel();
        setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, BasicConfig.PANELCONTAINER_BACKGROUND_COLOR));
        lblInfo.setBorder(BorderFactory.createMatteBorder(4, 0, 4, 0, BasicConfig.PANELCONTAINER_BACKGROUND_COLOR));
        lblInfo.setMaximumSize(new Dimension(0, 23));
        lblInfo.setMinimumSize(new Dimension(0, 23));
        lblInfo.setPreferredSize(new Dimension(0, 23));
        lblInfo.setOpaque(true);
        lblInfo.setBackground(BasicConfig.LABELINFO_BACKGROUND_COLOR);
        lblInfo.setForeground(BasicConfig.LABELINFO_FOREGROUND_COLOR);
        ((BasicInternalFrameUI) getUI()).setNorthPane(null);
        northPane = new BasicInternalFrameNorthPane(this, IconTypes.getScaledIcon(icon, iconWidth, iconHeight));
        northPane.setSecurityLevel(securityLevel);
        this.add(northPane, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(lblInfo, BorderLayout.NORTH);
        generalButtons = new GeneralButtons(this);
        setCentralPanel(new BasicTabContainer());
    }

    public boolean saveData() {
        if (centralPanel instanceof BasicPrimitivePanel) {
            return ((BasicPrimitivePanel) centralPanel).saveData();
        } else if (centralPanel instanceof BasicTabContainer) {
            return ((BasicTabContainer) centralPanel).saveData();
        } else {
            return true;
        }
    }

    public void initialize() {
        if (centralPanelClass != null) {
            if (centralPanelClass.getSuperclass().equals(BasicPrimitivePanel.class)) {
                try {
                    setCentralPanel((Component) centralPanelClass.newInstance());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Advisor.printException(e);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    Advisor.printException(e);
                }
            } else if (centralPanelClass.getSuperclass().equals(BasicTabContainer.class)) {
                try {
                    setCentralPanel((Component) centralPanelClass.newInstance());
                } catch (IllegalAccessException e) {
                    Advisor.printException(e);
                } catch (InstantiationException e) {
                    Advisor.printException(e);
                }
            }
        }
        if (isMaximizable()) {
            setMaximum(true);
        }
        super.initialize();
    }

    public void setCentralPanel(Component _panel) {
        if (centralPanel != null) {
            centerPanel.remove(centralPanel);
        }
        if (_panel != null) {
            if (_panel instanceof BasicPrimitivePanel) {
                ((BasicPrimitivePanel) _panel).setParentInternalFrame(this);
            }
            if (_panel instanceof BasicTabContainer) {
                ((BasicTabContainer) _panel).setParentInternalFrame(this);
            }
            centralPanel = _panel;
            Rectangle panelBounds = _panel.getBounds();
            setBounds(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height + northPane.getPreferredSize().height + lblInfo.getPreferredSize().height);
            centerPanel.add(_panel, BorderLayout.CENTER);
        }
    }

    public void close() {
        generalButtons.hideButtons();
        generalButtons.close();
        if (parentIFrame != null) {
            parentIFrame.refresh();
        }
        super.close();
    }

    public void createKeyBindings() {
        Action anAction = new IFrameAction();
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK, true), "close");
        getActionMap().put("close", anAction);
    }

    public void show() {
        super.show();
        generalButtons.setDesktop(getDesktop());
        generalButtons.setSize(generalButtons.getPreferredSize().width, getHeight());
        generalButtons.relocate();
    }

    public void setLocation(int _x, int _y) {
        super.setLocation(_x, _y);
        generalButtons.relocate();
    }

    public void moveTo(int _x, int _y) {
        super.setLocation(_x, _y);
    }

    public void setLocation(Point _point) {
        super.setLocation(_point);
        generalButtons.relocate();
    }

    public void setFunctions() {
        if (generalButtons.isVisible()) {
            hideButtons();
        } else {
            showButtons();
        }
    }

    public void setIcon(boolean _icon) {
        if (_icon) {
            generalButtons.close();
            super.setIcon(_icon);
        }
    }

    public void showButtons() {
        generalButtons.showButtons();
    }

    public void hideButtons() {
        generalButtons.hideButtons();
    }

    public GeneralButtons getGeneralButtons() {
        return generalButtons;
    }

    public void refresh() {
        if (centralPanel instanceof BasicPrimitivePanel) {
            ((BasicPrimitivePanel) centralPanel).refresh();
        } else if (centralPanel instanceof BasicTabContainer) {
            ((BasicTabContainer) centralPanel).refresh();
        }
    }

    public void reload() {
        try {
            Robot robot = new Robot();
            BufferedImage im = robot.createScreenCapture(new Rectangle(this.getLocationOnScreen().x, this.getLocationOnScreen().y, this.getWidth(), this.getHeight()));
            ImageSelection imgSel = new ImageSelection(im);
            clipboard.setContents(imgSel, null);
            System.out.println("Captura completa");
        } catch (AWTException e) {
            e.printStackTrace();
        }
        if (centralPanel instanceof BasicPrimitivePanel) {
            ((BasicPrimitivePanel) centralPanel).reload();
        } else if (centralPanel instanceof BasicTabContainer) {
            ((BasicTabContainer) centralPanel).reload();
        }
    }

    public void setClosable(boolean _closable) {
        super.setClosable(_closable);
        northPane.setClosable(_closable);
    }

    public void setIconifiable(boolean _iconifiable) {
        super.setIconifiable(_iconifiable);
        northPane.setIconifiable(_iconifiable);
    }

    public void setInfo(String _text) {
        lblInfo.setText(_text);
    }

    public void setParentInternalFrame(ExtendedInternalFrame _parentIFrame) {
        parentIFrame = _parentIFrame;
    }

    public BasicInternalFrameNorthPane getNorthPane() {
        return northPane;
    }

    public Component getCentralPanel() {
        return centralPanel;
    }

    private class IFrameAction implements Action {

        public void addPropertyChangeListener(PropertyChangeListener arg0) {
        }

        public Object getValue(String arg0) {
            return null;
        }

        public boolean isEnabled() {
            close(true);
            return false;
        }

        public void putValue(String arg0, Object arg1) {
        }

        public void removePropertyChangeListener(PropertyChangeListener arg0) {
        }

        public void setEnabled(boolean arg0) {
        }

        public void actionPerformed(ActionEvent arg0) {
        }
    }

    public static class ImageSelection implements Transferable {

        private BufferedImage image;

        public ImageSelection(BufferedImage image) {
            this.image = image;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }

    public void setToolTipText(String _toolTipText) {
        super.setToolTipText(_toolTipText);
    }

    public void setSecurityLevel(int _level) {
        northPane.setSecurityLevel(_level);
    }

    public void closeWindow() {
        if (isClosable()) {
            close();
        } else {
            setIcon(true);
        }
    }

    public void closeWindow(boolean _savedata) {
        if (isClosable()) {
            close(_savedata);
        } else {
            setIcon(true);
        }
    }

    public void setTitle(String _title) {
        title = _title;
        northPane.setTitle(_title);
        super.setTitle(_title);
    }

    public void setMaximum(boolean _maximum) {
        try {
            super.setMaximum(_maximum);
        } catch (PropertyVetoException f) {
            Advisor.messageBox("No se puede maximizar la ventana", "Error");
        }
    }
}
