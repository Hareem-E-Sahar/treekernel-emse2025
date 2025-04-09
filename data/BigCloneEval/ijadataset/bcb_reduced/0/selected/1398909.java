package net.sf.excompcel.gui.controls;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import net.sf.excompcel.gui.util.DimensionCalculator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * A Label Control to View an Image or Text with a active Hyperlink behind.
 * 
 * @author Administrator
 * @since v0.1
 * 
 */
public class HyperlinkLabel extends JLabel {

    /** Logger. */
    private static Logger log = Logger.getLogger(HyperlinkLabel.class);

    /** serialVersionUID. */
    private static final long serialVersionUID = 9022697226923933941L;

    /** URL to HyperLink. */
    private URL url;

    /** Popup Menu on right Mouse Click. */
    private JPopupMenu popupMenu;

    /** Window Resource. */
    private ResourceBundle resources;

    private final int WIDTH_MINIMUM = 100;

    private final int HEIGTH_MINIMUM = 20;

    private final int WIDTH_MAXIMUM = 150;

    private final int HEIGTH_MAXIMUM = 40;

    /**
	 * MouseListener to get Mouse Click Event to start Hyperlink in default
	 * Browser.
	 */
    private transient MouseListener linker = new MouseAdapter() {

        @Override
        public void mouseClicked(final MouseEvent e) {
            log.debug(e);
            HyperlinkLabel self = (HyperlinkLabel) e.getSource();
            if (self.url == null) {
                return;
            }
            if (SwingUtilities.isRightMouseButton(e)) {
                log.debug("Right Mouse Click. " + e.getModifiersEx());
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            } else {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(self.url.toString()));
                    } else {
                        log.info("Cant't open link.");
                    }
                } catch (IOException e1) {
                    log.error(e1);
                } catch (URISyntaxException ex) {
                    log.error(ex);
                } catch (Exception ex) {
                    log.error(ex);
                }
            }
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    };

    /**
	 * Constructor Set Label Text.
	 * 
	 * @param label The Label Text as String.
	 */
    public HyperlinkLabel(final String label) {
        super(label);
        initResoures();
        setForeground(Color.BLUE);
        addMouseListener(linker);
        initMinMaxSize();
    }

    /**
	 * Set Minumun, Maximum and Preferred Size of Control.
	 */
    private void initMinMaxSize() {
        Dimension min = new Dimension(WIDTH_MINIMUM, HEIGTH_MINIMUM);
        Dimension max = new Dimension(WIDTH_MAXIMUM, HEIGTH_MAXIMUM);
        setMinimumSize(min);
        setMaximumSize(max);
        setPreferredSize(DimensionCalculator.middle(min, max));
    }

    /**
	 * 
	 */
    private void initResoures() {
        try {
            resources = ResourceBundle.getBundle("gui/resource/HyperlinkLabel");
        } catch (MissingResourceException e) {
            log.error(e);
        }
        initPopUpMenuProcessLabel();
    }

    /**
	 * Constructor Set Label and Tooltip text.
	 * 
	 * @param label The Label Text as String.
	 * @param tip Tooltip text.
	 */
    public HyperlinkLabel(final String label, final String tip) {
        this(label);
        setToolTipText(tip);
    }

    /**
	 * Constructor Set Label Text and URL.
	 * 
	 * @param label The Label Text as String.
	 * @param url URL
	 */
    public HyperlinkLabel(final String label, final URL url) {
        this(label);
        this.url = url;
    }

    /**
	 * Constructor Set Label and Tooltip Text and URL.
	 * 
	 * @param label The Label Text as String.
	 * @param tip Tooltip text
	 * @param url URL
	 */
    public HyperlinkLabel(final String label, final String tip, final URL url) {
        this(label, url);
        setToolTipText(tip);
    }

    /**
	 * Constructor Set Image, Tooltip Text and URL.
	 * 
	 * @param img ImageIcon
	 * @param tip String Tooltip Text
	 * @param url URL HyperLink
	 */
    public HyperlinkLabel(final ImageIcon img, final String tip, final URL url) {
        this(img, tip);
        this.url = url;
    }

    /**
	 * Constructor Set Image and Tooltip Text.
	 * 
	 * @param img ImageIcon
	 * @param tip String Tooltip Text
	 */
    public HyperlinkLabel(final ImageIcon img, final String tip) {
        super(img);
        setToolTipText(tip);
        addMouseListener(linker);
        initMinMaxSize();
    }

    /**
	 * Set URL of this Label.
	 * 
	 * @param url {@link URL}
	 */
    public void setURL(final URL url) {
        this.url = url;
    }

    /**
	 * Get URL of this Label.
	 * 
	 * @return {@link URL}
	 */
    public URL getURL() {
        return url;
    }

    /**
	 * 
	 */
    private void initPopUpMenuProcessLabel() {
        popupMenu = new JPopupMenu();
        String menuTitle = "Copy";
        if (resources != null) {
            try {
                menuTitle = resources.getString("Popup.Menu.FileCopy.Title");
            } catch (Exception e) {
                log.error(e);
            }
        }
        JMenuItem menuItem = new JMenuItem(menuTitle);
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                if (new File(getText()).exists()) {
                    Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable contents = new Transferable() {

                        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                            return getText();
                        }

                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[] { DataFlavor.stringFlavor };
                        }

                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            for (DataFlavor df : getTransferDataFlavors()) {
                                if (df.equals(flavor)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    };
                    systemClipboard.setContents(contents, null);
                }
            }
        });
        popupMenu.add(menuItem);
        addMouseListener(new ProcessLabelPopupListener(popupMenu));
    }

    /**
	 * PopUp Menu on Process Label
	 * @author detlev struebig
	 * @version v0.6
	 */
    class ProcessLabelPopupListener extends MouseAdapter {

        /** Logger. */
        private Logger log = Logger.getLogger(ProcessLabelPopupListener.class);

        private JPopupMenu popup;

        public ProcessLabelPopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        @Override
        public void mouseClicked(MouseEvent evt) {
            if (SwingUtilities.isRightMouseButton(evt)) {
                log.debug("Right Mouse Click. " + evt.getModifiersEx());
                popup.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }
}
