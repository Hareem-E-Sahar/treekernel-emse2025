package viewer.windowpane;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import viewer.core.MapWindow;
import viewer.core.MapWindowChangeListener;
import viewer.geometry.manage.EventJDialog;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 * 
 */
public class MapWindowDialog extends EventJDialog {

    /**
	 * Simple test for the dialog
	 * 
	 * @param args
	 *            none
	 */
    public static void main(String args[]) {
        MapWindow window = new MapWindow(300, 300, 14, 400, 400, 0, 0);
        MapWindowDialog dialog = new MapWindowDialog(null, "test", window);
        dialog.setSize(300, 200);
        dialog.setVisible(true);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private static final long serialVersionUID = -3817433094278662941L;

    final MapWindow mapWindow;

    JLabel labelCenterLon;

    JLabel labelCenterLat;

    /**
	 * Create a new Dialog.
	 * 
	 * @param parent
	 *            the parent frame.
	 * @param title
	 *            the title of the dialog.
	 * @param mapWindow
	 *            the MapWindow instance to monitor.
	 */
    public MapWindowDialog(JFrame parent, String title, MapWindow mapWindow) {
        super(parent, title);
        this.mapWindow = mapWindow;
        labelCenterLon = new JLabel();
        labelCenterLat = new JLabel();
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        setContentPane(content);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_START;
        c.weightx = 1.0;
        c.weighty = 1.0;
        Panel panel = new Panel();
        content.add(panel, c);
        mapWindow.addChangeListener(new MapWindowChangeListener() {

            @Override
            public void changed() {
                updateCenterPosition();
            }
        });
    }

    void updateCenterPosition() {
        double centerLon = mapWindow.getCenterLon();
        double centerLat = mapWindow.getCenterLat();
        String lon = String.format("%.6f", centerLon);
        String lat = String.format("%.6f", centerLat);
        labelCenterLon.setText(lon);
        labelCenterLat.setText(lat);
    }

    private abstract class ClipboardButton extends JButton implements ActionListener {

        private static final long serialVersionUID = -5009156933177940901L;

        ClipboardButton(String title) {
            super(title);
            addActionListener(this);
        }

        public abstract String getClipboardText();

        @Override
        public void actionPerformed(ActionEvent event) {
            Clipboard clipboard = getToolkit().getSystemClipboard();
            Transferable transferable = new Transferable() {

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    if (flavor.equals(DataFlavor.stringFlavor)) {
                        return true;
                    }
                    return false;
                }

                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] { DataFlavor.stringFlavor };
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (flavor.equals(DataFlavor.stringFlavor)) {
                        return getClipboardText();
                    }
                    throw new UnsupportedFlavorException(flavor);
                }
            };
            ClipboardOwner owner = null;
            clipboard.setContents(transferable, owner);
        }
    }

    private abstract class UrlButton extends JButton implements ActionListener {

        private static final long serialVersionUID = -8729725449611359987L;

        UrlButton(String title) {
            super(title);
            addActionListener(this);
        }

        public abstract String getUrl();

        @Override
        public void actionPerformed(ActionEvent event) {
            String url = getUrl();
            System.out.println(url);
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private class Panel extends JPanel {

        private static final long serialVersionUID = 8474089488587560617L;

        Panel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            JLabel label = new JLabel("center position:");
            JLabel labelLon = new JLabel("lon:");
            JLabel labelLat = new JLabel("lat:");
            List<JButton> buttons = new ArrayList<JButton>();
            JButton buttonOsmWeb = new UrlButton("Openstreetmap Mapnik") {

                private static final long serialVersionUID = -7264367289609795290L;

                @Override
                public String getUrl() {
                    return String.format("http://www.openstreetmap.org/?lat=%f&lon=%f&zoom=%d&layers=M", mapWindow.getCenterLat(), mapWindow.getCenterLon(), mapWindow.getZoom());
                }
            };
            JButton buttonPotlatch1 = new UrlButton("Potlatch 1") {

                private static final long serialVersionUID = -5705984576501268795L;

                @Override
                public String getUrl() {
                    return String.format("http://www.openstreetmap.org/edit?editor=potlatch&lat=%f&lon=%f&zoom=%d&layers=M", mapWindow.getCenterLat(), mapWindow.getCenterLon(), mapWindow.getZoom());
                }
            };
            JButton buttonPotlatch2 = new UrlButton("Potlatch 2") {

                private static final long serialVersionUID = 2759613679591189523L;

                @Override
                public String getUrl() {
                    return String.format("http://www.openstreetmap.org/edit?editor=potlatch2&lat=%f&lon=%f&zoom=%d&layers=M", mapWindow.getCenterLat(), mapWindow.getCenterLon(), mapWindow.getZoom());
                }
            };
            JButton buttonCopyDouble = new ClipboardButton("Clipboard (floating point)") {

                private static final long serialVersionUID = -139734902428928357L;

                @Override
                public String getClipboardText() {
                    String text = mapWindow.getCenterLat() + "," + mapWindow.getCenterLon();
                    return text;
                }
            };
            JButton buttonCopyDegMinSec = new ClipboardButton("Clipboard (deg/min/sec/)") {

                private static final long serialVersionUID = 9182954207805942607L;

                @Override
                public String getClipboardText() {
                    String text = degMinSec(mapWindow.getCenterLat(), mapWindow.getCenterLon());
                    return text;
                }

                private String degMinSec(double centerLat, double centerLon) {
                    String text = degMinSecLat(centerLat) + "," + degMinSecLon(centerLon);
                    return text;
                }

                private String degMinSecLat(double lat) {
                    String letter = lat >= 0 ? "N" : "S";
                    double abs = Math.abs(lat);
                    return degs(abs) + "/" + mins(abs) + "/" + secs(abs) + "/" + letter;
                }

                private String degMinSecLon(double lon) {
                    String letter = lon >= 0 ? "E" : "W";
                    double abs = Math.abs(lon);
                    return degs(abs) + "/" + mins(abs) + "/" + secs(abs) + "/" + letter;
                }

                private int degs(double d) {
                    return (int) Math.floor(d);
                }

                private int mins(double d) {
                    return ((int) Math.round((d - degs(d)) * 3600)) / 60;
                }

                private int secs(double d) {
                    return ((int) Math.round((d - degs(d)) * 3600)) % 60;
                }
            };
            buttons.add(buttonCopyDouble);
            buttons.add(buttonCopyDegMinSec);
            buttons.add(buttonOsmWeb);
            buttons.add(buttonPotlatch1);
            buttons.add(buttonPotlatch2);
            c.weightx = 1.0;
            c.weighty = 0.0;
            c.anchor = GridBagConstraints.PAGE_START;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            add(label, c);
            c.gridwidth = 1;
            c.gridy = 1;
            c.gridx = 0;
            add(labelLon, c);
            c.gridx = 1;
            add(labelCenterLon, c);
            c.gridy = 2;
            c.gridx = 0;
            add(labelLat, c);
            c.gridx = 1;
            add(labelCenterLat, c);
            c.gridx = 0;
            c.gridwidth = 2;
            for (JButton button : buttons) {
                c.gridy += 1;
                add(button, c);
            }
            c.weighty = 1.0;
            add(new JPanel(), c);
        }
    }
}
