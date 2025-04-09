package desknow.ui.stuff;

import desknow.resources.ConfigManager;
import java.awt.Desktop;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class UpdatesWindow extends JFrame implements HyperlinkListener {

    private JEditorPane updatesEditorPane;

    private ConfigManager configManager;

    private String updateLink;

    public UpdatesWindow(ConfigManager confMan, String updateLink) {
        this.configManager = confMan;
        LogoPanel logo = new LogoPanel();
        Insets insets = getInsets();
        add(logo);
        logo.setBounds(55, 10, 380, 100);
        this.updateLink = updateLink;
        String updatesText = "<font face='Geneva, Arial, Helvetica, sans-serif'>A Desk.Now update is available, go download it:" + "<br><br>" + updateLink + "</font>";
        setSize(370, 220);
        updatesEditorPane = new JEditorPane("text/html", updatesText + "</font>");
        updatesEditorPane.setEditable(false);
        updatesEditorPane.setOpaque(false);
        updatesEditorPane.addHyperlinkListener(this);
        JPanel updatesPane = new JPanel();
        updatesPane.add(updatesEditorPane);
        add(updatesPane);
        updatesPane.setBounds(-10, 110, 380, 120);
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int xPos = ((int) screenSize.getWidth() / 2) - (getWidth() / 2);
        int yPos = ((int) screenSize.getHeight() / 2) - (getHeight() / 2);
        setBounds(xPos, yPos, getWidth(), getHeight());
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/link.png"));
        setIconImage(image);
        setTitle("Desk.Now updates");
        setLayout(null);
        setResizable(false);
        setVisible(true);
    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
        URL url = event.getURL();
        if (event.getEventType() == HyperlinkEvent.EventType.ENTERED && !event.getDescription().startsWith("copyUrl:")) {
            updatesEditorPane.setToolTipText(url.toString());
        } else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
            updatesEditorPane.setToolTipText(null);
        } else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (event.getDescription().startsWith("copyUrl:")) {
                String toCopy = event.getDescription().replaceFirst("copyUrl:", "");
                StringSelection data = new StringSelection(toCopy);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(data, data);
            } else {
                String browser = configManager.getBrowser();
                if (!Desktop.isDesktopSupported()) {
                    try {
                        if (!browser.equals("")) {
                            Runtime.getRuntime().exec(browser + " " + url.toString());
                        } else {
                            Runtime.getRuntime().exec("firefox " + url.toString());
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Unable to find a web browser, please set up one on settings window", "Web browser error", JOptionPane.WARNING_MESSAGE);
                    }
                }
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI uri = new URI(url.toString());
                    desktop.browse(uri);
                } catch (Exception e) {
                    return;
                }
            }
        }
    }
}
